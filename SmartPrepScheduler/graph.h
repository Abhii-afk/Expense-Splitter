#ifndef GRAPH_H
#define GRAPH_H

#include <vector>
#include <string>
#include <unordered_map>
#include <utility>

class Graph {
private:
    std::vector<std::string> subjects;
    std::unordered_map<std::string, std::vector<std::string>> adjList;
    
    // Helper for DFS cycle detection
    bool dfsCycle(const std::string& node, std::unordered_map<std::string, int>& visited) const;

public:
    // Constructors
    Graph() = default;
    Graph(const std::vector<std::string>& subjects, const std::vector<std::pair<std::string, std::string>>& prerequisites);
    
    // Core features
    void addDirectedEdge(const std::string& u, const std::string& v);
    bool hasCycle() const;
    std::vector<std::string> topologicalSort() const; // Kahn's algorithm
    
    void printStudyOrder() const;
};

#endif
