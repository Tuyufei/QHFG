package com.cug.qhfg.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cug.qhfg.common.Result;
import com.cug.qhfg.dto.RegisterRequest;
import com.cug.qhfg.entity.SysUser;
import com.cug.qhfg.mapper.SysUserMapper;
import com.cug.qhfg.util.utils.VerificationCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Random;


@RestController
public class RegisterController {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private VerificationCodeService codeService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/sendcode")
    public Result<?> sendCode(@RequestParam String email) {

        // 验证邮箱格式
        if (email == null || !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            return Result.error("邮箱格式不正确", 415);
        }
        // 1. 生成验证码
        String code = String.valueOf(new Random().nextInt(899999) + 100000); // 6位随机数字

        try {

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(email);
            helper.setSubject("注册验证码");

            // 设置 HTML 内容
            String html = "<html><body>" +
                    "<h2>欢迎注册！</h2>" +
                    "<p>您的验证码是：<b style='color:blue; font-size:18px;'>" + code + "</b></p>" +
                    "<p>有效期为 <b>5分钟</b>，请及时使用。</p>" +
                    "</body></html>";

            helper.setText(html, true); // 第二个参数设为 true 表示是 HTML

            mailSender.send(message);
            codeService.saveCode(email, code);

            return Result.success("验证码发送成功");

        } catch (Exception e) {
            return Result.error("发送失败：" + e.getMessage(),500);
        }
    }

    // 2. 注册接口
    @PostMapping("/register")
    public Result<String> register(@RequestBody RegisterRequest request) {

        // 验证用户名规则
        String username = request.getUsername();
        if (username == null || !username.matches("^[a-zA-Z0-9_]{4,20}$")) {
            return Result.error("用户名长度需在 4 - 20 个字符，只能包含字母、数字和下划线", 416);
        }

        // 验证验证码
        if (!codeService.validateCode(request.getEmail(), request.getCode())) {
            return Result.error("验证码错误",411);
        }

        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", request.getUsername())
                .eq("is_deleted", false); // 只查未删除的用户
        boolean exists = sysUserMapper.selectCount(queryWrapper) > 0;

        if (exists) {
            return Result.error("用户名已存在",412);
        }

        // 检查邮箱是否唯一
        queryWrapper.clear();
        queryWrapper.eq("email", request.getEmail())
                .eq("is_deleted", false);
        exists = sysUserMapper.selectCount(queryWrapper) > 0;
        if (exists) {
            return Result.error("邮箱已被注册",413);
        }

        // 检查电话是否唯一
        queryWrapper.clear();
        queryWrapper.eq("phone", request.getPhone())
                .eq("is_deleted", false);
        exists = sysUserMapper.selectCount(queryWrapper) > 0;
        if (exists) {
            return Result.error("电话已被注册",414);
        }


        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setCompany(request.getCompany());
        user.setRegion(request.getRegion());
        user.setRole("normalUser"); // 默认角色
        user.setIsDeleted(false);

        // 将 LocalDateTime 转换为 Date
        LocalDateTime localDateTime = LocalDateTime.now();
        Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        user.setCreateDate(date);  // 设置给用户的 createDate 字段

        int result = sysUserMapper.insert(user);  //
        codeService.removeCode(request.getEmail());

        if (result > 0) {
            return Result.success("注册成功");
        } else {
            return Result.error("注册失败",419);
        }
    }

}
