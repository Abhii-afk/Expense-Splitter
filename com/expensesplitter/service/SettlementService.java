package com.expensesplitter.service;

import com.expensesplitter.model.User;
import com.expensesplitter.repository.SettlementRepository;

public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final UserService userService;

    public SettlementService(SettlementRepository settlementRepository, UserService userService) {
        this.settlementRepository = settlementRepository;
        this.userService = userService;
    }

    public void settlePayment(String payerName, String receiverName, double amount, int groupId) {
        if (payerName == null || payerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Payer name cannot be empty.");
        }
        if (receiverName == null || receiverName.trim().isEmpty()) {
            throw new IllegalArgumentException("Receiver name cannot be empty.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive.");
        }

        User payer = userService.getUserByName(payerName.trim());
        User receiver = userService.getUserByName(receiverName.trim());

        if (payer == null) {
            throw new IllegalArgumentException("Payer not found: " + payerName);
        }
        if (receiver == null) {
            throw new IllegalArgumentException("Receiver not found: " + receiverName);
        }

        settlementRepository.recordSettlement(
            groupId,
            Integer.parseInt(payer.getId()),
            Integer.parseInt(receiver.getId()),
            amount
        );
    }
}