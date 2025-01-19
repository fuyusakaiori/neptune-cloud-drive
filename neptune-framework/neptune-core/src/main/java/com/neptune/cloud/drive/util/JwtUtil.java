package com.neptune.cloud.drive.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * JWT
 */
public class JwtUtil {

    public static final Long TWO_LONG = 2L;

    /**
     * 秘钥
     */
    private final static String JWT_PRIVATE_KEY = "0CB16040A41140E48F2F93A7BE222C46";

    /**
     * 刷新时间
     */
    private final static String RENEWAL_TIME = "RENEWAL_TIME";

    /**
     * 生成 token
     */
    public static String generateToken(String subject, String claimKey, Object claimValue, Long expire) {
        return Jwts.builder()
                .setSubject(subject)
                .claim(claimKey, claimValue)
                .claim(RENEWAL_TIME, new Date(System.currentTimeMillis() + expire / TWO_LONG))
                .setExpiration(new Date(System.currentTimeMillis() + expire))
                .signWith(SignatureAlgorithm.HS256, JWT_PRIVATE_KEY)
                .compact();
    }

    /**
     * 解析 token
     */
    public static Object analyzeToken(String token, String claimKey) {
        if (StringUtils.isBlank(token)) {
            return null;
        }
        Claims claims = Jwts.parser()
                .setSigningKey(JWT_PRIVATE_KEY)
                .parseClaimsJws(token)
                .getBody();
        return claims.get(claimKey);
    }

}
