package com.expensesplitter.model;

public class User {

    private String userId;
    private String name;

    public User(String userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    // --- Getters ---

    public String getId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    // --- Display ---

    @Override
    public String toString() {
        return name + " (" + userId + ")";
    }
}
