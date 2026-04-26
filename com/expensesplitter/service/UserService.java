package com.expensesplitter.service;

import com.expensesplitter.model.User;
import com.expensesplitter.repository.UserRepository;

import java.util.List;

public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("User name cannot be empty.");
        }
        User existing = getUserByName(name.trim());
        if (existing != null) {
            return existing;
        }
        userRepository.createUser(name.trim());
        return getUserByName(name.trim());
    }

    public List<User> getAllUsers() {
        return userRepository.getAllUsers();
    }

    public User getUserByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        List<User> users = userRepository.getAllUsers();
        for (User user : users) {
            if (user.getName().equalsIgnoreCase(name.trim())) {
                return user;
            }
        }
        return null;
    }
}