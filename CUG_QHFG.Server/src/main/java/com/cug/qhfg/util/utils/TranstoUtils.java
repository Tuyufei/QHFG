package com.cug.qhfg.util.utils;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;

/**
 * @Description TODO
 * @Author fjh
 * @Date 2025/4/5
 */
public class TranstoUtils {

    private static final Map<String, String> CHINESE_TO_ENGLISH = new HashMap<>();

    static {
        CHINESE_TO_ENGLISH.put("发电侧", "pg");
        CHINESE_TO_ENGLISH.put("电网侧", "grid");
        CHINESE_TO_ENGLISH.put("用户侧", "cus");

        CHINESE_TO_ENGLISH.put("风力发电", "wind");
        CHINESE_TO_ENGLISH.put("光伏发电", "pv");
        CHINESE_TO_ENGLISH.put("光热发电", "csp");
        CHINESE_TO_ENGLISH.put("水力发电", "hydp");

        CHINESE_TO_ENGLISH.put("低空风电", "low");
        CHINESE_TO_ENGLISH.put("高空风电", "high");
        CHINESE_TO_ENGLISH.put("集中式光伏发电", "");
        CHINESE_TO_ENGLISH.put("分布式光伏发电", "");

        CHINESE_TO_ENGLISH.put("海上风电", "off");
        CHINESE_TO_ENGLISH.put("陆上风电", "on");

        CHINESE_TO_ENGLISH.put("潜力数据", "pot");
        CHINESE_TO_ENGLISH.put("现状数据", "curr");

        CHINESE_TO_ENGLISH.put("2.5MW", "2_5mw");
        CHINESE_TO_ENGLISH.put("5MW", "5mw");
        CHINESE_TO_ENGLISH.put("6.5MW", "6_5mw");
        CHINESE_TO_ENGLISH.put("15MW", "15mw");
        CHINESE_TO_ENGLISH.put("6MW", "6mw");
        CHINESE_TO_ENGLISH.put("8MW", "8mw");
        CHINESE_TO_ENGLISH.put("陆基5MW", "lb5mw");
        CHINESE_TO_ENGLISH.put("空基5MW", "ab5mw");
        CHINESE_TO_ENGLISH.put("单晶硅", "mono");
        CHINESE_TO_ENGLISH.put("异质结", "hjt");
        CHINESE_TO_ENGLISH.put("钙钛矿", "psc");
        CHINESE_TO_ENGLISH.put("钙钛矿_硅叠层", "pcS");
        CHINESE_TO_ENGLISH.put("塔式", "tow");
        CHINESE_TO_ENGLISH.put("径流式", "ror");

        CHINESE_TO_ENGLISH.put("容量因子", "cf");
        CHINESE_TO_ENGLISH.put("发电潜力", "gp");
        CHINESE_TO_ENGLISH.put("装机潜力", "cp");
        CHINESE_TO_ENGLISH.put("平准化度电成本", "lcoe");
        CHINESE_TO_ENGLISH.put("发电量", "eg");
        CHINESE_TO_ENGLISH.put("发电厂", "pploc");
        CHINESE_TO_ENGLISH.put("装机量", "ic");
        CHINESE_TO_ENGLISH.put("输配电线路", "tdl");
        CHINESE_TO_ENGLISH.put("全社会用电量", "tec");
        CHINESE_TO_ENGLISH.put("人均社会用电量", "pcec");
        /*CHINESE_TO_ENGLISH.put("总电量消耗","tpc");
        CHINESE_TO_ENGLISH.put("陆上光伏发电", "onpv");*/

        
        CHINESE_TO_ENGLISH.put("小时", "hr");
        CHINESE_TO_ENGLISH.put("年", "yr");
        CHINESE_TO_ENGLISH.put("月", "mo");
        CHINESE_TO_ENGLISH.put("1km", "1km");
        CHINESE_TO_ENGLISH.put("10km", "10km");
        CHINESE_TO_ENGLISH.put("25km", "25km");
        CHINESE_TO_ENGLISH.put("100m", "100m");


        CHINESE_TO_ENGLISH.put("区域", "reg");
        CHINESE_TO_ENGLISH.put("次区域", "subreg");
        CHINESE_TO_ENGLISH.put("国家", "cntry");
        CHINESE_TO_ENGLISH.put("电网", "grid");
        CHINESE_TO_ENGLISH.put("省级", "prov");
        CHINESE_TO_ENGLISH.put("地级", "city");
        CHINESE_TO_ENGLISH.put("县级", "county");
    }


    //中英文切换
    public static final Map<String, String> Translation = new HashMap<>();

    static {
        Translation.put("发电侧", "Power generation side");
        Translation.put("电网侧", "Power grid side");
        Translation.put("用户侧", "User side");

        Translation.put("风力发电", "Wind power generation");
        Translation.put("低空风电", "Low-altitude wind power");
        Translation.put("海上风电", "Offshore wind power");
        Translation.put("陆上风电", "Onshore wind power");
        Translation.put("高空风电", "High-altitude wind power");
        Translation.put("光伏发电", "Photovoltaic power generation");
        Translation.put("陆上光伏发电", "Onshore photovoltaic power generation");
        Translation.put("光热发电", "Solar thermal power generation");

        Translation.put("潜力数据", "Potential data");
        Translation.put("现状数据", "Current data");


        Translation.put("发电潜力", "Power generation potential");
        Translation.put("容量因子", "Capacity factor");
        Translation.put("装机潜力", "Installation potential");

        Translation.put("发电量", "Power generation");
        Translation.put("装机量", "Installed capacity");
        Translation.put("电厂区位", "Location of power plant");


        Translation.put("小时", "hour");
        Translation.put("年", "year");
        Translation.put("月", "month");
        Translation.put("多年平均","Multi-year average");
    }
    public static final Map<String, String> Trans = new HashMap<>();

    static {
        Trans.put("Power generation side","发电侧");
        Trans.put("Power grid side","电网侧");
        Trans.put("User side","用户侧");

        Trans.put("Wind power generation","风力发电");
        Trans.put("Low-altitude wind power","低空风电");
        Trans.put("Offshore wind power","海上风电");
        Trans.put("Onshore wind power","陆上风电");
        Trans.put("High-altitude wind power","高空风电");
        Trans.put("Photovoltaic power generation","光伏发电");
        Trans.put("Onshore photovoltaic power generation","陆上光伏发电");
        Trans.put("Solar thermal power generation","光热发电");

        Trans.put("Potential data","潜力数据");
        Trans.put("Current data","现状数据");


        Trans.put("Power generation potential","发电潜力");
        Trans.put("Capacity factor","容量因子");
        Trans.put("Installation potential","装机潜力");

        Trans.put("Power generation","发电量");
        Trans.put("Installed capacity","装机量");
        Trans.put("Location of power plant","电厂区位");


        Trans.put("hour","小时");
        Trans.put("year","年");
        Trans.put("month","月");
        Trans.put("Multi-year average","多年平均");
    }
    public static String MapTranstoTableName(String map, String timeScale,  String level) {
        // 拆分 URL 路径
        String[] parts = map.split("/");
        StringBuilder tableNameBuilder = new StringBuilder();

        // 遍历每个部分并转换为英文,生成表名中种类部分
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            String englishPart = CHINESE_TO_ENGLISH.getOrDefault(part, part);
            if (i > 0) {
                tableNameBuilder.append("_");
            }
            tableNameBuilder.append(englishPart);
        }

        //表名加上时间分辨率部分
        tableNameBuilder.append("_").append(timeScale);

        // 表名加上level后缀，需要先中英文转换
        String englishLevel = CHINESE_TO_ENGLISH.getOrDefault(level, level);
        tableNameBuilder.append("_").append(englishLevel);


        return tableNameBuilder.toString();
    }

    public static List<List> convertDataFormat(List<Map<String,Object>> queryResult){

        List<List> resultData=new ArrayList<>();

        List<Map<String,Object>> SeriesData=new ArrayList<>();

        MultiValueMap<List<String>,Double> multiValueMap=new LinkedMultiValueMap<>();
        Set<String> timeList=new LinkedHashSet();


        //把查询来的数据按照一个地点对应的数值排好
        queryResult.forEach(row -> {
            List<String> name=new ArrayList<>();

            String place_china = (String) row.get("place_china");
            String place_english = (String) row.get("place_english");
            name.add(place_china);
            name.add(place_english);

            String t=row.get("time").toString();
            timeList.add(t);

            Double value = (Double) row.get("value");

            multiValueMap.add(name,value);

        });

        List<String> xAxisData=new ArrayList<>(timeList);

        for (Map.Entry m:multiValueMap.entrySet()){
            Map<String,Object> data=new HashMap<>();
            data.put("name",m.getKey());
            data.put("data",m.getValue());
            SeriesData.add(data);
        }

        resultData.add(xAxisData);
        resultData.add(SeriesData);

        return resultData;
    }



    public static String translateMapPath(String fullPath) {
        String[] parts = fullPath.split("/");

        // 保留前缀和后缀
        if (parts.length < 4) {
            throw new IllegalArgumentException("路径结构不合法");
        }

        StringBuilder translatedPath = new StringBuilder();

        // 重建路径
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) translatedPath.append("/");

            if (i <= 2 || i == parts.length - 1) {
                // 保留 /io/data 和最后的文件名
                translatedPath.append(parts[i]);
            } else {
                // 翻译中间部分
                translatedPath.append(Trans.getOrDefault(parts[i], parts[i]));
            }
        }

        return translatedPath.toString();
    }


}
