package com.example.agriculture.repository;

import com.example.agriculture.config.DataSource;
import com.example.agriculture.entity.BankAccount;
import com.example.agriculture.entity.CashAccount;
import com.example.agriculture.entity.Enum.Bank;
import com.example.agriculture.entity.Enum.MobileBankingService;
import com.example.agriculture.entity.FinancialAccount;
import com.example.agriculture.entity.MobileBankingAccount;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class FinancialAccountRepository {

    private final DataSource dataSource;

    public FinancialAccountRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public FinancialAccount findById(String accountId) {
        FinancialAccount account;
        account = findCashAccount(accountId);
        if (account != null) return account;
        account = findMobileBankingAccount(accountId);
        if (account != null) return account;
        account = findBankAccount(accountId);
        return account;
    }

    public boolean exists(String accountId) {
        return findById(accountId) != null;
    }

    public CashAccount findCashAccount(String accountId) {
        String sql = "SELECT id, amount FROM financial_account_cash WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, accountId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                CashAccount ca = new CashAccount();
                ca.setId(rs.getString("id"));
                ca.setAmount(rs.getDouble("amount"));
                return ca;
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void creditCashAccount(Connection conn, String accountId, double amount) throws SQLException {
        String sql = "UPDATE financial_account_cash SET amount = amount + ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, amount);
            stmt.setString(2, accountId);
            stmt.executeUpdate();
        }
    }

    public MobileBankingAccount findMobileBankingAccount(String accountId) {
        String sql = "SELECT id, amount, holder_name, service, mobile_number " +
                     "FROM financial_account_mobile WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, accountId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                MobileBankingAccount mba = new MobileBankingAccount();
                mba.setId(rs.getString("id"));
                mba.setAmount(rs.getDouble("amount"));
                mba.setHolderName(rs.getString("holder_name"));
                mba.setMobileBankingService(MobileBankingService.valueOf(rs.getString("service")));
                mba.setMobileNumber(rs.getString("mobile_number"));
                return mba;
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void creditMobileBankingAccount(Connection conn, String accountId, double amount) throws SQLException {
        String sql = "UPDATE financial_account_mobile SET amount = amount + ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, amount);
            stmt.setString(2, accountId);
            stmt.executeUpdate();
        }
    }

    public BankAccount findBankAccount(String accountId) {
        String sql = "SELECT id, amount, holder_name, bank_name, bank_code, branch_code, " +
                     "account_number, account_key FROM financial_account_bank WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, accountId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                BankAccount ba = new BankAccount();
                ba.setId(rs.getString("id"));
                ba.setAmount(rs.getDouble("amount"));
                ba.setHolderName(rs.getString("holder_name"));
                ba.setBankName(Bank.valueOf(rs.getString("bank_name")));
                ba.setBankCode(rs.getString("bank_code"));
                ba.setBankBranchCode(rs.getString("branch_code"));
                ba.setBankAccountNumber(rs.getString("account_number"));
                ba.setBankAccountKey(rs.getString("account_key"));
                return ba;
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void creditBankAccount(Connection conn, String accountId, double amount) throws SQLException {
        String sql = "UPDATE financial_account_bank SET amount = amount + ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, amount);
            stmt.setString(2, accountId);
            stmt.executeUpdate();
        }
    }

    public void creditAccount(Connection conn, String accountId, double amount) throws SQLException {
        if (findCashAccount(accountId) != null) {
            creditCashAccount(conn, accountId, amount);
        } else if (findMobileBankingAccount(accountId) != null) {
            creditMobileBankingAccount(conn, accountId, amount);
        } else {
            creditBankAccount(conn, accountId, amount);
        }
    }
}
