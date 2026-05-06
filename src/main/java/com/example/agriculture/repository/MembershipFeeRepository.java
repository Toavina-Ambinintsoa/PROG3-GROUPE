package com.example.agriculture.repository;

import com.example.agriculture.config.DataSource;
import com.example.agriculture.entity.CreateMembershipFee;
import com.example.agriculture.entity.Enum.Status;
import com.example.agriculture.entity.Enum.Frequency;
import com.example.agriculture.entity.MembershipFee;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MembershipFeeRepository {

    private final DataSource dataSource;

    public MembershipFeeRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<MembershipFee> findByCollectivityId(String collectivityId) {
        String sql = """
                SELECT id, eligible_from, frequency, amount, label, status
                FROM membership_fee
                WHERE collectivity_id = ?
                ORDER BY eligible_from DESC
                """;
        List<MembershipFee> fees = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, collectivityId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                fees.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return fees;
    }

    public MembershipFee findById(String feeId) {
        String sql = """
                SELECT id, eligible_from, frequency, amount, label, status
                FROM membership_fee WHERE id = ?
                """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, feeId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean exists(String feeId) {
        return findById(feeId) != null;
    }

    public List<MembershipFee> save(String collectivityId, List<CreateMembershipFee> fees) {
        String sql = """
                INSERT INTO membership_fee (collectivity_id, eligible_from, frequency, amount, label, status)
                VALUES (?, ?, ?, ?, ?, 'ACTIVE')
                RETURNING id, eligible_from, frequency, amount, label, status
                """;
        List<MembershipFee> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (CreateMembershipFee fee : fees) {
                stmt.setString(1, collectivityId);
                stmt.setDate(2, Date.valueOf(fee.getEligibleFrom()));
                stmt.setString(3, fee.getFrequency().name());
                stmt.setDouble(4, fee.getAmount());
                stmt.setString(5, fee.getLabel());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public double getTotalAnnualFees(String collectivityId) {
        String sql = """
                SELECT COALESCE(SUM(amount), 0)
                FROM membership_fee
                WHERE collectivity_id = ?
                  AND frequency = 'ANNUALLY'
                  AND status = 'ACTIVE'
                """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, collectivityId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getDouble(1);
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private MembershipFee mapRow(ResultSet rs) throws SQLException {
        MembershipFee fee = new MembershipFee();
        fee.setId(rs.getString("id"));
        fee.setEligibleFrom(rs.getDate("eligible_from").toLocalDate());
        fee.setFrequency(Frequency.valueOf(rs.getString("frequency")));
        fee.setAmount(rs.getDouble("amount"));
        fee.setLabel(rs.getString("label"));
        fee.setStatus(Status.valueOf(rs.getString("status")));
        return fee;
    }
}
