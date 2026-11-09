package com.cug.qhfg.controller;

import com.cug.qhfg.common.Result;
import com.cug.qhfg.dto.RegisterRequest;
import com.cug.qhfg.service.SysUserService;
import com.cug.qhfg.util.utils.VerificationCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * @Description TODO
 * @Author fjh
 * @Date 2025/7/9
 */

@RestController
@RequestMapping("/user")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private VerificationCodeService codeService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RedisTemplate redisTemplate;


    @GetMapping("/sendmodcode")
    public Result<?> sendCode(String email) {

        // 验证邮箱格式
        if (email == null || !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            return Result.error("邮箱格式不正确", 415);
        }

        //验证用户是否存在
        if (!sysUserService.hasUser(email)){
            return Result.error("用户邮箱不存在",412);
        }

        //发送验证码
        if(sysUserService.sendModifyCode(email)){
            return Result.success("验证码发送成功");
        }else {
            return Result.error("发送失败：",500);
        }
    }

    @PostMapping("/modifypwd")
    public Result<?> modifyPassword(@RequestBody RegisterRequest request){

        // 验证验证码
        if (!codeService.validateCode(request.getEmail(), request.getCode())) {
            return Result.error("验证码错误",411);
        }


        int result= sysUserService.modifyPwd(request.getEmail(),passwordEncoder.encode(request.getPassword()));

        if (result > 0) {
            redisTemplate.delete("verify_code:"+ request.getEmail());
            return Result.success("修改成功！");
        } else {
            return Result.error("修改失败",419);
        }

    }
}
