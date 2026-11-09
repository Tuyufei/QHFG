package com.qiyun.qhfg.entity;


import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.util.Date;

@SuppressWarnings("serial")
@Data
public class StyleInfo extends Model<StyleInfo> {
    
    private String id;
    
    private String styleName;
    
    private Integer bandRed;
    
    private Integer bandGreen;
    
    private Integer bandBlue;
    
    private String stretchMethod;
    
    private String styleFormat;
    
    private String styleContent;
    
    private String styleType;
    
    private String deleteFlag;
    
    private Date createTime;
    
    private String createBy;
    
    private Date updateTime;
    
    private String updateBy;
    
    private String remarks;
    
    private String dataStatus;
}

