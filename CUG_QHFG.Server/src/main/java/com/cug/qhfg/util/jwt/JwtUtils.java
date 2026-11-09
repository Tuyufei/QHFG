package com.cug.qhfg.util.jwt;


import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;

/**
 * jwt加密和解密的工具类
 */

@Slf4j
public class JwtUtils {

    /**
     * 签发JWT
     * @param id
     * @param subject 可以是JSON数据 尽可能少
     * @param ttlMillis
     * @return
     */
    public static String createJWT(String id, String subject, long ttlMillis) {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        SecretKey secretKey = generalKey();
        JwtBuilder builder = Jwts.builder()
                .setId(id)
                .setSubject(subject)   // 主题
                .setIssuer("clj")     // 签发者
                .setIssuedAt(now)      // 签发时间
                .signWith(signatureAlgorithm, secretKey); // 签名算法以及密匙
        if (ttlMillis >= 0) {
            long expMillis = nowMillis + ttlMillis;
            Date expDate = new Date(expMillis);
            builder.setExpiration(expDate); // 过期时间
        }
        return builder.compact();
    }

    /**
     * 生成jwt token
     * @param username
     * @return
     */
    public static String genJwtToken(String username){
        return createJWT(username,username,60*60*1000);
    }

    /**
     * 验证JWT
     * @param jwtStr
     * @return
     */
    public static CheckResult validateJWT(String jwtStr) {
        CheckResult checkResult = new CheckResult();
        Claims claims = null;
        try {
            claims = parseJWT(jwtStr);
            checkResult.setSuccess(true);
            checkResult.setClaims(claims);
        } catch (ExpiredJwtException e) {
            checkResult.setErrCode(JwtConstant.JWT_ERRCODE_EXPIRE);
            checkResult.setSuccess(false);
        } catch (SignatureException e) {
            checkResult.setErrCode(JwtConstant.JWT_ERRCODE_FAIL);
            checkResult.setSuccess(false);
        } catch (Exception e) {
            checkResult.setErrCode(JwtConstant.JWT_ERRCODE_FAIL);
            checkResult.setSuccess(false);
        }
        return checkResult;
    }

    public static boolean isValidateJWT(String jwtStr) {
        CheckResult checkResult = new CheckResult();
        Claims claims = null;
        try {
            claims = parseJWT(jwtStr);
        } catch (ExpiredJwtException e) {
            return false;
        } catch (SignatureException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
        return true;
    }


    /**
     * 生成加密Key
     * @return
     */
    public static SecretKey generalKey() {
        byte[] encodedKey = Base64.decode(JwtConstant.JWT_SECERT);
        SecretKey key = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
        return key;
    }


    /**
     * 解析JWT字符串
     * @param jwt
     * @return
     * @throws Exception
     */
    public static Claims parseJWT(String jwt) {
        SecretKey secretKey = generalKey();
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(jwt)
                .getBody();
    }

    public static void main(String[] args) throws InterruptedException {

//        Map<String, Object> userInfoMap = new HashMap<>();
//        userInfoMap.put("username", "John");
//        userInfoMap.put("role", "admin");
//
//        String subjectJson = JSON.toJSONString(userInfoMap);
//
//        String sc = createJWT("1",subjectJson, 60 * 60 * 1000);
//        System.out.println(sc);
//        System.out.println(validateJWT(sc).getErrCode());
//        System.out.println(validateJWT(sc).getClaims().getId());
//        System.out.println(validateJWT(sc).getClaims().getSubject());

        String sc ="eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIxIiwic3ViIjoie1wicm9sZVwiOlwiYWRtaW5cIixcInVzZXJuYW1lXCI6XCJKb2huXCJ9IiwiaXNzIjoiY2xqIiwiaWF0IjoxNzMyNjk4MjU0LCJleHAiOjE3MzI3MDE4NTR9.lNeVYVIXMrKl2clnd7oVacq4vAauRYBx_gTSWGWQnq0";
        System.out.println(validateJWT(sc).getClaims());
        Claims claims = validateJWT(sc).getClaims();

        System.out.println(claims.getSubject());
        System.out.println(claims.getId());
    }

}
