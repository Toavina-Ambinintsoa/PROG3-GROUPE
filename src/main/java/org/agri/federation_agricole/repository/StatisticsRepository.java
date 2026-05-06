package org.agri.federation_agricole.repository;

import org.agri.federation_agricole.config.DataSource;
import org.agri.federation_agricole.dto.CollectivityInformationDTO;
import org.agri.federation_agricole.dto.CollectivityLocalStatisticsDTO;
import org.agri.federation_agricole.dto.CollectivityOverallStatisticsDTO;
import org.agri.federation_agricole.entity.Enum.Gender;
import org.agri.federation_agricole.entity.Member;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class StatisticsRepository {

    private final DataSource dataSource;

    public StatisticsRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * For each active member of the collectivity:
     *  - earnedAmount  : sum of payments made by that member during [from, to]
     *  - unpaidAmount  : sum of amounts still owed on ACTIVE contributions
     *                    that were eligible during [from, to] and not yet fully covered
     */
    public List<CollectivityLocalStatisticsDTO> getLocalStatistics(
            String collectivityId, LocalDate from, LocalDate to) {

        // 1. Get all active members of the collectivity
        String memberQuery = """
                SELECT m.id, m.first_name, m.last_name, m.birth_date, m.gender,
                       m.address, m.profession, m.phone, m.email, m.registration_date
                FROM members m
                JOIN collectivity_members cm ON cm.member_id = m.id
                WHERE cm.collectivity_id = ?
                  AND (cm.resignation_date IS NULL OR cm.resignation_date > ?)
                """;

        // 2. Sum of payments per member during the period
        String earnedQuery = """
                SELECT COALESCE(SUM(p.amount), 0) AS earned
                FROM payments p
                WHERE p.member_id = ?
                  AND p.collectivity_id = ?
                  AND p.payment_date >= ?
                  AND p.payment_date <= ?
                """;

        // 3. Unpaid potential = expected dues (ACTIVE contributions eligible in period) - payments made
        //    We compute the total expected amount for each ACTIVE contribution
        //    based on frequency and eligibility window, then subtract payments.
        String unpaidQuery = """
                SELECT
                    c.id AS contribution_id,
                    c.frequency,
                    c.amount,
                    c.eligible_since,
                    COALESCE(SUM(p.amount), 0) AS paid_for_contribution
                FROM contributions c
                LEFT JOIN payments p
                    ON p.contribution_id = c.id
                    AND p.member_id = ?
                    AND p.payment_date >= ?
                    AND p.payment_date <= ?
                WHERE c.collectivity_id = ?
                  AND c.status = 'ACTIVE'
                  AND c.eligible_since <= ?
                GROUP BY c.id, c.frequency, c.amount, c.eligible_since
                """;

        List<CollectivityLocalStatisticsDTO> result = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {

            // Fetch members
            PreparedStatement memberStmt = conn.prepareStatement(memberQuery);
            memberStmt.setString(1, collectivityId);
            memberStmt.setDate(2, Date.valueOf(to));
            ResultSet memberRs = memberStmt.executeQuery();

            while (memberRs.next()) {
                Member member = new Member();
                member.setId(memberRs.getString("id"));
                member.setFirstName(memberRs.getString("first_name"));
                member.setLastName(memberRs.getString("last_name"));
                member.setBirthDate(memberRs.getDate("birth_date").toLocalDate());
                member.setGender(Gender.valueOf(memberRs.getString("gender")));
                member.setAddress(memberRs.getString("address"));
                member.setProfession(memberRs.getString("profession"));
                member.setPhone(memberRs.getString("phone"));
                member.setEmail(memberRs.getString("email"));
                member.setRegistrationDate(memberRs.getDate("registration_date").toLocalDate());

                // Earned amount
                PreparedStatement earnedStmt = conn.prepareStatement(earnedQuery);
                earnedStmt.setString(1, member.getId());
                earnedStmt.setString(2, collectivityId);
                earnedStmt.setDate(3, Date.valueOf(from));
                earnedStmt.setDate(4, Date.valueOf(to));
                ResultSet earnedRs = earnedStmt.executeQuery();
                long earned = 0;
                if (earnedRs.next()) {
                    earned = earnedRs.getLong("earned");
                }

                // Unpaid amount: expected total - paid total for ACTIVE contributions
                PreparedStatement unpaidStmt = conn.prepareStatement(unpaidQuery);
                unpaidStmt.setString(1, member.getId());
                unpaidStmt.setDate(2, Date.valueOf(from));
                unpaidStmt.setDate(3, Date.valueOf(to));
                unpaidStmt.setString(4, collectivityId);
                unpaidStmt.setDate(5, Date.valueOf(to));
                ResultSet unpaidRs = unpaidStmt.executeQuery();

                long totalExpected = 0;
                long totalPaidForContributions = 0;
                while (unpaidRs.next()) {
                    int contributionAmount = unpaidRs.getInt("amount");
                    String frequency = unpaidRs.getString("frequency");
                    LocalDate eligibleSince = unpaidRs.getDate("eligible_since").toLocalDate();
                    long paidForContribution = unpaidRs.getLong("paid_for_contribution");

                    long occurrences = countOccurrences(frequency, eligibleSince, from, to);
                    totalExpected += (long) contributionAmount * occurrences;
                    totalPaidForContributions += paidForContribution;
                }

                long unpaid = Math.max(0, totalExpected - totalPaidForContributions);

                result.add(new CollectivityLocalStatisticsDTO(member, earned, unpaid));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    /**
     * For each collectivity:
     *  - newMembersNumber                    : members whose registration_date is within [from, to]
     *  - overallMemberCurrentDuePercentage   : % of members who are up-to-date on all ACTIVE dues
     */
    public List<CollectivityOverallStatisticsDTO> getOverallStatistics(LocalDate from, LocalDate to) {

        String collectivitiesQuery = """
                SELECT id, number, name FROM collectivities ORDER BY id
                """;

        String newMembersQuery = """
                SELECT COUNT(*) AS cnt
                FROM collectivity_members cm
                JOIN members m ON m.id = cm.member_id
                WHERE cm.collectivity_id = ?
                  AND m.registration_date >= ?
                  AND m.registration_date <= ?
                """;

        // A member is "current" if, for every ACTIVE contribution, the total paid >= expected
        // We consider all active members (no resignation before end of period)
        String totalMembersQuery = """
                SELECT COUNT(DISTINCT cm.member_id) AS cnt
                FROM collectivity_members cm
                WHERE cm.collectivity_id = ?
                  AND (cm.resignation_date IS NULL OR cm.resignation_date > ?)
                """;

        // Members who have fully covered all active contributions
        String currentMembersQuery = """
                SELECT cm.member_id
                FROM collectivity_members cm
                WHERE cm.collectivity_id = ?
                  AND (cm.resignation_date IS NULL OR cm.resignation_date > ?)
                """;

        String paidCheckQuery = """
                SELECT
                    c.id,
                    c.frequency,
                    c.amount,
                    c.eligible_since,
                    COALESCE(SUM(p.amount), 0) AS paid
                FROM contributions c
                LEFT JOIN payments p
                    ON p.contribution_id = c.id
                    AND p.member_id = ?
                    AND p.payment_date >= ?
                    AND p.payment_date <= ?
                WHERE c.collectivity_id = ?
                  AND c.status = 'ACTIVE'
                  AND c.eligible_since <= ?
                GROUP BY c.id, c.frequency, c.amount, c.eligible_since
                """;

        List<CollectivityOverallStatisticsDTO> result = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {

            PreparedStatement colStmt = conn.prepareStatement(collectivitiesQuery);
            ResultSet colRs = colStmt.executeQuery();

            while (colRs.next()) {
                String collectivityId = colRs.getString("id");
                CollectivityInformationDTO info = new CollectivityInformationDTO(
                        collectivityId,
                        colRs.getObject("number") != null ? colRs.getInt("number") : null,
                        colRs.getString("name")
                );

                // New members
                PreparedStatement newStmt = conn.prepareStatement(newMembersQuery);
                newStmt.setString(1, collectivityId);
                newStmt.setDate(2, Date.valueOf(from));
                newStmt.setDate(3, Date.valueOf(to));
                ResultSet newRs = newStmt.executeQuery();
                int newMembersCount = 0;
                if (newRs.next()) {
                    newMembersCount = newRs.getInt("cnt");
                }

                // Total active members
                PreparedStatement totalStmt = conn.prepareStatement(totalMembersQuery);
                totalStmt.setString(1, collectivityId);
                totalStmt.setDate(2, Date.valueOf(to));
                ResultSet totalRs = totalStmt.executeQuery();
                int totalMembers = 0;
                if (totalRs.next()) {
                    totalMembers = totalRs.getInt("cnt");
                }

                // Count current members (up-to-date on all ACTIVE dues)
                int currentMembers = 0;
                if (totalMembers > 0) {
                    PreparedStatement currentStmt = conn.prepareStatement(currentMembersQuery);
                    currentStmt.setString(1, collectivityId);
                    currentStmt.setDate(2, Date.valueOf(to));
                    ResultSet currentRs = currentStmt.executeQuery();

                    while (currentRs.next()) {
                        String memberId = currentRs.getString("member_id");

                        PreparedStatement paidStmt = conn.prepareStatement(paidCheckQuery);
                        paidStmt.setString(1, memberId);
                        paidStmt.setDate(2, Date.valueOf(from));
                        paidStmt.setDate(3, Date.valueOf(to));
                        paidStmt.setString(4, collectivityId);
                        paidStmt.setDate(5, Date.valueOf(to));
                        ResultSet paidRs = paidStmt.executeQuery();

                        boolean isCurrent = true;
                        while (paidRs.next()) {
                            int amount = paidRs.getInt("amount");
                            String frequency = paidRs.getString("frequency");
                            LocalDate eligibleSince = paidRs.getDate("eligible_since").toLocalDate();
                            long paid = paidRs.getLong("paid");

                            long occurrences = countOccurrences(frequency, eligibleSince, from, to);
                            long expected = (long) amount * occurrences;

                            if (paid < expected) {
                                isCurrent = false;
                                break;
                            }
                        }

                        if (isCurrent) {
                            currentMembers++;
                        }
                    }
                }

                double percentage = totalMembers == 0 ? 0.0
                        : Math.round((currentMembers * 100.0 / totalMembers) * 100.0) / 100.0;

                result.add(new CollectivityOverallStatisticsDTO(info, newMembersCount, percentage));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    /**
     * Compute the number of payment occurrences for a contribution of a given frequency
     * within the window [windowStart, windowEnd], knowing the contribution became eligible
     * from eligibleSince.
     * The effective start is max(eligibleSince, windowStart).
     */
    private long countOccurrences(String frequency, LocalDate eligibleSince,
                                  LocalDate windowStart, LocalDate windowEnd) {
        LocalDate effectiveStart = eligibleSince.isAfter(windowStart) ? eligibleSince : windowStart;
        if (effectiveStart.isAfter(windowEnd)) return 0;

        return switch (frequency) {
            case "MONTHLY" -> countMonths(effectiveStart, windowEnd);
            case "ANNUALLY" -> countYears(effectiveStart, windowEnd);
            case "WEEKLY" -> countWeeks(effectiveStart, windowEnd);
            case "PUNCTUALLY" -> 1L;
            default -> 1L;
        };
    }

    private long countMonths(LocalDate start, LocalDate end) {
        long months = 0;
        LocalDate cursor = start.withDayOfMonth(1);
        while (!cursor.isAfter(end.withDayOfMonth(1))) {
            months++;
            cursor = cursor.plusMonths(1);
        }
        return months;
    }

    private long countYears(LocalDate start, LocalDate end) {
        long years = 0;
        LocalDate cursor = start.withDayOfYear(1);
        while (!cursor.isAfter(end.withDayOfYear(1))) {
            years++;
            cursor = cursor.plusYears(1);
        }
        return years;
    }

    private long countWeeks(LocalDate start, LocalDate end) {
        long days = end.toEpochDay() - start.toEpochDay();
        return (days / 7) + 1;
    }
}