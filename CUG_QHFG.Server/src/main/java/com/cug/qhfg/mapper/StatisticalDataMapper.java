package com.cug.qhfg.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface StatisticalDataMapper {
    /**
     * 根据动态表名查询数据
     * @param tableName 动态生成的表名
     * @return 查询结果列表
     */
    @Select("SELECT * FROM ${tableName}")
    List<LinkedHashMap<String, Object>> queryAllByTableName(String tableName);


    /**
     * 根据动态表名和动态列名及对应值查询数据
     * @param tableName 动态生成的表名
     * @param columnName 动态列名，如 area, reg, cntry 等
     * @param area 列对应的值
     * @return 查询结果列表
     */
    @Select("SELECT * FROM ${tableName} WHERE \"${columnName}\" = #{area}")
    List<LinkedHashMap<String, Object>> queryByTableNameAndColumn(
            @Param("tableName") String tableName,
            @Param("columnName") String columnName,
            @Param("area") String area
    );

    /**
     *@Description 根据动态表名和动态列名及对应值查询数据，根据选择区域查询全部时间(查询‘年’、‘月’时间分辨率)
     *@Author fjh
     *@param tableName
     *@param places
     *@Return java.util.List<java.util.LinkedHashMap < java.lang.String, java.lang.Object>>
     */
    @Select("<script>" +
            "SELECT * FROM ${tableName} WHERE place_china IN " +
            "<foreach item='item' collection='places' open='(' separator=',' close=')'>" +
            "#{item}" +
            "</foreach>" +
            "ORDER BY time ASC"+
            "</script>"
    )
    List<Map<String,Object>> queryAllTimeByPlaces(@Param("tableName") String tableName,@Param("places") List<String> places);


    /**
     *@Description 根据动态表名和动态列名及查询数据，根据选择区域查询部分时间(查询‘年’、‘月’时间分辨率)
     *@Author fjh
     *@param tableName
     *@param places
     *@param times
     *@Return java.util.List<java.util.LinkedHashMap < java.lang.String, java.lang.Object>>
     */
    @Select("<script>" +
            "SELECT * FROM ${tableName} WHERE place_china IN " +
            "<foreach item='item' collection='places' open='(' separator=',' close=')'>" +
            "#{item}" +
            "</foreach>"+
            " AND time IN " +
            "<foreach item='item' collection='times' open='(' separator=',' close=')'>" +
            "#{item}" +
            "</foreach>" +
            "ORDER BY time ASC"+
            "</script>")
    List<Map<String,Object>> querySegTimeByplaces(@Param("tableName")String tableName,@Param("places") List<String> places,@Param("times") List<String> times);


    /**
     *@Description 根据动态表名和动态列名及对应值查询数据，根据选择区域查询全部时间(查询‘小时’时间分辨率)
     *@Author fjh
     *@param tableName
     *@param place
     *@Return java.util.List<java.util.LinkedHashMap < java.lang.String, java.lang.Object>>
     */
    @Select("SELECT place_china,place_english,to_char(time,'YYYY-MM-DD HH24') as time,value FROM ${tableName} WHERE place_china=#{place} ORDER BY time ASC")
    List<Map<String,Object>> queryAllTimeByPlacesHour(@Param("tableName") String tableName,@Param("place") String place);


    /**
     *@Description 根据动态表名和动态列名及查询数据，根据选择区域查询部分时间(查询‘小时‘时间分辨率)
     *@Author fjh
     *@param tableName
     *@param place
     *@param times
     *@Return java.util.List<java.util.LinkedHashMap < java.lang.String, java.lang.Object>>
     */
    @Select("SELECT place_china,place_english,to_char(time,'YYYY-MM-DD HH24') as time,value FROM ${tableName} WHERE place_china=#{place} and time between #{times[0]}::timestamp AND #{times[1]}::timestamp ORDER BY time ASC")
    List<Map<String,Object>> querySegTimeByplacesHour(@Param("tableName")String tableName,@Param("place") String place,@Param("times") List<String> times);


}
