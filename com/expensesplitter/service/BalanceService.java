package com.expensesplitter.service;

import com.expensesplitter.repository.ExpenseRepository;
import com.expensesplitter.repository.SettlementRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BalanceService {

    private final ExpenseRepository expenseRepository;
    private final SettlementRepository settlementRepository;
    private final ExecutorService executor;

    public BalanceService(ExpenseRepository expenseRepository, SettlementRepository settlementRepository) {
        this.expenseRepository = expenseRepository;
        this.settlementRepository = settlementRepository;
        this.executor = Executors.newFixedThreadPool(4);
    }

    public Map<Integer, Double> getBalances(int groupId) {
        Map<Integer, Double> balanceMap = new ConcurrentHashMap<>();
        List<Callable<Void>> tasks = new ArrayList<>();

        String expenseSql = "SELECT id, paid_by, total_amount FROM expenses WHERE group_id = ?";
        List<ExpenseData> expenses = new ArrayList<>();
        
        try (Connection conn = com.expensesplitter.util.DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(expenseSql)) {
            pstmt.setInt(1, groupId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                expenses.add(new ExpenseData(
                    rs.getInt("id"),
                    rs.getInt("paid_by"),
                    rs.getDouble("total_amount")
                ));
            }
        } catch (Exception e) {
            System.err.println("Error fetching expenses: " + e.getMessage());
        }

        for (ExpenseData exp : expenses) {
            final int expenseId = exp.id;
            final int paidBy = exp.paidBy;
            final double amount = exp.amount;
            
            Callable<Void> task = () -> {
                balanceMap.merge(paidBy, amount, Double::sum);
                
                String splitsSql = "SELECT user_id, amount_owed FROM expense_splits WHERE expense_id = ?";
                try (Connection conn = com.expensesplitter.util.DBConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(splitsSql)) {
                    pstmt.setInt(1, expenseId);
                    ResultSet rs = pstmt.executeQuery();
                    while (rs.next()) {
                        int userId = rs.getInt("user_id");
                        double owed = rs.getDouble("amount_owed");
                        balanceMap.merge(userId, -owed, Double::sum);
                    }
                }
                return null;
            };
            tasks.add(task);
        }

        Callable<Void> settlementTask = () -> {
            var settlements = settlementRepository.getSettlementsByGroup(groupId);
            for (SettlementRepository.Settlement s : settlements) {
                balanceMap.merge(s.payerId, -s.amount, Double::sum);
                balanceMap.merge(s.receiverId, s.amount, Double::sum);
            }
            return null;
        };
        tasks.add(settlementTask);

        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Error in parallel processing: " + e.getMessage());
        }

        return new HashMap<>(balanceMap);
    }

    private static class ExpenseData {
        final int id;
        final int paidBy;
        final double amount;

        ExpenseData(int id, int paidBy, double amount) {
            this.id = id;
            this.paidBy = paidBy;
            this.amount = amount;
        }
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public List<Transaction> simplifyDebts(Map<Integer, Double> balances) {
        List<Transaction> transactions = new ArrayList<>();

        List<Integer> creditors = new ArrayList<>();
        List<Integer> debtors = new ArrayList<>();

        for (var entry : balances.entrySet()) {
            if (entry.getValue() > 0.01) {
                creditors.add(entry.getKey());
            } else if (entry.getValue() < -0.01) {
                debtors.add(entry.getKey());
            }
        }

        Map<Integer, Double> remaining = new HashMap<>(balances);

        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            int creditor = creditors.get(0);
            int debtor = debtors.get(0);

            double credit = remaining.get(creditor);
            double debt = -remaining.get(debtor);

            double amount = Math.min(credit, debt);

            remaining.put(creditor, credit - amount);
            remaining.put(debtor, remaining.get(debtor) + amount);

            transactions.add(new Transaction(debtor, creditor, amount));

            if (Math.abs(remaining.get(creditor)) < 0.01) {
                creditors.remove(0);
            }
            if (Math.abs(remaining.get(debtor)) < 0.01) {
                debtors.remove(0);
            }
        }

        return transactions;
    }

    public static class Transaction {
        public final int fromUser;
        public final int toUser;
        public final double amount;

        public Transaction(int fromUser, int toUser, double amount) {
            this.fromUser = fromUser;
            this.toUser = toUser;
            this.amount = amount;
        }
    }
}