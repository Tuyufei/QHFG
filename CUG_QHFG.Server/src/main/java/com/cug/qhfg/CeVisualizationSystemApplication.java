package com.cug.qhfg;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@MapperScan("com.cug.qhfg.mapper")
@EnableCaching
public class CeVisualizationSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(CeVisualizationSystemApplication.class, args);
    }

}
