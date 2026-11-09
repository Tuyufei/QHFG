package com.cug.qhfg.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cug.qhfg.service.FetchGISMapService;
import com.cug.qhfg.util.utils.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.*;

import static com.cug.qhfg.util.utils.TranstoUtils.translateMapPath;

/**
 * @Description TODO
 * @Author fjh
 * @Date 2024/12/24
 */
@Slf4j
@Service
public class FetchGISMapServiceImpl implements FetchGISMapService {

    @Value("${QGISIP}")
    private String IP;

    @Value("${QGISPort}")
    private String port;

    @Autowired
    private HttpUtils httpUtils;

    // 从请求参数，到shp图属性表的列的映射
    private static final Map<String, String> PARAMETER_TO_COLUMN = new HashMap<>();

    // 互换后的映射
    private static final Map<String, String> COLUMN_TO_PARAMETER = new HashMap<>();


    static {
        // 初始化映射字典
        PARAMETER_TO_COLUMN.put("区域", "区域1");
        PARAMETER_TO_COLUMN.put("次区域", "区域2");
        PARAMETER_TO_COLUMN.put("国家", "国家");
        PARAMETER_TO_COLUMN.put("地区", "国家_1");
        PARAMETER_TO_COLUMN.put("电网", "电网_0");
        PARAMETER_TO_COLUMN.put("县级", "县级");
        PARAMETER_TO_COLUMN.put("地级", "地级");
        PARAMETER_TO_COLUMN.put("省级", "省级");

        // 实现键值互换
        for (Map.Entry<String, String> entry : PARAMETER_TO_COLUMN.entrySet()) {
            COLUMN_TO_PARAMETER.put(entry.getValue(), entry.getKey());
        }

    }

    @Override
    public byte[] fetchMap(Map<String, Object> params) {

        String url=IP+":"+port+"/ows/";

        HttpHeaders headers=new HttpHeaders();
        headers.set("accept","image/png");

        Map<String,Object> parameters=new HashMap<>();
        parameters.put("SERVICE","WMS");
        parameters.put("VERSION","1.1.0");
        parameters.put("REQUEST","GetMap");
        parameters.put("CRS","EPSG:4326");
        parameters.put("FORMAT","image/png");

        for (Map.Entry el:params.entrySet()){
            parameters.put((String) el.getKey(),el.getValue());
        }


        byte[] image=null;
        try{
            image= (byte[]) httpUtils.HttpGet(url,parameters,headers,byte[].class).getBody();
            return image;
        } catch (HttpClientErrorException e) {
            // 处理4xx错误
            System.err.println("客户端请求错误. Status code: " + e.getStatusCode());

        } catch (HttpServerErrorException e) {
            // 处理5xx错误
            System.err.println("服务端响应错误--图片. Status code: " + e.getStatusCode());

        } catch (ResourceAccessException e) {
            // 处理I/O相关的问题，例如连接超时等
            System.err.println("连接超时: " + e.getMessage());
        }

        return image;
    }

    @Override
    public Double getPointValue(double longitude, double latitude, String map, String layers) {

        String url=IP+":"+port+"/ows/";

        HttpHeaders headers=new HttpHeaders();
        headers.set("Accept","*/*");

        // 构造极小范围BBOX确保点击点命中
        double delta = 0.01;
        String bbox = String.format("%f,%f,%f,%f",
                longitude - delta, latitude - delta,
                longitude + delta, latitude + delta);

        // 创建一个 Map 用于存储参数
        Map<String, Object> params = new HashMap<>();
        params.put("SERVICE", "WMS");
        params.put("VERSION", "1.1.1");
        params.put("REQUEST", "GetFeatureInfo");
        params.put("QUERY_LAYERS", layers);
        params.put("MAP", map);
        params.put("INFO_FORMAT", "application/json");
        params.put("WIDTH", "1");
        params.put("HEIGHT", "1");
        params.put("BBOX", bbox);
        params.put("CRS", "EPSG:4326");
        params.put("X", "0");
        params.put("Y", "0");

        // 标志位，判断MAP是否包含“现状数据”,shp图的图例和tif图的图例参数不一样，通过判断MAP中包不包含“现状数据”来区分tif和shp
        boolean isSHP = params.get("MAP").toString().contains("现状数据");

        if (isSHP) {
            try{
                String valueJson = (String) httpUtils.HttpGet(url,params,headers,String.class).getBody();
                JSONObject rootObject = JSONObject.parseObject(valueJson);
                // 获取 features 数组
                JSONArray featuresArray = rootObject.getJSONArray("features");
                if (featuresArray != null && featuresArray.size() > 0) {
                    // 获取数组中的第一个元素
                    JSONObject firstFeature = featuresArray.getJSONObject(0);
                    String idValue = firstFeature.getString("id");
                    // 使用字符串处理方法提取前面的数字部分
                    int index = idValue.indexOf('.');
                    String numberPart = idValue.substring(0, index);

                    // 获取 properties 对象
                    JSONObject propertiesObject = firstFeature.getJSONObject("properties");
                    if (propertiesObject != null) {
                        // 获取 Band 1 对应的值
                        String band1Str = propertiesObject.getString("Y" + numberPart);
                        Double band1Value = null;

                        // 如果Band 1不为nan
                        if (!"nan".equalsIgnoreCase(band1Str)) {
                            return band1Value = Double.parseDouble(band1Str);
                        }
                    }
                }
            } catch (HttpClientErrorException e) {
                // 处理4xx错误
                System.err.println("客户端请求错误. Status code: " + e.getStatusCode());

            } catch (HttpServerErrorException e) {
                // 处理5xx错误
                System.err.println("服务端响应错误. Status code: " + e.getStatusCode());

            } catch (ResourceAccessException e) {
                // 处理I/O相关的问题，例如连接超时等
                System.err.println("连接超时: " + e.getMessage());
            }
        }
        else {
            try{
                String valueJson = (String) httpUtils.HttpGet(url,params,headers,String.class).getBody();
                JSONObject rootObject = JSONObject.parseObject(valueJson);
                // 获取 features 数组
                JSONArray featuresArray = rootObject.getJSONArray("features");
                if (featuresArray != null && featuresArray.size() > 0) {
                    // 获取数组中的第一个元素
                    JSONObject firstFeature = featuresArray.getJSONObject(0);
                    // 获取 properties 对象
                    JSONObject propertiesObject = firstFeature.getJSONObject("properties");
                    if (propertiesObject != null) {
                        // 获取 Band 1 对应的值
                        String band1Str = propertiesObject.getString("Band 1");
                        Double band1Value = null;

                        // 如果Band 1不为nan
                        if (!"nan".equalsIgnoreCase(band1Str)) {
                            return band1Value = Double.parseDouble(band1Str);
                        }
                    }
                }
            } catch (HttpClientErrorException e) {
                // 处理4xx错误
                System.err.println("客户端请求错误. Status code: " + e.getStatusCode());

            } catch (HttpServerErrorException e) {
                // 处理5xx错误
                System.err.println("服务端响应错误. Status code: " + e.getStatusCode());

            } catch (ResourceAccessException e) {
                // 处理I/O相关的问题，例如连接超时等
                System.err.println("连接超时: " + e.getMessage());
            }
        }

        return null;
    }

    @Override
    public byte[] fetchBoundary(Map<String, Object> params) {
        String url=IP+":"+port+"/ows/";

        HttpHeaders headers=new HttpHeaders();
        headers.set("accept","image/png");

        Map<String,Object> parameters=new HashMap<>();
        parameters.put("SERVICE","WMS");
        parameters.put("VERSION","1.1.0");
        parameters.put("REQUEST","GetMap");
        parameters.put("CRS","EPSG:4326");
        parameters.put("FORMAT","image/png");

        parameters.put("MAP","/io/data/boundary/boundary.qgs");

        // 区分海洋还是陆地
        String layers = "Global_" + params.get("class").toString();
        parameters.put("LAYERS",layers);

        // 区分区域
        List<String> areaList = Arrays.asList(params.get("area").toString().split(","));

        // 构造查询条件列表
        List<String> filterConditions = new ArrayList<>();
        String column = PARAMETER_TO_COLUMN.get(params.get("level").toString());
        for (String area : areaList) {
            // 只构造列名和值的比较条件
            String condition = "\"" + column + "\"" + " = '" + area + "'";
            filterConditions.add(condition);
        }

        // 将所有条件用 OR 连接起来
        String innerFilter = String.join(" OR ", filterConditions);
        // 只拼接一次 layers
        String filter = layers + ":"  + innerFilter ;
        parameters.put("FILTER", filter);
        System.out.println(filter);

//        // 构造查询条件
//        String filter = layers+":"+ "\"" + PARAMETER_TO_COLUMN.get(params.get("level").toString()) + "\"" + " = '" + params.get("area").toString() + "'";
//        parameters.put("FILTER",filter);


        for (Map.Entry el:params.entrySet()){
            parameters.put((String) el.getKey(),el.getValue());
        }

        byte[] image=null;
        try{
            image= (byte[]) httpUtils.HttpGet(url,parameters,headers,byte[].class).getBody();
            return image;
        } catch (HttpClientErrorException e) {
            // 处理4xx错误
            System.err.println("客户端请求错误. Status code: " + e.getStatusCode());

        } catch (HttpServerErrorException e) {
            // 处理5xx错误
            System.err.println("服务端响应错误. Status code: " + e.getStatusCode());

        } catch (ResourceAccessException e) {
            // 处理I/O相关的问题，例如连接超时等
            System.err.println("连接超时: " + e.getMessage());
        }

        return image;
    }

    @Override
    public Map<String, Object> extractBasicInfo(String url,
                                                Map<String, Object> params,
                                                HttpHeaders headers,
                                                String[] keys,
                                                boolean useColumnMap) {
        try {
            String valueJson = (String) httpUtils.HttpGet(url, params, headers, String.class).getBody();
            JSONObject rootObject = JSONObject.parseObject(valueJson);
            JSONArray featuresArray = rootObject.getJSONArray("features");
            if (featuresArray == null || featuresArray.isEmpty()) return null;

            JSONObject properties = featuresArray.getJSONObject(0).getJSONObject("properties");
            if (properties == null) return null;

            Map<String, Object> result = new LinkedHashMap<>();
            for (String key : keys) {
                if (properties.containsKey(key)) {
                    String mapKey = useColumnMap ? COLUMN_TO_PARAMETER.get(key) : key;
                    result.put(mapKey, properties.getString(key));
                }
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace(); // 或者打日志
            return null;
        }
    }


    @Override
    public Map<String, Object> getAreaOfPoint(double longitude, double latitude) {
        String url = IP + ":" + port + "/ows/";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "*/*");

        double delta = 0.01;
        String bbox = String.format("%f,%f,%f,%f",
                longitude - delta, latitude - delta,
                longitude + delta, latitude + delta);

        Map<String, Object> params = new HashMap<>();
        params.put("SERVICE", "WMS");
        params.put("VERSION", "1.1.0");
        params.put("REQUEST", "GetFeatureInfo");
        params.put("INFO_FORMAT", "application/json");
        params.put("WIDTH", "1");
        params.put("HEIGHT", "1");
        params.put("BBOX", bbox);
        params.put("CRS", "EPSG:4326");
        params.put("X", "0");
        params.put("Y", "0");

        // 1. 查询全球边界
        params.put("MAP", "/io/data/boundary/boundary.qgs");
        params.put("QUERY_LAYERS", "Global_boundary");

        Map<String, Object> result = extractBasicInfo(url, params, headers,
                new String[]{"区域1", "区域2", "国家", "国家_1"}, true);
        if (result == null) {
            params.put("QUERY_LAYERS", "Global_ocean");
            result = extractBasicInfo(url, params, headers,
                    new String[]{"区域1", "区域2", "国家", "国家_1"}, true);
            if (result == null) {
                return Collections.emptyMap();
            }
        }

        // 如果不是中国，直接返回
        if (!"中国".equals(result.get(COLUMN_TO_PARAMETER.get("国家")))) {
            return result;
        }

        // 2. 查询中国边界
        params.put("MAP", "/io/data/boundary/china_boundary.qgs");
        params.put("QUERY_LAYERS", "china_boundary");

        Map<String, Object> chinaResult = extractBasicInfo(url, params, headers,
                new String[]{"电网_0", "省级", "地级", "县级"}, true);

        if (chinaResult != null) {
            result.putAll(chinaResult);
        }

        return result;
    }


}
