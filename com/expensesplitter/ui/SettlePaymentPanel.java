package com.expensesplitter.ui;

import com.expensesplitter.model.Group;
import com.expensesplitter.model.User;
import com.expensesplitter.service.GroupService;
import com.expensesplitter.service.SettlementService;
import com.expensesplitter.service.UserService;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettlePaymentPanel extends JPanel {

    private SettlementService settlementService;
    private GroupService groupService;
    private UserService userService;
    private JComboBox<String> payerCombo;
    private JComboBox<String> receiverCombo;
    private JComboBox<String> groupCombo;
    private JTextField amountField;
    private Map<String, Integer> groupMap;

    public SettlePaymentPanel(SettlementService settlementService, GroupService groupService, UserService userService) {
        this.settlementService = settlementService;
        this.groupService = groupService;
        this.userService = userService;
        setLayout(new BorderLayout(10, 10));
        add(createFormPanel(), BorderLayout.CENTER);
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel title = new JLabel("Settle Payment");
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
        groupCombo.addActionListener(e -> refreshPayerReceiverForGroup());
        panel.add(groupCombo, gbc);

        JButton btnRefresh = new JButton("↻");
        btnRefresh.setPreferredSize(new Dimension(30, 25));
        btnRefresh.addActionListener(e -> refreshGroups());
        panel.add(btnRefresh, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        panel.add(new JLabel("Payer:"), gbc);

        gbc.gridx = 1;
        payerCombo = new JComboBox<>();
        payerCombo.setPreferredSize(new Dimension(200, 25));
        panel.add(payerCombo, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        panel.add(new JLabel("Receiver:"), gbc);

        gbc.gridx = 1;
        receiverCombo = new JComboBox<>();
        receiverCombo.setPreferredSize(new Dimension(200, 25));
        panel.add(receiverCombo, gbc);

        refreshPayerReceiverForGroup();

        gbc.gridy = 4;
        gbc.gridx = 0;
        panel.add(new JLabel("Amount:"), gbc);

        gbc.gridx = 1;
        amountField = new JTextField(20);
        panel.add(amountField, gbc);

        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JButton btnSettle = new JButton("Settle");
        btnSettle.addActionListener(e -> settle());
        panel.add(btnSettle, gbc);

        return panel;
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

    private void refreshPayerReceiverForGroup() {
        payerCombo.removeAllItems();
        receiverCombo.removeAllItems();
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
                    payerCombo.addItem(name);
                    receiverCombo.addItem(name);
                }
            }
        } catch (Exception e) {
            // ignore
        }
    }

    private void settle() {
        String selectedGroup = (String) groupCombo.getSelectedItem();
        String payer = (String) payerCombo.getSelectedItem();
        String receiver = (String) receiverCombo.getSelectedItem();
        String amountStr = amountField.getText().trim();

        if (selectedGroup == null || payer == null || receiver == null || amountStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int groupId = groupMap.get(selectedGroup);
        double amount;
        
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
            settlementService.settlePayment(payer, receiver, amount, groupId);
            JOptionPane.showMessageDialog(this, "Payment settled: " + payer + " pays " + receiver + " Rs." + amount, "Success", JOptionPane.INFORMATION_MESSAGE);
            clearFields();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        amountField.setText("");
    }
}