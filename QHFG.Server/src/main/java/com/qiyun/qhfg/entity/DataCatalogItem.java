package com.qiyun.qhfg.entity;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.util.Date;

@SuppressWarnings("serial")
@Data
public class DataCatalogItem extends Model<DataCatalogItem> {
    
    private String id;
    
    private Integer catalogItemType;
    
    private String catalogItemId;
    
    private String parentId;
    
    private Date regDate;
    
    private String tags;
    
    private String description;
    
    private String title;
    
    private String srcFormat;
    
    private String footprint;
    
    private String footprintNative;
    
    private String coDecoderName;
    
    private String coWorkspace;
    
    private String ogcName;
    
    private String coFileUri;
    
    private Float coSeXMax;
    
    private Float coSeXMin;
    
    private Float coSeXPixelSize;
    
    private Float coSeYMax;
    
    private Float coSeYMin;
    
    private Float coSeYPixelSize;
    
    private String irMetadataUri;
    
    private Date lastModifyDate;
    
    private String updateUser;
    
    private String thumbImg;
    
    private String geoRes;
    
    private Date timeCycle;
    
    private Date dataTime;
    
    private String text;
    
    private String region;
    
    private String qgsMap;
    
    private String source;

    @TableField(value = "mata", typeHandler = JacksonTypeHandler.class)
    private JSONObject mata;
}

