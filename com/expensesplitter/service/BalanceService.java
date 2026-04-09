package com.expensesplitter.service;

import com.expensesplitter.model.Expense;
import com.expensesplitter.model.Group;
import com.expensesplitter.model.User;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BalanceService {
    private static final double EPSILON = 0.01;

    // ═══════════════════════════════════════════════════════════════
    //  CORE: Update balances after an expense (netting algorithm)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Updates the group's balanceMap after a new expense is added.
     *
     * ALGORITHM (per non-payer participant P):
     *
     *   splitShare = totalAmount / numberOfParticipants
     *
     *   key        = "P→payer"    (P owes payer)
     *   reverseKey = "payer→P"    (payer owes P — from a PREVIOUS expense)
     *
     *   CASE 1: reverseKey exists and reverseValue ≥ splitShare
     *           → Reduce reverse entry:  reverseValue -= splitShare
     *           → If result ≈ 0, remove the entry entirely
     *           → Example: payer owed P ₹100, now P's share is ₹60
     *                      → update "payer→P" = 40  (just reduce the old debt)
     *
     *   CASE 2: reverseKey exists but reverseValue < splitShare
     *           → Remove reverse entry entirely
     *           → Put remainder into forward key: "P→payer" += (splitShare - reverseValue)
     *           → Example: payer owed P ₹30, now P's share is ₹100
     *                      → remove "payer→P", set "P→payer" = 70
     *
     *   CASE 3: No reverseKey exists
     *           → Simply add: "P→payer" += splitShare
     *           → Example: fresh debt, P now owes payer ₹100
     */
    public void updateBalances(Group group, Expense expense) {
        if (group == null || expense == null) {
            throw new IllegalArgumentException("Group and expense cannot be null.");
        }
        if (expense.getParticipants() == null || expense.getParticipants().isEmpty()) {
            throw new IllegalArgumentException("Expense participants cannot be empty.");
        }
        if (expense.getAmount() <= 0) {
            throw new IllegalArgumentException("Expense amount must be greater than zero.");
        }

        // Step 1: Calculate equal share
        double splitShare = expense.getAmount() / expense.getParticipants().size();

        // Step 2: Get the group's balance map (HashMap<String, Double>)
        Map<String, Double> map = group.getBalanceMap();
        User payer = expense.getPaidBy();

        // Step 3: Process each participant (skip the payer themselves)
        for (User participant : expense.getParticipants()) {

            // Payer doesn't owe themselves
            if (participant.getId().equals(payer.getId())) {
                continue;
            }

            // Build directed-pair keys
            String key = makeKey(participant.getId(), payer.getId());         // "P→payer"
            String reverseKey = makeKey(payer.getId(), participant.getId());  // "payer→P"

            // ── NETTING LOGIC ──
            if (map.containsKey(reverseKey) && map.get(reverseKey) > 0) {

                double existing = map.get(reverseKey);

                if (existing >= splitShare) {
                    // CASE 1: Reverse debt is larger — just reduce it
                    double newVal = existing - splitShare;
                    if (newVal < EPSILON) {
                        map.remove(reverseKey);    // fully cancelled out
                    } else {
                        map.put(reverseKey, newVal);
                    }
                } else {
                    // CASE 2: Reverse debt is smaller — remove it, carry remainder forward
                    map.remove(reverseKey);
                    double remainder = splitShare - existing;
                    map.put(key, map.getOrDefault(key, 0.0) + remainder);
                }

            } else {
                // CASE 3: No prior reverse debt — simply add new debt
                map.put(key, map.getOrDefault(key, 0.0) + splitShare);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  INDIVIDUAL: Direct lending between two people
    // ═══════════════════════════════════════════════════════════════

    /**
     * A lent B a specific amount. B now owes A.
     * Uses the same netting logic to offset any existing reverse debt.
     *
     * Example: A lent B ₹500 → "B→A" += 500 (with netting)
     */
    public void updateDirectDebt(Group group, User creditor, User debtor, double amount) {
        if (group == null || creditor == null || debtor == null) {
            throw new IllegalArgumentException("Group and users cannot be null.");
        }
        if (creditor.getId().equals(debtor.getId())) {
            throw new IllegalArgumentException("Creditor and debtor cannot be the same user.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
        Map<String, Double> map = group.getBalanceMap();

        String key = makeKey(debtor.getId(), creditor.getId());         // "debtor→creditor"
        String reverseKey = makeKey(creditor.getId(), debtor.getId());  // "creditor→debtor"

        // Same netting logic as equal split
        if (map.containsKey(reverseKey) && map.get(reverseKey) > 0) {
            double existing = map.get(reverseKey);
            if (existing >= amount) {
                double newVal = existing - amount;
                if (newVal < EPSILON) {
                    map.remove(reverseKey);
                } else {
                    map.put(reverseKey, newVal);
                }
            } else {
                map.remove(reverseKey);
                double remainder = amount - existing;
                map.put(key, map.getOrDefault(key, 0.0) + remainder);
            }
        } else {
            map.put(key, map.getOrDefault(key, 0.0) + amount);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  GO DUTCH: Custom split — each person's share is different
    // ═══════════════════════════════════════════════════════════════

    /**
     * One person paid, but each participant owes a different amount.
     * shareMap: User → how much that user owes (their individual share).
     *
     * Example: A paid ₹500. B ate ₹200, C ate ₹150, A ate ₹150.
     *   → shareMap = {B: 200, C: 150}  (payer's own share not included)
     */
    public void updateCustomBalances(Group group, User payer, Map<User, Double> shareMap) {
        if (group == null || payer == null) {
            throw new IllegalArgumentException("Group and payer cannot be null.");
        }
        if (shareMap == null || shareMap.isEmpty()) {
            throw new IllegalArgumentException("Custom share map cannot be empty.");
        }
        Map<String, Double> map = group.getBalanceMap();

        for (Map.Entry<User, Double> entry : shareMap.entrySet()) {
            User debtor = entry.getKey();
            double share = entry.getValue();
            if (debtor == null) {
                throw new IllegalArgumentException("Share map contains a null user.");
            }
            if (share < 0) {
                throw new IllegalArgumentException("Share cannot be negative.");
            }
            if (share < EPSILON) {
                continue;
            }

            if (debtor.getId().equals(payer.getId())) {
                continue;  // skip payer's own share
            }

            String key = makeKey(debtor.getId(), payer.getId());
            String reverseKey = makeKey(payer.getId(), debtor.getId());

            // Same netting logic
            if (map.containsKey(reverseKey) && map.get(reverseKey) > 0) {
                double existing = map.get(reverseKey);
                if (existing >= share) {
                    double newVal = existing - share;
                    if (newVal < EPSILON) {
                        map.remove(reverseKey);
                    } else {
                        map.put(reverseKey, newVal);
                    }
                } else {
                    map.remove(reverseKey);
                    double remainder = share - existing;
                    map.put(key, map.getOrDefault(key, 0.0) + remainder);
                }
            } else {
                map.put(key, map.getOrDefault(key, 0.0) + share);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  QUERY: Get balances (who owes whom)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Returns all non-zero balance entries as a list of strings.
     * Each string: "DebtorName owes CreditorName ₹amount"
     */
    public List<String> getBalances(Group group) {
        List<String> result = new ArrayList<>();
        Map<String, Double> map = group.getBalanceMap();

        for (Map.Entry<String, Double> entry : map.entrySet()) {
            if (entry.getValue() > EPSILON) {
                String[] ids = parseKey(entry.getKey());
                String debtorName = findUserName(group, ids[0]);
                String creditorName = findUserName(group, ids[1]);
                result.add(debtorName + " owes " + creditorName + " ₹" + String.format("%.2f", entry.getValue()));
            }
        }
        result.sort(String::compareTo);
        return result;
    }

    /**
     * Returns human-readable settlement instructions.
     * Currently same as getBalances (equal-split produces minimal settlements).
     * Extensible for smarter simplification algorithms later.
     */
    public List<String> getSettlements(Group group) {
        return getBalances(group);
    }

    // ═══════════════════════════════════════════════════════════════
    //  DISPLAY: Formatted balance output
    // ═══════════════════════════════════════════════════════════════

    /**
     * Returns a formatted, multi-line string showing all balances.
     * Ready to be printed by CLIHandler.
     *
     * Example output:
     *   ┌─── Balances for Trip ───┐
     *   │ B owes A ₹50.00         │
     *   │ C owes A ₹55.00         │
     *   │ C owes B ₹50.00         │
     *   └─────────────────────────┘
     */
    public String getFormattedBalances(Group group) {
        List<String> balances = getBalances(group);
        StringBuilder sb = new StringBuilder();

        if (balances.isEmpty()) {
            sb.append("All settled up. No outstanding balances.");
            return sb.toString();
        }

        String title = "Balances for " + group.getName();
        int contentWidth = title.length();
        for (String line : balances) {
            if (line.length() > contentWidth) {
                contentWidth = line.length();
            }
        }
        String border = repeat('-', contentWidth + 4);
        sb.append("+").append(border).append("+\n");
        sb.append(String.format("|  %-" + contentWidth + "s  |%n", title));
        sb.append("+").append(border).append("+\n");
        for (String line : balances) {
            sb.append(String.format("|  %-" + contentWidth + "s  |%n", line));
        }
        sb.append("+").append(border).append("+");
        return sb.toString();
    }

    /**
     * Calculates and returns the net balance summary for each member.
     * Positive means the user gets back money, negative means the user owes money.
     * 
     * Example:
     *   A: +Rs.105.00
     *   B: Rs.0.00
     *   C: -Rs.105.00
     */
    public String getMemberSummary(Group group) {
        Map<String, Double> netBalances = new HashMap<>();
        
        // Initialize all members to 0
        for (User user : group.getMembers()) {
            netBalances.put(user.getName(), 0.0);
        }
        
        // Calculate net: keys are "debtor→creditor" = amount 
        // -> debtor loses money, creditor gains money
        Map<String, Double> map = group.getBalanceMap();
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            String[] ids = parseKey(entry.getKey());
            String debtorName = findUserName(group, ids[0]);
            String creditorName = findUserName(group, ids[1]);
            double amount = entry.getValue();
            
            netBalances.put(debtorName, netBalances.getOrDefault(debtorName, 0.0) - amount);
            netBalances.put(creditorName, netBalances.getOrDefault(creditorName, 0.0) + amount);
        }
        
        Map<String, Double> sortedNetBalances = new LinkedHashMap<>();
        netBalances.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey, String.CASE_INSENSITIVE_ORDER))
                .forEach(entry -> sortedNetBalances.put(entry.getKey(), entry.getValue()));

        StringBuilder sb = new StringBuilder();
        sb.append("\n  Member net summary\n");
        sb.append("  ------------------\n");
        for (Map.Entry<String, Double> entry : sortedNetBalances.entrySet()) {
            double net = entry.getValue();
            if (net > EPSILON) {
                sb.append("    ").append(entry.getKey()).append(": +Rs.").append(String.format("%.2f", net)).append(" (to receive)\n");
            } else if (net < -EPSILON) {
                sb.append("    ").append(entry.getKey()).append(": -Rs.").append(String.format("%.2f", Math.abs(net))).append(" (to pay)\n");
            } else {
                sb.append("    ").append(entry.getKey()).append(": Rs.0.00 (settled)\n");
            }
        }
        return sb.toString();
    }

    // ═══════════════════════════════════════════════════════════════
    //  DEBUG: Raw balance map dump
    // ═══════════════════════════════════════════════════════════════

    /**
     * Returns the raw HashMap contents for debugging.
     * Shows internal keys (IDs) and values without name resolution.
     *
     * Example output:
     *   [DEBUG] Balance Map for Trip:
     *     USR-2→USR-1 = 50.00
     *     USR-3→USR-1 = 55.00
     *     USR-3→USR-2 = 50.00
     */
    public String getRawBalanceMap(Group group) {
        Map<String, Double> map = group.getBalanceMap();
        StringBuilder sb = new StringBuilder();

        sb.append("[DEBUG] Balance Map for ").append(group.getName()).append(":\n");

        if (map.isEmpty()) {
            sb.append("  (empty)");
            return sb.toString();
        }

        for (Map.Entry<String, Double> entry : map.entrySet()) {
            sb.append("  ").append(entry.getKey())
              .append(" = ").append(String.format("%.2f", entry.getValue()))
              .append("\n");
        }
        return sb.toString();
    }

    // ═══════════════════════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Builds the directed-pair key: "idA→idB"
     * Meaning: idA owes idB
     */
    private String makeKey(String idA, String idB) {
        return idA + "→" + idB;
    }

    /**
     * Parses the directed-pair key back into [debtorId, creditorId].
     */
    private String[] parseKey(String key) {
        return key.split("→");
    }

    /**
     * Finds a user's display name by ID within the group.
     * Falls back to raw ID if user not found (defensive).
     */
    private String findUserName(Group group, String userId) {
        for (User member : group.getMembers()) {
            if (member.getId().equals(userId)) {
                return member.getName();
            }
        }
        return userId;
    }

    private String repeat(char ch, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(ch);
        }
        return sb.toString();
    }
}
