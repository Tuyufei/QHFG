package com.cug.qhfg.service.impl;

import com.cug.qhfg.mapper.StatisticalDataMapper;
import com.cug.qhfg.service.FetchStatisticalDataService;
import com.cug.qhfg.util.utils.ExcelUtil;
import com.cug.qhfg.util.utils.TranstoUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

import static com.cug.qhfg.util.utils.TranstoUtils.translateMapPath;

@Service
@Slf4j
public class FetchStatisticalDataServiceImpl implements FetchStatisticalDataService {

    @Value("${ExcelBasePath}")
    private String excelBasePath;

    @Autowired
    private ExcelUtil excelUtil;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private StatisticalDataMapper statisticalDataMapper;

    @Autowired
    private Executor executor;

    // 中英文对照字典
    private static final Map<String, String> CHINESE_TO_ENGLISH = new HashMap<>();
    static {
        CHINESE_TO_ENGLISH.put("发电侧", "pg");
        CHINESE_TO_ENGLISH.put("光伏发电", "pv");
        CHINESE_TO_ENGLISH.put("陆上光伏发电", "onpv");
        CHINESE_TO_ENGLISH.put("潜力数据", "pot");
        CHINESE_TO_ENGLISH.put("单晶硅", "mono");
        CHINESE_TO_ENGLISH.put("发电潜力", "gp");
        CHINESE_TO_ENGLISH.put("容量因子", "cf");
        CHINESE_TO_ENGLISH.put("装机潜力", "cp");
        CHINESE_TO_ENGLISH.put("现状数据", "curr");
        CHINESE_TO_ENGLISH.put("发电量", "eg");
        CHINESE_TO_ENGLISH.put("电厂区位", "pploc");
        CHINESE_TO_ENGLISH.put("装机量", "ic");
        CHINESE_TO_ENGLISH.put("总电量消耗","tpc");
        CHINESE_TO_ENGLISH.put("光热发电", "csp");
        CHINESE_TO_ENGLISH.put("塔式", "tow");
        CHINESE_TO_ENGLISH.put("风力发电", "wind");
        CHINESE_TO_ENGLISH.put("低空风电", "low");
        CHINESE_TO_ENGLISH.put("海上风电", "off");
        CHINESE_TO_ENGLISH.put("陆上风电", "on");
        CHINESE_TO_ENGLISH.put("高空风电", "high");
        CHINESE_TO_ENGLISH.put("电网侧", "grid");
        CHINESE_TO_ENGLISH.put("用户侧", "cus");
        CHINESE_TO_ENGLISH.put("小时", "hr");
        CHINESE_TO_ENGLISH.put("年", "yr");
        CHINESE_TO_ENGLISH.put("月", "mo");
        CHINESE_TO_ENGLISH.put("10km", "10km");
        CHINESE_TO_ENGLISH.put("25km", "25km");
        CHINESE_TO_ENGLISH.put("100m", "100m");
        CHINESE_TO_ENGLISH.put("15MW", "15mw");
        CHINESE_TO_ENGLISH.put("6MW", "6mw");
        CHINESE_TO_ENGLISH.put("8MW", "8mw");
        CHINESE_TO_ENGLISH.put("2.5MW", "2_5mw");
        CHINESE_TO_ENGLISH.put("5MW", "5mw");
        CHINESE_TO_ENGLISH.put("6.5MW", "6_5mw");
        CHINESE_TO_ENGLISH.put("区域", "reg");
        CHINESE_TO_ENGLISH.put("次区域", "subreg");
        CHINESE_TO_ENGLISH.put("国家", "cntry");
        CHINESE_TO_ENGLISH.put("电网", "grid");
        CHINESE_TO_ENGLISH.put("省级", "prov");
        CHINESE_TO_ENGLISH.put("地级", "city");
        CHINESE_TO_ENGLISH.put("县级", "county");
    }

// 从中文列的列名，去获取英文列的列名
    private static final Map<String, String> COLUMN_NAME_MAPPING = new HashMap<>();
    static {
        // 初始化映射字典
        COLUMN_NAME_MAPPING.put("cntry", "Country");
        COLUMN_NAME_MAPPING.put("reg", "Regions");
        COLUMN_NAME_MAPPING.put("city", "Prefecture");
        COLUMN_NAME_MAPPING.put("county", "County");
        COLUMN_NAME_MAPPING.put("subreg", "Subregions");
        COLUMN_NAME_MAPPING.put("grid", "Grid");
        COLUMN_NAME_MAPPING.put("prov", "Province");

    }

    @Override
    public String parseMapToTableName(String map,String timeScale,String year,boolean isGlobal,String level) {
        // 拆分 URL 路径
        String[] parts = map.split("/");
        StringBuilder tableNameBuilder = new StringBuilder();

        // 遍历每个部分并转换为英文
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            String englishPart = CHINESE_TO_ENGLISH.getOrDefault(part, part);
            if (i > 0) {
                tableNameBuilder.append("_");
            }
            tableNameBuilder.append(englishPart);
        }

        // 添加时间尺度后缀,需要先中英文转换
        String englishTimeScale = CHINESE_TO_ENGLISH.getOrDefault(timeScale, timeScale);
        tableNameBuilder.append("_").append(englishTimeScale);
        // 添加年份后缀,仅当时间尺度为小时或月份的时候才加年份后缀
        if (englishTimeScale.equals("hr") || englishTimeScale.equals("mo")) {
            tableNameBuilder.append("_").append(year);
        }

        // 如果是全球数据，添加global后缀，否则添加china后缀
        if (isGlobal) {
            tableNameBuilder.append("_global");
        }else {
            tableNameBuilder.append("_china");
        }

        // 添加level后缀，需要先中英文转换
        String englishLevel = CHINESE_TO_ENGLISH.getOrDefault(level, level);
        tableNameBuilder.append("_").append(englishLevel);


        return tableNameBuilder.toString();
    }

    @Override
    public List<LinkedHashMap<String, Object>> queryAllByTableName(String tableName) {
        return statisticalDataMapper.queryAllByTableName(tableName);

    }

    @Override
    public String parseTableNameToColumnName(String tableName) {
        //获取tableName中的最后一个_
        int index = tableName.lastIndexOf("_");
        //截取最后一个_后面的字符串
        return tableName.substring(index + 1);
    }

    @Override
    public Map<String, Object> queryAll(String map, String timeScale, String year, boolean isGlobal, String level) {

//        // 生成缓存键
//        String cacheKey = "queryAll:" + map + ":" + timeScale + ":" + year + ":" + isGlobal + ":" + level;
//
//        // 先从 Redis 中获取数据
//        String cachedData = stringRedisTemplate.opsForValue().get(cacheKey);
//        if (cachedData != null) {
//            return JSON.parseObject(cachedData, Map.class);
//        }

        // 如果 Redis 中没有数据，则从数据库中获取
        List<LinkedHashMap<String, Object>> maps = statisticalDataMapper.queryAllByTableName((parseMapToTableName(map, timeScale, year, isGlobal, level)));
        Map<String, Object> convertedMap = convertDataFormat(maps, parseTableNameToColumnName(parseMapToTableName(map, timeScale, year, isGlobal, level)));

//        // 将结果存入 Redis，设置过期时间为 1 小时
//        stringRedisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(convertedMap));
//        stringRedisTemplate.expire(cacheKey, 3600, java.util.concurrent.TimeUnit.SECONDS);

        return convertedMap;
    }

    @Override
    public Map<String, Object> queryByArea(String map,String timeScale,String year,
                                                                boolean isGlobal,String level, String area)
    {
        String tableName = parseMapToTableName(map, timeScale, year, isGlobal, level);
        String columnName = parseTableNameToColumnName(tableName);
        List<LinkedHashMap<String, Object>> maps = statisticalDataMapper.queryByTableNameAndColumn(tableName, columnName, area);
        return convertDataFormat(maps,columnName);

    }

    @Override
    public Map<String, Object> queryByTime(String map, String timeScale, String year, boolean isGlobal, String level, String time) {
        return Collections.emptyMap();
    }

//    @Override
//    public Map<String, Object> convertDataFormat(List<Map<String, Object>> originalData,String columnName) {
//        Map<String, Object> result = new HashMap<>();
//        List<String> xAxisData = new ArrayList<>();
//        List<Map<String, Object>> seriesData = new ArrayList<>();
//
//        // 获取对应的列名
//        String enColumnName = COLUMN_NAME_MAPPING.getOrDefault(columnName, "");
//
//        // 提取 xAxisData
//        if (!originalData.isEmpty()) {
//            Map<String, Object> firstItem = originalData.get(0);
//            for (String key : firstItem.keySet()) {
//                if (!key.equals(columnName) && !key.equals(enColumnName)) {
//                    xAxisData.add(key);
//                }
//            }
//        }
//
//        // 提取 SeriesData
//        for (Map<String, Object> item : originalData) {
//            Map<String, Object> seriesItem = new HashMap<>();
//            List<String> names = new ArrayList<>();
//            names.add((String) item.get(columnName));
//            names.add((String) item.get(enColumnName));
//            seriesItem.put("name", names);
//
//            List<Double> dataValues = new ArrayList<>();
//            for (String xAxis : xAxisData) {
//                Object value = item.get(xAxis);
//                if (value != null) {
//                    try {
//                        dataValues.add(Double.parseDouble(value.toString()));
//                    } catch (NumberFormatException e) {
//                        // 处理无法转换为 Double 的情况
//                        dataValues.add(null);
//                    }
//                } else {
//                    dataValues.add(null);
//                }
//            }
//            seriesItem.put("data", dataValues);
//
//            seriesData.add(seriesItem);
//        }
//
//        result.put("xAxisData", xAxisData);
//        result.put("SeriesData", seriesData);
//        return result;
//    }
    @Override
    public Map<String, Object> convertDataFormat(List<LinkedHashMap<String, Object>> originalData, String columnName) {
        Map<String, Object> result = new LinkedHashMap<>();
        List<String> xAxisData = new ArrayList<>();
        List<Map<String, Object>> seriesData = new ArrayList<>();

        // 获取对应的列名
        String enColumnName = COLUMN_NAME_MAPPING.getOrDefault(columnName, "");

        // 提取 xAxisData
        if (!originalData.isEmpty()) {
            Map<String, Object> firstItem = originalData.get(0);
            for (String key : firstItem.keySet()) {
                if (!key.equals(columnName) && !key.equals(enColumnName)) {
                    xAxisData.add(key);
                }
            }
        }

        // 提取 SeriesData
        for (Map<String, Object> item : originalData) {
            Map<String, Object> seriesItem = new HashMap<>();
            List<String> names = new ArrayList<>();
            names.add((String) item.get(columnName));
            names.add((String) item.get(enColumnName));
            seriesItem.put("name", names);

            List<Double> dataValues = new ArrayList<>();
            for (String xAxis : xAxisData) {
                Object value = item.get(xAxis);
                if (value != null) {
                    try {
                        dataValues.add(Double.parseDouble(value.toString()));
                    } catch (NumberFormatException e) {
                        // 处理无法转换为 Double 的情况
                        dataValues.add(null);
                    }
                } else {
                    dataValues.add(null);
                }
            }
            seriesItem.put("data", dataValues);

            seriesData.add(seriesItem);
        }

        result.put("xAxisData", xAxisData);
        result.put("SeriesData", seriesData);
        return result;
    }

    /**
     *@Description 查找到对应数据"年、月"时间分辨率
     *@Author fjh
     *@param tableName
     *@param places
     *@param times
     *@Return java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     */
    @Override
    public List<Map<String, Object>> findTimeDataByPlaces(String tableName, List<String> places,List<String> times) {
        List<Map<String, Object>> queryResult=new ArrayList<>();

        if (times.size()==0){
            queryResult=statisticalDataMapper.queryAllTimeByPlaces(tableName,places);
        }else {
            queryResult=statisticalDataMapper.querySegTimeByplaces(tableName,places,times);
        }
        return queryResult;
    }


//    @Override
//    public List<Map<String, Object>> findTimeDataByPlacesHour(String tableName, List<String> places,List<String> times) throws InterruptedException, ExecutionException {
//        List<Future<List<Map<String, Object>>>> futures=new ArrayList<>();
//
//        if (times.size()==0){
//            for (String place:places){
//                Future<List<Map<String, Object>>> future=
//                        CompletableFuture.supplyAsync(() -> statisticalDataMapper.queryAllTimeByPlacesHour(tableName,place), executor);
//                futures.add(future);
//            }
//        }else {
//            for (String place:places){
//                Future<List<Map<String, Object>>> future=
//                        CompletableFuture.supplyAsync(() -> statisticalDataMapper.querySegTimeByplacesHour(tableName,place,times), executor);
//                futures.add(future);
//            }
//        }
//
//        // 等待所有查询完成
//        List<Map<String, Object>> result = new ArrayList<>();
//        for (Future<List<Map<String, Object>>> future : futures) {
//            result.addAll(future.get());
//        }
//        return result;
//    }

    @Override
    public List<List> getTimeDataByPlacesHour(String tableName, List<String> places,List<String> times) throws ExecutionException, InterruptedException {

        //得到的xAxisData和SeriesData
        List<List> resultData=new ArrayList<>();


        List<CompletableFuture<Void>> futures=new ArrayList<>();
        ConcurrentHashMap<List<String>,List<Double>> multivalueMap=new ConcurrentHashMap<>();
        Set<String> timeSet=new ConcurrentSkipListSet();

        for (String place:places){
            CompletableFuture<Void> future=CompletableFuture.supplyAsync(() -> {
                List<Map<String, Object>> rows;

                if (times.size()==0){
                    rows=statisticalDataMapper.queryAllTimeByPlacesHour(tableName,place);
                }else {
                    rows=statisticalDataMapper.querySegTimeByplacesHour(tableName,place,times);
                }

                for (Map<String,Object> row:rows){
                    List<String> name=new ArrayList<>();
                    name.add((String) row.get("place_china"));
                    name.add((String) row.get("place_english"));

                    String time = row.get("time").toString();
                    Double value = (Double) row.get("value");

                    timeSet.add(time);
                    multivalueMap.computeIfAbsent(name,k->new CopyOnWriteArrayList<>()).add(value);
                }
                return null;
            },executor);

            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();


        List<String> xAxisData = new ArrayList<>(timeSet); // 横坐标时间

        List<Map<String, Object>> seriesData = new ArrayList<>();
        for (Map.Entry<List<String>, List<Double>> entry : multivalueMap.entrySet()) {
            Map<String, Object> data = new HashMap<>();
            data.put("name", entry.getKey());
            data.put("data", entry.getValue());
            seriesData.add(data);
        }

        resultData.add(xAxisData);
        resultData.add(seriesData);

        return resultData;
    }


    /**
     *@Description 返回前端所需的格式
     *@Author fjh
     *@param map
     *@param timeScale
     *@param timeSpan
     *@param level
     *@param places
     *@Return java.util.Map<java.lang.String, java.lang.Object>
     */
    @Override
    public Map<String, Object> getFrontForFormat(String map, String timeScale, List<String> timeSpan ,String level,List<String> places) throws ExecutionException, InterruptedException {
        Map<String,Object> result=new HashMap<>();

        //获得的map转换成表名
        String tableName=TranstoUtils.MapTranstoTableName(map,timeScale,level);

        //查询出数据库中对应的结果
        List<Map<String, Object>> res=new ArrayList<>();

        List<List> resultData=new ArrayList<>();
        if (timeScale.startsWith("hr")){
            //查询“小时”的
            /*res=findTimeDataByPlacesHour(tableName,places,timeSpan);
            resultData=TranstoUtils.convertDataFormat(res);*/
            //把数据库的结果转换成前端所需要的格式
            resultData=getTimeDataByPlacesHour(tableName,places,timeSpan);

        }else {
            //查询“年”和“月”的
            res=findTimeDataByPlaces(tableName,places,timeSpan);
            //把数据库的结果转换成前端所需要的格式
            resultData=TranstoUtils.convertDataFormat(res);
        }


        //得到结果
        List<String> xAxisData=resultData.get(0);
        List<Map<String,Object>> SeriesData=resultData.get(1);

        result.put("xAxisData",xAxisData);
        result.put("SeriesData",SeriesData);

        return result;
    }

    @Override
    public Map<String, Object> getExcelData(String map,String timeScale,String year, String level, String language) throws Exception {

//        // 添加语言支持
//        if ("english".equals(language)) {
//            // 处理英文路径 MAP前面加上/io/data/
//            String newMap = "/io/data/" + map;
//            String translatedMap = translateMapPath(newMap);
//
//            if (translatedMap != null && translatedMap.length() >= 9) {
//                // 去掉前面的/io/data/
//                map = translatedMap.substring(9);
//            } else {
//                // 处理翻译结果不符合预期的情况
//                log.warn("translateMapPath 返回结果长度不足 9 或为 null，使用原始 map: {}", map);
//            }
//        }

        // 根据 level 推断 scope
        String scope;
        if ("国家".equals(level) || "区域".equals(level)|| "次区域".equals(level)) {
            scope = "Global";
        } else if ("县级".equals(level) || "地级".equals(level)|| "省级".equals(level)|| "电网".equals(level)) {
            scope = "China";
        } else {
            // 可以根据实际情况添加默认值或者抛出异常
            throw new IllegalArgumentException("无法根据当前 level 值推断 scope: " + level);
        }

        String filePath;
        if(timeScale.equals("年")){
            filePath = excelBasePath + map + "/" +timeScale + "/" + scope + ".xlsx";
        }
        else {
            filePath = excelBasePath + map + "/" +timeScale + "/" + year + "/" + scope + ".xlsx";
        }

        Workbook workbook = excelUtil.getWorkBook(filePath);
        List<Sheet> sheetlist = excelUtil.getRegionSheetsByPrefix(workbook, level);
        Map<String, Object> data = excelUtil.getSheetData(sheetlist.get(0));

        // 获取数据列表和列名列表
        List<Map<String, Object>> dataList = (List<Map<String, Object>>) data.get("data");
        List<String> columns = (List<String>) data.get("columns");

        if (dataList != null && columns != null) {
            List<String> columnsToRemove = new ArrayList<>();
            for (String column : columns) {
                boolean hasChinese = containsChinese(column);
                boolean hasEnglish = containsEnglish(column);
                // 只处理包含中文或英文的列名
                if (hasChinese || hasEnglish) {
                    if ("chinese".equals(language) && hasEnglish) {
                        columnsToRemove.add(column);
                    } else if ("english".equals(language) && hasChinese) {
                        columnsToRemove.add(column);
                    }
                }
            }

            // 移除列名和对应的数据
            for (String columnToRemove : columnsToRemove) {
                columns.remove(columnToRemove);
                for (Map<String, Object> row : dataList) {
                    row.remove(columnToRemove);
                }
            }
        }

        return data;
    }

    /**
     * 判断字符串是否包含中文字符
     * @param str 待判断的字符串
     * @return 如果包含中文字符返回 true，否则返回 false
     */
    private boolean containsChinese(String str) {
        for (char c : str.toCharArray()) {
            if (Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断字符串是否只包含英文字符
     * @param str 待判断的字符串
     * @return 如果只包含英文字符返回 true，否则返回 false
     */
    private boolean containsEnglish(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        for (char c : str.toCharArray()) {
            if (!((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z'))) {
                return false;
            }
        }
        return true;
    }


}
