package com.qiyun.qhfg.filter;

import cn.dev33.satoken.util.SaResult;
import com.alibaba.fastjson.JSON;
import com.qiyun.qhfg.entity.SysUser;
import com.qiyun.qhfg.utils.EncryptAndDecrypte;
import com.qiyun.qhfg.utils.ResultUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @PACKAGE_NAME: com.example.fxpgJava.filter
 * @NAME: ManageApiFilter 经理用户的过滤
 * @USER: spike
 * @DATE: 2023/4/12 8:55
 * @PROJECT_NAME: FXPG_Java
 */
@Order(1)
public class ManageApiFilter implements Filter {

    //权限不通过错误提升
    private final static String AUTH_FAILED = "请求来源非法";
    private final static String AUTH_403 = "用户无权限";
    private final static String PARAM_NAME_EXCLUSIONS = "exclusions";
    private Set<String> excludesPattern;

    @Override
    public void init(FilterConfig filterConfig) {
        //这里是注册类中添加的白名单，转为Set集合
        String param = filterConfig.getInitParameter(PARAM_NAME_EXCLUSIONS);
        if (param != null && param.trim().length() != 0) {
            this.excludesPattern = new HashSet(Arrays.asList(param.split("\\s*,\\s*")));
        }
    }

    /**
     * 是否需要过滤
     *
     * @param uri
     * @return
     */
    public boolean isNeedFilter(String uri) {
        for (String includeUrl : excludesPattern) {
            if (includeUrl.contains("*")) {
                if (uri.startsWith(includeUrl.substring(0, includeUrl.length() - 1))) {
                    return false;
                }
            } else {
                if (includeUrl.equals(uri)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Value("${encryption.key}")
    private String encryptionKey;

    @Value("${encryption.iv}")
    private String encryptionIv;

    @SneakyThrows
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        HttpServletRequest req = (HttpServletRequest) request;
        boolean needFilter = isNeedFilter(req.getRequestURI());
        if (needFilter) {
            String authorization = req.getHeader("Authorization");
            if (req.getHeader("cookie") != null) {
                List<String> cookieList = Arrays.asList(req.getHeader("cookie").split(";"));
                for (String cookie : cookieList) {
                    if (cookie.split("token=").length > 1) {
                        authorization = cookie.split("token=")[1];
                    }
                }
            }
            SysUser user = null;
            if (!StringUtils.isEmpty(authorization)) {
                user = JSON.parseObject(EncryptAndDecrypte.decrypteString(authorization, encryptionKey, encryptionIv), SysUser.class);
            } else {
                Cookie[] cookies = req.getCookies();
                if (Objects.isNull(cookies)) {
                    response = sendErrorMsg(AUTH_FAILED, response);
                    return;
                }
                for (Cookie cookie : cookies) {
                    String key = cookie.getName();
                    if (key.equals(ResultUtil.TOKEN) && cookie.getValue() != null) {
                        String token = cookie.getValue();
                        user = JSON.parseObject(EncryptAndDecrypte.decrypteString(token, encryptionKey, encryptionIv), SysUser.class);
                        continue;
                    }
                    // 判断token是否为空
                    if (key.equals(ResultUtil.TOKEN) && cookie.getValue() == null) {
                        response = sendErrorMsg(AUTH_FAILED, response);
                        return;
                    }
                }
            }
            //判断 用户对象为空 或 用户账号为空 或 用户角色为空
            if (user == null || Objects.isNull(user.getUserName()) || Objects.isNull(user.getRole())) {
                response = sendErrorMsg(AUTH_FAILED, response);
                return;
            }
            // POST请求只能admin（管理员）通过
            if (!"GET".equalsIgnoreCase(req.getMethod()) && !user.getRole().equals("管理员")) {
                response = sendErrorMsg(AUTH_403, response);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private ServletResponse sendErrorMsg(String msg, ServletResponse response) throws IOException {
        SaResult saResult = ResultUtil.error(msg);
        //没有带token的接口会被拦截，固定返回code为-99
        saResult.setCode(-99);
        String jsonString = JSON.toJSONString(saResult);
        ServletOutputStream outputStream = response.getOutputStream();
        //json格式返回
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        outputStream.write(jsonString.getBytes());
        outputStream.flush();
        return response;
    }

    @Override
    public void destroy() {

    }
}
