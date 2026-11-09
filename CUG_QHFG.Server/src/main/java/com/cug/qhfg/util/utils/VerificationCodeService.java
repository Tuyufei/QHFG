package com.cug.qhfg.util.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class VerificationCodeService {

    private static final String CODE_PREFIX = "verify_code:";

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 保存验证码到 Redis，设置有效期为 5 分钟
     */
    public void saveCode(String email, String code) {
        String key = CODE_PREFIX + email;
        redisTemplate.opsForValue().set(key, code, Duration.ofMinutes(5));
    }

    /**
     * 校验验证码
     */
    public boolean validateCode(String email, String code) {
        String key = CODE_PREFIX + email;
        String cached = redisTemplate.opsForValue().get(key);
        return code != null && code.equals(cached);
    }

    /**
     * 删除验证码（注册成功后可删除）
     */
    public void removeCode(String email) {
        redisTemplate.delete(CODE_PREFIX + email);
    }
}

