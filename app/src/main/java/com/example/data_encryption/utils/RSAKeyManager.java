package com.example.data_encryption.utils;

import android.content.Context;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;

public class RSAKeyManager {

    private static final String KEY_ALIAS = "my_rsa_key";
    private static KeyPair keyPair;
    private static KeyStore keyStore;

    // Initialize KeyStore
    private static void initKeyStore() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("RSAKeyManager", "Error initializing KeyStore: " + e.getMessage());
        }
    }

    // Generate RSA Key Pair
    public static void generateRSAKeyPair(Context context) {
        try {
            initKeyStore();
            // Check if key already exists
            if (keyStore.containsAlias(KEY_ALIAS)) {
                // Remove existing key
                keyStore.deleteEntry(KEY_ALIAS);
                Log.d("RSAKeyManager", "Existing key deleted.");
            }
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA);
            keyPairGenerator.initialize(2048);  // 2048-bit key size for better security
            keyPair = keyPairGenerator.generateKeyPair();
            Log.d("RSAKeyManager", "RSA Key Pair generated successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("RSAKeyManager", "Error generating RSA Key Pair: " + e.getMessage());
        }
    }

    // Check if RSA Key Pair exists
    public static boolean doesKeyPairExist() {
        boolean exists = keyPair != null;
        Log.d("RSAKeyManager", "RSA Key Pair exists: " + exists);
        return exists;
    }

    // Get RSA Public Key in Base64 format
    public static String getRSAPublicKeyBase64() {
        if (keyPair != null) {
            PublicKey publicKey = keyPair.getPublic();
            String publicKeyBase64 = Base64.encodeToString(publicKey.getEncoded(), Base64.NO_WRAP);
            Log.d("RSAKeyManager", "RSA Public Key (Base64): " + publicKeyBase64);
            return publicKeyBase64;
        }
        Log.e("RSAKeyManager", "RSA Key Pair not found.");
        return null;
    }

    // Get RSA Private Key
    public static PrivateKey getRSAPrivateKey() {
        if (keyPair != null) {
            PrivateKey privateKey = keyPair.getPrivate();
            Log.d("RSAKeyManager", "RSA Private Key retrieved successfully.");
            return privateKey;
        }
        Log.e("RSAKeyManager", "RSA Key Pair not found.");
        return null;
    }

    // Delete RSA Key Pair
    public static void deleteRSAKeyPair() {
        try {
            initKeyStore();
            if (keyStore.containsAlias(KEY_ALIAS)) {
                keyStore.deleteEntry(KEY_ALIAS);
                keyPair = null;
                Log.d("RSAKeyManager", "RSA Key Pair deleted.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("RSAKeyManager", "Error deleting RSA Key Pair: " + e.getMessage());
        }
    }
}
