package com.cug.qhfg.service;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;


public interface FetchStatisticalDataService {
    String parseMapToTableName(String map,String timeScale,String year,boolean isGlobal,String level);

    List<LinkedHashMap<String, Object>> queryAllByTableName(String tableName);

    String parseTableNameToColumnName(String tableName);

    Map<String, Object> queryAll(String map,String timeScale,String year,boolean isGlobal,String level);

    Map<String, Object> queryByArea(String map,String timeScale,String year,boolean isGlobal,String level,String area);

    Map<String, Object> queryByTime(String map,String timeScale,String year,boolean isGlobal,String level,String time);

    // 前端数据格式转换
    Map<String, Object> convertDataFormat(List<LinkedHashMap<String, Object>> originalData,String columnName);





    //查找到对应数据,"年、月"时间分辨率
    List<Map<String, Object>> findTimeDataByPlaces(String tableName, List<String> places,List<String> times);


    //查找到对应数据"小时"时间分辨率
    //List<Map<String, Object>> findTimeDataByPlacesHour(String tableName, List<String> places,List<String> times) throws InterruptedException, ExecutionException;


    //查找到对应数据"小时"时间分辨率(改)
    List<List> getTimeDataByPlacesHour(String tableName, List<String> places,List<String> times) throws ExecutionException, InterruptedException;

    //获得前端所需格式
    Map<String,Object> getFrontForFormat(String map, String timeScale, List<String> timeSpan ,String level,List<String> areas) throws ExecutionException, InterruptedException;

    // 获取excel数据，通过读本地excel文件
    Map<String,Object> getExcelData(String map,String timeScale,String year, String level, String language) throws Exception;


}
