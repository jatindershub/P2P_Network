package com.example.p2pnetwork.helper;

import android.os.Build;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public class EncryptionHelper {
    private static PublicKey publicKey;
    private static PrivateKey privateKey;
    private static SecretKey aesKey;

    public static void initializeKeys() throws Exception {
        if (publicKey == null || privateKey == null) {
            // Generate RSA key pair
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair keyPair = keyGen.generateKeyPair();
            publicKey = keyPair.getPublic();
            privateKey = keyPair.getPrivate();
        }

        if (aesKey == null) {
            // Generate AES key
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            aesKey = keyGen.generateKey();
        }
    }

    public static String encryptMessage(String message) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        byte[] encryptedBytes = cipher.doFinal(message.getBytes());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Base64.getEncoder().encodeToString(encryptedBytes);
        }
        return null;
    }

    public static String createHash(String message) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(message.getBytes());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Base64.getEncoder().encodeToString(hashBytes);
        }
        return null;
    }

    public static String decryptMessage(String encryptedMessage) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, aesKey);
        byte[] decryptedBytes = new byte[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedMessage));
        }
        return new String(decryptedBytes);
    }

    public static boolean verifyHash(String message, String receivedHash) throws Exception {
        String computedHash = createHash(message);
        return computedHash.equals(receivedHash);
    }
}

