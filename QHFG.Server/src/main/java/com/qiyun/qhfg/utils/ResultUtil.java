package com.qiyun.qhfg.utils;

import cn.dev33.satoken.util.SaResult;
import com.alibaba.fastjson.JSON;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @PACKAGE_NAME: com.example.fxpgJava.utils
 * @NAME: ResultUtil 返回结果包装类
 * @USER: spike
 * @DATE: 2023/4/10 11:39
 * @PROJECT_NAME: FXPG_Java
 */

public class ResultUtil {

    //    令牌
    public final static String TOKEN = "token";

    //    页码
    public final static String PAGE_INDEX = "PageIndex";

    public final static String SMALL_PAGE_INDEX = "pageIndex";

    //    页内行数
    public final static String PAGE_SIZE = "PageSize";

    public final static String SMALL_PAGE_SIZE = "pageSize";

    //    总数
    public final static String TOTAL = "total";
    //    省市区
    public static final String PROVINCE = "province";
    public static final String CITY = "city";
    public static final String COUNTY = "county";

    public static final String TABLE_NAME_HEAD = "table_name_head";

    public static final String EXCEL_TEMPLATE = "excelTemplate";

    public static SaResult initResult(SaResult saResult) {
        saResult.set("error", "");
        saResult.set("warn", "");
        saResult.set("log", null);
        saResult.set("message", "");
        saResult.set("oper_id", null);
        saResult.set("oper_name", null);
        return saResult;
    }

    public static SaResult data(Object data) {
        SaResult saResult = SaResult.data(data);
        saResult = ResultUtil.initResult(saResult);
        saResult.set("status", "success");
        saResult.set("success", true);
        return saResult;
    }

    public static SaResult error(String msg) {
        SaResult saResult = SaResult.error(msg);
        saResult = ResultUtil.initResult(saResult);
        saResult.set("status", "error");
        saResult.set("success", false);
        return saResult;
    }

    public static SaResult setTotal(SaResult saResult, List<Map<String, Object>> list) {
        // 总数初始值
        Long total = 0L;
        if (!list.isEmpty()) {
            // 从list中获取第一个的总数
            total = (Long) list.get(0).get(ResultUtil.TOTAL);
        }
        saResult.set(ResultUtil.TOTAL, total);
        for (Map<String, Object> map : list) {
            map.remove(ResultUtil.TOTAL);
        }
        return saResult;
    }

    public static Map<String, Object> putCountMap(List<Map<String, Object>> list, String fieldName) {
        Map<String, Object> tempMap = new HashMap<>();
        for (Map<String, Object> map : list) {
            String createTime = map.get("createTime").toString();
            String field = map.get(fieldName).toString();
            Long num = (Long) map.get("num");
            Map<String, Object> dataMap;
            if (tempMap.get(createTime) != null) {
                dataMap = (Map<String, Object>) tempMap.get(createTime);
                dataMap.put(field, num);
            } else {
                dataMap = new HashMap<>();
                dataMap.put(field, num);
                tempMap.put(createTime, dataMap);
            }
        }
        return tempMap;
    }

    public static List<Map<String, Object>> orderTableHead(List<Map<String, Object>> tableHeadList, String[] orderList) {
        List<Map<String, Object>> headList = new ArrayList<>();
        Map<String, Object> tableHeadMap = new HashMap<>();
        for (Map<String, Object> map : tableHeadList) {
            String label = (String) map.get(ExcelUtil.LABEL);
            map.put("isActive", true);
            tableHeadMap.put(label, map);
        }
        for (String key : orderList) {
            Map<String, Object> tempMap = (Map<String, Object>) tableHeadMap.get(key);
            if (tempMap == null) {
                continue;
            }
            headList.add(tempMap);
            tableHeadMap.remove(key);
        }
        for (Object map : tableHeadMap.values()) {
            headList.add((Map<String, Object>) map);
        }
        return headList;
    }

    public static List<Map> getDisProcessList() {
        String jsonStr = "[\n" +
                "    {\"disName\":\"冷空气\",\"disType\":\"coldair\"},\n" +
                "    {\"disName\":\"干旱\",\"disType\":\"drought\"},\n" +
                "    {\"disName\":\"霜冻\",\"disType\":\"frost\"},\n" +
                "    {\"disName\":\"冰冻\",\"disType\":\"frozen\"},\n" +
                "    {\"disName\":\"高温\",\"disType\":\"hightemp\"},\n" +
                "    {\"disName\":\"暴雨\",\"disType\":\"rainstorm\"},\n" +
                "    {\"disName\":\"暴雪\",\"disType\":\"snowstorm\"},\n" +
                "    {\"disName\":\"雷电\",\"disType\":\"thunder\"}\n" +
                "]";
        List<Map> mapList = JSON.parseArray(jsonStr, Map.class);
        return mapList;
    }

    public static SaResult setResultPageInfo(List<Map<String, Object>> listMap, Integer pageIndex, Integer pageSize) {
        SaResult saResult = ResultUtil.data(listMap);
        saResult.set(ResultUtil.PAGE_INDEX, pageIndex);
        saResult.set(ResultUtil.PAGE_SIZE, pageSize);
        saResult = ResultUtil.setTotal(saResult, listMap);
        return saResult;
    }

    public static List<Map> getDisasterDetailsHead() {
        String jsonStr = "[\n" +
                "    {\"label\":\"唯一idMD5\",\"key\":\"id\"},\n" +
                "    {\"label\":\"记录id\",\"key\":\"drecordid\"},\n" +
                "    {\"label\":\"发布单位\",\"key\":\"vsendername\"},\n" +
                "    {\"label\":\"启动时间\",\"key\":\"vstarttime\"},\n" +
                "    {\"label\":\"灾害类型\",\"key\":\"maintypename\"},\n" +
                "    {\"label\":\"直接经济损失\",\"key\":\"vgeneralloss\"},\n" +
                "    {\"label\":\"农业经济损失\",\"key\":\"vafcsloss\"},\n" +
                "    {\"label\":\"受灾总人数\",\"key\":\"vrzapop\"},\n" +
                "    {\"label\":\"天气过程描述\",\"key\":\"vsummary\"},\n" +
                "    {\"label\":\"二级灾害\",\"key\":\"secondtypename\"},\n" +
                "    {\"label\":\"城市名称\",\"key\":\"cityname\"},\n" +
                "    {\"label\":\"县\",\"key\":\"county\"},\n" +
                "    {\"label\":\"开始时间\",\"key\":\"starttime\"},\n" +
                "    {\"label\":\"结束时间\",\"key\":\"endtime\"}\n" +
                "]";
        List<Map> mapList = JSON.parseArray(jsonStr, Map.class);
        return mapList;
    }

    public static List<Map> getWorkWarnRainProductHead() {
        String jsonStr = "[\n" +
                "    {\"label\":\"资源文件创建时间\",\"key\":\"createtime\"},\n" +
                "    {\"label\":\"结果集\",\"key\":\"work_file\"},\n" +
                "    {\"label\":\"评估开始时间\",\"key\":\"starttime\"},\n" +
                "    {\"label\":\"评估结束时间\",\"key\":\"endtime\"},\n" +
                "    {\"label\":\"评估区域\",\"key\":\"area\"},\n" +
                "    {\"label\":\"评估参数\",\"key\":\"param\"},\n" +
                "    {\"label\":\"用户\",\"key\":\"user\"}\n" +
                "]";
        List<Map> mapList = JSON.parseArray(jsonStr, Map.class);
        return mapList;
    }

    public static List<Map> getDisProcessRainstormHead() {
        String jsonStr = "[\n" +
                "    {\"label\":\"站号\",\"key\":\"iiiii\"},\n" +
                "    {\"label\":\"过程开始时间\",\"key\":\"start_time\"},\n" +
                "    {\"label\":\"过程结束时间\",\"key\":\"end_time\"},\n" +
                "    {\"label\":\"持续时间\",\"key\":\"duration\"},\n" +
                "    {\"label\":\"过程累积降水量（mm）\",\"key\":\"pre_process\"},\n" +
                "    {\"label\":\"平均降水量\",\"key\":\"pre_avg\"},\n" +
                "    {\"label\":\"1小时降水量最大值（mm）\",\"key\":\"pre_max_1h\"},\n" +
                "    {\"label\":\"3小时降水量最大值（mm）\",\"key\":\"pre_max_3h\"},\n" +
                "    {\"label\":\"6小时降水量最大值（mm）\",\"key\":\"pre_max_6h\"},\n" +
                "    {\"label\":\"12小时降水量最大值（mm）\",\"key\":\"pre_max_12h\"},\n" +
                "    {\"label\":\"事件标识\",\"key\":\"event_mark\"}\n" +
                "]";
        List<Map> mapList = JSON.parseArray(jsonStr, Map.class);
        return mapList;
    }

    public static Map<String, String> getProvinceCityCounty(String region) {
        String province = null;
        String city = null;
        String county = null;
        if (region.split(",").length == 3) {
            province = region.split(",")[0];
            city = region.split(",")[1];
            county = region.split(",")[2];
        }
        if (region.split(",").length == 2) {
            String temp = "湖北省,全省";
            if (region.equals(temp)) {
                province = region.split(",")[0];
            } else {
                province = region.split(",")[0];
                city = region.split(",")[1];
            }
        }
        if (region.split(",").length == 1) {
            province = region.split(",")[0];
        }
        Map<String, String> map = new HashMap<>();
        map.put(PROVINCE, province);
        map.put(CITY, city);
        map.put(COUNTY, county);
        return map;
    }

    /**
     * 获取相对路径
     *
     * @param path
     * @return
     */
    public static String getResourcesPath(String path) {
        String realPath;
        //linux window兼容
        if (System.getProperty("user.dir").substring(0, 1).equals(File.separator)) {
            realPath = System.getProperty("user.dir") + File.separator + path;
        } else {
            realPath = System.getProperty("user.dir") + "\\src\\main\\resources\\" + path;
        }
        return realPath;
    }

    public static Map<String, Object> getYearStrAndMonthsMap(String cycle, String time, String yearStart, String yearEnd) {
        List<String> months = new ArrayList<>();
        if (cycle.equals("month")) {
            int month = Integer.valueOf(time);
            if (time.length() < 2) {
                time = "0" + time;
            }
            months.add(time);
            yearStart += time + "01";
            String monthStr = String.valueOf(month + 1);
            if (monthStr.length() < 2) {
                monthStr = "0" + monthStr;
            }
            yearEnd += monthStr + "01";
        }
        if (cycle.equals("season")) {
            switch (time) {
                case "春":
                    yearStart += "0301";
                    yearEnd += "0601";
                    months.add("03");
                    months.add("04");
                    months.add("05");
                    break;
                case "夏":
                    yearStart += "0601";
                    yearEnd += "0901";
                    months.add("06");
                    months.add("07");
                    months.add("08");
                    break;
                case "秋":
                    yearStart += "0901";
                    yearEnd += "1201";
                    months.add("09");
                    months.add("10");
                    months.add("11");
                    break;
                case "冬":
                    yearStart += "1201";
                    yearEnd += "0301";
                    months.add("12");
                    months.add("01");
                    months.add("02");
                    break;
            }
        }
        if (cycle.equals("year")) {
            yearStart += "0101";
            Integer tempNum = Integer.valueOf(yearEnd) + 1;
            yearEnd = tempNum.toString() + "0101";
            months = null;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("yearStart", yearStart);
        map.put("yearEnd", yearEnd);
        map.put("months", months);
        return map;
    }

    /**
     * 计算对比条件月份
     *
     * @param season
     * @param month
     * @return
     */
    public static Map<String, Object> getSeasonMonth(String season, String month) {
        List<String> seasonMonths = new ArrayList<>();
        List<String> seasonStrList = Arrays.asList(season.split(","));
        for (String seasonStr : seasonStrList) {
            switch (seasonStr) {
                case "春":
                    seasonMonths.add("03");
                    seasonMonths.add("04");
                    seasonMonths.add("05");
                    break;
                case "夏":
                    seasonMonths.add("06");
                    seasonMonths.add("07");
                    seasonMonths.add("08");
                    break;
                case "秋":
                    seasonMonths.add("09");
                    seasonMonths.add("10");
                    seasonMonths.add("11");
                    break;
                case "冬":
                    seasonMonths.add("12");
                    seasonMonths.add("01");
                    seasonMonths.add("02");
                    break;
            }
        }
        List<String> months = new ArrayList<>();
        List<String> monthStrList = Arrays.asList(month.split(","));
        for (String monthStr : monthStrList) {
            if (monthStr.length() < 2) {
                monthStr = "0" + monthStr;
            }
            months.add(monthStr);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("seasonMonths", seasonMonths);
        map.put("months", months);
        return map;
    }

    public static Map<String, Object> getYearAndMonthMap(String cycle, String time, String yearStart, String yearEnd) {
        List<String> months = new ArrayList<>();
        if (cycle.equals("month")) {
            List<String> monthStrList = Arrays.asList(time.split(","));
            for (String monthStr : monthStrList) {
                if (monthStr.length() < 2) {
                    monthStr = "0" + monthStr;
                }
                months.add(monthStr);
            }
        }
        if (cycle.equals("season")) {
            List<String> seasonStrList = Arrays.asList(time.split(","));
            for (String season : seasonStrList) {
                switch (season) {
                    case "春":
                        months.add("03");
                        months.add("04");
                        months.add("05");
                        break;
                    case "夏":
                        months.add("06");
                        months.add("07");
                        months.add("08");
                        break;
                    case "秋":
                        months.add("09");
                        months.add("10");
                        months.add("11");
                        break;
                    case "冬":
                        months.add("12");
                        months.add("01");
                        months.add("02");
                        break;
                }
            }
        }
        if (cycle.equals("year")) {
            months = null;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("yearStart", yearStart);
        map.put("yearEnd", yearEnd);
        map.put("months", months);
        return map;
    }

    public static BigDecimal ROUND_HALF_UP(BigDecimal value, int scale) {
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            BigDecimal integer = value.setScale(scale, BigDecimal.ROUND_DOWN);
            BigDecimal half = integer.add(BigDecimal.valueOf(-0.5));
            if (value.compareTo(half) >= 0) {
                value = value.setScale(scale, BigDecimal.ROUND_DOWN);
            } else {
                value = value.setScale(scale, BigDecimal.ROUND_UP);
            }
        } else {
            value = value.setScale(scale, BigDecimal.ROUND_HALF_UP);
        }
        return value;
    }
}
