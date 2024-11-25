package com.example.data_encryption.utils;

import android.content.Context;
import android.security.keystore.KeyProperties;
import android.security.keystore.KeyGenParameterSpec;
import android.util.Base64;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PublicKey;

public class RSAKeyManager {

    private static final String KEY_ALIAS = "my_rsa_key";

    // Generate RSA Key Pair
    public static void generateRSAKeyPair(Context context) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
            keyPairGenerator.initialize(
                    new KeyGenParameterSpec.Builder(
                            KEY_ALIAS,
                            KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT
                    )
                            .setKeySize(2048)
                            .build()
            );
            keyPairGenerator.generateKeyPair();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Check if RSA Key Pair exists
    public static boolean doesKeyPairExist() {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            return keyStore.containsAlias(KEY_ALIAS);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get RSA Public Key in Base64 format
    public static String getRSAPublicKeyBase64() {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            KeyStore.Entry entry = keyStore.getEntry(KEY_ALIAS, null);
            if (entry instanceof KeyStore.PrivateKeyEntry) {
                PublicKey publicKey = ((KeyStore.PrivateKeyEntry) entry).getCertificate().getPublicKey();
                return Base64.encodeToString(publicKey.getEncoded(), Base64.NO_WRAP);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Return null if the key is not found or an error occurs
    }
}
