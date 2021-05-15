package com.future.xlink.utils;

import android.text.TextUtils;
import android.util.Base64;

import com.future.xlink.logs.Log4J;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES加密
 *
 * @author Sands
 */
public class AESEncrypter {
    /**
     * 秘钥长度
     */
    private static final int SECURE_KEY_LENGTH = 16;

    private    static  final  String KEY="";

    private static final String IV_KEY = "";


    /**
     * 采用AES128加密
     *
     * @param content   要加密的内容
     * @param appKey 密钥
     * @return
     */
    public static String encrypt(String content, String appKey) {
        if (content == null) {
            return null;
        }
        try {
            // 获得密匙数据
            byte[] rawKeyData = getAESKey(TextUtils.isEmpty(appKey)?KEY:appKey);
            // 从原始密匙数据创建KeySpec对象
            SecretKeySpec key = new SecretKeySpec(rawKeyData, "AES");
            // Cipher对象实际完成加密操作
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            // 用密匙初始化Cipher对象
            byte[] initParam = appKey.getBytes();
            IvParameterSpec ivParameterSpec = new IvParameterSpec(initParam);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivParameterSpec);
            // 正式执行加密操作

            byte[] encryptByte = cipher.doFinal(content.getBytes());

            return Base64.encodeToString(encryptByte, Base64.NO_WRAP);

        } catch (UnsupportedEncodingException e) {

        } catch (NoSuchAlgorithmException e) {

        } catch (NoSuchPaddingException E) {

        } catch (InvalidAlgorithmParameterException e) {

        } catch (InvalidKeyException e) {

        } catch (IllegalBlockSizeException e) {

        } catch (BadPaddingException e) {

        }
        return null;

    }

    private static byte[] getAESKey(String key)
            throws UnsupportedEncodingException {
        byte[] keyBytes;
        keyBytes = key.getBytes("UTF-8");
        byte[] keyBytes16 = new byte[SECURE_KEY_LENGTH];
        System.arraycopy(keyBytes, 0, keyBytes16, 0,
                Math.min(keyBytes.length, SECURE_KEY_LENGTH));
        return keyBytes16;
    }

    /**
     * 采用AES128解密
     *
     * @param content
     * @param secureKey
     * @return
     * @throws Exception ,Exception
     * @throws Exception
     */
    public static String decrypt(String content, String secureKey) {
        if (content == null) {
            return null;
        }

//        byte[] data = Base64.decode(content, Base64.NO_WRAP);

        try {
            // 获得密匙数据
            byte[] rawKeyData = getAESKey(TextUtils.isEmpty(secureKey)?KEY:secureKey); // secureKey.getBytes();
            // 从原始密匙数据创建一个KeySpec对象
            SecretKeySpec key = new SecretKeySpec(rawKeyData, "AES");
            // Cipher对象实际完成解密操作
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
//            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7");
            // 用密匙初始化Cipher对象
            byte[] initParam = secureKey.getBytes();
            IvParameterSpec ivParameterSpec = new IvParameterSpec(initParam);
            cipher.init(Cipher.DECRYPT_MODE, key, ivParameterSpec);
         byte[ ] data=   Base64.decode(content.getBytes("UTF-8"),Base64.NO_WRAP);
         byte [] bytes=content.getBytes();
            String str=new String(cipher.doFinal(bytes,0,bytes.length), "UTF-8");
            Log4J.info(AESEncrypter.class,"data",str);

            return new String(cipher.doFinal(data), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {

        } catch (NoSuchPaddingException e) {

        } catch (InvalidKeyException e) {

        } catch (InvalidAlgorithmParameterException e) {

        } catch (IllegalBlockSizeException e) {

            e.printStackTrace();
        } catch (BadPaddingException e) {

        }


        return null;
    }
}