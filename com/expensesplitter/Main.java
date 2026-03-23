package com.expensesplitter;

import com.expensesplitter.service.BalanceService;
import com.expensesplitter.service.ExpenseService;
import com.expensesplitter.service.GroupService;
import com.expensesplitter.ui.CLIHandler;

/**
 * Entry point — wires services together and starts the CLI.
 * No business logic here. Only dependency setup.
 */
public class Main {

    public static void main(String[] args) {

        // 1. Create services
        GroupService groupService = new GroupService();
        BalanceService balanceService = new BalanceService();
        ExpenseService expenseService = new ExpenseService(balanceService);

        // 2. Create the CLI handler with all services injected
        CLIHandler cli = new CLIHandler(groupService, expenseService, balanceService);

        // 3. Start the application
        System.out.println("Welcome to Expense Splitter!");
        cli.start();
    }
}
