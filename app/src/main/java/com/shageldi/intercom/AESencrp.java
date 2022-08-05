package com.shageldi.intercom;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AESencrp {
   // String value;
    private static final String ALGO = "AES/ECB/NoPadding";
    private static byte[] keyValue;



//            new byte[] { 'T', 'h', 'e', 'B', 'e', 's', 't',
//                    'S', 'e', 'c', 'r','e', 't', 'K', 'e', 'y' };

    public static byte[] encrypt(byte[] Data,String value) throws Exception {
        keyValue=value.getBytes();
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = c.doFinal(Data);
        //String encryptedValue = new BASE64Encoder().encode(encVal);
        return encVal;
    }

    public static byte[] decrypt(byte[] encryptedData,String value) throws Exception {
        keyValue=value.getBytes();
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.DECRYPT_MODE, key);

        byte[] decValue = c.doFinal(encryptedData);
        return decValue;
    }

    private static Key generateKey() throws Exception {
        Key key = new SecretKeySpec(keyValue, ALGO);
        return key;
    }
}
