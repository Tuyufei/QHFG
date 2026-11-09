package com.cug.qhfg.controller;

import com.cug.qhfg.common.Result;
import com.cug.qhfg.entity.SysUser;
import com.cug.qhfg.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
public class LoginController {

    @Autowired
    private LoginService loginService;

    @PostMapping("/login")
    public Result login(@RequestBody SysUser sysUser){
        System.out.println("llllogin");
        return loginService.login(sysUser);

    }

    @GetMapping("/logout")
    @PreAuthorize("hasAuthority('normal')")
    public Result logout(){
        return loginService.logout();
    }

}
