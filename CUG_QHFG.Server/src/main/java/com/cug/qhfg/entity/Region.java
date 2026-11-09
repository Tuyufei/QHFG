package com.cug.qhfg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description TODO
 * @Author fjh
 * @Date 2025/6/7
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "data_statistic_region")
public class Region {

    @TableId(type = IdType.AUTO)
    private Integer Id;

    private String Map;

    private String RegionsZn;

    private String RegionsEn;
}
