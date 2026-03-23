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
        if (groupsByName.containsKey(name.toLowerCase())) {
            return null;  // duplicate
        }
        String id = IdGenerator.generateId("GRP");
        Group group = new Group(id, name);
        groupsByName.put(name.toLowerCase(), group);
        return group;
    }

    /**
     * Adds a new member to the specified group.
     * Returns the created User.
     */
    public User addMemberToGroup(Group group, String userName) {
        String userId = IdGenerator.generateId("USR");
        User user = new User(userId, userName);
        group.addMember(user);
        return user;
    }

    /**
     * Finds a group by its name (case-insensitive).
     * Returns the Group or null if not found.
     */
    public Group getGroupByName(String name) {
        return groupsByName.get(name.toLowerCase());
    }

    /**
     * Returns all groups as a list.
     */
    public List<Group> getAllGroups() {
        return new ArrayList<>(groupsByName.values());
    }
}
