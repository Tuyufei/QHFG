package com.cug.qhfg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cug.qhfg.entity.ConditionTree;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;


/**
 *
 * @Description TODO
 * @Author fjh
 */
@Mapper
public interface ConditionTreeMapper extends BaseMapper<ConditionTree> {

    @Select("WITH RECURSIVE SubtreeCTE AS (" +
            "    SELECT Condition_id, name, name_en,parent_id, type" +
            "    FROM data_select_conditions_tree" +
            "    WHERE parent_id = #{conditionId}" +
            "      AND del_flag = false" +
            "      AND type IN ('dnode', 'datatype')" +
            "    UNION ALL" +
            "    SELECT t.Condition_id, t.name,t.name_en, t.parent_id, t.type" +
            "    FROM data_select_conditions_tree t" +
            "             INNER JOIN SubtreeCTE s ON t.parent_id = s.Condition_id" +
            "    WHERE t.type IN ('dnode', 'datatype')" +
            "      AND t.del_flag = false" +
            ")" +
            "SELECT * FROM SubtreeCTE ORDER BY Condition_id ASC"
    )
    List<ConditionTree> getDataTypeConditions(ConditionTree conditionTree);
}
