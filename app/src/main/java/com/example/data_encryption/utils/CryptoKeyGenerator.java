package com.example.data_encryption.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import androidx.biometric.BiometricPrompt;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.KeyGeneratorSpi;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class CryptoKeyGenerator {

    private static final String KEY_NAME = "my_secure_key";

    public static void generateCryptoKey(){
        try{

            KeyGenerator keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
            );

            keyGenerator.init(new KeyGenParameterSpec.Builder(
                    KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setUserAuthenticationRequired(true)
                    .setInvalidatedByBiometricEnrollment(true)
                    .build());

            keyGenerator.generateKey();
        }

        catch (Exception e){
            e.printStackTrace();
        }
    }

    public static byte[] generateKeyFromBiometrics(){

        try{
            generateCryptoKey();
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME, null);
            String biometricData = key.toString();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(biometricData.getBytes());
        }

        catch (Exception e){
            return null;
        }
    }


    public static void encryptFile(Activity activity, File inputFile, byte[] key){
        try{
            SecretKey secretKey = new SecretKeySpec(key,0,key.length,"AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            FileInputStream inputStream = new FileInputStream(inputFile);
            byte[] inputBytes = new byte[(int) inputFile.length()];
            inputStream.read(inputBytes);

            byte[] outputBytes = cipher.doFinal(inputBytes);

//            File encryptedFile = saveEncryptedFile(activity, outputBytes, "encryptedfile");



            System.out.println("File successfully encrypted");

            File internalStorage = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File encyptedFile = new File(internalStorage, "encryptedFile");
            FileOutputStream outputStream = new FileOutputStream(encyptedFile);
            outputStream.write(outputBytes);
            outputStream.flush();
            outputStream.close();

            System.out.println("file downloaded");

//            downloadEncryptedFile(activity, encryptedFile);
//            System.out.println("File downloaded");



        }
        catch (Exception e){
            System.out.println("Exception occurred: " + e.toString());
        }
    }

    public static File createExternalFile(String fileName) {
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);

        File file = new File(directory, fileName);

        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    public static File saveEncryptedFile(Activity activity, byte[] encryptedData, String fileName) throws IOException {
        File file = new File(activity.getFilesDir(), fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(encryptedData);
        }
        return file;
    }

    public static void downloadEncryptedFile(Activity activity, File file) {
        Uri fileUri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".fileprovider", file);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, "application/octet-stream");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        activity.startActivity(Intent.createChooser(intent, "Download Encrypted File"));


    }
}
