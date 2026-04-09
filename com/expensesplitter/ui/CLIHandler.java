package com.expensesplitter.ui;

import com.expensesplitter.model.Group;
import com.expensesplitter.model.User;
import com.expensesplitter.service.BalanceService;
import com.expensesplitter.service.ExpenseService;
import com.expensesplitter.service.GroupService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * CLIHandler — the ONLY class that uses Scanner / System.out.
 * All user interaction is funneled through here.
 * Replaceable with a GUI or REST controller later.
 */
public class CLIHandler {

    private Scanner scanner;
    private GroupService groupService;
    private ExpenseService expenseService;
    private BalanceService balanceService;

    public CLIHandler(GroupService groupService, ExpenseService expenseService, BalanceService balanceService) {
        this.scanner = new Scanner(System.in);
        this.groupService = groupService;
        this.expenseService = expenseService;
        this.balanceService = balanceService;
    }

    // ══════════════════════════════════════════════════════════════
    // MAIN MENU LOOP
    // ══════════════════════════════════════════════════════════════

    public void start() {
        boolean running = true;

        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    handleCreateGroup();
                    break;
                case "2":
                    handleIndividualExpense();
                    break;
                case "3":
                    handleGoDutch();
                    break;
                case "4":
                    handleShowBalances();
                    break;
                case "5":
                    handleShowExpenseHistory();
                    break;
                case "6":
                    handleShowAllGroups();
                    break;
                case "7":
                    handleSettlePayment();
                    break;
                case "0":
                    running = false;
                    System.out.println("\n  Session closed. Thanks for using Expense Splitter.\n");
                    break;
                default:
                    System.out.println("\n  Invalid choice. Please pick a listed option.\n");
            }
        }

        scanner.close();
    }

    private void printMenu() {
        System.out.println("\n+--------------------------------------+");
        System.out.println("|           EXPENSE SPLITTER           |");
        System.out.println("+--------------------------------------+");
        System.out.println("| 1. Create Group (and Add Members)    |");
        System.out.println("| 2. Add Individual Expense (A lent B) |");
        System.out.println("| 3. Go Dutch (Custom Split)           |");
        System.out.println("| 4. Show Balances                     |");
        System.out.println("| 5. Show Expense History              |");
        System.out.println("| 6. Show All Groups                   |");
        System.out.println("| 7. Settle Payment (Clear Debt)       |");
        System.out.println("| 0. Exit                              |");
        System.out.println("+--------------------------------------+");
        System.out.print("  Your choice: ");
    }

    // ══════════════════════════════════════════════════════════════
    // 1. CREATE GROUP & ADD MEMBERS
    // ══════════════════════════════════════════════════════════════

    private void handleCreateGroup() {
        System.out.print("\n  Enter group name: ");
        String name = scanner.nextLine().trim();

        if (name.isEmpty()) {
            System.out.println("  Error: Group name cannot be empty.");
            return;
        }

        Group group = groupService.createGroup(name);
        if (group != null) {
            System.out.println("  Group created: " + group);
            System.out.println("\n  Add members to '" + name + "'");
            System.out.println("  (Press Enter on a blank line when you're done)");

            while (true) {
                System.out.print("  Enter member name: ");
                String memberName = scanner.nextLine().trim();
                if (memberName.isEmpty()) {
                    break;
                }

                User user = groupService.addMemberToGroup(group, memberName);
                if (user == null) {
                    System.out.println("  Error: '" + memberName + "' already exists in this group.");
                } else {
                    System.out.println(
                            "  Added " + user.getName() + " | Members now: " + group.getMembers().size());
                }
            }
            if (group.getMembers().size() < 2) {
                System.out.println("  Note: Add at least 2 members to start splitting expenses.");
            }
        } else {
            System.out.println("  Error: A group with the name '" + name + "' already exists.");
        }
    }

    // ══════════════════════════════════════════════════════════════
    // 4. INDIVIDUAL EXPENSE (A lent B directly)
    // ══════════════════════════════════════════════════════════════

    private void handleIndividualExpense() {
        Group group = promptForGroup();
        if (group == null)
            return;

        List<User> members = group.getMembers();
        if (members.size() < 2) {
            System.out.println("  Error: Group needs at least 2 members.");
            return;
        }

        System.out.println("\n  Members of '" + group.getName() + "':");
        printMemberList(members);

        // Who lent?
        System.out.print("  Who paid on behalf of someone else? (enter number): ");
        int lenderIndex = readInt() - 1;
        if (lenderIndex < 0 || lenderIndex >= members.size()) {
            System.out.println("  Error: Invalid selection.");
            return;
        }
        User lender = members.get(lenderIndex);

        // Who borrowed?
        System.out.print("  Who should repay this amount? (enter number): ");
        int borrowerIndex = readInt() - 1;
        if (borrowerIndex < 0 || borrowerIndex >= members.size()) {
            System.out.println("  Error: Invalid selection.");
            return;
        }
        User borrower = members.get(borrowerIndex);

        if (lender.getId().equals(borrower.getId())) {
            System.out.println("  Error: Lender and borrower cannot be the same person.");
            return;
        }

        // How much?
        System.out.print("  Enter amount: Rs.");
        double amount = readDouble();
        if (amount <= 0) {
            System.out.println("  Error: Amount must be greater than zero.");
            return;
        }

        try {
            String description = lender.getName() + " lent " + borrower.getName();
            expenseService.addDirectExpense(group, lender, borrower, amount, description);
            System.out.println("\n  Expense recorded successfully.");
            System.out.println(
                    "    " + lender.getName() + " covered Rs." + String.format("%.2f", amount) + " for " + borrower.getName());
            System.out.println("    Balance updated: " + borrower.getName() + " owes " + lender.getName());
        } catch (IllegalArgumentException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════
    // 5. GO DUTCH (Custom Split — each person's share is different)
    // ══════════════════════════════════════════════════════════════

    private void handleGoDutch() {
        Group group = promptForGroup();
        if (group == null)
            return;

        List<User> members = group.getMembers();
        if (members.size() < 2) {
            System.out.println("  Error: Group needs at least 2 members.");
            return;
        }

        System.out.println("\n  Members of '" + group.getName() + "':");
        printMemberList(members);

        // Who paid the bill?
        System.out.print("  Who is covering the bill this time? (enter number): ");
        int payerIndex = readInt() - 1;
        if (payerIndex < 0 || payerIndex >= members.size()) {
            System.out.println("  Error: Invalid selection.");
            return;
        }
        User payer = members.get(payerIndex);

        // Enter each person's share
        System.out.println("\n  Enter each member's share (enter 0 if not involved):");
        Map<User, Double> shareMap = new HashMap<>();
        double totalEntered = 0;

        for (User member : members) {
            if (member.getId().equals(payer.getId())) {
                continue; // skip payer — their share is whatever's left
            }
            System.out.print("    " + member.getName() + "'s share: Rs.");
            double share = readDouble();
            if (share < 0) {
                System.out.println("  Error: Share cannot be negative.");
                return;
            }
            if (share > 0) {
                shareMap.put(member, share);
                totalEntered += share;
            }
        }

        if (shareMap.isEmpty()) {
            System.out.println("  Error: No shares entered. Nothing to split.");
            return;
        }

        try {
            String description = "Go Dutch split by " + payer.getName();
            expenseService.addCustomExpense(group, payer, description, shareMap);
        } catch (IllegalArgumentException e) {
            System.out.println("  Error: " + e.getMessage());
            return;
        }

        System.out.println("\n  Go Dutch split recorded.");
        System.out.println("    Bill covered by : " + payer.getName());
        System.out.println("    Total assigned  : Rs." + String.format("%.2f", totalEntered));
        System.out.println("    Split details   :");
        for (Map.Entry<User, Double> entry : shareMap.entrySet()) {
            System.out.println(
                    "      " + entry.getKey().getName() + " owes Rs." + String.format("%.2f", entry.getValue()));
        }
    }

    // ══════════════════════════════════════════════════════════════
    // 6. SHOW BALANCES
    // ══════════════════════════════════════════════════════════════

    private void handleShowBalances() {
        Group group = promptForGroup();
        if (group == null)
            return;

        System.out.println();
        System.out.println(balanceService.getFormattedBalances(group));
        System.out.println(balanceService.getMemberSummary(group));
    }

    // ══════════════════════════════════════════════════════════════
    // 7. SHOW EXPENSE HISTORY
    // ══════════════════════════════════════════════════════════════

    private void handleShowExpenseHistory() {
        Group group = promptForGroup();
        if (group == null)
            return;

        List<String> history = expenseService.getExpenseSummary(group);

        if (history.isEmpty()) {
            System.out.println("\n  No expenses recorded in '" + group.getName() + "' yet.");
        } else {
        System.out.println("\n  Expense history for '" + group.getName() + "'");
            System.out.println("  ----------------------------------------");
            for (String line : history) {
                System.out.println("    " + line);
            }
            System.out.println("  ----------------------------------------");
            System.out.println("  Entries logged: " + group.getExpenses().size());
        }
    }

    // ══════════════════════════════════════════════════════════════
    // 8. SHOW ALL GROUPS
    // ══════════════════════════════════════════════════════════════

    private void handleShowAllGroups() {
        List<Group> groups = groupService.getAllGroups();

        if (groups.isEmpty()) {
            System.out.println("  No groups created yet. Use option 1 to create your first group.");
        } else {
            System.out.println("\n  All Groups:");
            System.out.println("  ------------------------------------------------------");
            System.out.println("    Name                 Members    Expenses");
            System.out.println("  ------------------------------------------------------");
            for (Group g : groups) {
                System.out.println(String.format("    %-20s %7d %11d",
                        g.getName(), g.getMembers().size(), g.getExpenses().size()));
            }
            System.out.println("  ------------------------------------------------------");
        }
    }

    // ══════════════════════════════════════════════════════════════
    // 7. SETTLE PAYMENT (CLEAR DEBT)
    // ══════════════════════════════════════════════════════════════

    private void handleSettlePayment() {
        Group group = promptForGroup();
        if (group == null)
            return;

        List<User> members = group.getMembers();
        if (members.size() < 2) {
            System.out.println("  Error: Group needs at least 2 members.");
            return;
        }

        System.out.println("\n  Members of '" + group.getName() + "':");
        printMemberList(members);

        System.out.print("  Who is making the payment? (enter number): ");
        int payerIndex = readInt() - 1;
        if (payerIndex < 0 || payerIndex >= members.size()) {
            System.out.println("  Error: Invalid selection.");
            return;
        }
        User payingUser = members.get(payerIndex);

        System.out.print("  Who is receiving it? (enter number): ");
        int receiverIndex = readInt() - 1;
        if (receiverIndex < 0 || receiverIndex >= members.size()) {
            System.out.println("  Error: Invalid selection.");
            return;
        }
        User receivingUser = members.get(receiverIndex);

        if (payingUser.getId().equals(receivingUser.getId())) {
            System.out.println("  Error: Cannot settle payment with oneself.");
            return;
        }

        System.out.print("  Enter amount paid to settle: Rs.");
        double amount = readDouble();
        if (amount <= 0) {
            System.out.println("  Error: Amount must be greater than zero.");
            return;
        }

        try {
            expenseService.addSettlement(group, payingUser, receivingUser, amount);

            System.out.println("\n  Settlement recorded successfully.");
            System.out.println("    " + payingUser.getName() + " paid Rs." + String.format("%.2f", amount) + " to "
                    + receivingUser.getName());
        } catch (IllegalArgumentException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════════════

    private Group promptForGroup() {
        List<Group> groups = groupService.getAllGroups();
        if (groups.isEmpty()) {
            System.out.println("  Error: No groups found. Create one first (option 1).");
            return null;
        }

        System.out.println("  Available groups:");
        for (Group g : groups) {
            System.out.println("    - " + g.getName());
        }

        System.out.print("  Enter group name: ");
        String name = scanner.nextLine().trim();
        Group group = groupService.getGroupByName(name);

        if (group == null) {
            System.out.println("  Error: Group '" + name + "' not found.");
        }
        return group;
    }

    private void printMemberList(List<User> members) {
        for (int i = 0; i < members.size(); i++) {
            System.out.println("    " + (i + 1) + ". " + members.get(i).getName());
        }
    }

    private int readInt() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("  Error: Please enter a valid number.");
            return -1;
        }
    }

    private double readDouble() {
        try {
            return Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
        System.out.println("  Error: Please enter a valid number (example: 120 or 120.50).");
        return -1;
    }
}
}
