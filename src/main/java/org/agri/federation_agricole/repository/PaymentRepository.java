package org.agri.federation_agricole.repository;

import org.agri.federation_agricole.config.DataSource;
import org.agri.federation_agricole.entity.CreateMemberPayment;
import org.agri.federation_agricole.entity.Enum.PaymentMode;
import org.agri.federation_agricole.entity.FinancialAccount;
import org.agri.federation_agricole.entity.MemberPayment;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PaymentRepository {
    private final DataSource dataSource;
    private final FinancialAccountRepository financialAccountRepository;
    private final TransactionRepository transactionRepository;

    public PaymentRepository(DataSource dataSource,
                             FinancialAccountRepository financialAccountRepository,
                             TransactionRepository transactionRepository) {
        this.dataSource = dataSource;
        this.financialAccountRepository = financialAccountRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<MemberPayment> createPayments(String memberId, List<CreateMemberPayment> createPayments) {
        String insertQuery = """
                INSERT INTO payments (id, collectivity_id, member_id, amount, account_id, payment_method, payment_date)
                VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id
                """;
        String collectivityQuery = "SELECT collectivity_id FROM collectivity_members WHERE member_id = ? LIMIT 1";

        List<String> savedIds = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            // Retrieve the collectivity_id for this member
            PreparedStatement colStmt = conn.prepareStatement(collectivityQuery);
            colStmt.setString(1, memberId);
            ResultSet colRs = colStmt.executeQuery();
            if (!colRs.next()) {
                throw new RuntimeException("Member not found in any collectivity");
            }
            String collectivityId = colRs.getString("collectivity_id");

            conn.setAutoCommit(false);
            for (CreateMemberPayment c : createPayments) {
                String id = getNextPaymentId(collectivityId);
                PreparedStatement ps = conn.prepareStatement(insertQuery);
                ps.setString(1, id);
                ps.setString(2, collectivityId);
                ps.setString(3, memberId);
                ps.setInt(4, c.getAmount());
                ps.setString(5, c.getAccountCreditedIdentifier());
                ps.setString(6, c.getPaymentMode().name());
                ps.setDate(7, Date.valueOf(LocalDate.now()));
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    savedIds.add(rs.getString("id"));
                }
                // Automatically store a transaction
                transactionRepository.saveTransaction(conn, collectivityId, memberId,
                        c.getAmount(), c.getAccountCreditedIdentifier(), c.getPaymentMode().name());
            }
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return getPaymentsByIds(savedIds);
    }

    private List<MemberPayment> getPaymentsByIds(List<String> ids) {
        if (ids.isEmpty()) return new ArrayList<>();
        String query = """
                SELECT id, amount, payment_method, account_id, payment_date
                FROM payments WHERE id = ?
                """;
        List<MemberPayment> payments = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            for (String id : ids) {
                PreparedStatement ps = conn.prepareStatement(query);
                ps.setString(1, id);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    MemberPayment payment = new MemberPayment();
                    payment.setId(rs.getString("id"));
                    payment.setAmount(rs.getInt("amount"));
                    payment.setPaymentMode(PaymentMode.valueOf(rs.getString("payment_method")));
                    payment.setCreationDate(rs.getDate("payment_date").toLocalDate());
                    FinancialAccount account = financialAccountRepository.getById(rs.getString("account_id"));
                    payment.setAccountCredited(account);
                    payments.add(payment);
                }
            }
            return payments;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String getLastPaymentId(String collectivityId) {
        String query = "SELECT id FROM payments WHERE collectivity_id = ? ORDER BY id DESC LIMIT 1";
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, collectivityId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("id");
            }
            String[] parts = collectivityId.split("-");
            return parts[0] + "-PAY0";
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String getNextPaymentId(String collectivityId) {
        String lastId = getLastPaymentId(collectivityId);
        String[] parts = lastId.split("-PAY");
        if (parts.length != 2) {
            String[] colParts = collectivityId.split("-");
            return colParts[0] + "-PAY1";
        }
        int num = Integer.parseInt(parts[1]);
        num += 1;
        return parts[0] + "-PAY" + num;
    }
}