package com.cug.qhfg.service;


import com.cug.qhfg.entity.ConditionTree;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 *
 * @Description TODO
 * @Author fjh
 */

public interface ConditionTreeService {
    List<ConditionTree> getConditionTree();

    String getNode(int id);

    int modifyConditionName(ConditionTree conditionTree);


    boolean createNextLevelCondition(ConditionTree conditionTree);

    int removeCondition(ConditionTree conditionTree);


    //获取默认目录
    List<ConditionTree> getResourceConditions();

    //
    List<ConditionTree> getDataTypeConditions(ConditionTree conditionTree);

    //获取子目录
    List<ConditionTree> getChildConditions(ConditionTree conditionTree);


}
