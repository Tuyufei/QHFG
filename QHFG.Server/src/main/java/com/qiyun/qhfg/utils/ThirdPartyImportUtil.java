package com.qiyun.qhfg.utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * @PACKAGE_NAME: com.example.fxpgJava.utils
 * @NAME: ThirdPartyImportUtil 第三方数据导入工具类
 * @USER: spike
 * @DATE: 2023/5/22 9:55
 * @PROJECT_NAME: FXPG_Java
 */
public class ThirdPartyImportUtil {

    /**
     * 调用Get请求方法
     *
     * @param httpUrl
     * @return
     */
    public static String doGet(String httpUrl, String cookie) {
        HttpURLConnection connection = null;
        InputStream is = null;
        BufferedReader br = null;
        String result = null;// 返回结果字符串
        try {
            // 创建远程url连接对象
            URL url = new URL(httpUrl);
            // 通过远程url连接对象打开一个连接，强转成httpURLConnection类
            connection = (HttpURLConnection) url.openConnection();
            // 设置连接方式：get
            connection.setRequestMethod("GET");
            // 设置连接主机服务器的超时时间：15000毫秒
            connection.setConnectTimeout(15000);
            // 设置读取远程返回的数据时间：60000毫秒
            connection.setReadTimeout(60000);
            //设置cookie
            if (cookie != null) {
                connection.setRequestProperty("Authorization", cookie);
            }
            // 发送请求
            connection.connect();
            // 通过connection连接，获取输入流
            if (connection.getResponseCode() == 200) {
                is = connection.getInputStream();
                // 封装输入流is，并指定字符集
                br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                // 存放数据
                StringBuffer sbf = new StringBuffer();
                String temp = null;
                while ((temp = br.readLine()) != null) {
                    sbf.append(temp);
                    sbf.append("\r\n");
                }
                result = sbf.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // 关闭资源
                if (null != br) {
                    br.close();
                }
                if (null != is) {
                    is.close();
                }
                connection.disconnect();// 关闭远程连接
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static String getInfoFromThirdParty(String httpUrl, String cookie, Map<String, String> paraMap) {
        httpUrl += "?";
        if (paraMap != null) {
            for (Map.Entry<String, String> entity : paraMap.entrySet()) {
                String key = entity.getKey();
                String value = entity.getValue();
                String temp = key + "=" + value + "&";
                httpUrl += temp;
            }
        }
        httpUrl = httpUrl.substring(0, httpUrl.length() - 1);
        return doGet(httpUrl, cookie);
    }

    public static String getCookieStr(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        String cookie = null;
        if (cookies != null) {
            cookie = "";
            for (Cookie cookieStr : cookies) {
                String key = cookieStr.getName();
                String value = cookieStr.getValue();
                cookie += key + "=" + value + ";";
            }
            cookie = cookie.substring(0, cookie.length() - 1);
        }
        return cookie;
    }

}
