package com.cug.qhfg.controller;


import com.cug.qhfg.common.Result;
import com.cug.qhfg.service.FetchStatisticalDataService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/statistical")
public class StatisticalDataController {

    @Autowired
    private FetchStatisticalDataService fetchStatisticalDataService;
    @PreAuthorize("hasAuthority('normal')")
    @GetMapping("/getdata")
    public Result<Map<String, Object>> getData(@RequestParam String map,
                                                     @RequestParam String timeScale,
                                                     @RequestParam String year,
                                                     @RequestParam boolean isGlobal,
                                                     @RequestParam String level,
                                                     @RequestParam String area)
    {
        // 参数验证
        if (map == null || map.isEmpty()) {
            return Result.error("Invalid parameter: map is null or empty");
        }

        Map<String, Object> data = new HashMap<>();
        // 如果area为空，则查询全部
        if (area.isEmpty()) {
            data = fetchStatisticalDataService.queryAll(map, timeScale, year, isGlobal, level);
        }
        else {
            data = fetchStatisticalDataService.queryByArea(map, timeScale, year, isGlobal, level, area);
        }

        return Result.success(data);

    }
    @PreAuthorize("hasAuthority('normal')")
    @GetMapping("/getlinedata")
    public Result<Map<String, Object>> getdatanew(@RequestParam("map") String map,
                                                  @RequestParam("timescale") String timeScale,
                                                  @RequestParam("timespan") List<String> timeSpan,
                                                  @RequestParam("level") String level,
                                                  @RequestParam("places") List<String> places) throws ExecutionException, InterruptedException {

        Map<String,Object> result=fetchStatisticalDataService.getFrontForFormat(map,timeScale,timeSpan,level,places);

        return Result.success(result) ;

    }
    @PreAuthorize("hasAuthority('normal')")
    @GetMapping("/getexceldata")
    public Result<Map<String, Object>> getExcelData(@RequestParam("map") String map,
                                                    @RequestParam("timeScale") String timeScale,
                                                    @RequestParam("year") String year,
                                                    @RequestParam("level") String level,
                                                    @RequestParam("language") String language) throws Exception {

        Map<String,Object> result=fetchStatisticalDataService.getExcelData(map,timeScale,year,level,language);

        return Result.success(result) ;

    }
    @PreAuthorize("hasAuthority('download')")
    @GetMapping("/download")
    public ResponseEntity<byte[]> dowmLoadData(@RequestParam("map") String map,
                                               @RequestParam("timeScale") String timeScale,
                                               @RequestParam("year") String year,
                                               @RequestParam("level") String level,
                                               @RequestParam("language") String language) throws Exception {

        Map<String,Object> result=fetchStatisticalDataService.getExcelData(map,timeScale,year,level,language);

        // 创建新的 Workbook 和 Sheet
        Workbook newWorkbook = new XSSFWorkbook();
        Sheet newSheet = newWorkbook.createSheet("Data");

        // 写入表头
        Row headerRow = newSheet.createRow(0);
        List<String> columns = (List<String>) result.get("columns");
        for (int i = 0; i < columns.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns.get(i));
        }

        // 写入数据行
        List<Map<String, Object>> rows = (List<Map<String, Object>>) result.get("data");
        for (int i = 0; i < rows.size(); i++) {
            Row dataRow = newSheet.createRow(i + 1);
            Map<String, Object> rowData = rows.get(i);
            for (int j = 0; j < columns.size(); j++) {
                Cell cell = dataRow.createCell(j);
                Object value = rowData.get(columns.get(j));
                if (value != null) {
                    if (value instanceof String) {
                        cell.setCellValue((String) value);
                    } else if (value instanceof Number) {
                        cell.setCellValue(((Number) value).doubleValue());
                    } else if (value instanceof Boolean) {
                        cell.setCellValue((Boolean) value);
                    }
                }
            }
        }

        // 将 Workbook 写入 ByteArrayOutputStream
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        newWorkbook.write(bos);
        byte[] excelBytes = bos.toByteArray();
        newWorkbook.close();
        bos.close();

        // 设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "data.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelBytes);

    }

}
