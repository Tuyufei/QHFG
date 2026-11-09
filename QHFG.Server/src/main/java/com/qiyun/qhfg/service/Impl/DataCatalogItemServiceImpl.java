package com.qiyun.qhfg.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiyun.qhfg.dao.DataCatalogItemDao;
import com.qiyun.qhfg.entity.DataCatalogItem;
import com.qiyun.qhfg.service.DataCatalogItemService;
import org.springframework.stereotype.Service;

/**
 * (DataCatalogItem)表服务实现类
 *
 * @author makejava
 * @since 2024-12-26 10:49:44
 */
@Service("dataCatalogItemService")
public class DataCatalogItemServiceImpl extends ServiceImpl<DataCatalogItemDao, DataCatalogItem> implements DataCatalogItemService {

}

