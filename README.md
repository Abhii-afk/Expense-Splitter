# Expense Splitter

Expense Splitter is a Java-based Command Line Interface (CLI) application that helps you track shared expenses, split bills, and settle debts easily with your friends or groups. It eliminates the hassle of manually calculating who owes whom!

## Features

- **Create Groups:** Organize your friends and expenses by creating different groups (e.g., 'Trip to Goa', 'Apartment Rent').
- **Add Members:** Add multiple users to each group to start splitting bills.
- **Individual Expenses (Direct Lending):** Record straightforward debts when one person lends money to another directly.
- **Go Dutch (Custom Splits):** Divide complex bills where a single payer covers the total, but individual shares vary.
- **Show Balances:** View summarized balances to know exactly who owes whom. Includes a consolidated member summary.
- **Expense History:** Review the log of all expenses recorded within a particular group to keep track of spending.
- **Settle Payments:** Clear debts simply by recording when someone pays back their owed amount.
- **Multiple Groups Management:** Easily switch contexts by viewing and logging expenses across multiple different groups.

## Technologies Used

- **Java**: Core programming language.
- **Object-Oriented Programming (OOP)**: Clear separation of models, services, and UI layers.
- **In-Memory Data Structures**: Data is stored during the execution using Java Collections (Lists, Maps) for fast calculation.

## Project Structure

```text
com.expensesplitter
├── Main.java                 # Entry point, initializes services and starts the app
├── model/                    # Data models (Expense, Group, User)
├── service/                  # Business logic and calculations
│   ├── BalanceService.java   # Core logic for tracking and calculating net balances
│   ├── ExpenseService.java   # Manages adding expenses and processing settlements
│   └── GroupService.java     # Manages group lifecycle and member additions
├── ui/                       # User Interface layer
│   └── CLIHandler.java       # Handles interactive console menus and user inputs
└── util/                     # Utilities
    └── IdGenerator.java      # Generates unique IDs for users, groups, and expenses
```

## How to Run

1. Ensure that the **Java Development Kit (JDK 8 or above)** is installed.
2. Clone or download this project to your local machine.
3. Navigate to the root folder of this project in your terminal or command prompt.
4. Compile the source code:
   ```bash
   javac com/expensesplitter/*.java com/expensesplitter/*/*.java
   ```
5. Run the main class:
   ```bash
   java com.expensesplitter.Main
   ```
*(Alternatively, you can open the folder in an IDE like IntelliJ IDEA, VS Code, or Eclipse and simply run the `Main` class.)*

## Usage Guide Example

1. Start the application. The menu will list all available options.
2. Select **`1. Create Group (and Add Members)`**. Name it "Weekend Trip" and add members (e.g., "Alice" and "Bob").
3. Select **`2. Add Individual Expense`** if Alice lent Rs. 500 to Bob. 
4. Select **`3. Go Dutch`** for a restaurant bill paid by Bob, but shared unequally among group members.
5. Select **`4. Show Balances`** to see the net amount Bob owes Alice (or vice versa).
6. When the trip ends, use **`7. Settle Payment (Clear Debt)`** to mark debts as paid.

---

*This application was built progressively, emphasizing robust validations, clean object-oriented design, and real-world applicability for shared expenses.*