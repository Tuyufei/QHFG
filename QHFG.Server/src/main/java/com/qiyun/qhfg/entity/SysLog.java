package com.qiyun.qhfg.entity;


import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.util.Date;

@SuppressWarnings("serial")
@Data
public class SysLog extends Model<SysLog> {
    
    private Integer id;
    
    private String type;
    
    private String name;
    
    private String message;
    
    private Integer level;
    
    private Date operTime;
    
    private String operUser;
    
    private Integer delFlag;
}

