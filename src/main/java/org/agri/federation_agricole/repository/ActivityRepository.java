package org.agri.federation_agricole.repository;

import org.agri.federation_agricole.config.DataSource;
import org.agri.federation_agricole.dto.ActivityMemberAttendanceDTO;
import org.agri.federation_agricole.dto.MemberDescriptionDTO;
import org.agri.federation_agricole.entity.CollectivityActivity;
import org.agri.federation_agricole.entity.CreateActivityMemberAttendance;
import org.agri.federation_agricole.entity.CreateCollectivityActivity;
import org.agri.federation_agricole.entity.MonthlyRecurrenceRule;
import org.agri.federation_agricole.entity.Enum.ActivityType;
import org.agri.federation_agricole.entity.Enum.AttendanceStatus;
import org.agri.federation_agricole.entity.Enum.Occupation;
import org.agri.federation_agricole.exception.BadRequestException;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ActivityRepository {

    private final DataSource dataSource;

    public ActivityRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // -------------------------------------------------------------------------
    // Activities
    // -------------------------------------------------------------------------

    public List<CollectivityActivity> getActivitiesByCollectivityId(String collectivityId) {
        String query = """
                SELECT id, label, activity_type, member_occupation_concerned,
                       recurrence_week_ordinal, recurrence_day_of_week, executive_date
                FROM activities
                WHERE collectivity_id = ?
                ORDER BY id
                """;
        List<CollectivityActivity> activities = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, collectivityId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                activities.add(mapActivity(rs));
            }
            return activities;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<CollectivityActivity> saveActivities(String collectivityId,
                                                     List<CreateCollectivityActivity> createList) {
        String insertQuery = """
                INSERT INTO activities
                    (id, collectivity_id, label, activity_type, member_occupation_concerned,
                     recurrence_week_ordinal, recurrence_day_of_week, executive_date)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id
                """;
        List<String> savedIds = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            for (CreateCollectivityActivity c : createList) {
                // Validate: recurrenceRule and executiveDate are mutually exclusive
                if (c.getRecurrenceRule() != null && c.getExecutiveDate() != null) {
                    throw new BadRequestException(
                            "recurrenceRule and executiveDate cannot both be provided");
                }
                if (c.getRecurrenceRule() == null && c.getExecutiveDate() == null) {
                    throw new BadRequestException(
                            "Either recurrenceRule or executiveDate must be provided");
                }

                String id = getNextActivityId(collectivityId);
                PreparedStatement ps = conn.prepareStatement(insertQuery);
                ps.setString(1, id);
                ps.setString(2, collectivityId);
                ps.setString(3, c.getLabel());
                ps.setString(4, c.getActivityType() != null ? c.getActivityType().name() : null);

                // Store concerned occupations as comma-separated string
                String occupations = buildOccupationsCsv(c.getMemberOccupationConcerned());
                ps.setString(5, occupations);

                if (c.getRecurrenceRule() != null) {
                    ps.setInt(6, c.getRecurrenceRule().getWeekOrdinal());
                    ps.setString(7, c.getRecurrenceRule().getDayOfWeek());
                } else {
                    ps.setNull(6, Types.INTEGER);
                    ps.setNull(7, Types.VARCHAR);
                }

                if (c.getExecutiveDate() != null) {
                    ps.setDate(8, Date.valueOf(c.getExecutiveDate()));
                } else {
                    ps.setNull(8, Types.DATE);
                }

                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    savedIds.add(rs.getString("id"));
                }
            }
            conn.commit();
        } catch (BadRequestException e) {
            throw e;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return getActivitiesByIds(savedIds);
    }

    public CollectivityActivity getActivityById(String activityId) {
        String query = """
                SELECT id, label, activity_type, member_occupation_concerned,
                       recurrence_week_ordinal, recurrence_day_of_week, executive_date
                FROM activities WHERE id = ?
                """;
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, activityId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapActivity(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // -------------------------------------------------------------------------
    // Attendance
    // -------------------------------------------------------------------------

    /**
     * Save attendance records.
     * Rule: once ATTENDED or MISSING, cannot be changed (only UNDEFINED can be updated).
     */
    public List<ActivityMemberAttendanceDTO> saveAttendance(String collectivityId,
                                                            String activityId,
                                                            List<CreateActivityMemberAttendance> createList) {
        // Check existing attendance for each member — reject if already ATTENDED or MISSING
        String checkQuery = """
                SELECT attendance_status FROM activity_attendance
                WHERE activity_id = ? AND member_id = ?
                """;
        String upsertQuery = """
                INSERT INTO activity_attendance (id, activity_id, collectivity_id, member_id, attendance_status)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT (activity_id, member_id)
                DO UPDATE SET attendance_status = EXCLUDED.attendance_status
                WHERE activity_attendance.attendance_status = 'UNDEFINED'
                RETURNING id
                """;

        List<String> savedIds = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            for (CreateActivityMemberAttendance c : createList) {
                // Check if already confirmed
                PreparedStatement checkPs = conn.prepareStatement(checkQuery);
                checkPs.setString(1, activityId);
                checkPs.setString(2, c.getMemberIdentifier());
                ResultSet checkRs = checkPs.executeQuery();
                if (checkRs.next()) {
                    String existing = checkRs.getString("attendance_status");
                    if ("ATTENDED".equals(existing) || "MISSING".equals(existing)) {
                        throw new BadRequestException(
                                "Attendance already confirmed for member: " + c.getMemberIdentifier());
                    }
                }

                String id = getNextAttendanceId(activityId);
                PreparedStatement ps = conn.prepareStatement(upsertQuery);
                ps.setString(1, id);
                ps.setString(2, activityId);
                ps.setString(3, collectivityId);
                ps.setString(4, c.getMemberIdentifier());
                ps.setString(5, c.getAttendanceStatus().name());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    savedIds.add(rs.getString("id"));
                }
            }
            conn.commit();
        } catch (BadRequestException e) {
            throw e;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return getAttendanceByIds(savedIds);
    }

    /**
     * Get all attendance records for an activity.
     * Returns ATTENDED/MISSING/UNDEFINED for concerned members of the collectivity,
     * and only ATTENDED for outside members.
     */
    public List<ActivityMemberAttendanceDTO> getAttendanceByActivityId(String collectivityId,
                                                                       String activityId) {
        // 1. Get activity to know which occupations are concerned
        CollectivityActivity activity = getActivityById(activityId);
        if (activity == null) return new ArrayList<>();

        String concernedOccupations = buildOccupationsCsv(activity.getMemberOccupationConcerned());

        // 2. Get all stored attendance records for this activity
        String query = """
                SELECT aa.id, aa.member_id, aa.attendance_status,
                       m.first_name, m.last_name, m.email,
                       cm.occupation, cm.collectivity_id AS member_collectivity_id
                FROM activity_attendance aa
                JOIN members m ON m.id = aa.member_id
                LEFT JOIN collectivity_members cm
                    ON cm.member_id = aa.member_id
                    AND cm.collectivity_id = ?
                WHERE aa.activity_id = ?
                ORDER BY aa.id
                """;

        List<ActivityMemberAttendanceDTO> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, collectivityId);
            ps.setString(2, activityId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                MemberDescriptionDTO desc = new MemberDescriptionDTO(
                        rs.getString("member_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("occupation")
                );
                result.add(new ActivityMemberAttendanceDTO(
                        rs.getString("id"),
                        desc,
                        AttendanceStatus.valueOf(rs.getString("attendance_status"))
                ));
            }

            // 3. For members of the collectivity that are concerned but have no record yet → UNDEFINED
            if (concernedOccupations != null && !concernedOccupations.isEmpty()) {
                addUndefinedForConcernedMembers(conn, collectivityId, activityId,
                        concernedOccupations, result);
            }

            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Compute assiduity rate for a specific member over a period.
     * Only activities of their own collectivity where they are concerned are counted.
     * Activities from other collectivities they attended are NOT counted.
     */
    public double getMemberAssiduityPercentage(String collectivityId, String memberId,
                                               String memberOccupation,
                                               LocalDate from, LocalDate to) {
        // Count activities where this member is concerned (by occupation or all members)
        // An activity is concerned if memberOccupationConcerned is null/empty OR contains the member's occupation
        String totalQuery = """
                SELECT COUNT(*) AS total
                FROM activities a
                WHERE a.collectivity_id = ?
                  AND (
                    a.member_occupation_concerned IS NULL
                    OR a.member_occupation_concerned = ''
                    OR a.member_occupation_concerned LIKE ?
                  )
                  AND (
                    a.executive_date IS NOT NULL AND a.executive_date >= ? AND a.executive_date <= ?
                  )
                """;

        // Recurring activities in the period — count occurrences
        String recurringQuery = """
                SELECT a.recurrence_week_ordinal, a.recurrence_day_of_week,
                       a.member_occupation_concerned
                FROM activities a
                WHERE a.collectivity_id = ?
                  AND a.recurrence_week_ordinal IS NOT NULL
                  AND (
                    a.member_occupation_concerned IS NULL
                    OR a.member_occupation_concerned = ''
                    OR a.member_occupation_concerned LIKE ?
                  )
                """;

        String attendedQuery = """
                SELECT COUNT(*) AS attended
                FROM activity_attendance aa
                JOIN activities a ON a.id = aa.activity_id
                WHERE aa.member_id = ?
                  AND a.collectivity_id = ?
                  AND aa.attendance_status = 'ATTENDED'
                  AND (
                    a.executive_date IS NOT NULL AND a.executive_date >= ? AND a.executive_date <= ?
                  )
                """;

        String likeOccupation = "%" + memberOccupation + "%";

        try (Connection conn = dataSource.getConnection()) {
            // One-shot activities count
            PreparedStatement totalPs = conn.prepareStatement(totalQuery);
            totalPs.setString(1, collectivityId);
            totalPs.setString(2, likeOccupation);
            totalPs.setDate(3, Date.valueOf(from));
            totalPs.setDate(4, Date.valueOf(to));
            ResultSet totalRs = totalPs.executeQuery();
            long totalActivities = totalRs.next() ? totalRs.getLong("total") : 0;

            // Add recurring activity occurrences in the period
            PreparedStatement recurPs = conn.prepareStatement(recurringQuery);
            recurPs.setString(1, collectivityId);
            recurPs.setString(2, likeOccupation);
            ResultSet recurRs = recurPs.executeQuery();
            while (recurRs.next()) {
                int weekOrdinal = recurRs.getInt("recurrence_week_ordinal");
                String dayOfWeek = recurRs.getString("recurrence_day_of_week");
                totalActivities += countRecurringOccurrences(weekOrdinal, dayOfWeek, from, to);
            }

            if (totalActivities == 0) return 100.0;

            // Attended count (one-shot only — recurring attendance is also stored with executive date)
            PreparedStatement attendedPs = conn.prepareStatement(attendedQuery);
            attendedPs.setString(1, memberId);
            attendedPs.setString(2, collectivityId);
            attendedPs.setDate(3, Date.valueOf(from));
            attendedPs.setDate(4, Date.valueOf(to));
            ResultSet attendedRs = attendedPs.executeQuery();
            long attended = attendedRs.next() ? attendedRs.getLong("attended") : 0;

            double pct = (attended * 100.0) / totalActivities;
            return Math.round(pct * 100.0) / 100.0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Compute overall assiduity percentage for a collectivity over a period.
     * = average of individual assiduity rates of active members.
     */
    public double getCollectivityAssiduityPercentage(String collectivityId,
                                                     LocalDate from, LocalDate to) {
        String membersQuery = """
                SELECT cm.member_id, cm.occupation
                FROM collectivity_members cm
                WHERE cm.collectivity_id = ?
                  AND (cm.resignation_date IS NULL OR cm.resignation_date > ?)
                """;

        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(membersQuery);
            ps.setString(1, collectivityId);
            ps.setDate(2, Date.valueOf(to));
            ResultSet rs = ps.executeQuery();

            double totalPct = 0;
            int memberCount = 0;
            while (rs.next()) {
                String memberId = rs.getString("member_id");
                String occupation = rs.getString("occupation");
                if (occupation == null) occupation = "JUNIOR";
                double pct = getMemberAssiduityPercentage(collectivityId, memberId, occupation, from, to);
                totalPct += pct;
                memberCount++;
            }

            if (memberCount == 0) return 0.0;
            double avg = totalPct / memberCount;
            return Math.round(avg * 100.0) / 100.0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private CollectivityActivity mapActivity(ResultSet rs) throws SQLException {
        CollectivityActivity a = new CollectivityActivity();
        a.setId(rs.getString("id"));
        a.setLabel(rs.getString("label"));
        String type = rs.getString("activity_type");
        if (type != null) a.setActivityType(ActivityType.valueOf(type));

        // Parse occupations CSV
        String occupationsCsv = rs.getString("member_occupation_concerned");
        if (occupationsCsv != null && !occupationsCsv.isBlank()) {
            List<Occupation> occupations = new ArrayList<>();
            for (String occ : occupationsCsv.split(",")) {
                String trimmed = occ.trim();
                if (!trimmed.isEmpty()) {
                    occupations.add(Occupation.valueOf(trimmed));
                }
            }
            a.setMemberOccupationConcerned(occupations);
        }

        int weekOrdinal = rs.getInt("recurrence_week_ordinal");
        String dayOfWeek = rs.getString("recurrence_day_of_week");
        if (!rs.wasNull() && dayOfWeek != null) {
            a.setRecurrenceRule(new MonthlyRecurrenceRule(weekOrdinal, dayOfWeek));
        }

        Date execDate = rs.getDate("executive_date");
        if (execDate != null) {
            a.setExecutiveDate(execDate.toLocalDate());
        }

        return a;
    }

    private String buildOccupationsCsv(List<Occupation> occupations) {
        if (occupations == null || occupations.isEmpty()) return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < occupations.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(occupations.get(i).name());
        }
        return sb.toString();
    }

    private void addUndefinedForConcernedMembers(Connection conn, String collectivityId,
                                                 String activityId, String occupationsCsv,
                                                 List<ActivityMemberAttendanceDTO> existing)
            throws SQLException {
        // Collect already-recorded member IDs
        List<String> alreadyRecorded = new ArrayList<>();
        for (ActivityMemberAttendanceDTO att : existing) {
            alreadyRecorded.add(att.getMemberDescription().getId());
        }

        // Build occupation filter
        String[] occupations = occupationsCsv.split(",");
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < occupations.length; i++) {
            if (i > 0) placeholders.append(",");
            placeholders.append("?::occupation_type");
        }

        String membersQuery = """
                SELECT m.id, m.first_name, m.last_name, m.email, cm.occupation
                FROM members m
                JOIN collectivity_members cm ON cm.member_id = m.id
                WHERE cm.collectivity_id = ?
                  AND cm.occupation IN (%s)
                  AND (cm.resignation_date IS NULL)
                """.formatted(placeholders);

        PreparedStatement ps = conn.prepareStatement(membersQuery);
        ps.setString(1, collectivityId);
        for (int i = 0; i < occupations.length; i++) {
            ps.setString(i + 2, occupations[i].trim());
        }
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            String memberId = rs.getString("id");
            if (!alreadyRecorded.contains(memberId)) {
                MemberDescriptionDTO desc = new MemberDescriptionDTO(
                        memberId,
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("occupation")
                );
                existing.add(new ActivityMemberAttendanceDTO(null, desc, AttendanceStatus.UNDEFINED));
            }
        }
    }

    private List<CollectivityActivity> getActivitiesByIds(List<String> ids) {
        List<CollectivityActivity> result = new ArrayList<>();
        if (ids.isEmpty()) return result;
        String query = """
                SELECT id, label, activity_type, member_occupation_concerned,
                       recurrence_week_ordinal, recurrence_day_of_week, executive_date
                FROM activities WHERE id = ?
                """;
        try (Connection conn = dataSource.getConnection()) {
            for (String id : ids) {
                PreparedStatement ps = conn.prepareStatement(query);
                ps.setString(1, id);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) result.add(mapActivity(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<ActivityMemberAttendanceDTO> getAttendanceByIds(List<String> ids) {
        List<ActivityMemberAttendanceDTO> result = new ArrayList<>();
        if (ids.isEmpty()) return result;
        String query = """
                SELECT aa.id, aa.member_id, aa.attendance_status,
                       m.first_name, m.last_name, m.email,
                       cm.occupation
                FROM activity_attendance aa
                JOIN members m ON m.id = aa.member_id
                LEFT JOIN collectivity_members cm ON cm.member_id = aa.member_id
                WHERE aa.id = ?
                """;
        try (Connection conn = dataSource.getConnection()) {
            for (String id : ids) {
                PreparedStatement ps = conn.prepareStatement(query);
                ps.setString(1, id);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    MemberDescriptionDTO desc = new MemberDescriptionDTO(
                            rs.getString("member_id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("email"),
                            rs.getString("occupation")
                    );
                    result.add(new ActivityMemberAttendanceDTO(
                            rs.getString("id"),
                            desc,
                            AttendanceStatus.valueOf(rs.getString("attendance_status"))
                    ));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // -------------------------------------------------------------------------
    // ID generation — same pattern as the rest of the project
    // -------------------------------------------------------------------------

    private String getLastActivityId(String collectivityId) {
        String query = "SELECT id FROM activities WHERE collectivity_id = ? ORDER BY id DESC LIMIT 1";
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, collectivityId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("id");
            String prefix = collectivityId.split("-")[0];
            return prefix + "-ACT0";
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String getNextActivityId(String collectivityId) {
        String lastId = getLastActivityId(collectivityId);
        String[] parts = lastId.split("-ACT");
        if (parts.length != 2) {
            return collectivityId.split("-")[0] + "-ACT1";
        }
        int num = Integer.parseInt(parts[1]);
        return parts[0] + "-ACT" + (num + 1);
    }

    private String getLastAttendanceId(String activityId) {
        String query = "SELECT id FROM activity_attendance WHERE activity_id = ? ORDER BY id DESC LIMIT 1";
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, activityId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("id");
            String prefix = activityId.split("-")[0];
            return prefix + "-ATT0";
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String getNextAttendanceId(String activityId) {
        String lastId = getLastAttendanceId(activityId);
        String[] parts = lastId.split("-ATT");
        if (parts.length != 2) {
            return activityId.split("-")[0] + "-ATT1";
        }
        int num = Integer.parseInt(parts[1]);
        return parts[0] + "-ATT" + (num + 1);
    }

    // -------------------------------------------------------------------------
    // Recurrence helper — count how many times a "Nth DOW of month" falls in [from, to]
    // -------------------------------------------------------------------------

    /**
     * Count occurrences of "weekOrdinal-th dayOfWeek of each month" that fall within [from, to].
     * E.g. weekOrdinal=2, dayOfWeek="SU" → every 2nd Sunday.
     */
    private long countRecurringOccurrences(int weekOrdinal, String dayOfWeek,
                                           LocalDate from, LocalDate to) {
        java.time.DayOfWeek dow = parseDayOfWeek(dayOfWeek);
        long count = 0;
        // Iterate month by month
        java.time.YearMonth startMonth = java.time.YearMonth.from(from);
        java.time.YearMonth endMonth = java.time.YearMonth.from(to);
        java.time.YearMonth cursor = startMonth;
        while (!cursor.isAfter(endMonth)) {
            LocalDate occurrence = nthDayOfWeekInMonth(cursor, weekOrdinal, dow);
            if (occurrence != null && !occurrence.isBefore(from) && !occurrence.isAfter(to)) {
                count++;
            }
            cursor = cursor.plusMonths(1);
        }
        return count;
    }

    /**
     * Returns the date of the N-th occurrence of dayOfWeek in the given month,
     * or null if the month doesn't have that many occurrences.
     */
    private LocalDate nthDayOfWeekInMonth(java.time.YearMonth month, int n,
                                          java.time.DayOfWeek dow) {
        LocalDate first = month.atDay(1);
        // Find first occurrence of dow in this month
        int daysUntilDow = (dow.getValue() - first.getDayOfWeek().getValue() + 7) % 7;
        LocalDate firstOccurrence = first.plusDays(daysUntilDow);
        LocalDate nthOccurrence = firstOccurrence.plusWeeks(n - 1);
        if (nthOccurrence.getMonth() != month.getMonth()) return null;
        return nthOccurrence;
    }

    private java.time.DayOfWeek parseDayOfWeek(String code) {
        return switch (code.toUpperCase()) {
            case "MO" -> java.time.DayOfWeek.MONDAY;
            case "TU" -> java.time.DayOfWeek.TUESDAY;
            case "WE" -> java.time.DayOfWeek.WEDNESDAY;
            case "TH" -> java.time.DayOfWeek.THURSDAY;
            case "FR" -> java.time.DayOfWeek.FRIDAY;
            case "SA" -> java.time.DayOfWeek.SATURDAY;
            case "SU" -> java.time.DayOfWeek.SUNDAY;
            default -> throw new BadRequestException("Unknown day of week code: " + code);
        };
    }
}