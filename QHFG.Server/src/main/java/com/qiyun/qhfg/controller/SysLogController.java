package com.qiyun.qhfg.controller;



import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiyun.qhfg.entity.SysLog;
import com.qiyun.qhfg.service.SysLogService;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.ApiOperation;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;

/**
 * (SysLog)表控制层
 *
 * @author makejava
 * @since 2024-12-26 10:26:48
 */
@RestController
@RequestMapping("sysLog")
public class SysLogController {
    /**
     * 服务对象
     */
    @Resource
    private SysLogService sysLogService;

    /**
     * 分页查询所有数据
     *
     * @param page 分页对象
     * @param sysLog 查询实体
     * @return 所有数据
     */
    @ApiOperation(value = "分页查询所有数据", tags = "系统日志")
    @GetMapping
    public SaResult selectAll(Page<SysLog> page, SysLog sysLog) {
        return SaResult.data(this.sysLogService.page(page, new QueryWrapper<>(sysLog)));
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @ApiOperation(value = "通过主键查询单条数据", tags = "系统日志")
    @GetMapping("{id}")
    public SaResult selectOne(@PathVariable Integer id) {
        return SaResult.data(this.sysLogService.getById(id));
    }

    /**
     * 新增数据
     *
     * @param sysLog 实体对象
     * @return 新增结果
     */
    @ApiOperation(value = "新增数据", tags = "系统日志")
    @PostMapping
    public SaResult insert(@RequestBody SysLog sysLog) {
        return SaResult.data(this.sysLogService.save(sysLog));
    }

    /**
     * 修改数据
     *
     * @param sysLog 实体对象
     * @return 修改结果
     */
    @ApiOperation(value = "修改数据", tags = "系统日志")
    @PutMapping
    public SaResult update(@RequestBody SysLog sysLog) {
        return SaResult.data(this.sysLogService.updateById(sysLog));
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @ApiOperation(value = "删除数据", tags = "系统日志")
    @DeleteMapping
    public SaResult delete(@RequestParam("idList") List<Long> idList) {
        return SaResult.data(this.sysLogService.removeByIds(idList));
    }
}

