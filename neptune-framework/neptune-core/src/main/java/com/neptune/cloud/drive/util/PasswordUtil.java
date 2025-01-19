package com.neptune.cloud.drive.util;

import java.security.NoSuchAlgorithmException;

/**
 * 密码工具类
 */
public class PasswordUtil {

    /**
     * 密码加密
     */
    public static String encryptPassword(String salt, String password) throws NoSuchAlgorithmException {
        return MessageDigestUtil.sha256(MessageDigestUtil.sha1(password) + salt);
    }

    /**
     * 生成盐值
     */
    public static String generateSalt() throws NoSuchAlgorithmException {
        return MessageDigestUtil.md5(UUIDUtil.getUUID());
    }

}
