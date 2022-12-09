package com.flink.streaming.core.udf;

import org.apache.flink.table.functions.ScalarFunction;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class MemberCryptoUDF extends ScalarFunction {

    private static final String DEFAULT_CHARSET = "UTF-8";
    private static final String AES_SECURE_RANDOM_KEY = "SHA1PRNG";
    private static final String AES_SEED = "7343680Ls435kj3468Ms312111014681";
    private static final String AES_ALGORITHM_KEY = "AES";
    private static final String AES_GCM_CIPHER_KEY = "AES_256/GCM/NoPadding";
    private static final int AES_GCM_KEY_SIZE = 256;
    private static final int AES_GCM_IV_LENGTH = 12;
    private static final int AES_GCM_TAG_LENGTH = 16;

    private static final String USER_SECRET_KEY = "26a711f75b90055fe169fca73a0e0f03";

    public static String eval(String s) {
        return aesGCM256Decrypt(s, DEFAULT_CHARSET, USER_SECRET_KEY);
    }

    public static void main(String[] args) {
        System.out.println(aesGCM256Decrypt("T9iTaO/nr5LyHp2UAjeUrhJtrw==", DEFAULT_CHARSET, USER_SECRET_KEY));
    }

    private static String aesGCM256Decrypt(String data, String charsetName, String userSecretKey) {

        try {
            if (null == data || data.length() == 0) {
                return data;
            }

            byte[] cipherText = Base64.getDecoder().decode(data.getBytes(DEFAULT_CHARSET));

            SecureRandom secureRandom = SecureRandom.getInstance(AES_SECURE_RANDOM_KEY);
            secureRandom.setSeed(AES_SEED.getBytes());

            KeyGenerator keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM_KEY);
            keyGenerator.init(AES_GCM_KEY_SIZE, secureRandom);
            // Generate Key
            byte[] key = keyGenerator.generateKey().getEncoded();
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, AES_ALGORITHM_KEY);

            byte[] iv = new byte[AES_GCM_IV_LENGTH]; //NEVER REUSE THIS IV WITH SAME KEY
            secureRandom.nextBytes(iv);
            // Generate parameterSpec
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(AES_GCM_TAG_LENGTH * 8, iv);

            final Cipher cipher = Cipher.getInstance(AES_GCM_CIPHER_KEY);

            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, gcmParameterSpec);

            if (null != userSecretKey && userSecretKey.length() > 0) {
                cipher.updateAAD(userSecretKey.getBytes());
            }

            byte[] plainText = cipher.doFinal(cipherText);

            return new String(plainText, charsetName);
        } catch (Exception e) {
            return data;
        }
    }

}
