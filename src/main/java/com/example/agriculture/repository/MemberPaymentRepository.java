package com.example.agriculture.repository;

import com.example.agriculture.config.DataSource;
import com.example.agriculture.entity.FinancialAccount;
import com.example.agriculture.entity.MemberPayment;
import com.example.agriculture.entity.Enum.PaymentMode;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MemberPaymentRepository {

    private final DataSource dataSource;
    private final FinancialAccountRepository financialAccountRepository;

    public MemberPaymentRepository(DataSource dataSource,
                                   FinancialAccountRepository financialAccountRepository) {
        this.dataSource = dataSource;
        this.financialAccountRepository = financialAccountRepository;
    }

    public MemberPayment save(Connection conn,
                              int memberId,
                              double amount,
                              PaymentMode paymentMode,
                              String membershipFeeId,
                              String accountCreditedId) throws SQLException {
        String sql = """
                INSERT INTO member_payment
                    (member_id, amount, payment_mode, membership_fee_id, account_credited_id, creation_date)
                VALUES (?, ?, ?, ?, ?, CURRENT_DATE)
                RETURNING id, creation_date
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            stmt.setDouble(2, amount);
            stmt.setString(3, paymentMode.name());
            stmt.setString(4, membershipFeeId);
            stmt.setString(5, accountCreditedId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                MemberPayment payment = new MemberPayment();
                payment.setId(rs.getString("id"));
                payment.setAmount(amount);
                payment.setPaymentMode(paymentMode);
                payment.setCreationDate(rs.getDate("creation_date").toLocalDate());
                payment.setAccountCredited(financialAccountRepository.findById(accountCreditedId));
                return payment;
            }
        }
        throw new SQLException("Failed to insert member payment");
    }

    public List<MemberPayment> findByMemberId(int memberId) {
        String sql = """
                SELECT id, amount, payment_mode, account_credited_id, creation_date
                FROM member_payment WHERE member_id = ?
                ORDER BY creation_date DESC
                """;
        List<MemberPayment> payments = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                MemberPayment p = new MemberPayment();
                p.setId(rs.getString("id"));
                p.setAmount(rs.getDouble("amount"));
                p.setPaymentMode(PaymentMode.valueOf(rs.getString("payment_mode")));
                p.setCreationDate(rs.getDate("creation_date").toLocalDate());
                p.setAccountCredited(
                    financialAccountRepository.findById(rs.getString("account_credited_id")));
                payments.add(p);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return payments;
    }
}
