package com.cug.qhfg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author fjh
 *
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName(value = "data_select_conditions_tree")
public class ConditionTree {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Integer conditionId;
    /**
     *
     */
    private String name;
    /**
     *
     */
    private String nameEn;

    private int parentId;
    /**
     *
     */
    private String type;

    private boolean delFlag;


}
