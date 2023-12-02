package com.ym.chat.utils;

import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
/**
 * @author amit
 * @date 2022-01-11 11:17
 **/
public class AesUtils {
    private static final String AES = "AES";
    /**
     * 加密解密算法/加密模式/填充方式
     */
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static java.util.Base64.Encoder base64Encoder = java.util.Base64.getEncoder();
    private static java.util.Base64.Decoder base64Decoder = java.util.Base64.getDecoder();

    private static String userId = "woyouyiwangexiaomimijiubugaosuni";
    private static String code = "doyouloveme";
    static {
        java.security.Security.setProperty("crypto.policy", "unlimited");
    }
    /**
     * AES加密
     */
    public static byte[] encode(String userIds, String codes, byte[] content) {
        try {
            String key = MD5.getMD5(userId);
            byte[] keyVi = MD5.getMD5(userId + code).substring(0,16).getBytes();
            SecretKey secretKey = new SecretKeySpec(key.getBytes(), AES);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(keyVi));
            // 根据密码器的初始化方式加密
            byte[] byteAES = cipher.doFinal(content);
            // 将加密后的数据转换为字符串
            return base64Encoder.encode(byteAES);
        } catch (Exception e) {
//            log.error("AES加密失败",e);
        }
        return null;
    }

    /**
     * AES解密
     */
    public static byte[] decode(String userIds,String codes, byte[] content) {
        try {
            String key = MD5.getMD5(userId);
            byte[] keyVi = MD5.getMD5(userId + code).substring(0,16).getBytes();
            SecretKey secretKey = new SecretKeySpec(key.getBytes(), AES);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(keyVi));
            // 将加密并编码后的内容解码成字节数组
            byte[] byteContent = base64Decoder.decode(content);
            // 解密
            byte[] byteDecode = cipher.doFinal(byteContent);
            return byteDecode;
        } catch (Exception e) {
//            log.error("AES解密失败",e);
        }
        return null;
    }
}