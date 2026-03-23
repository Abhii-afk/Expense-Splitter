package com.expensesplitter.service;

import com.expensesplitter.model.Expense;
import com.expensesplitter.model.Group;
import com.expensesplitter.model.User;
import com.expensesplitter.util.IdGenerator;

import java.util.ArrayList;
import java.util.List;

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

        // ── Step 1: Validate inputs ──
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
        if (participants == null || participants.size() < 2) {
            throw new IllegalArgumentException("At least 2 participants are required.");
        }
        boolean payerIncluded = false;
        for (User p : participants) {
            if (p.getId().equals(payer.getId())) {
                payerIncluded = true;
                break;
            }
        }
        if (!payerIncluded) {
            throw new IllegalArgumentException("Payer must be included in participants.");
        }

        // ── Step 2: Generate unique ID ──
        String expenseId = IdGenerator.generateId("EXP");

        // ── Step 3: Create Expense object ──
        Expense expense = new Expense(expenseId, description, amount, payer, participants);

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
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }

        // ── 1. Update balance (payingUser effectively lent money to receivingUser) ──
        balanceService.updateDirectDebt(group, payingUser, receivingUser, amount);

        // ── 2. Add to history ──
        String expenseId = IdGenerator.generateId("EXP");
        List<User> participants = new ArrayList<>();
        participants.add(receivingUser);

        Expense expense = new Expense(expenseId, "Settlement: " + payingUser.getName() + " paid " + receivingUser.getName(), amount, payingUser, participants);
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
            double perPerson = calculateEqualSplit(exp.getAmount(), exp.getParticipants().size());
            summary.add(
                index + ". " + exp.getDescription()
                + " | ₹" + String.format("%.2f", exp.getAmount())
                + " paid by " + exp.getPaidBy().getName()
                + " | Split: ₹" + String.format("%.2f", perPerson)
                + " × " + exp.getParticipants().size() + " people"
            );
            index++;
        }
        return summary;
    }
}
