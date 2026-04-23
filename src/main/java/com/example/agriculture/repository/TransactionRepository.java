package com.example.agriculture.repository;

import com.example.agriculture.config.DataSource;
import com.example.agriculture.entity.*;
import com.example.agriculture.entity.Enum.PaymentMode;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class TransactionRepository {

    private final DataSource dataSource;
    private final MemberRepository memberRepository;
    private final FinancialAccountRepository financialAccountRepository;

    public TransactionRepository(DataSource dataSource,
                                 MemberRepository memberRepository,
                                 FinancialAccountRepository financialAccountRepository) {
        this.dataSource = dataSource;
        this.memberRepository = memberRepository;
        this.financialAccountRepository = financialAccountRepository;
    }

    /**
     * Record a transaction inside the collectivity (called automatically when a member pays).
     */
    public CollectivityTransaction save(Connection conn,
                                        String collectivityId,
                                        int memberId,
                                        double amount,
                                        PaymentMode paymentMode,
                                        String accountCreditedId) throws SQLException {
        String sql = """
                INSERT INTO collectivity_transaction
                    (collectivity_id, member_id, amount, payment_mode, account_credited_id, creation_date)
                VALUES (?, ?, ?, ?, ?, CURRENT_DATE)
                RETURNING id, creation_date
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, collectivityId);
            stmt.setInt(2, memberId);
            stmt.setDouble(3, amount);
            stmt.setString(4, paymentMode.name());
            stmt.setString(5, accountCreditedId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                CollectivityTransaction tx = new CollectivityTransaction();
                tx.setId(rs.getString("id"));
                tx.setCreationDate(rs.getDate("creation_date").toLocalDate());
                tx.setAmount(amount);
                tx.setPaymentMode(paymentMode);
                tx.setAccountCredited(financialAccountRepository.findById(accountCreditedId));
                tx.setMemberDebited(memberRepository.findById(memberId));
                return tx;
            }
        }
        throw new SQLException("Failed to insert transaction");
    }

    public List<CollectivityTransaction> findByCollectivityIdAndPeriod(String collectivityId,
                                                                        LocalDate from,
                                                                        LocalDate to) {
        String sql = """
                SELECT id, creation_date, amount, payment_mode, account_credited_id, member_id
                FROM collectivity_transaction
                WHERE collectivity_id = ?
                  AND creation_date >= ?
                  AND creation_date <= ?
                ORDER BY creation_date DESC
                """;
        List<CollectivityTransaction> txList = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, collectivityId);
            stmt.setDate(2, Date.valueOf(from));
            stmt.setDate(3, Date.valueOf(to));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                CollectivityTransaction tx = new CollectivityTransaction();
                tx.setId(rs.getString("id"));
                tx.setCreationDate(rs.getDate("creation_date").toLocalDate());
                tx.setAmount(rs.getDouble("amount"));
                tx.setPaymentMode(PaymentMode.valueOf(rs.getString("payment_mode")));
                tx.setAccountCredited(
                    financialAccountRepository.findById(rs.getString("account_credited_id")));
                tx.setMemberDebited(memberRepository.findById(rs.getInt("member_id")));
                txList.add(tx);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return txList;
    }
}
