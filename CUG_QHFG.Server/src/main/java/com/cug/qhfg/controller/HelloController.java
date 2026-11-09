package com.cug.qhfg.controller;


import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @RequestMapping("/hello")
//    @PreAuthorize("hasAuthority('download')")
    public String sayHello() {
        return "Hello, World!";
    }

    @RequestMapping("/no")
    @PreAuthorize("hasAuthority('normal')")
    public String sayno() {
        return "no!";
    }
}
