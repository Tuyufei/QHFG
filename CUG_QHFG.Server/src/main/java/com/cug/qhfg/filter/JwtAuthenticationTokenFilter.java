package com.cug.qhfg.filter;

import com.alibaba.fastjson.JSON;
import com.cug.qhfg.security.LoginUser;
import com.cug.qhfg.service.SysUserService;
import com.cug.qhfg.util.jwt.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.metadata.DataSourcePoolMetadataProvidersConfiguration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;


@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private DataSourcePoolMetadataProvidersConfiguration dataSourcePoolMetadataProvidersConfiguration;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 获取token
        String token = request.getHeader("token");

        // 如果没有token，就放行
        if(!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 解析token
        Claims claims = JwtUtils.validateJWT(token).getClaims();
        String userid = claims.getId();
        String username = claims.getSubject();

        // 获取用户信息(到redis去查)
        String redisKey = "login:" + userid;
        String jsonLoginUser = stringRedisTemplate.opsForValue().get(redisKey);
        LoginUser loginUser = JSON.parseObject(jsonLoginUser, LoginUser.class);
        System.out.println(loginUser);

        if(Objects.isNull(loginUser)){
            throw new RuntimeException("token非法");
        }

        // 存入 SecurityContextHolder
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(loginUser,null,loginUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 放行
        filterChain.doFilter(request, response);
    }
}
