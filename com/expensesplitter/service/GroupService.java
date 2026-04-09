package com.expensesplitter.service;

import com.expensesplitter.model.Group;
import com.expensesplitter.model.User;
import com.expensesplitter.util.IdGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupService {

    private Map<String, Group> groupsByName;  // O(1) lookup by name

    public GroupService() {
        this.groupsByName = new HashMap<>();
    }

    /**
     * Creates a new group with the given name.
     * Returns the created Group, or null if a group with that name already exists.
     */
    public Group createGroup(String name) {
        String normalized = normalizeName(name);
        if (normalized == null) {
            throw new IllegalArgumentException("Group name cannot be empty.");
        }
        if (groupsByName.containsKey(normalized)) {
            return null;  // duplicate
        }
        String id = IdGenerator.generateId("GRP");
        Group group = new Group(id, name.trim());
        groupsByName.put(normalized, group);
        return group;
    }

    /**
     * Adds a new member to the specified group.
     * Returns the created User.
     */
    public User addMemberToGroup(Group group, String userName) {
        if (group == null) {
            throw new IllegalArgumentException("Group cannot be null.");
        }
        if (userName == null || userName.trim().isEmpty()) {
            throw new IllegalArgumentException("User name cannot be empty.");
        }
        for (User existing : group.getMembers()) {
            if (existing.getName().equalsIgnoreCase(userName.trim())) {
                return null;
            }
        }
        String userId = IdGenerator.generateId("USR");
        User user = new User(userId, userName.trim());
        group.addMember(user);
        return user;
    }

    /**
     * Finds a group by its name (case-insensitive).
     * Returns the Group or null if not found.
     */
    public Group getGroupByName(String name) {
        String normalized = normalizeName(name);
        if (normalized == null) {
            return null;
        }
        return groupsByName.get(normalized);
    }

    /**
     * Returns all groups as a list.
     */
    public List<Group> getAllGroups() {
        return new ArrayList<>(groupsByName.values());
    }

    private String normalizeName(String name) {
        if (name == null) {
            return null;
        }
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toLowerCase();
    }
}
