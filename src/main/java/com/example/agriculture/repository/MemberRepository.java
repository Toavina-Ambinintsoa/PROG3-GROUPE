package com.example.agriculture.repository;

import com.example.agriculture.config.DataSource;
import com.example.agriculture.entity.Enum.Gender;
import com.example.agriculture.entity.Enum.MemberOccupation;
import com.example.agriculture.entity.Member;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class MemberRepository {
    private DataSource dataSource;

    public MemberRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public boolean collectivityExists(Long collectivityId) {
        String sql = "SELECT COUNT(*) FROM collectivity WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, collectivityId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

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
            SELECT
                id, first_name, last_name, birth_date,
                gender, address, profession, phone_number,
                email, occupation, adhesion_date
            FROM member
            WHERE id IN (""" + placeholders + ")";

        List<Member> members = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < memberIds.size(); i++) {
                stmt.setInt(i + 1, memberIds.get(i));
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
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
                members.add(member);
            }
        }

        return members;
    }
}
