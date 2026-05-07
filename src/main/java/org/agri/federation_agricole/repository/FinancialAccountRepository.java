package org.agri.federation_agricole.repository;

import org.agri.federation_agricole.config.DataSource;
import org.agri.federation_agricole.entity.FinancialAccount;
import org.agri.federation_agricole.entity.Enum.AccountType;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class FinancialAccountRepository {
    private final DataSource dataSource;

    public FinancialAccountRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<FinancialAccount> getFinancialAccountsWithBalance(String collectivityId, LocalDate at) {
        // Balance = initial_balance + sum of payments credited to this account up to `at`
        String query = """
                SELECT a.id, a.type, a.initial_balance, a.holder, a.phone,
                       COALESCE(SUM(p.amount), 0) AS total_payments
                FROM accounts a
                LEFT JOIN payments p ON p.account_id = a.id AND p.payment_date <= ?
                WHERE a.collectivity_id = ?
                GROUP BY a.id, a.type, a.initial_balance, a.holder, a.phone
                """;
        List<FinancialAccount> accounts = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setDate(1, Date.valueOf(at));
            ps.setString(2, collectivityId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                FinancialAccount account = new FinancialAccount();
                account.setId(rs.getString("id"));
                account.setCollectivityId(collectivityId);
                account.setAccountType(AccountType.valueOf(rs.getString("type")));
                int balance = rs.getInt("initial_balance") + rs.getInt("total_payments");
                account.setInitialBalance(balance);
                account.setHolder(rs.getString("holder"));
                account.setPhone(rs.getString("phone"));
                accounts.add(account);
            }
            return accounts;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public FinancialAccount getById(String id) {
        String query = "SELECT id, collectivity_id, type, initial_balance, holder, phone FROM accounts WHERE id = ?";
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                FinancialAccount account = new FinancialAccount();
                account.setId(rs.getString("id"));
                account.setCollectivityId(rs.getString("collectivity_id"));
                account.setAccountType(AccountType.valueOf(rs.getString("type")));
                account.setInitialBalance(rs.getInt("initial_balance"));
                account.setHolder(rs.getString("holder"));
                account.setPhone(rs.getString("phone"));
                return account;
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}