package com.expensesplitter.service;

import com.expensesplitter.model.Expense;
import com.expensesplitter.model.Group;
import com.expensesplitter.model.User;
import com.expensesplitter.util.IdGenerator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ExpenseService {

    private BalanceService balanceService;

    public ExpenseService(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    /**
     * Creates a new expense, adds it to the group, and triggers balance update.
     *
     * FLOW:
     *   1. Validate inputs (amount > 0, participants ≥ 2, payer in list)
     *   2. Generate unique expense ID
     *   3. Create Expense object
     *   4. Store expense in the Group's expense list
     *   5. Delegate to BalanceService to update the balance map
     *   6. Return the expense for confirmation
     */
    public Expense addExpense(Group group, User payer, double amount, String description, List<User> participants) {
        if (group == null || payer == null) {
            throw new IllegalArgumentException("Group and payer cannot be null.");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty.");
        }

        // ── Step 1: Validate inputs ──
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
        if (participants == null || participants.size() < 2) {
            throw new IllegalArgumentException("At least 2 participants are required.");
        }
        Set<String> participantIds = new HashSet<>();
        boolean payerIncluded = false;
        for (User p : participants) {
            if (p == null) {
                throw new IllegalArgumentException("Participants cannot contain null users.");
            }
            if (!participantIds.add(p.getId())) {
                throw new IllegalArgumentException("Duplicate participants are not allowed.");
            }
            if (p.getId().equals(payer.getId())) {
                payerIncluded = true;
            }
        }
        if (!payerIncluded) {
            throw new IllegalArgumentException("Payer must be included in participants.");
        }

        // ── Step 2: Generate unique ID ──
        String expenseId = IdGenerator.generateId("EXP");

        // ── Step 3: Create Expense object ──
        Expense expense = new Expense(expenseId, description.trim(), amount, payer, participants);

        // ── Step 4: Store in group ──
        group.addExpense(expense);

        // ── Step 5: Update balances (netting happens here) ──
        balanceService.updateBalances(group, expense);

        // ── Step 6: Return for confirmation ──
        return expense;
    }

    /**
     * Records a settlement where one user pays back another.
     * Updates direct debt balance and logs it in the expense history.
     */
    public Expense addSettlement(Group group, User payingUser, User receivingUser, double amount) {
        if (group == null || payingUser == null || receivingUser == null) {
            throw new IllegalArgumentException("Group and users cannot be null.");
        }
        if (payingUser.getId().equals(receivingUser.getId())) {
            throw new IllegalArgumentException("Paying and receiving users cannot be the same.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }

        // ── 1. Update balance (payingUser effectively lent money to receivingUser) ──
        balanceService.updateDirectDebt(group, payingUser, receivingUser, amount);

        // ── 2. Add to history ──
        String expenseId = IdGenerator.generateId("EXP");
        List<User> participants = new ArrayList<>();
        participants.add(payingUser);
        participants.add(receivingUser);

        Expense expense = new Expense(expenseId, "Settlement: " + payingUser.getName() + " paid " + receivingUser.getName(), amount, payingUser, participants);
        group.addExpense(expense);

        return expense;
    }

    public Expense addDirectExpense(Group group, User lender, User borrower, double amount, String description) {
        if (group == null || lender == null || borrower == null) {
            throw new IllegalArgumentException("Group and users cannot be null.");
        }
        if (lender.getId().equals(borrower.getId())) {
            throw new IllegalArgumentException("Lender and borrower cannot be the same.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty.");
        }

        balanceService.updateDirectDebt(group, lender, borrower, amount);
        String expenseId = IdGenerator.generateId("EXP");
        List<User> participants = new ArrayList<>();
        participants.add(lender);
        participants.add(borrower);
        Expense expense = new Expense(expenseId, description.trim(), amount, lender, participants);
        group.addExpense(expense);
        return expense;
    }

    /**
     * Calculates the equal share per person.
     *
     * Example: amount=300, count=3 → returns 100.0
     */
    public double calculateEqualSplit(double amount, int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be greater than zero.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
        return amount / count;
    }

    /**
     * Returns a formatted summary of all expenses in a group.
     * Useful for reviewing spending history.
     */
    public List<String> getExpenseSummary(Group group) {
        List<String> summary = new ArrayList<>();
        int index = 1;
        for (Expense exp : group.getExpenses()) {
            int participantCount = exp.getParticipants().size();
            String splitLabel = "Custom";
            if (participantCount > 1) {
                double perPerson = calculateEqualSplit(exp.getAmount(), participantCount);
                splitLabel = "₹" + String.format("%.2f", perPerson) + " × " + participantCount + " people";
            } else {
                String settlementPrefix = "Settlement:";
                if (exp.getDescription().startsWith(settlementPrefix)) {
                    splitLabel = "Settlement";
                }
            }
            summary.add(
                index + ". " + exp.getDescription()
                + " | ₹" + String.format("%.2f", exp.getAmount())
                + " paid by " + exp.getPaidBy().getName()
                + " | Split: " + splitLabel
            );
            index++;
        }
        return summary;
    }

    public Expense addCustomExpense(Group group, User payer, String description, Map<User, Double> shareMap) {
        if (group == null || payer == null) {
            throw new IllegalArgumentException("Group and payer cannot be null.");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty.");
        }
        if (shareMap == null || shareMap.isEmpty()) {
            throw new IllegalArgumentException("At least one custom share is required.");
        }

        double total = 0.0;
        Set<String> participantIds = new HashSet<>();
        participantIds.add(payer.getId());
        List<User> participants = new ArrayList<>();
        participants.add(payer);

        for (Map.Entry<User, Double> entry : shareMap.entrySet()) {
            User user = entry.getKey();
            Double share = entry.getValue();
            if (user == null || share == null) {
                throw new IllegalArgumentException("Custom share entries cannot be null.");
            }
            if (user.getId().equals(payer.getId())) {
                throw new IllegalArgumentException("Payer cannot be in custom debtor list.");
            }
            if (share <= 0) {
                throw new IllegalArgumentException("Custom shares must be greater than zero.");
            }
            if (!participantIds.add(user.getId())) {
                throw new IllegalArgumentException("Duplicate users in custom share map are not allowed.");
            }
            participants.add(user);
            total += share;
        }

        String expenseId = IdGenerator.generateId("EXP");
        Expense expense = new Expense(expenseId, description.trim(), total, payer, participants);
        group.addExpense(expense);
        balanceService.updateCustomBalances(group, payer, shareMap);
        return expense;
    }
}
