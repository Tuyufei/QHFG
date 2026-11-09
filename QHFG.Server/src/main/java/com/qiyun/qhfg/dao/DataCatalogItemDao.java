package com.qiyun.qhfg.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qiyun.qhfg.entity.DataCatalogItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * (DataCatalogItem)表数据库访问层
 *
 * @author makejava
 * @since 2024-12-26 10:49:43
 */
@Mapper
public interface DataCatalogItemDao extends BaseMapper<DataCatalogItem> {

}

