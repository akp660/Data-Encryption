package com.example.data_encryption.ApiDirectory.model;

public class UserModel {
    private String name;
    private String email;
    private String encryption_token;
    private String password;

    public UserModel(String name, String email, String encryption_token, String password) {
        this.name = name;
        this.email = email;
        this.encryption_token = encryption_token;
        this.password = password;
    }

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

    public String getEncryption_token() {
        return encryption_token;
    }

    public void setEncryption_token(String encryption_token) {
        this.encryption_token = encryption_token;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
