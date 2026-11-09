package com.cug.qhfg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cug.qhfg.entity.Region;
import com.cug.qhfg.mapper.RegionMapper;
import com.cug.qhfg.service.RegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description TODO
 * @Author fjh
 * @Date 2025/6/7
 */
@Service
public class RegionServiceImpl implements RegionService {

    @Autowired
    private RegionMapper regionMapper;

    @Override
    public Region getRegionByMap(Region region) {
        LambdaQueryWrapper<Region> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.select(Region::getRegionsZn,Region::getRegionsEn).eq(Region::getMap,region.getMap());

        Region regions=regionMapper.selectOne(queryWrapper);
        if (regions==null){
            return null;
        }else {
            return regions;
        }

    }


}
