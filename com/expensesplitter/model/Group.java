package com.expensesplitter.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Group {

    private String groupId;
    private String groupName;
    private List<User> members;
    private List<Expense> expenses;
    private Map<String, Double> balanceMap;  // "debtorId→creditorId" → amount

    public Group(String groupId, String groupName) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.members = new ArrayList<>();
        this.expenses = new ArrayList<>();
        this.balanceMap = new HashMap<>();
    }

    // --- Getters ---

    public String getId() {
        return groupId;
    }

    public String getName() {
        return groupName;
    }

    public List<User> getMembers() {
        return members;
    }

    public List<Expense> getExpenses() {
        return expenses;
    }

    public Map<String, Double> getBalanceMap() {
        return balanceMap;
    }

    // --- Mutators ---

    public void addMember(User user) {
        members.add(user);
    }

    public void addExpense(Expense expense) {
        expenses.add(expense);
    }

    // --- Display ---

    @Override
    public String toString() {
        return groupName + " (" + members.size() + " members)";
    }
}
