package com.cug.qhfg.service.impl;

import com.alibaba.fastjson.JSON;
import com.cug.qhfg.common.Result;

import com.cug.qhfg.entity.SysUser;
import com.cug.qhfg.security.LoginUser;
import com.cug.qhfg.service.LoginService;
import com.cug.qhfg.util.jwt.JwtConstant;
import com.cug.qhfg.util.jwt.JwtUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    public Result login(SysUser sysUser) {

        //使用AuthenticationManager的authenticate方法进行用户认证
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(sysUser.getUsername(), sysUser.getPassword());
        Authentication authenticate;
        try {
            authenticate = authenticationManager.authenticate(authenticationToken);
        } catch (Exception e) {
            // 返回通用错误信息，避免泄露安全信息
            return Result.error("登录失败，请检查用户名和密码", 401);
        }

        //如果认证通过,使用id和username 来生成token
        LoginUser loginUser = (LoginUser) authenticate.getPrincipal();
        String userid = loginUser.getSysUser().getId().toString();
        String username = loginUser.getSysUser().getUsername().toString();
        String email = loginUser.getSysUser().getEmail().toString();
        String phone = loginUser.getSysUser().getPhone().toString();
        String company = loginUser.getSysUser().getCompany().toString();
        String region = loginUser.getSysUser().getRegion().toString();

        String token = JwtUtils.createJWT(userid,username, JwtConstant.JWT_TTL);

        // 读取用户所有信息存到redis中
        String jsonString = JSON.toJSONString(loginUser);
        System.out.println(jsonString);
        stringRedisTemplate.opsForValue().set("login:"+userid,jsonString);

        // 返回前端
        // 获取用户角色信息
        String role = loginUser.getRole();

        Map<String,Object> map = new HashMap<>();
        map.put("role",role);
        map.put("username",username);
        map.put("email",email);
        map.put("phone",phone);
        map.put("company",company);
        map.put("region",region);
        map.put("token",token);

        return Result.success(map);
    }

    @Override
    public Result logout() {
        // 拿到用户信息
        UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Integer userid = loginUser.getSysUser().getId();
        // 删除redis中的值
        stringRedisTemplate.delete("login:" + userid);

        return Result.success("登出成功");
    }
}
