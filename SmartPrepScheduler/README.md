# SmartPrep: Study Schedule Optimizer

## Introduction
SmartPrep is a command-line based study scheduler designed to help students and professionals manage their study routines effectively. By leveraging Data Structures and Algorithms, SmartPrep generates an optimal study plan tailored to your needs. It utilizes Graph Algorithms (Topological Sort), Greedy Strategy, and Dynamic Programming (Knapsack) to handle subject dependencies, prioritize tasks efficiently, and optimally allocate your valuable study hours.

## Features
- **Dependency Handling:** Uses Graph algorithms (Topological Sort) to ensure prerequisite subjects are scheduled before advanced ones.
- **Greedy Prioritization:** Ranks and prioritizes study topics based on their deadlines and difficulty levels.
- **Time Optimization:** Employs Dynamic Programming (Knapsack problem variant) for the optimal allocation of available study hours across various subjects to maximize learning.
- **Adaptive Rescheduling:** Dynamically adjusts the study schedule based on user progress and completed tasks.
- **Risk Detection:** Identifies and flags risks for incomplete preparation, ensuring you stay on top of critical deadlines.

## Tech Stack
- **Language:** C++
- **Core Concepts:** Graphs, Greedy Algorithms, Dynamic Programming

## How It Works
1. **Input Collection:** The user inputs the subjects, their difficulty levels, estimated hours required, deadlines, and any prerequisite dependencies.
2. **Dependency Resolution:** The system builds a directed graph of the subjects and performs a Topological Sort to determine a valid study sequence.
3. **Prioritization:** Available subjects (with met prerequisites) are prioritized using a Greedy approach weighting deadlines and difficulty.
4. **Time Allocation:** The engine uses Dynamic Programming to allocate the user's available daily/weekly study hours optimally among the prioritized subjects to maximize productivity.
5. **Schedule Generation:** The final study plan is presented to the user. As progress is reported, the engine adapts and reschedules remaining tasks.

## Project Structure
```text
SmartPrep/
├── main.cpp         # Entry point and user interface logic
├── graph.h          # Graph structure definitions for dependency management
├── graph.cpp        # Graph implementation (Topological sort, etc.)
├── scheduler.h      # Scheduler definitions and algorithms (Greedy, DP)
└── scheduler.cpp    # Optimization logic and scheduling implementation
```

## How to Run

### Compilation
To compile the project, navigate to the project directory in your terminal and run:
```bash
g++ main.cpp graph.cpp scheduler.cpp -o smartprep
```

### Execution
After successful compilation, run the executable:

**On Linux/macOS:**
```bash
./smartprep
```

**On Windows:**
```cmd
smartprep.exe
```

## Sample Input and Output

### Example Walkthrough
**Input:**
```
Total study hours available: 8
Subjects:
1. Math (Difficulty: 8, Hours: 4, Deadline: 3 days, Prerequisite: None)
2. Physics (Difficulty: 7, Hours: 5, Deadline: 4 days, Prerequisite: Math)
3. Chemistry (Difficulty: 6, Hours: 3, Deadline: 2 days, Prerequisite: None)
```

**Output:**
```
--- SmartPrep Optimal Study Schedule ---
1. Math (4 hours)
2. Chemistry (3 hours)
3. Physics (5 hours) - Note: Prerequisite Math completed.

Risk Detected: None. You have enough time to complete the preparation.
```
*(Note: Actual CLI output formatting may vary based on specific algorithm weights and input data.)*

## Future Improvements
- [ ] Graphical User Interface (GUI) or Web integration.
- [ ] Persistent storage to save and load user profiles/schedules from a file or database.
- [ ] Integration with external calendar applications (Google Calendar, Outlook).
- [ ] Implement varied difficulty weighting based on individual student learning curve.

## Contributors
- **[Contributor 1 Name]** - *Lead Developer*
- **[Contributor 2 Name]** - *Algorithms & Logic*
- **[Contributor 3 Name]** - *Documentation & Testing*
