package com.example.data_encryption.ApiDirectory.model;

public class UserModel {
    private String name;
    private String email;
    private String password;
    private String rsa_public_key;

    public UserModel(String name, String email, String rsa_public_key, String password) {
        this.name = name;
        this.email = email;
        this.rsa_public_key = rsa_public_key;
        this.password = password;
    }

    // Getters and setters
    public UserModel() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRsa_public_key() {
        return rsa_public_key;
    }

    public void setRsa_public_key(String rsa_public_key) {
        this.rsa_public_key = rsa_public_key;
    }
}
