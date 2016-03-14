package com.bigbasket.mobileapp.util;

import android.util.Base64;

import com.crashlytics.android.Crashlytics;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * Created by manu on 9/3/16.
 */
public class BBUtil {
    public static String getEncryptedString(String value) {
        try {
            DESKeySpec keySpec = new DESKeySpec("bigbasket@321".getBytes("UTF8"));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey key = keyFactory.generateSecret(keySpec);

            byte[] clearText = value.getBytes("UTF8");
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            return Base64.encodeToString(cipher.doFinal(clearText), Base64.DEFAULT);
        } catch (InvalidKeyException | UnsupportedEncodingException | InvalidKeySpecException | NoSuchAlgorithmException | BadPaddingException | NoSuchPaddingException | IllegalBlockSizeException e) {
            Crashlytics.logException(e);
        }
        return value;
    }

    public static String getDecryptedString(String value) {
        try {
            DESKeySpec keySpec = new DESKeySpec("bigbasket@321".getBytes("UTF8"));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey key = keyFactory.generateSecret(keySpec);

            byte[] encrypedPwdBytes = Base64.decode(value, Base64.DEFAULT);
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decrypedValueBytes = (cipher.doFinal(encrypedPwdBytes));
            return new String(decrypedValueBytes);
        } catch (InvalidKeyException | UnsupportedEncodingException | InvalidKeySpecException | NoSuchAlgorithmException | BadPaddingException | NoSuchPaddingException | IllegalBlockSizeException e) {
            Crashlytics.logException(e);
        }
        return value;
    }
}
