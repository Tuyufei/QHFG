package com.qiyun.qhfg.controller;


import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiyun.qhfg.entity.DataCatalogItem;
import com.qiyun.qhfg.service.DataCatalogItemService;
import io.swagger.annotations.ApiOperation;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * (DataCatalogItem)表控制层
 *
 * @author makejava
 * @since 2024-12-26 10:25:40
 */
@RestController
@RequestMapping("dataCatalogItem")
public class DataCatalogItemController {
    /**
     * 服务对象
     */
    @Resource
    private DataCatalogItemService dataCatalogItemService;


    /**
     * 数据目录接口：一个通过id参数，获取当前id参数下的所有子节点
     *
     * @param objId id
     * @return 所有数据
     */
    @ApiOperation(value = "数据目录接口", tags = "数据编目记录")
    @GetMapping("/selectAllNodeById")
    public SaResult selectAllNodeById(String id) {
        DataCatalogItem dataCatalogItem = new DataCatalogItem();
        dataCatalogItem.setCatalogItemType(1);
        List<Map<String, Object>> listData = this.dataCatalogItemService.listMaps(new QueryWrapper<>(dataCatalogItem).orderByAsc("( case when parent_id IS NULL then 0 else 1 end )").orderByAsc("parent_id"));
        Map<String, Object> idMap = new HashMap<>();
        for (Map<String, Object> obj : listData) {
            String objId = obj.get("id").toString();
            String name = (String) obj.get("name");
            idMap.put(objId, obj);
            obj.put("label", name);
        }
        List<Map<String, Object>> listTree = new ArrayList<>();
        for (Map<String, Object> obj : listData) {
            String objId = obj.get("id").toString();
            String pid = (String) obj.get("parent_id");
            if (pid == null) {
                Map<String, Object> treeMap = (Map<String, Object>) idMap.get(objId);
                treeMap.put("children", new ArrayList<>());
                listTree.add(treeMap);
            } else {
                Map<String, Object> treeMap = (Map<String, Object>) idMap.get(pid);
                List<Map<String, Object>> children = (List<Map<String, Object>>) treeMap.get("children");
                if (children == null) {
                    children = new ArrayList<>();
                }
                children.add(obj);
                treeMap.put("children", children);
            }
        }
        if(StringUtils.isEmpty(id)){
            return SaResult.data(listTree);
        }
        return SaResult.data(idMap.get(id));

    }

    /**
     * 分页查询所有数据
     *
     * @param page            分页对象
     * @param dataCatalogItem 查询实体
     * @return 所有数据
     */
    @ApiOperation(value = "分页查询所有数据", tags = "数据编目记录")
    @GetMapping
    public SaResult selectAll(Page page, DataCatalogItem dataCatalogItem) {
        QueryWrapper queryWrapper = new QueryWrapper<>(dataCatalogItem);
        queryWrapper.gt("catalog_item_type",1);
        return SaResult.data(this.dataCatalogItemService.page(page, queryWrapper));
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @ApiOperation(value = "通过主键查询单条数据", tags = "数据编目记录")
    @GetMapping("{id}")
    public SaResult selectOne(@PathVariable Serializable id) {
        return SaResult.data(this.dataCatalogItemService.getById(id));
    }

    /**
     * 新增数据
     *
     * @param dataCatalogItem 实体对象
     * @return 新增结果
     */
    @ApiOperation(value = "新增数据", tags = "数据编目记录")
    @PostMapping
    public SaResult insert(@RequestBody DataCatalogItem dataCatalogItem) {
        return SaResult.data(this.dataCatalogItemService.save(dataCatalogItem));
    }

    /**
     * 修改数据
     *
     * @param dataCatalogItem 实体对象
     * @return 修改结果
     */
    @ApiOperation(value = "修改数据", tags = "数据编目记录")
    @PutMapping
    public SaResult update(@RequestBody DataCatalogItem dataCatalogItem) {
        return SaResult.data(this.dataCatalogItemService.updateById(dataCatalogItem));
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @ApiOperation(value = "删除数据", tags = "数据编目记录")
    @DeleteMapping
    public SaResult delete(@RequestParam("idList") List<String> idList) {
        return SaResult.data(this.dataCatalogItemService.removeByIds(idList));
    }
}

