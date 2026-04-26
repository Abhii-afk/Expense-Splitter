package com.expensesplitter.repository;

import com.expensesplitter.model.Group;
import com.expensesplitter.model.User;
import com.expensesplitter.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class GroupRepository {

    public void createGroup(String name) {
        String sql = "INSERT INTO group_table (name) VALUES (?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Error creating group: " + e.getMessage());
        }
    }

    public Group getGroupByName(String name) {
        String sql = "SELECT id, name FROM group_table WHERE name = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Group(rs.getString("id"), rs.getString("name"));
            }
        } catch (Exception e) {
            System.err.println("Error fetching group: " + e.getMessage());
        }
        return null;
    }

    public List<Group> getAllGroups() {
        List<Group> groups = new ArrayList<>();
        String sql = "SELECT id, name FROM group_table";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                groups.add(new Group(rs.getString("id"), rs.getString("name")));
            }
        } catch (Exception e) {
            System.err.println("Error fetching groups: " + e.getMessage());
        }
        return groups;
    }

    public void addMemberToGroup(String groupId, String userId) {
        String sql = "INSERT INTO group_members (group_id, user_id) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, groupId);
            pstmt.setString(2, userId);
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Error adding member to group: " + e.getMessage());
        }
    }

    public List<String> getMemberIds(String groupId) {
        List<String> memberIds = new ArrayList<>();
        String sql = "SELECT user_id FROM group_members WHERE group_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, groupId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                memberIds.add(rs.getString("user_id"));
            }
        } catch (Exception e) {
            System.err.println("Error fetching members: " + e.getMessage());
        }
        return memberIds;
    }
}