package com.expensesplitter.repository;

import com.expensesplitter.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SettlementRepository {

    public void recordSettlement(int groupId, int payerId, int receiverId, double amount) {
        String sql = "INSERT INTO settlements (group_id, payer_id, receiver_id, amount) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, groupId);
            pstmt.setInt(2, payerId);
            pstmt.setInt(3, receiverId);
            pstmt.setDouble(4, amount);
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Error recording settlement: " + e.getMessage());
        }
    }

    public List<Settlement> getSettlementsByGroup(int groupId) {
        List<Settlement> settlements = new ArrayList<>();
        String sql = "SELECT id, group_id, payer_id, receiver_id, amount FROM settlements WHERE group_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, groupId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                settlements.add(new Settlement(
                    rs.getInt("id"),
                    rs.getInt("group_id"),
                    rs.getInt("payer_id"),
                    rs.getInt("receiver_id"),
                    rs.getDouble("amount")
                ));
            }
        } catch (Exception e) {
            System.err.println("Error fetching settlements: " + e.getMessage());
        }
        return settlements;
    }

    public static class Settlement {
        public final int id;
        public final int groupId;
        public final int payerId;
        public final int receiverId;
        public final double amount;

        public Settlement(int id, int groupId, int payerId, int receiverId, double amount) {
            this.id = id;
            this.groupId = groupId;
            this.payerId = payerId;
            this.receiverId = receiverId;
            this.amount = amount;
        }
    }
}