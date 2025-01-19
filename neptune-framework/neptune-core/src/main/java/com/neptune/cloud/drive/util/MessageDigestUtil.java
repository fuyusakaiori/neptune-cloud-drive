package com.neptune.cloud.drive.util;

import cn.hutool.core.codec.Base64;
import com.neptune.cloud.drive.constant.StringConstant;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 散列算法工具类
 */
public class MessageDigestUtil {

    private static final String MD5_STR = "MD5";

    private static final String SHA1_STR = "SHA1";

    private static final String SHA256_STR = "SHA-256";


    /**
     * 加密 + base64 编码
     */
    private static String hash(String content, String mode) throws NoSuchAlgorithmException {
        // 1. 判断是否为空
        if (StringUtils.isEmpty(content)) {
            return StringConstant.EMPTY;
        }
        // 2. 加密
        byte[] hashValue = hash(content.getBytes(StandardCharsets.UTF_8), mode);
        // 3. 判断加密是否成功
        if (ArrayUtils.isEmpty(hashValue)) {
            return StringConstant.EMPTY;
        }
        // 4. base64 编码
        return Base64.encode(hashValue);
    }

    /**
     * 加密
     */
    private static byte[] hash(byte[] content, String mode) throws NoSuchAlgorithmException {
        // 1. 判断加密内容是否为空
        if (ArrayUtils.isEmpty(content) || StringUtils.isEmpty(mode)) {
            return null;
        }
        // 2. 初始化消息摘要实例
        MessageDigest messageDigest = MessageDigest.getInstance(mode);
        // 3. 加密消息
        return messageDigest.digest(content);
    }

    /**
     * 采用 MD5 加密
     */
    public static String md5(String content) throws NoSuchAlgorithmException {
        return hash(content, MD5_STR);
    }

    /**
     * 采用 SHA1 加密
     */
    public static String sha1(String content) throws NoSuchAlgorithmException {
        return hash(content, SHA1_STR);
    }

    /**
     * 采用 SHA256 加密
     */
    public static String sha256(String content) throws NoSuchAlgorithmException {
        return hash(content, SHA256_STR);
    }

}
