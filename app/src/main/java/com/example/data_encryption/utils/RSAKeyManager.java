package com.example.data_encryption.utils;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;

public class RSAKeyManager {
    private static final String KEY_ALIAS = "my_rsa_key";
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";

    // Initialize KeyStore
    private static KeyStore getKeyStore() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);
        return keyStore;
    }

    // Generate RSA Key Pair
    public static void generateRSAKeyPair(Context context,String email) {
        try {
            KeyStore keyStore = getKeyStore();
            String NEW_ALIAS = KEY_ALIAS+email;
            if (keyStore.containsAlias(NEW_ALIAS)) {
                keyStore.deleteEntry(NEW_ALIAS);
                Log.d("RSAKeyManager", "Existing key deleted.");
            }

            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEYSTORE);

            KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                    NEW_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                    .setKeySize(2048)
                    .build();

            keyPairGenerator.initialize(spec);
            keyPairGenerator.generateKeyPair();

            Log.d("RSAKeyManager", "RSA Key Pair generated and stored in AndroidKeyStore.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("RSAKeyManager", "Error generating RSA Key Pair: " + e.getMessage());
        }
    }

    // Check if RSA Key Pair exists
    public static boolean doesKeyPairExist(String email) {
        try {
            KeyStore keyStore = getKeyStore();
            String NEW_ALIAS = KEY_ALIAS+email;
            boolean exists = keyStore.containsAlias(NEW_ALIAS);
            Log.d("RSAKeyManager", "RSA Key Pair exists: " + exists);
            return exists;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("RSAKeyManager", "Error checking key existence: " + e.getMessage());
            return false;
        }
    }

    // Get RSA Public Key in Base64 format
    public static String getRSAPublicKeyBase64(String email) {
        try {
            KeyStore keyStore = getKeyStore();
            String NEW_ALIAS = KEY_ALIAS+email;
            if (!keyStore.containsAlias(NEW_ALIAS)) {
                Log.e("RSAKeyManager", "RSA Key Pair not found in KeyStore.");
                return null;
            }

            Certificate cert = keyStore.getCertificate(NEW_ALIAS);
            PublicKey publicKey = cert.getPublicKey();
            String publicKeyBase64 = Base64.encodeToString(publicKey.getEncoded(), Base64.NO_WRAP);
            Log.d("RSAKeyManager", "RSA Public Key retrieved from KeyStore.");
            return publicKeyBase64;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("RSAKeyManager", "Error getting RSA Public Key: " + e.getMessage());
            return null;
        }
    }

    // Get RSA Private Key
    public static PrivateKey getRSAPrivateKey(String email) {
        try {
            KeyStore keyStore = getKeyStore();
            String NEW_ALIAS = KEY_ALIAS+email;
            if (!keyStore.containsAlias(NEW_ALIAS)) {
                Log.e("RSAKeyManager", "RSA Key Pair not found in KeyStore.");
                return null;
            }

            PrivateKey privateKey = (PrivateKey) keyStore.getKey(NEW_ALIAS, null);
            Log.d("RSAKeyManager", "RSA Private Key retrieved from KeyStore.");
            return privateKey;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("RSAKeyManager", "Error getting RSA Private Key: " + e.getMessage());
            return null;
        }
    }

    // Delete RSA Key Pair
    public static void deleteRSAKeyPair(String email) {
        try {
            KeyStore keyStore = getKeyStore();
            String NEW_ALIAS = KEY_ALIAS+email;

            if (keyStore.containsAlias(NEW_ALIAS)) {
                keyStore.deleteEntry(NEW_ALIAS);
                Log.d("RSAKeyManager", "RSA Key Pair deleted from KeyStore.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("RSAKeyManager", "Error deleting RSA Key Pair: " + e.getMessage());
        }
    }
}