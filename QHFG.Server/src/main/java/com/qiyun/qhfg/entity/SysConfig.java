package com.qiyun.qhfg.entity;


import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.util.Date;

@SuppressWarnings("serial")
@Data
public class SysConfig extends Model<SysConfig> {
    
    private Integer id;
    
    private String type;
    
    private String name;
    
    private String value;
    
    private Date updateUser;
    
    private Integer delFlag;
    
    private Date updateTime;
}

