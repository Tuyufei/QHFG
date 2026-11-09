package com.cug.qhfg.util.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;


/**
 * @author spike
 * @date 2022/4/29 14:43
 */
@Slf4j
@Component
public class ExcelUtil {


    /**
     * @param filePath
     * @Description 获取excel表
     * @Author fjh
     * @Return org.apache.poi.ss.usermodel.Workbook
     */
    public static Workbook getWorkBook(String filePath) throws Exception {
        File file = new File(filePath);
        try (FileInputStream fis = new FileInputStream(file)) {
            if (filePath.endsWith(".xls")) {
                return new HSSFWorkbook(fis);
            } else if (filePath.endsWith(".xlsx")) {
                return new XSSFWorkbook(fis);
            } else {
                throw new IllegalArgumentException("Unsupported file format");
            }
        }
    }

    /**
     * @param workbook
     * @param sheetName
     * @Description 获取对应区域sheet
     * @Author fjh
     * @Return org.apache.poi.ss.usermodel.Sheet
     */
    public static Sheet getRegionSheet(Workbook workbook, String sheetName) {
        if (workbook == null || sheetName == null || sheetName.trim().isEmpty()) {
            throw new IllegalArgumentException("Workbook 或 Sheet 名称为空");
        }
        return workbook.getSheet(sheetName);
    }

    // 通过索引获取工作表
    public static Sheet getRegionSheet(Workbook workbook, int sheetIndex) {
        if (workbook == null) {
            throw new IllegalArgumentException("Workbook 为空");
        }
        if (sheetIndex < 0 || sheetIndex >= workbook.getNumberOfSheets()) {
            throw new IllegalArgumentException("Sheet 索引超出范围");
        }
        return workbook.getSheetAt(sheetIndex);
    }

    // 通过前缀匹配 sheetName 获取工作表列表
    public static List<Sheet> getRegionSheetsByPrefix(Workbook workbook, String prefix) {
        if (workbook == null || prefix == null || prefix.trim().isEmpty()) {
            throw new IllegalArgumentException("Workbook 或前缀为空");
        }

        List<Sheet> matchingSheets = new ArrayList<>();
        int numberOfSheets = workbook.getNumberOfSheets();
        for (int i = 0; i < numberOfSheets; i++) {
            Sheet sheet = workbook.getSheetAt(i);
            String actualSheetName = sheet.getSheetName();
            if (actualSheetName.startsWith(prefix)) {
                matchingSheets.add(sheet);
            }
        }
        return matchingSheets;
    }


    /**
     *@Description 获取默认七大区域的数据
     *@Author fjh
     *@param sheet
     *@Return java.util.Map<java.util.List < java.lang.String>,java.util.List<java.lang.Object>>
     */

    public static Map<String, List<Object>> getDefaultRegion(Sheet sheet) {

        List<Object> value = new ArrayList<>();

        Map<String, List<Object>> result = new LinkedHashMap<>();
        Row firstRow = sheet.getRow(0);
        if (firstRow != null) {
            for (int i = 2; i < firstRow.getLastCellNum(); i++) {
                Cell cell = firstRow.getCell(i);
                if (cell != null) {
                    value.add(String.valueOf(cell));
                }
            }
        }
        result.put("xAxisData", value);

        List<Object> vvalue=new ArrayList<>();


        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Map<String,List<Object>> map=new LinkedHashMap<>();
            List<Object> vl1=new ArrayList<>();

            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue; // 跳过空行
            }
            for (int i = 0; i < 2; i++) {
                Cell cell = row.getCell(i);
                vl1.add(String.valueOf(cell));
            }
            map.put("name",vl1);

            List<Object> vl2=new ArrayList<>();
            for (int i = 2; i < row.getLastCellNum(); i++) {
                Cell cell = row.getCell(i);
                if (cell != null) {
                    vl2.add(Float.valueOf(String.valueOf(cell)));
                }
            }
            map.put("data",vl2);

            vvalue.add(map);
        }

        result.put("SeriesData",vvalue);


        return result;
    }
    /**
     *@Description 获取选择的区域的数据
     *@Author fjh
     *@param sheet
     *@param regionset 选择的区域以Set存储
     *@Return java.util.Map<java.util.List < java.lang.String>,java.util.List<java.lang.Object>>
     */
    public static Map<String, List<Object>> getRegionData(Sheet sheet, Set regionset) {

        List<Object> value = new ArrayList<>();

        Map<String, List<Object>> result = new LinkedHashMap<>();
        Row firstRow = sheet.getRow(0);
        if (firstRow != null) {
            for (int i = 2; i < firstRow.getLastCellNum(); i++) {
                Cell cell = firstRow.getCell(i);
                if (cell != null) {
                    value.add(String.valueOf(cell));
                }
            }
        }
        result.put("xAxisData", value);

        List<Object> vvalue = new ArrayList<>();

        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Cell cellvalue = sheet.getRow(rowIndex).getCell(0); // 获取第一列的值
            if (regionset.contains(String.valueOf(cellvalue))){
                Map<String, List<Object>> map = new LinkedHashMap<>();
                List<Object> vl1 = new ArrayList<>();

                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue; // 跳过空行
                }
                for (int i = 0; i < 2; i++) {
                    Cell cell = row.getCell(i);
                    vl1.add(String.valueOf(cell));
                }
                map.put("name", vl1);

                List<Object> vl2 = new ArrayList<>();
                for (int i = 2; i < row.getLastCellNum(); i++) {
                    Cell cell = row.getCell(i);
                    if (cell != null) {
                        vl2.add(Float.valueOf(String.valueOf(cell)));
                    }
                }
                map.put("data", vl2);

                vvalue.add(map);
                regionset.remove(String.valueOf(cellvalue));
                if (regionset.isEmpty()){
                    break;
                }
            }
        }

        result.put("SeriesData", vvalue);

        return result;

    }

    public static Map<String, Object> getSheetData(Sheet sheet) {
        // 存储列名
        List<String> columns = new ArrayList<>();
        // 存储所有行的数据
        List<Map<String, Object>> data = new ArrayList<>();

        // 获取首行作为列名
        Row firstRow = sheet.getRow(0);
        if (firstRow != null) {
            for (int i = 0; i < firstRow.getLastCellNum(); i++) {
                Cell cell = firstRow.getCell(i);
                if (cell != null) {
                    columns.add(cell.getStringCellValue());
                } else {
                    // 处理空单元格，添加默认列名
                    columns.add("Column" + (i + 1));
                }
            }
        }

        // 从第二行开始遍历数据行
        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue; // 跳过空行
            }
            // 存储当前行的数据
            Map<String, Object> rowData = new LinkedHashMap<>();
            for (int i = 0; i < columns.size(); i++) {
                Cell cell = row.getCell(i);
                if (cell != null) {
                    switch (cell.getCellType()) {
                        case STRING:
                            rowData.put(columns.get(i), cell.getStringCellValue());
                            break;
                        case NUMERIC:
                            if (DateUtil.isCellDateFormatted(cell)) {
                                rowData.put(columns.get(i), cell.getDateCellValue());
                            } else {
                                rowData.put(columns.get(i), cell.getNumericCellValue());
                            }
                            break;
                        case BOOLEAN:
                            rowData.put(columns.get(i), cell.getBooleanCellValue());
                            break;
                        case FORMULA:
                            rowData.put(columns.get(i), cell.getCellFormula());
                            break;
                        default:
                            rowData.put(columns.get(i), "");
                    }
                } else {
                    rowData.put(columns.get(i), "");
                }
            }
            data.add(rowData);
        }

        // 构建结果 Map
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("columns", columns);
        result.put("data", data);

        return result;
    }


}
