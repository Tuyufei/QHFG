package com.qiyun.qhfg.controller;



import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiyun.qhfg.entity.StyleInfo;
import com.qiyun.qhfg.service.StyleInfoService;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.ApiOperation;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;

/**
 * (StyleInfo)表控制层
 *
 * @author makejava
 * @since 2024-12-26 10:26:21
 */
@RestController
@RequestMapping("styleInfo")
public class StyleInfoController {
    /**
     * 服务对象
     */
    @Resource
    private StyleInfoService styleInfoService;

    /**
     * 分页查询所有数据
     *
     * @param page 分页对象
     * @param styleInfo 查询实体
     * @return 所有数据
     */
    @ApiOperation(value = "分页查询所有数据", tags = "样式信息")
    @GetMapping
    public SaResult selectAll(Page<StyleInfo> page, StyleInfo styleInfo) {
        return SaResult.data(this.styleInfoService.page(page, new QueryWrapper<>(styleInfo)));
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @ApiOperation(value = "通过主键查询单条数据", tags = "样式信息")
    @GetMapping("{id}")
    public SaResult selectOne(@PathVariable Serializable id) {
        return SaResult.data(this.styleInfoService.getById(id));
    }

    /**
     * 新增数据
     *
     * @param styleInfo 实体对象
     * @return 新增结果
     */
    @ApiOperation(value = "新增数据", tags = "样式信息")
    @PostMapping
    public SaResult insert(@RequestBody StyleInfo styleInfo) {
        return SaResult.data(this.styleInfoService.save(styleInfo));
    }

    /**
     * 修改数据
     *
     * @param styleInfo 实体对象
     * @return 修改结果
     */
    @ApiOperation(value = "修改数据", tags = "样式信息")
    @PutMapping
    public SaResult update(@RequestBody StyleInfo styleInfo) {
        return SaResult.data(this.styleInfoService.updateById(styleInfo));
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @ApiOperation(value = "删除数据", tags = "样式信息")
    @DeleteMapping
    public SaResult delete(@RequestParam("idList") List<String> idList) {
        return SaResult.data(this.styleInfoService.removeByIds(idList));
    }
}

