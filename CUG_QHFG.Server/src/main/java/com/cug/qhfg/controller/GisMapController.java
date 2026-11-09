package com.cug.qhfg.controller;


import com.cug.qhfg.common.Result;
import com.cug.qhfg.service.FetchGISMapService;
import com.cug.qhfg.service.FetchLegendService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.cug.qhfg.util.utils.TranstoUtils.translateMapPath;


/**
 * @Description TODO
 * @Author fjh
 * @Date 2024/12/23
 */

@RestController
@RequestMapping("/map")
@PreAuthorize("hasAuthority('normal')")
@Slf4j
public class GisMapController {

    @Autowired
    private FetchGISMapService fetchGISMapService;
    @Autowired
    private FetchLegendService fetchLegendService;

    @GetMapping("/getmap")
    public byte[] getmap(@RequestParam Map<String,Object> parameters){

        byte[] img=fetchGISMapService.fetchMap(parameters);

        return img;
    }

    @GetMapping("/getlegend")
    public Result<byte[]> getLegend(@RequestParam Map<String,Object> parameters){

        // 检查必要参数是否存在
        if (!parameters.containsKey("MAP") || !parameters.containsKey("LAYER")) {
            return Result.error("参数缺失，请提供MAP 和 LAYER 参数");
        }
        byte[] img=fetchLegendService.fetchLegend(parameters);

        return Result.success(img);
    }

    @GetMapping("/getpoint")
    public Result<Map<String, Object>> getPoint(@RequestParam Map<String,Object> parameters){

        // 检查必要参数是否存在
        if (!parameters.containsKey("LONGITUDE") || !parameters.containsKey("LATITUDE")
                || !parameters.containsKey("MAP") || !parameters.containsKey("LAYERS")) {
            return Result.error("参数缺失，请提供 LONGITUDE、LATITUDE、MAP 和 LAYERS 参数");
        }

        try {
            double longitude = Double.parseDouble(parameters.get("LONGITUDE").toString());
            double latitude = Double.parseDouble(parameters.get("LATITUDE").toString());
            String map = parameters.get("MAP").toString();
            String layers = parameters.get("LAYERS").toString();
            Double value = fetchGISMapService.getPointValue(longitude, latitude, map, layers);

            Map<String, Object> data = new HashMap<>();
            data.put("value", value);

            // 获取该点对应的区域信息
            Map<String, Object> areaInfo = fetchGISMapService.getAreaOfPoint(longitude, latitude);
            if (areaInfo != null) {
                data.put("area", areaInfo);
            }

            return Result.success(data);
        } catch (NumberFormatException e) {
            return Result.error("参数格式错误，LONGITUDE 和 LATITUDE 必须为有效的数字");
        }

    }

    @GetMapping("/getboundary")
    public byte[] getBoundary(@RequestParam Map<String,Object> parameters){

        byte[] img=fetchGISMapService.fetchBoundary(parameters);

        return img;
    }

}
