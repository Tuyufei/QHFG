package com.cug.qhfg.util.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * @Description Http请求工具类
 * @Author fjh
 * @Date 2024/12/22
 */
@Component
public class HttpUtils {
    @Autowired
    private RestTemplate restTemplate;

    //Get请求
    public ResponseEntity<?> HttpGet(String url, Map<String, Object> parameter, HttpHeaders headers,Class<?> clazz){

        if (!parameter.isEmpty()){
            url=url+"?";
            for (String key: parameter.keySet()){
                url=url+key+"="+parameter.get(key)+"&";
            }
            url=url.substring(0,url.length()-1);
        }


        HttpEntity httpEntity=new HttpEntity<>(null,headers);

        ResponseEntity<?> response=restTemplate.exchange(url, HttpMethod.GET,httpEntity,clazz);

        return response;
    }

    //Post请求-form传递参数
    public ResponseEntity<?> HttpPostForm(String url, MultiValueMap<String,Object> params,HttpHeaders headers,Class<?> clazz){

        HttpEntity httpEntity=new HttpEntity<>(params,headers);

        ResponseEntity<?> response=restTemplate.postForEntity(url,httpEntity,clazz);

        return response;
    }

    //Post请求-json传递参数
    public ResponseEntity<?> HttpPostJson(String url, Map<String,Object> params,HttpHeaders headers,Class<?> clazz){

        HttpEntity httpEntity=new HttpEntity<>(params,headers);

        ResponseEntity<?> response=restTemplate.postForEntity(url,httpEntity,clazz);

        return response;
    }


}
