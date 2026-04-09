#ifndef SCHEDULER_H
#define SCHEDULER_H

#include "graph.h"
#include <unordered_map>
#include <vector>
#include <string>

struct Subject {
    std::string name;
    int daysLeft;
    int difficulty;
    int requiredHours;
    std::vector<std::string> prerequisites;

    int getImportance() const {
        int urgency = (daysLeft <= 0) ? 100 : (100 / daysLeft);
        return difficulty + urgency;
    }
};

class Scheduler {
private:
    std::unordered_map<std::string, Subject> subjects;

    // Dynamically builds graph based on all added subjects
    Graph buildGraph() const;

public:
    void addSubject(const Subject& subject);
    void addPrerequisite(const std::string& subjectName, const std::string& prereqName);
    void updateProgress(const std::string& subjectName, int actualHoursStudied);
    
    std::vector<Subject> getHighRiskSubjects() const;
    
    std::vector<Subject> getPrerequisiteOrder() const;
    void printGraphStudyOrder() const;

    std::vector<Subject> generateGreedySchedule() const;
    int maximizeEfficiencyDP(int availableHours, std::vector<Subject>& selectedSubjects) const;
};

#endif
