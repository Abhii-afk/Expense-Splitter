#include <iostream>
#include <string>
#include <vector>
#include "scheduler.h"

using namespace std;

void printMenu() {
    cout << "\n=== SmartPrep Scheduler ===\n";
    cout << "1. Add Subject\n";
    cout << "2. Add Prerequisite\n";
    cout << "3. Generate Schedule\n";
    cout << "4. Update Progress\n";
    cout << "5. Show Risk Alerts\n";
    cout << "6. Exit\n";
    cout << "Enter your choice: ";
}

int main() {
    Scheduler scheduler;

    // Load Requested Test Data
    Subject dsa = {"DSA", 5, 9, 10, {}};
    Subject os = {"OS", 3, 7, 6, {}};
    Subject dbms = {"DBMS", 7, 6, 5, {}};
    scheduler.addSubject(dsa);
    scheduler.addSubject(os);
    scheduler.addSubject(dbms);
    scheduler.addPrerequisite("OS", "DSA");

    int choice;

    while (true) {
        printMenu();
        if (!(cin >> choice)) {
            cin.clear();
            cin.ignore(10000, '\n');
            continue;
        }

        if (choice == 1) {
            Subject sub;
            cout << "\nEnter Subject Name: ";
            cin >> sub.name;
            cout << "Enter Exam Deadline (Days Left): ";
            cin >> sub.daysLeft;
            cout << "Enter Difficulty (1-10): ";
            cin >> sub.difficulty;
            cout << "Enter Required Study Hours: ";
            cin >> sub.requiredHours;
            
            scheduler.addSubject(sub);
            cout << "Subject '" << sub.name << "' added successfully!\n";

        } else if (choice == 2) {
            string subName, prereqName;
            cout << "\nEnter Subject Name: ";
            cin >> subName;
            cout << "Enter Prerequisite Name: ";
            cin >> prereqName;
            
            scheduler.addPrerequisite(subName, prereqName);
            cout << "Prerequisite linked. '" << prereqName << "' must be studied before '" << subName << "'.\n";
            
        } else if (choice == 3) {
            cout << "\n=== GENERATED STUDY SCHEDULES ===\n";
            
            cout << "\n--- 1. Prerequisite Order (Topological Sort) ---\n";
            scheduler.printGraphStudyOrder();
            
            cout << "\n--- 2. Greedy Schedule (By Urgency & Difficulty) ---\n";
            vector<Subject> schedule = scheduler.generateGreedySchedule();
            if (schedule.empty()) {
                cout << "No remaining subjects need studying.\n";
            } else {
                for (const auto& sub : schedule) {
                    cout << "- " << sub.name << " (Days Left: " << sub.daysLeft 
                         << ", Required Hours: " << sub.requiredHours << ")\n";
                }
            }
            
            cout << "\n--- 3. Optimized DP Plan (Max Efficiency) ---\n";
            int maxHours;
            cout << "Enter Total Available Study Hours for DP Optimization: ";
            cin >> maxHours;
            
            vector<Subject> selected;
            int maxEfficiency = scheduler.maximizeEfficiencyDP(maxHours, selected);
            
            cout << "Maximum Efficiency Score: " << maxEfficiency << "\n";
            cout << "Subjects to cover within " << maxHours << " hours:\n";
            for (const auto& sub : selected) {
                cout << "- " << sub.name << " (Requires: " << sub.requiredHours 
                     << "h, Importance: " << sub.getImportance() << ")\n";
            }

        } else if (choice == 4) {
            string name;
            int hours;
            cout << "\nEnter Subject Name: ";
            cin >> name;
            cout << "Enter Actual Hours Studied: ";
            cin >> hours;
            scheduler.updateProgress(name, hours);
            cout << "Progress updated successfully!\n";

        } else if (choice == 5) {
            cout << "\n--- High Risk Subjects ---\n";
            vector<Subject> risky = scheduler.getHighRiskSubjects();
            if (risky.empty()) {
                cout << "No subjects are currently at high risk. Great job!\n";
            } else {
                cout << "WARNING: The following subjects require more hours than physically possible before the deadline!\n";
                for (const auto& sub : risky) {
                    cout << "- [HIGH RISK] " << sub.name 
                         << " (Needs " << sub.requiredHours << "h, but only " 
                         << sub.daysLeft << " days left)\n";
                }
            }
            
        } else if (choice == 6) {
            cout << "Exiting. Good luck with your studies!\n";
            break;
            
        } else {
            cout << "Invalid choice. Try again.\n";
        }
    }

    return 0;
}
