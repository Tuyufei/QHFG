package com.cug.qhfg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Description TODO
 * @Author fjh
 * @Date 2025/6/9
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "data_description")
public class Description {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String map;

    private String dataName;

    private String descriptionZn;

    private String descriptionEn;

    @TableField(value = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime time;

    private String developer;
}
