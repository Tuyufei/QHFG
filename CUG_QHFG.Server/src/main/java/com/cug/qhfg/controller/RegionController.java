package com.cug.qhfg.controller;


import com.cug.qhfg.common.Result;
import com.cug.qhfg.entity.Region;
import com.cug.qhfg.service.RegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description TODO
 * @Author fjh
 * @Date 2025/6/7
 */

@RestController
@RequestMapping("/region")
public class RegionController {

    @Autowired
    private RegionService regionService;

    @GetMapping("/getregions")
    public Result getRegions(Region region){

        if(region.getMap().isEmpty()){
           return Result.error("参数值缺失，请提供map参数值");
        }

        Region reg=regionService.getRegionByMap(region);

        if (reg==null){
            return Result.error("无数据,请提供正确的map路径");
        }else {
            return Result.success(reg);
        }

    }
}
