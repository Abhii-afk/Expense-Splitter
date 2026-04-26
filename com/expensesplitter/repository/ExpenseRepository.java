package com.expensesplitter.repository;

import com.expensesplitter.model.Expense;
import com.expensesplitter.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ExpenseRepository {

    public int createExpense(int groupId, int paidBy, double totalAmount, String description) {
        String sql = "INSERT INTO expenses (group_id, paid_by, total_amount, description) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, groupId);
                pstmt.setInt(2, paidBy);
                pstmt.setDouble(3, totalAmount);
                pstmt.setString(4, description);
                pstmt.executeUpdate();

                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    conn.commit();
                    return rs.getInt(1);
                }
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception e) {
            System.err.println("Error creating expense: " + e.getMessage());
        }
        return -1;
    }

    public void addExpenseSplit(int expenseId, int userId, double amountOwed) {
        String sql = "INSERT INTO expense_splits (expense_id, user_id, amount_owed) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, expenseId);
                pstmt.setInt(2, userId);
                pstmt.setDouble(3, amountOwed);
                pstmt.executeUpdate();
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception e) {
            System.err.println("Error adding expense split: " + e.getMessage());
        }
    }

    public List<Expense> getExpensesByGroup(int groupId) {
        List<Expense> expenses = new ArrayList<>();
        String sql = "SELECT id, group_id, paid_by, total_amount, description FROM expenses WHERE group_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, groupId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Expense exp = new Expense(
                    String.valueOf(rs.getInt("id")),
                    rs.getString("description"),
                    rs.getDouble("total_amount"),
                    null,
                    new ArrayList<>()
                );
                expenses.add(exp);
            }
        } catch (Exception e) {
            System.err.println("Error fetching expenses: " + e.getMessage());
        }
        return expenses;
    }
}