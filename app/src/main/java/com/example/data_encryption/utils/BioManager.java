package com.example.data_encryption.utils;

import android.app.Activity;
import android.os.Build;
import android.widget.Toast;

import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.io.IOException;
import java.security.KeyStore;
import java.util.concurrent.Executor;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

public class BioManager {


    public static void authenticateUser(Activity activity, AuthHandler authHandler) {
        Executor executor = ContextCompat.getMainExecutor(activity);

        BiometricPrompt biometricPrompt = new BiometricPrompt((FragmentActivity) activity, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(activity, "Authentication failed: "+ errString, Toast.LENGTH_SHORT).show();
                try {
                    authHandler.onAuthFailure();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(activity, "Authentication succeeded", Toast.LENGTH_SHORT).show();
                try {
                    authHandler.onAuthSuccess();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(activity, "Authentication failed", Toast.LENGTH_SHORT).show();
                try {
                    authHandler.onAuthFailure();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Sensor")
                .setSubtitle("Apply your biometrics")
                .setNegativeButtonText("Use account password")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }



    public static boolean isBiometricAvailable(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            BiometricManager biometricManager = BiometricManager.from(activity);
            int canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK);

            switch (canAuthenticate) {
                case BiometricManager.BIOMETRIC_SUCCESS:
                    return true;
                case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                default:
                    return false;
            }
        } else {
            return false;
        }
    }
}

