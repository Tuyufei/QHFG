package com.cug.qhfg.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String fullName;
    private String phone;
    private String email;
    private String company;
    private String region;
    private String code; // 验证码
}

 