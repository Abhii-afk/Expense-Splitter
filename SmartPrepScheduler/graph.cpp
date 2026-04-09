#include "graph.h"
#include <iostream>
#include <queue>

Graph::Graph(const std::vector<std::string>& subjects, const std::vector<std::pair<std::string, std::string>>& prerequisites) 
    : subjects(subjects) {
    for (const auto& sub : subjects) {
        adjList[sub] = {};
    }
    for (const auto& pre : prerequisites) {
        addDirectedEdge(pre.first, pre.second);
    }
}

void Graph::addDirectedEdge(const std::string& u, const std::string& v) {
    adjList[u].push_back(v);
}

bool Graph::dfsCycle(const std::string& node, std::unordered_map<std::string, int>& visited) const {
    visited[node] = 1; // 1 = visiting

    if (adjList.find(node) != adjList.end()) {
        for (const std::string& neighbor : adjList.at(node)) {
            if (visited[neighbor] == 1) {
                return true; 
            }
            if (visited[neighbor] == 0) {
                if (dfsCycle(neighbor, visited)) {
                    return true;
                }
            }
        }
    }
    
    visited[node] = 2; // 2 = visited completely
    return false;
}

bool Graph::hasCycle() const {
    std::unordered_map<std::string, int> visited;
    for (const auto& sub : subjects) {
        visited[sub] = 0; // 0 = unvisited
    }

    for (const auto& sub : subjects) {
        if (visited[sub] == 0) {
            if (dfsCycle(sub, visited)) {
                return true;
            }
        }
    }
    return false;
}

std::vector<std::string> Graph::topologicalSort() const {
    std::vector<std::string> order;
    std::unordered_map<std::string, int> inDegree;
    for (const auto& sub : subjects) {
        inDegree[sub] = 0;
    }
    
    for (const auto& pair : adjList) {
        for (const std::string& neighbor : pair.second) {
            inDegree[neighbor]++;
        }
    }
    
    // Kahn's Algorithm
    std::queue<std::string> q;
    for (const auto& sub : subjects) {
        if (inDegree.find(sub) != inDegree.end() && inDegree[sub] == 0) {
            q.push(sub);
        }
    }
    
    while (!q.empty()) {
        std::string current = q.front();
        q.pop();
        order.push_back(current);
        
        if (adjList.find(current) != adjList.end()) {
            for (const std::string& neighbor : adjList.at(current)) {
                inDegree[neighbor]--;
                if (inDegree[neighbor] == 0) {
                    q.push(neighbor);
                }
            }
        }
    }
    
    return order;
}

void Graph::printStudyOrder() const {
    if (hasCycle()) {
        std::cout << "Invalid dependencies\n";
    } else {
        std::vector<std::string> order = topologicalSort();
        std::cout << "Valid study order:\n";
        for (const std::string& sub : order) {
            std::cout << "- " << sub << "\n";
        }
    }
}
