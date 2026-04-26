package com.expensesplitter.ui;

import com.expensesplitter.model.Group;
import com.expensesplitter.model.User;
import com.expensesplitter.service.ExpenseService;
import com.expensesplitter.service.GroupService;
import com.expensesplitter.service.UserService;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddExpensePanel extends JPanel {

    private ExpenseService expenseService;
    private GroupService groupService;
    private UserService userService;
    private JComboBox<String> groupCombo;
    private JComboBox<String> paidByCombo;
    private JTextField amountField;
    private JTextField descriptionField;
    private JTextField splitsField;
    private Map<String, Integer> groupMap;

    public AddExpensePanel(ExpenseService expenseService, GroupService groupService, UserService userService) {
        this.expenseService = expenseService;
        this.groupService = groupService;
        this.userService = userService;
        setLayout(new BorderLayout(10, 10));
        add(createFormPanel(), BorderLayout.CENTER);
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel title = new JLabel("Add Expense");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        panel.add(new JLabel("Group:"), gbc);

        gbc.gridx = 1;
        groupCombo = new JComboBox<>();
        groupCombo.setPreferredSize(new Dimension(180, 25));
        refreshGroups();
        groupCombo.addActionListener(e -> refreshPaidByForGroup());
        panel.add(groupCombo, gbc);

        JButton btnRefreshGroup = new JButton("↻");
        btnRefreshGroup.setPreferredSize(new Dimension(30, 25));
        btnRefreshGroup.addActionListener(e -> refreshGroups());
        panel.add(btnRefreshGroup, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        panel.add(new JLabel("Paid By:"), gbc);

        gbc.gridx = 1;
        paidByCombo = new JComboBox<>();
        paidByCombo.setPreferredSize(new Dimension(200, 25));
        refreshPaidByForGroup();
        panel.add(paidByCombo, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        panel.add(new JLabel("Total Amount:"), gbc);

        gbc.gridx = 1;
        amountField = new JTextField(20);
        panel.add(amountField, gbc);

        gbc.gridy = 4;
        gbc.gridx = 0;
        panel.add(new JLabel("Description:"), gbc);

        gbc.gridx = 1;
        descriptionField = new JTextField(20);
        panel.add(descriptionField, gbc);

        gbc.gridy = 5;
        gbc.gridx = 0;
        panel.add(new JLabel("Splits (name:amt, ...):"), gbc);

        gbc.gridx = 1;
        splitsField = new JTextField(20);
        panel.add(splitsField, gbc);

        gbc.gridy = 6;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JButton btnAdd = new JButton("Add Expense");
        btnAdd.addActionListener(e -> addExpense());
        panel.add(btnAdd, gbc);

        return panel;
    }

    private void refreshUsers() {
        paidByCombo.removeAllItems();
        try {
            List<User> users = userService.getAllUsers();
            for (User u : users) {
                paidByCombo.addItem(u.getName());
            }
        } catch (Exception e) {
            // ignore
        }
    }

    private void refreshPaidByForGroup() {
        paidByCombo.removeAllItems();
        String selectedGroup = (String) groupCombo.getSelectedItem();
        if (selectedGroup == null) {
            return;
        }
        Integer groupId = groupMap.get(selectedGroup);
        if (groupId == null) {
            return;
        }
        try {
            List<String> memberIds = groupService.getMemberIds(String.valueOf(groupId));
            Map<String, String> idToName = new HashMap<>();
            for (User u : userService.getAllUsers()) {
                idToName.put(u.getId(), u.getName());
            }
            for (String memberId : memberIds) {
                String name = idToName.get(memberId);
                if (name != null) {
                    paidByCombo.addItem(name);
                }
            }
        } catch (Exception e) {
            // ignore
        }
    }

    private void refreshGroups() {
        groupCombo.removeAllItems();
        groupMap = new HashMap<>();
        try {
            List<Group> groups = groupService.getAllGroups();
            for (Group g : groups) {
                groupCombo.addItem(g.getName());
                groupMap.put(g.getName(), Integer.parseInt(g.getId()));
            }
        } catch (Exception e) {
            // ignore
        }
    }

    private void addExpense() {
        String selectedGroup = (String) groupCombo.getSelectedItem();
        String paidBy = (String) paidByCombo.getSelectedItem();
        String amountStr = amountField.getText().trim();
        String description = descriptionField.getText().trim();
        String splitsStr = splitsField.getText().trim();

        if (selectedGroup == null || paidBy == null || amountStr.isEmpty() || splitsStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all required fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int groupId = groupMap.get(selectedGroup);
        double amount;
        Map<String, Double> splits;
        
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "Amount must be positive.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid amount. Must be a number.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            splits = parseSplits(splitsStr);
            if (splits.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Invalid splits format. Use: name:amount, name:amount", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid splits format. Use: name:amount, name:amount", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            expenseService.addExpense(groupId, paidBy, amount, description, splits);
            JOptionPane.showMessageDialog(this, "Expense added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearFields();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Map<String, Double> parseSplits(String input) {
        Map<String, Double> splits = new HashMap<>();
        
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Splits cannot be empty");
        }
        
        String[] parts = input.split(",");
        if (parts.length == 0) {
            throw new IllegalArgumentException("No splits provided");
        }
        
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            
            String[] kv = trimmed.split(":");
            if (kv.length != 2) {
                throw new IllegalArgumentException("Invalid format: '" + trimmed + "'. Use name:amount");
            }
            
            String name = kv[0].trim();
            String amountStr = kv[1].trim();
            
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name cannot be empty in: '" + trimmed + "'");
            }
            
            double amount;
            try {
                amount = Double.parseDouble(amountStr);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Invalid amount '" + amountStr + "' in: '" + trimmed + "'");
            }
            
            if (amount < 0) {
                throw new IllegalArgumentException("Amount cannot be negative: '" + trimmed + "'");
            }
            
            splits.put(name, amount);
        }
        
        if (splits.isEmpty()) {
            throw new IllegalArgumentException("No valid splits found");
        }
        
        return splits;
    }

    private void clearFields() {
        groupCombo.setSelectedIndex(0);
        amountField.setText("");
        descriptionField.setText("");
        splitsField.setText("");
    }
}