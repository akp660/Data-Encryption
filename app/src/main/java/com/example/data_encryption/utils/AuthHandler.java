package com.example.data_encryption.utils;

import java.io.IOException;

public interface AuthHandler {
    void onAuthSuccess() throws IOException;
    void onAuthFailure() throws IOException;
}
