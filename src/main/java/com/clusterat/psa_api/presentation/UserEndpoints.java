package com.clusterat.psa_api.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/v1/users")
class UserController {
    @GetMapping
    public String getUsers() {
        String users = "List of users";
        return users;
    }

    @GetMapping("/{id}")
    public String getUserById(@PathVariable("id") int id) {
        String user = "User details for ID: " + id;
        return user;
    }
}
