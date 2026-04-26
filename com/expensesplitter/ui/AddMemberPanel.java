package com.expensesplitter.ui;

import com.expensesplitter.model.Group;
import com.expensesplitter.model.User;
import com.expensesplitter.service.GroupService;
import com.expensesplitter.service.UserService;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class AddMemberPanel extends JPanel {

    private GroupService groupService;
    private UserService userService;
    private JComboBox<String> groupCombo;
    private JTextField nameField;
    private Map<String, Integer> groupMap;
    private JButton btnAdd;

    public AddMemberPanel(GroupService groupService, UserService userService) {
        this.groupService = groupService;
        this.userService = userService;
        setLayout(new BorderLayout(10, 10));
        add(createFormPanel(), BorderLayout.CENTER);
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel title = new JLabel("Add Member to Group");
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
        panel.add(groupCombo, gbc);

        JButton btnRefresh = new JButton("↻");
        btnRefresh.setPreferredSize(new Dimension(30, 25));
        btnRefresh.addActionListener(e -> refreshGroups());
        panel.add(btnRefresh, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        panel.add(new JLabel("New Member Name:"), gbc);

        gbc.gridx = 1;
        nameField = new JTextField(20);
        panel.add(nameField, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        btnAdd = new JButton("Add Member");
        btnAdd.addActionListener(e -> addMember());
        panel.add(btnAdd, gbc);

        return panel;
    }

    private void refreshGroups() {
        groupCombo.removeAllItems();
        groupMap = new java.util.HashMap<>();
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

    private void addMember() {
        String selectedGroup = (String) groupCombo.getSelectedItem();
        String name = nameField.getText().trim();

        if (selectedGroup == null || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int groupId = groupMap.get(selectedGroup);

        btnAdd.setEnabled(false);
        try {
            Group group = new Group(String.valueOf(groupId), "");
            User user = groupService.addMemberToGroup(group, name);
            if (user != null) {
                JOptionPane.showMessageDialog(this, "Member '" + user.getName() + "' added to group!", "Success", JOptionPane.INFORMATION_MESSAGE);
                nameField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Member already exists in group.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            btnAdd.setEnabled(true);
        }
    }
}