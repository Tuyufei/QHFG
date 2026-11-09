package com.cug.qhfg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cug.qhfg.common.Result;
import com.cug.qhfg.entity.SysUser;
import com.cug.qhfg.service.SysUserService;
import com.cug.qhfg.mapper.SysUserMapper;
import com.cug.qhfg.util.utils.VerificationCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import java.util.Random;

/**
* @author DELL
* @description 针对表【sys_user】的数据库操作Service实现
* @createDate 2025-05-10 14:20:19
*/
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser>
    implements SysUserService{

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private VerificationCodeService codeService;

    @Override
    public boolean hasUser(String email) {
        LambdaQueryWrapper<SysUser> wrapper=new LambdaQueryWrapper<SysUser>()
                .select().eq(SysUser::getEmail,email);

        SysUser user=sysUserMapper.selectOne(wrapper);
        if (user==null){
            return false;
        }else {
            return true;
        }

    }

    @Override
    public boolean sendModifyCode(String email) {
        // 1. 生成验证码
        String code = String.valueOf(new Random().nextInt(899999) + 100000); // 6位随机数字

        try {

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(email);
            helper.setSubject("修改密码验证码");

            // 设置 HTML 内容
            String html = "<html><body>" +
                    "<h2>您正在申请修改密码</h2>" +
                    "<p>本次操作的验证码为：<b style='color:blue; font-size:18px;'>" + code + "</b></p>" +
                    "<p>有效期为 <b>5分钟</b>，请及时使用。</p>" +
                    "</body></html>";

            helper.setText(html, true); // 第二个参数设为 true 表示是 HTML

            mailSender.send(message);
            codeService.saveCode(email, code);

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public int modifyPwd(String email,String password) {

        LambdaUpdateWrapper<SysUser> wrapper=new LambdaUpdateWrapper<SysUser>()
                .set(SysUser::getPassword,password).eq(SysUser::getEmail,email);

        int n=sysUserMapper.update(wrapper);

        return n;
    }
}




