package com.expensesplitter.service;

import com.expensesplitter.model.User;
import com.expensesplitter.repository.ExpenseRepository;
import com.expensesplitter.repository.UserRepository;

import java.util.Map;

public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserService userService;

    public ExpenseService(ExpenseRepository expenseRepository, UserService userService) {
        this.expenseRepository = expenseRepository;
        this.userService = userService;
    }

    public void addExpense(int groupId, String paidByUserName, double totalAmount, String description, Map<String, Double> splits) {
        if (paidByUserName == null || paidByUserName.trim().isEmpty()) {
            throw new IllegalArgumentException("Payer name cannot be empty.");
        }
        if (splits == null || splits.isEmpty()) {
            throw new IllegalArgumentException("Splits cannot be empty.");
        }

        User payer = userService.getUserByName(paidByUserName.trim());
        if (payer == null) {
            throw new IllegalArgumentException("Payer not found: " + paidByUserName);
        }

        int expenseId = expenseRepository.createExpense(groupId, Integer.parseInt(payer.getId()), totalAmount, description);
        if (expenseId == -1) {
            throw new RuntimeException("Failed to create expense");
        }

        for (Map.Entry<String, Double> entry : splits.entrySet()) {
            User user = userService.getUserByName(entry.getKey());
            if (user == null) {
                continue;
            }
            expenseRepository.addExpenseSplit(expenseId, Integer.parseInt(user.getId()), entry.getValue());
        }
    }
}