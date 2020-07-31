package com.wdl.tools.crypt;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES加密类
 *
 * @author wdl
 */
public final class AESUtil {

    public final static String ERROR_CONTENT_LENGTH_ACCORD = "10001";
    public static final String ENCRYPT_ECB_PKCS5PADDING = "AES/ECB/PKCS5Padding";
    public static final String ENCRYPT_ECB_NOPADDING = "AES/ECB/NoPadding";
    public static final String ENCRYPT_CBC_PKCS5PADDING = "AES/CBC/PKCS5Padding";
    public static final String ENCRYPT_CBC_NOPADDING = "AES/CBC/NoPadding";


    // 二行制转字符串
    private static String byte2hex(byte[] b) {
        StringBuilder hs = new StringBuilder();
        String stmp;
        for (int n = 0; b != null && n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0XFF);
            if (stmp.length() == 1) {
                hs.append('0');
            }
            hs.append(stmp);
        }
        return hs.toString().toLowerCase();
    }

    /**
     * ECB 加密方法
     *
     * @param strContent     String 加密内容
     * @param strKey         String 加密密钥
     * @param strPaddingMode String 填充模式：
     *                       ENCRYPT_ECB_PKCS5PADDING;
     *                       ENCRYPT_ECB_NOPADDING;
     */
    public static String EcbEncode(String strContent, String strKey, String strPaddingMode)
            throws NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException,
            IllegalBlockSizeException, UnsupportedEncodingException, InvalidKeyException {
        if (strPaddingMode.equals(ENCRYPT_ECB_NOPADDING)) {
            if (strContent.getBytes().length % 16 != 0) {
                return ERROR_CONTENT_LENGTH_ACCORD;
            }
        }
        byte[] byteContent = strContent.getBytes("UTF-8");
        byte[] enCodeFormat = strKey.getBytes();
        SecretKeySpec secretKeySpec = new SecretKeySpec(enCodeFormat, "AES");
        Cipher cipher = Cipher.getInstance(strPaddingMode);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        byte[] encryptedBytes = cipher.doFinal(byteContent);
        return Base64Util.base64Encode2String(encryptedBytes);
    }


    /**
     * ECB 解密方法
     *
     * @param strContent     String 解密内容
     * @param strKey         String 解密密钥
     * @param strPaddingMode String 填充模式
     *                       ENCRYPT_ECB_PKCS5PADDING;
     *                       ENCRYPT_ECB_NOPADDING;
     */
    public static String EcbDecode(String strContent, String strKey, String strPaddingMode)
            throws NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException,
            IllegalBlockSizeException, UnsupportedEncodingException, InvalidKeyException {
        byte[] byteContent = Base64Util.base64Decode(strContent);
        byte[] enCodeFormat = strKey.getBytes();
        SecretKeySpec secretKeySpec = new SecretKeySpec(enCodeFormat, "AES");
        Cipher cipher = Cipher.getInstance(strPaddingMode);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        byte[] result = cipher.doFinal(byteContent);
        return new String(result, "UTF-8");
    }


    /**
     * CBC 加密方法
     *
     * @param strContent         String 加密内容
     * @param strKey             String 加密密钥
     * @param strPaddingMode     String 填充模式：
     *                           ENCRYPT_CBC_PKCS5PADDING;
     *                           ENCRYPT_CBC_NOPADDING;
     * @param strIvParameterSpec String 初始向量;
     */
    public static String CbcEncode(String strContent, String strKey, String strPaddingMode, String strIvParameterSpec)
            throws NoSuchPaddingException, BadPaddingException,
            InvalidAlgorithmParameterException, NoSuchAlgorithmException,
            IllegalBlockSizeException, UnsupportedEncodingException, InvalidKeyException {
        if (strPaddingMode.equals(ENCRYPT_ECB_NOPADDING)) {
            if (strContent.getBytes().length % 16 != 0) {
                return ERROR_CONTENT_LENGTH_ACCORD;
            }
        }
        byte[] byteContent = strContent.getBytes("UTF-8");
        byte[] enCodeFormat = strKey.getBytes();
        SecretKeySpec secretKeySpec = new SecretKeySpec(enCodeFormat, "AES");
        byte[] initParam = strIvParameterSpec.getBytes();
        IvParameterSpec ivParameterSpec = new IvParameterSpec(initParam);
        Cipher cipher = Cipher.getInstance(strPaddingMode);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
        byte[] encryptedBytes = cipher.doFinal(byteContent);
        return Base64Util.base64Encode2String(encryptedBytes);
    }


    /**
     * CBC 解密方法
     *
     * @param strContent         String 加密内容
     * @param strKey             String 解密密钥
     * @param strPaddingMode     String 填充模式:
     *                           ENCRYPT_CBC_PKCS5PADDING;
     *                           ENCRYPT_CBC_NOPADDING;
     * @param strIvParameterSpec String 初始向量;
     */
    public static String CbcDecode(String strContent, String strKey, String strPaddingMode, String strIvParameterSpec)
            throws NoSuchPaddingException, BadPaddingException,
            InvalidAlgorithmParameterException, NoSuchAlgorithmException,
            IllegalBlockSizeException, UnsupportedEncodingException, InvalidKeyException {
        byte[] byteContent = Base64Util.base64Decode(strContent);
        byte[] enCodeFormat = strKey.getBytes();
        SecretKeySpec secretKeySpec = new SecretKeySpec(enCodeFormat, "AES");
        byte[] initParam = strIvParameterSpec.getBytes();
        IvParameterSpec ivParameterSpec = new IvParameterSpec(initParam);
        Cipher cipher = Cipher.getInstance(strPaddingMode);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
        byte[] result = cipher.doFinal(byteContent);
        return new String(result, "UTF-8");
    }

    /**
     * 生成符合加密要求的密钥
     *
     * @return String 符合要求的密钥
     */
    public static String buildAesKey() {
        KeyGenerator kgen = null;
        try {
            kgen = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        kgen.init(128);
        SecretKey skey = kgen.generateKey();
        return Base64Util.base64Encode2String(skey.getEncoded());
    }


}