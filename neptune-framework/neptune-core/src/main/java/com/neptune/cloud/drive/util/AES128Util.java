package com.neptune.cloud.drive.util;

import cn.hutool.core.codec.Base64;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

@Slf4j
public class AES128Util {

    /**
     * 默认向量常量
     */
    public static final String IV = "akjsfakjshf@#!~&";

    /**
     * 秘钥
     */
    private static final String P_KEY = StringUtils.reverse(IV);

    /**
     * 对称加密算法
     */
    private static final String AES_STR = "AES";

    /**
     * 对称加密算法实例
     */
    private static final String INSTANCE_STR = "AES/CBC/PKCS5Padding";

    /**
     * 加密
     */
    public static byte[] encrypt(byte[] content) throws Exception {
        // 0. 判断加密内容是否为空
        if (ArrayUtils.isEmpty(content)) {
            return null;
        }
        // 1. 初始化秘钥
        SecretKeySpec secretKeySpec = new SecretKeySpec(P_KEY.getBytes(), AES_STR);
        // 2. 初始化加密向量
        IvParameterSpec iv = new IvParameterSpec(IV.getBytes());
        // 3. 生成加密算法实例
        Cipher cipher = Cipher.getInstance(INSTANCE_STR);
        // 4. 初始化加密算法实例
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, iv);
        // 5. 生成加密内容
        return cipher.doFinal(content);
    }

    /**
     * 解密
     */
    public static byte[] decrypt(byte[] content) throws Exception {
        // 0. 判断解密内容是否为空
        if (ArrayUtils.isEmpty(content)) {
            return null;
        }
        // 1. 初始化秘钥
        SecretKeySpec secretKeySpec = new SecretKeySpec(P_KEY.getBytes(), AES_STR);
        // 2. 初始化加密向量
        IvParameterSpec iv = new IvParameterSpec(IV.getBytes(StandardCharsets.UTF_8));
        // 3. 生成加密算法实例
        Cipher cipher = Cipher.getInstance(INSTANCE_STR);
        // 4. 初始化加密算法实例
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, iv);
        // 5. 解密内容
        return cipher.doFinal(content);
    }

    /**
     * 加密: 字符串
     */
    public static String encrypt(String content) throws Exception {
        // 0. 判断字符串是否为空
        if (StringUtils.isEmpty(content)) {
            return StringUtils.EMPTY;
        }
        // 1. 初始化秘钥
        SecretKeySpec secretKeySpec = new SecretKeySpec(P_KEY.getBytes(), AES_STR);
        // 2. 初始化加密向量
        IvParameterSpec iv = new IvParameterSpec(IV.getBytes());
        // 3. 生成加密算法实例
        Cipher cipher = Cipher.getInstance(INSTANCE_STR);
        // 4. 初始化加密算法实例
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, iv);
        // 5. 加密内容
        byte[] encryption = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
        // 6. base64 编码
        return Base64.encode(encryption);
    }

    /**
     * 解密: 字符串
     */
    public static String decrypt(String content) throws Exception {
        // 0. 判断字符串是否为空
        if (StringUtils.isBlank(content)) {
            return StringUtils.EMPTY;
        }
        // 1. 初始化秘钥
        SecretKeySpec secretKeySpec = new SecretKeySpec(P_KEY.getBytes(), AES_STR);
        // 2. 初始化加密向量
        IvParameterSpec iv = new IvParameterSpec(IV.getBytes(StandardCharsets.UTF_8));
        // 3. 生成加密算法实例
        Cipher cipher = Cipher.getInstance(INSTANCE_STR);
        // 4. 初始化加密算法实例
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, iv);
        // 5. 解密内容
        byte[] result = cipher.doFinal(Base64.decode(content));
        // 6. 封装为字符串
        return new String(result, StandardCharsets.UTF_8);
    }

}
