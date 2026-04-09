#include "scheduler.h"
#include <algorithm>
#include <iostream>

void Scheduler::addSubject(const Subject& subject) {
    subjects[subject.name] = subject;
}

void Scheduler::addPrerequisite(const std::string& subjectName, const std::string& prereqName) {
    if (subjects.find(subjectName) != subjects.end()) {
        subjects[subjectName].prerequisites.push_back(prereqName);
    } else {
        std::cerr << "Error: Subject '" << subjectName << "' not found.\n";
    }
}

void Scheduler::updateProgress(const std::string& subjectName, int actualHoursStudied) {
    if (subjects.find(subjectName) != subjects.end()) {
        subjects[subjectName].requiredHours -= actualHoursStudied;
        if (subjects[subjectName].requiredHours < 0) {
            subjects[subjectName].requiredHours = 0;
        }
    }
}

std::vector<Subject> Scheduler::getHighRiskSubjects() const {
    std::vector<Subject> risky;
    for (const auto& pair : subjects) {
        // Assume maximum study capacity is physically 24 hours per day. 
        int availableTime = pair.second.daysLeft * 24; 
        if (pair.second.requiredHours > availableTime && pair.second.requiredHours > 0) {
            risky.push_back(pair.second);
        }
    }
    return risky;
}

Graph Scheduler::buildGraph() const {
    std::vector<std::string> subjectNames;
    std::vector<std::pair<std::string, std::string>> prereqs;

    for (const auto& pair : subjects) {
        if (std::find(subjectNames.begin(), subjectNames.end(), pair.first) == subjectNames.end()) {
            subjectNames.push_back(pair.first);
        }
        for (const std::string& p : pair.second.prerequisites) {
            prereqs.push_back({p, pair.first});
            
            if (std::find(subjectNames.begin(), subjectNames.end(), p) == subjectNames.end()) {
                subjectNames.push_back(p);
            }
        }
    }
    return Graph(subjectNames, prereqs);
}

std::vector<Subject> Scheduler::getPrerequisiteOrder() const {
    std::vector<Subject> orderedSubjects;
    Graph g = buildGraph();
    
    if (g.hasCycle()) {
        std::cerr << "Invalid dependencies\n";
        return orderedSubjects; // Empty indicating invalid
    }

    std::vector<std::string> order = g.topologicalSort();
    for (const std::string& name : order) {
        if (subjects.find(name) != subjects.end()) {
            orderedSubjects.push_back(subjects.at(name));
        }
    }
    return orderedSubjects;
}

void Scheduler::printGraphStudyOrder() const {
    Graph g = buildGraph();
    g.printStudyOrder();
}

std::vector<Subject> Scheduler::generateGreedySchedule() const {
    std::vector<Subject> schedule;
    for (const auto& pair : subjects) {
        if (pair.second.requiredHours > 0) {
            schedule.push_back(pair.second);
        }
    }
    
    std::sort(schedule.begin(), schedule.end(), [](const Subject& a, const Subject& b) {
        if (a.daysLeft == b.daysLeft) {
            return a.difficulty > b.difficulty;
        }
        return a.daysLeft < b.daysLeft;
    });
    
    return schedule;
}

int Scheduler::maximizeEfficiencyDP(int availableHours, std::vector<Subject>& selectedSubjects) const {
    std::vector<Subject> allSubjects;
    for (const auto& pair : subjects) {
        if (pair.second.requiredHours > 0) {
            allSubjects.push_back(pair.second);
        }
    }
    
    int n = allSubjects.size();
    if (n == 0 || availableHours <= 0) return 0;

    std::vector<std::vector<int>> dp(n + 1, std::vector<int>(availableHours + 1, 0));

    for (int i = 1; i <= n; ++i) {
        for (int w = 1; w <= availableHours; ++w) {
            if (allSubjects[i - 1].requiredHours <= w) {
                dp[i][w] = std::max(dp[i - 1][w], 
                    dp[i - 1][w - allSubjects[i - 1].requiredHours] + allSubjects[i - 1].getImportance());
            } else {
                dp[i][w] = dp[i - 1][w];
            }
        }
    }

    int w = availableHours;
    for (int i = n; i > 0 && dp[i][w] > 0; --i) {
        if (dp[i][w] != dp[i - 1][w]) {
            selectedSubjects.push_back(allSubjects[i - 1]);
            w -= allSubjects[i - 1].requiredHours;
        }
    }

    std::reverse(selectedSubjects.begin(), selectedSubjects.end());
    return dp[n][availableHours];
}
