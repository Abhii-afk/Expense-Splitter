package com.expensesplitter.ui;

import com.expensesplitter.model.Group;
import com.expensesplitter.model.User;
import com.expensesplitter.service.BalanceService;
import com.expensesplitter.service.GroupService;
import com.expensesplitter.service.UserService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShowBalancePanel extends JPanel {

    private BalanceService balanceService;
    private GroupService groupService;
    private UserService userService;
    private JComboBox<String> groupCombo;
    private JButton btnShow;
    private JTable balanceTable;
    private DefaultTableModel tableModel;
    private JLabel loadingLabel;
    private Map<String, Integer> groupMap;

    public ShowBalancePanel(BalanceService balanceService, GroupService groupService, UserService userService) {
        this.balanceService = balanceService;
        this.groupService = groupService;
        this.userService = userService;
        setLayout(new BorderLayout(10, 10));
        add(createFormPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Group:"));
        groupCombo = new JComboBox<>();
        groupCombo.setPreferredSize(new Dimension(180, 25));
        refreshGroups();
        groupCombo.addActionListener(e -> btnShow.doClick());
        panel.add(groupCombo);

        JButton btnRefresh = new JButton("↻");
        btnRefresh.setPreferredSize(new Dimension(30, 25));
        btnRefresh.addActionListener(e -> refreshGroups());
        panel.add(btnRefresh);

        btnShow = new JButton("Show Balances");
        btnShow.addActionListener(e -> showBalances());
        panel.add(btnShow);

        loadingLabel = new JLabel("Loading...");
        loadingLabel.setForeground(Color.BLUE);
        loadingLabel.setVisible(false);
        panel.add(loadingLabel);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        String[] columns = {"User Name", "Balance"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        balanceTable = new JTable(tableModel);
        balanceTable.setFont(new Font("Arial", Font.PLAIN, 14));
        balanceTable.setRowHeight(20);
        balanceTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        
        JScrollPane scrollPane = new JScrollPane(balanceTable);
        panel.add(scrollPane, BorderLayout.CENTER);
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

    private void showBalances() {
        String selectedGroup = (String) groupCombo.getSelectedItem();

        if (selectedGroup == null) {
            JOptionPane.showMessageDialog(this, "Please select a group.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int groupId = groupMap.get(selectedGroup);
        setLoading(true);
        
        SwingWorker<Map<Integer, Double>, Void> worker = new SwingWorker<>() {
            @Override
            protected Map<Integer, Double> doInBackground() {
                return balanceService.getBalances(groupId);
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    Map<Integer, Double> balances = get();
                    updateTable(balances);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ShowBalancePanel.this, 
                        "Error: " + ex.getMessage(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void updateTable(Map<Integer, Double> balances) {
        tableModel.setRowCount(0);
        
        if (balances.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No expenses found for this group.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        var users = userService.getAllUsers();
        Map<Integer, String> userNames = new HashMap<>();
        for (User u : users) {
            userNames.put(Integer.parseInt(u.getId()), u.getName());
        }

        for (Map.Entry<Integer, Double> entry : balances.entrySet()) {
            int userId = entry.getKey();
            double balance = entry.getValue();
            String name = userNames.getOrDefault(userId, "User " + userId);
            
            String balanceText;
            if (balance > 0.01) {
                balanceText = String.format("+%.2f (gets)", balance);
            } else if (balance < -0.01) {
                balanceText = String.format("%.2f (owes)", balance);
            } else {
                balanceText = "0.00 (settled)";
            }
            
            tableModel.addRow(new Object[]{name, balanceText});
        }
    }

    private void setLoading(boolean loading) {
        btnShow.setEnabled(!loading);
        groupCombo.setEnabled(!loading);
        loadingLabel.setVisible(loading);
    }
}