package com.expensesplitter.ui;

import com.expensesplitter.repository.ExpenseRepository;
import com.expensesplitter.repository.GroupRepository;
import com.expensesplitter.repository.SettlementRepository;
import com.expensesplitter.repository.UserRepository;
import com.expensesplitter.service.BalanceService;
import com.expensesplitter.service.ExpenseService;
import com.expensesplitter.service.GroupService;
import com.expensesplitter.service.SettlementService;
import com.expensesplitter.service.UserService;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private JPanel centerPanel;
    private CardLayout cardLayout;

    public MainFrame() {
        setTitle("Expense Splitter");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        UserRepository userRepository = new UserRepository();
        GroupRepository groupRepository = new GroupRepository();
        ExpenseRepository expenseRepository = new ExpenseRepository();
        SettlementRepository settlementRepository = new SettlementRepository();

        UserService userService = new UserService(userRepository);
        GroupService groupService = new GroupService(groupRepository, userService);
        ExpenseService expenseService = new ExpenseService(expenseRepository, userService);
        BalanceService balanceService = new BalanceService(expenseRepository, settlementRepository);
        SettlementService settlementService = new SettlementService(settlementRepository, userService);

        add(createTopPanel(), BorderLayout.NORTH);
        add(createCenterPanel(userService, groupService, expenseService, balanceService, settlementService), BorderLayout.CENTER);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton btnCreateGroup = new JButton("Create Group");
        JButton btnAddMember = new JButton("Add Member");
        JButton btnAddExpense = new JButton("Add Expense");
        JButton btnShowBalances = new JButton("Show Balances");
        JButton btnSettlePayment = new JButton("Settle Payment");

        btnCreateGroup.addActionListener(e -> showPanel("Create Group"));
        btnAddMember.addActionListener(e -> showPanel("Add Member"));
        btnAddExpense.addActionListener(e -> showPanel("Add Expense"));
        btnShowBalances.addActionListener(e -> showPanel("Show Balances"));
        btnSettlePayment.addActionListener(e -> showPanel("Settle Payment"));

        panel.add(btnCreateGroup);
        panel.add(btnAddMember);
        panel.add(btnAddExpense);
        panel.add(btnShowBalances);
        panel.add(btnSettlePayment);

        return panel;
    }

    private JPanel createCenterPanel(UserService userService, GroupService groupService, ExpenseService expenseService,
                                      BalanceService balanceService, SettlementService settlementService) {
        cardLayout = new CardLayout();
        centerPanel = new JPanel(cardLayout);

        centerPanel.add(new CreateGroupPanel(groupService), "Create Group");
        centerPanel.add(new AddMemberPanel(groupService, userService), "Add Member");
        centerPanel.add(new AddExpensePanel(expenseService, groupService, userService), "Add Expense");
        centerPanel.add(new ShowBalancePanel(balanceService, groupService, userService), "Show Balances");
        centerPanel.add(new SettlePaymentPanel(settlementService, groupService, userService), "Settle Payment");

        return centerPanel;
    }

    private void showPanel(String name) {
        cardLayout.show(centerPanel, name);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}