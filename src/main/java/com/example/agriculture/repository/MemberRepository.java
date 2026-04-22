package com.example.agriculture.repository;

import com.example.agriculture.config.DataSource;
import com.example.agriculture.entity.Enum.Gender;
import com.example.agriculture.entity.Enum.MemberOccupation;
import com.example.agriculture.entity.Member;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class MemberRepository {
    private DataSource dataSource;

    public MemberRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public boolean collectivityExists(String collectivityId) { // String, pas Long
        String sql = "SELECT COUNT(*) FROM collectivity WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, collectivityId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    public List<Member> findAllByIds(List<Integer> memberIds) throws SQLException {
        if (memberIds == null || memberIds.isEmpty()) {
            return new ArrayList<>();
        }

        String placeholders = memberIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(", "));

        String sql = """
                SELECT id, first_name, last_name, birth_date,
                       gender, address, profession, phone_number,
                       email, occupation, adhesion_date
                FROM member
                WHERE id IN (
                """ + placeholders + ")";

        List<Member> members = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < memberIds.size(); i++) {
                stmt.setInt(i + 1, memberIds.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                members.add(mapRow(rs));
            }
        }
        return members;
    }

    public Member findById(int memberId) {
        String sql = """
                SELECT id, first_name, last_name, birth_date,
                       gender, address, profession, phone_number,
                       email, occupation, adhesion_date
                FROM member WHERE id = ?
                """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean memberExists(int memberId) {
        String sql = "SELECT COUNT(*) FROM member WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getCollectivityIdOfMember(int memberId) {
        String sql = "SELECT collectivity_id FROM member WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("collectivity_id");
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isSeniorMember(int memberId) {
        String sql = "SELECT COUNT(*) FROM member WHERE id = ? AND occupation = 'SENIOR'";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Member save(Member member, String collectivityId) {
        String sql = """
                INSERT INTO member
                    (first_name, last_name, birth_date, gender,
                     address, profession, phone_number, email,
                     occupation, adhesion_date, collectivity_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_DATE, ?)
                RETURNING id
                """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, member.getFirstName());
            stmt.setString(2, member.getLastName());
            stmt.setDate(3, Date.valueOf(member.getBirthDate()));
            stmt.setString(4, member.getGender().name());
            stmt.setString(5, member.getAddress());
            stmt.setString(6, member.getProfession());
            stmt.setString(7, member.getPhoneNumber());
            stmt.setString(8, member.getEmail());
            stmt.setString(9, MemberOccupation.JUNIOR.name());
            stmt.setString(10, collectivityId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                member.setId(rs.getInt("id"));
            }
            return member;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Member mapRow(ResultSet rs) throws SQLException {
        Member member = new Member();
        member.setId(rs.getInt("id"));
        member.setFirstName(rs.getString("first_name"));
        member.setLastName(rs.getString("last_name"));
        member.setBirthDate(rs.getDate("birth_date").toLocalDate());
        member.setGender(Gender.valueOf(rs.getString("gender")));
        member.setAddress(rs.getString("address"));
        member.setProfession(rs.getString("profession"));
        member.setPhoneNumber(rs.getString("phone_number"));
        member.setEmail(rs.getString("email"));
        member.setOccupation(MemberOccupation.valueOf(rs.getString("occupation")));
        member.setAdhesionDate(rs.getDate("adhesion_date").toLocalDate());
        return member;
    }
}