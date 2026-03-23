package com.expensesplitter.model;

import java.util.ArrayList;
import java.util.List;

public class Expense {

    private String expenseId;
    private String description;
    private double amount;
    private User paidBy;
    private List<User> participants;

    public Expense(String expenseId, String description, double amount, User paidBy, List<User> participants) {
        this.expenseId = expenseId;
        this.description = description;
        this.amount = amount;
        this.paidBy = paidBy;
        this.participants = new ArrayList<>(participants);
    }

    // --- Getters ---

    public String getId() {
        return expenseId;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public User getPaidBy() {
        return paidBy;
    }

    public List<User> getParticipants() {
        return participants;
    }

    // --- Display ---

    @Override
    public String toString() {
        return description + " | ₹" + amount + " paid by " + paidBy.getName();
    }
}
