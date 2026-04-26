package com.expensesplitter.ui;

import com.expensesplitter.model.Group;
import com.expensesplitter.service.GroupService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class CreateGroupPanel extends JPanel {

    private GroupService groupService;
    private JTextField nameField;
    private JButton btnCreate;
    private JLabel loadingLabel;

    public CreateGroupPanel(GroupService groupService) {
        this.groupService = groupService;
        setLayout(new BorderLayout(10, 10));
        add(createFormPanel(), BorderLayout.CENTER);
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel title = new JLabel("Create New Group");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        panel.add(new JLabel("Group Name:"), gbc);

        gbc.gridx = 1;
        nameField = new JTextField(20);
        panel.add(nameField, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        btnCreate = new JButton("Create");
        btnCreate.addActionListener(e -> createGroup());
        panel.add(btnCreate, gbc);

        gbc.gridy = 3;
        loadingLabel = new JLabel("");
        loadingLabel.setForeground(Color.BLUE);
        loadingLabel.setVisible(false);
        panel.add(loadingLabel, gbc);

        return panel;
    }

    private void createGroup() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a group name.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        setLoading(true);
        
        SwingWorker<Group, Void> worker = new SwingWorker<>() {
            @Override
            protected Group doInBackground() {
                return groupService.createGroup(name);
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    Group group = get();
                    if (group != null) {
                        JOptionPane.showMessageDialog(CreateGroupPanel.this, 
                            "Group '" + group.getName() + "' created successfully!", 
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                        nameField.setText("");
                    } else {
                        JOptionPane.showMessageDialog(CreateGroupPanel.this, 
                            "Group already exists.", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(CreateGroupPanel.this, 
                        "Error: " + ex.getMessage(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void setLoading(boolean loading) {
        btnCreate.setEnabled(!loading);
        nameField.setEnabled(!loading);
        loadingLabel.setText(loading ? "Creating..." : "");
        loadingLabel.setVisible(loading);
    }
}