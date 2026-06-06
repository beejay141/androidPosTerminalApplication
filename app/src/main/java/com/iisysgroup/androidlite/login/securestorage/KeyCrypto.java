package com.iisysgroup.androidlite.login.securestorage;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Bamitale @Itex on 3/4/2016.
 */
public final class KeyCrypto {
    public static final int DESEDE_KEY_LENGTH = 24;
    public static final int AES_MIN_KEY_LENGTH = 16;
    Cipher cipher;
    SecretKey keySpec;
    String keyAlgorithm, cipherAlgorithm;

    public KeyCrypto(byte[] key, String cipherAlgorithm, String keyAlgorithm) throws Exception {

        this.cipherAlgorithm = cipherAlgorithm;
        this.keyAlgorithm = keyAlgorithm;
        cipher = Cipher.getInstance(cipherAlgorithm);
        keySpec = new SecretKeySpec(key, keyAlgorithm);
    }

    public static String hex(byte[] data) {
        StringBuilder sb = new StringBuilder();
        byte[] var2 = data;
        int var3 = data.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            byte b = var2[var4];
            sb.append(Character.forDigit((b & 240) >> 4, 16));
            sb.append(Character.forDigit(b & 15, 16));
        }

        return sb.toString();
    }

    public static byte[] hexStringToBytes(String s) {
        int iLength = s.length();
        int iBuff = iLength / 2;
        byte[] buff = new byte[iBuff];
        int j = 0;

        for (int i = 0; i < iLength; i += 2) {
            String s1 = s.substring(i, i + 2);
            buff[j++] = (byte) Integer.parseInt(s1, 16);
        }

        return buff;
    }

    public byte[] encryptData(String data) throws Exception {
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);

        return cipher.doFinal(data.getBytes("UTF-8"));

    }

    public byte[] decryptData(String data) throws Exception {
        cipher.init(Cipher.DECRYPT_MODE, keySpec);

        return cipher.doFinal(hexStringToBytes(data));
    }

}
