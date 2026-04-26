package com.expensesplitter.service;

import com.expensesplitter.model.Group;
import com.expensesplitter.model.User;
import com.expensesplitter.repository.GroupRepository;

import java.util.List;

public class GroupService {

    private final GroupRepository groupRepository;
    private final UserService userService;

    public GroupService(GroupRepository groupRepository, UserService userService) {
        this.groupRepository = groupRepository;
        this.userService = userService;
    }

    public Group createGroup(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Group name cannot be empty.");
        }
        Group existing = groupRepository.getGroupByName(name.trim());
        if (existing != null) {
            return null;
        }
        groupRepository.createGroup(name.trim());
        return groupRepository.getGroupByName(name.trim());
    }

    public User addMemberToGroup(Group group, String userName) {
        if (group == null) {
            throw new IllegalArgumentException("Group cannot be null.");
        }
        if (userName == null || userName.trim().isEmpty()) {
            throw new IllegalArgumentException("User name cannot be empty.");
        }
        User user = userService.createUser(userName.trim());
        groupRepository.addMemberToGroup(group.getId(), user.getId());
        return user;
    }

    public Group getGroupByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        return groupRepository.getGroupByName(name.trim());
    }

    public List<Group> getAllGroups() {
        return groupRepository.getAllGroups();
    }

    public List<String> getMemberIds(String groupId) {
        return groupRepository.getMemberIds(groupId);
    }
}
