package com.ipintelligence.dto;

public class ProfileResponse {
    private Integer id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String role;

    public ProfileResponse(Integer id, String username, String email, String firstName, String lastName, String role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }

    public Integer getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getRole() { return role; }
}
