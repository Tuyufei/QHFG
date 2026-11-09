package com.cug.qhfg.security;

import com.alibaba.fastjson.JSON;
import com.cug.qhfg.common.Result;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
*认证失败处理
* */

@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        // 构建 Result 对象
        Result<String> result = Result.error("未授权访问，请登录后重试",401);

        // 设置 HTTP 响应状态码
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401

        // 设置响应类型为 JSON
        response.setContentType("application/json;charset=UTF-8");

        // 使用 FastJSON 序列化 Result 对象并写入响应
        String jsonString = JSON.toJSONString(result);
        response.getWriter().write(jsonString);
    }
}
