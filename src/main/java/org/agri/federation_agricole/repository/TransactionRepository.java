package org.agri.federation_agricole.repository;

import org.agri.federation_agricole.config.DataSource;
import org.agri.federation_agricole.entity.CollectivityTransaction;
import org.agri.federation_agricole.entity.Enum.PaymentMode;
import org.agri.federation_agricole.entity.FinancialAccount;
import org.agri.federation_agricole.entity.Member;
import org.agri.federation_agricole.entity.Enum.AccountType;
import org.agri.federation_agricole.entity.Enum.Gender;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class TransactionRepository {
    private final DataSource dataSource;

    public TransactionRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<CollectivityTransaction> getTransactionsByPeriod(String collectivityId, LocalDate from, LocalDate to) {
        String query = """
                SELECT t.id, t.amount, t.method, t.created_at,
                       a.id as account_id, a.type as account_type, a.initial_balance, a.holder, a.phone,
                       m.id as member_id, m.first_name, m.last_name, m.birth_date, m.gender,
                       m.address, m.profession, m.phone as member_phone, m.email, m.registration_date
                FROM transactions t
                JOIN accounts a ON a.id = t.account_id
                JOIN members m ON m.id = t.member_id
                WHERE t.collectivity_id = ?
                  AND t.created_at >= ?
                  AND t.created_at <= ?
                ORDER BY t.created_at DESC
                """;
        List<CollectivityTransaction> transactions = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, collectivityId);
            ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(to));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                CollectivityTransaction transaction = new CollectivityTransaction();
                transaction.setId(rs.getString("id"));
                transaction.setAmount(rs.getInt("amount"));
                transaction.setCreationDate(rs.getDate("created_at").toLocalDate());
                transaction.setPaymentMode(PaymentMode.valueOf(rs.getString("method")));

                FinancialAccount account = new FinancialAccount();
                account.setId(rs.getString("account_id"));
                account.setAccountType(AccountType.valueOf(rs.getString("account_type")));
                account.setInitialBalance(rs.getInt("initial_balance"));
                account.setHolder(rs.getString("holder"));
                account.setPhone(rs.getString("phone"));
                transaction.setAccountCredited(account);

                Member member = new Member();
                member.setId(rs.getString("member_id"));
                member.setFirstName(rs.getString("first_name"));
                member.setLastName(rs.getString("last_name"));
                member.setBirthDate(rs.getDate("birth_date").toLocalDate());
                member.setGender(Gender.valueOf(rs.getString("gender")));
                member.setAddress(rs.getString("address"));
                member.setProfession(rs.getString("profession"));
                member.setPhone(rs.getString("member_phone"));
                member.setEmail(rs.getString("email"));
                member.setRegistrationDate(rs.getDate("registration_date").toLocalDate());
                transaction.setMemberDebited(member);

                transactions.add(transaction);
            }
            return transactions;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getLastTransactionId(String collectivityId) {
        String query = "SELECT id FROM transactions WHERE collectivity_id = ? ORDER BY id DESC LIMIT 1";
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, collectivityId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("id");
            }
            String[] parts = collectivityId.split("-");
            return parts[0] + "-TX0";
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getNextTransactionId(String collectivityId) {
        String lastId = getLastTransactionId(collectivityId);
        String[] parts = lastId.split("-TX");
        if (parts.length != 2) {
            String[] colParts = collectivityId.split("-");
            return colParts[0] + "-TX1";
        }
        int num = Integer.parseInt(parts[1]);
        num += 1;
        return parts[0] + "-TX" + num;
    }

    public void saveTransaction(Connection conn, String collectivityId, String memberId,
                                int amount, String accountId, String paymentMode) throws SQLException {
        String insertQuery = """
                INSERT INTO transactions (id, collectivity_id, member_id, amount, account_id, method, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        String id = getNextTransactionId(collectivityId);
        PreparedStatement ps = conn.prepareStatement(insertQuery);
        ps.setString(1, id);
        ps.setString(2, collectivityId);
        ps.setString(3, memberId);
        ps.setInt(4, amount);
        ps.setString(5, accountId);
        ps.setString(6, paymentMode);
        ps.setDate(7, Date.valueOf(LocalDate.now()));
        ps.executeUpdate();
    }
}