package com.cug.qhfg.security;

import com.alibaba.fastjson.JSON;
import com.cug.qhfg.common.Result;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        // 构建 Result 对象
        Result<String> result = Result.error("无访问权限",403);

        // 设置 HTTP 响应状态码
        response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403

        // 设置响应类型为 JSON
        response.setContentType("application/json;charset=UTF-8");

        // 使用 FastJSON 序列化 Result 对象并写入响应
        String jsonString = JSON.toJSONString(result);
        response.getWriter().write(jsonString);
    }
}
