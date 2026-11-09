package com.cug.qhfg.RestTemplate;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @Description TODO
 * @Author fjh
 * @Date 2024/12/22
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(){
        RestTemplate restTemplate = new RestTemplate();
        //restTemplate.getInterceptors().add(new LoggingRequestInterceptor());

        return restTemplate;
    }
}
