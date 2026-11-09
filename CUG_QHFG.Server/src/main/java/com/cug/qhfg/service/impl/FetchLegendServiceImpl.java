package com.cug.qhfg.service.impl;

import com.cug.qhfg.service.FetchLegendService;
import com.cug.qhfg.util.utils.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.HashMap;
import java.util.Map;

import static com.cug.qhfg.util.utils.TranstoUtils.translateMapPath;

/**
 * @Description TODO
 * @Author fjh
 * @Date 2025/2/25
 */
@Service
@Slf4j
public class FetchLegendServiceImpl implements FetchLegendService {

    @Value("${QGISIP}")
    private String IP;

    @Value("${server.port}")
    private String port;

    @Autowired
    private HttpUtils httpUtils;
    /**
     *@Description 获取图片图例
     *@Author fjh
     *@param params
     *@Return byte[]
     */
    @Override
    public byte[] fetchLegend(Map<String, Object> params) {

        String url=IP+":"+port+"/ows/";
        Map<String,Object> parameters=new HashMap<>();

        // 补充前端传来的参数，把MAP先加进来以便下面判断用到。
        for (Map.Entry el:params.entrySet()){
            parameters.put((String) el.getKey(),el.getValue());
        }

        // 标志位，判断MAP是否包含“现状数据”,shp图的图例和tif图的图例参数不一样，通过判断MAP中包不包含“现状数据”来区分tif和shp
        boolean isSHP = parameters.get("MAP").toString().contains("现状数据");

        if (isSHP) {
            parameters.put("SERVICE", "WMS");
            parameters.put("VERSION", "1.1.1");
            parameters.put("REQUEST", "GetLegendGraphic");
            parameters.put("FORMAT", "image/png");
            parameters.put("LAYERTITLE", "FALSE");
            parameters.put("TRANSPARENT", "TRUE");
            parameters.put("SRCWIDTH", "300");
            parameters.put("SRCHEIGHT", "400");
            parameters.put("BOXSPACE", "5");
            parameters.put("LAYERSPACE", "3");
            parameters.put("SYMBOLSPACE", "2");
            parameters.put("ICONLABELSPACE", "4");
            parameters.put("SYMBOLWIDTH", "12");
            parameters.put("SYMBOLHEIGHT", "12");
            parameters.put("LAYERFONTSIZE", "8");
            parameters.put("ITEMFONTSIZE", "18");

        } else {
            parameters.put("SERVICE","WMS");
            parameters.put("VERSION","1.1.1");
            parameters.put("REQUEST","GetLegendGraphic");
            parameters.put("FORMAT","image/png");
            parameters.put("TRANSPARENT","TRUE");
            parameters.put("SRCWIDTH","200");
            parameters.put("SRCHEIGHT","600");
            parameters.put("BOXSPACE","2");
            parameters.put("LAYERSPACE","0");
            parameters.put("SYMBOLSPACE","0");
            parameters.put("ICONLABELSPACE","2");
            parameters.put("SYMBOLWIDTH","10");
            parameters.put("SYMBOLHEIGHT","35");
            parameters.put("LAYERFONTSIZE","10");

//            parameters.put("ITEMFONTCOLOR", "0x555252");
//            parameters.put("LAYERFONTCOLOR", "0x555252");

            //字体大小
            parameters.put("ITEMFONTSIZE","15");
            //字体粗体
            // parameters.put("ITEMFONTBOLD","TRUE");

            //去掉图层名和band名
            parameters.put("LAYERTITLE","FALSE");
            parameters.put("RULELABEL","FALSE");
        }

        HttpHeaders headers=new HttpHeaders();
        headers.set("accept","image/png");

        byte[] img=null;
        try{
            img= (byte[]) httpUtils.HttpGet(url,parameters,headers,byte[].class).getBody();

        }catch (HttpClientErrorException e) {
            // 处理4xx错误
            System.err.println("客户端请求错误. Status code: " + e.getStatusCode());

        } catch (HttpServerErrorException e) {
            // 处理5xx错误
            System.err.println("服务端响应错误--图例. Status code: " + e.getStatusCode());

        } catch (ResourceAccessException e) {
            // 处理I/O相关的问题，例如连接超时等
            System.err.println("连接超时: " + e.getMessage());
        }

        return img;
    }
}
