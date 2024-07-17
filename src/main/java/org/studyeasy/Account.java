// Account.java
package org.studyeasy;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Account {
    private int accountId;
    private double balance;
    private String pin;
    private List<String> transactionHistory;

    public Account(int accountId, double balance, String pin) {
        this.accountId = accountId;
        this.balance = balance;
        this.pin = pin;
        this.transactionHistory = new ArrayList<>();
    }

    public static Account createAccount(double initialBalance, String pin) {
        Account newAccount = null;
        try (Connection connection = DatabaseUtil.getConnection()) {
            String query = "INSERT INTO accounts (balance, pin) VALUES (?, ?)";
            PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.setDouble(1, initialBalance);
            statement.setString(2, pin);
            statement.executeUpdate();

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                int accountId = generatedKeys.getInt(1);
                newAccount = new Account(accountId, initialBalance, pin);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return newAccount;
    }

    public static Account getAccount(int accountId) {
        Account account = null;
        try (Connection connection = DatabaseUtil.getConnection()) {
            String query = "SELECT * FROM accounts WHERE account_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, accountId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                double balance = resultSet.getDouble("balance");
                String pin = resultSet.getString("pin");
                account = new Account(accountId, balance, pin);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return account;
    }

    public double getBalance() {
        try (Connection connection = DatabaseUtil.getConnection()) {
            String query = "SELECT balance FROM accounts WHERE account_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, accountId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                balance = resultSet.getDouble("balance");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return balance;
    }

    public boolean withdraw(double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
            updateBalanceInDatabase();
            logTransaction("Withdrawal", amount);
            return true;
        }
        return false;
    }

    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            updateBalanceInDatabase();
            logTransaction("Deposit", amount);
        }
    }

    public boolean fastCash(double amount) {
        if (amount == 100.0 && amount <= balance) {
            balance -= amount;
            updateBalanceInDatabase();
            logTransaction("Fast Cash", amount);
            return true;
        }
        return false;
    }

    public boolean validatePIN(String enteredPIN) {
        return pin.equals(enteredPIN);
    }

    public void changePIN(String newPIN) {
        this.pin = newPIN;
        try (Connection connection = DatabaseUtil.getConnection()) {
            String updatePINQuery = "UPDATE accounts SET pin = ? WHERE account_id = ?";
            PreparedStatement statement = connection.prepareStatement(updatePINQuery);
            statement.setString(1, newPIN);
            statement.setInt(2, accountId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getMiniStatement() {
        List<String> miniStatement = new ArrayList<>();
        try (Connection connection = DatabaseUtil.getConnection()) {
            String query = "SELECT transaction_type, transaction_date FROM transactions WHERE account_id = ? ORDER BY transaction_date DESC LIMIT 10";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, accountId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String transactionType = resultSet.getString("transaction_type");
                Timestamp transactionDate = resultSet.getTimestamp("transaction_date");
                miniStatement.add(transactionType + " on " + transactionDate);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return miniStatement;
    }

    private void updateBalanceInDatabase() {
        try (Connection connection = DatabaseUtil.getConnection()) {
            String query = "UPDATE accounts SET balance = ? WHERE account_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setDouble(1, balance);
            statement.setInt(2, accountId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void logTransaction(String transactionType, double amount) {
        transactionHistory.add(transactionType + ": $" + amount);
        try (Connection connection = DatabaseUtil.getConnection()) {
            String query = "INSERT INTO transactions (account_id, transaction_type, transaction_date) VALUES (?, ?, NOW())";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, accountId);
            statement.setString(2, transactionType + ": $" + amount);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getAccountId() {
        return accountId;
    }
}
