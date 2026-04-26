package com.expensesplitter;

import com.expensesplitter.repository.GroupRepository;
import com.expensesplitter.repository.UserRepository;
import com.expensesplitter.service.BalanceService;
import com.expensesplitter.service.ExpenseService;
import com.expensesplitter.service.GroupService;
import com.expensesplitter.service.UserService;
import com.expensesplitter.ui.CLIHandler;

public class Main {

    public static void main(String[] args) {

        UserRepository userRepository = new UserRepository();
        UserService userService = new UserService(userRepository);

        GroupRepository groupRepository = new GroupRepository();
        GroupService groupService = new GroupService(groupRepository, userService);

        BalanceService balanceService = new BalanceService();
        ExpenseService expenseService = new ExpenseService(balanceService);

        CLIHandler cli = new CLIHandler(groupService, expenseService, balanceService);

        System.out.println("Welcome to Expense Splitter!");
        cli.start();
    }
}
