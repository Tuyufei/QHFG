package com.cug.qhfg.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.cug.qhfg.entity.ConditionTree;
import com.cug.qhfg.mapper.ConditionTreeMapper;
import com.cug.qhfg.service.ConditionTreeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.cug.qhfg.util.utils.TranstoUtils.Translation;

/**
 * @Description TODO
 * @Author fjh
 */
@Slf4j
@Service
public class ConditionTreeServiceImpl implements ConditionTreeService {

    @Autowired
    private ConditionTreeMapper conditionTreeMapper;

    /**
     *@Description 获取全部筛选条件列表
     *@Author fjh
     *@param
     *@Return java.util.List<com.cug.qhfg.entity.ConditionTree>
     */
    @Override
    public List<ConditionTree> getConditionTree() {
        LambdaQueryWrapper<ConditionTree> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper
                .select(ConditionTree::getConditionId,ConditionTree::getName,ConditionTree::getParentId,ConditionTree::getType)
                .eq(ConditionTree::isDelFlag,false).orderByAsc(ConditionTree::getConditionId);
        List<ConditionTree> cl=conditionTreeMapper.selectList(queryWrapper);
        return cl;
    }

    @Override
    public String getNode(int id) {

        ConditionTree c=conditionTreeMapper.selectById(id);

        return c.getType();
    }


    /**
     *@Description 修改筛选条件名字
     *@Author fjh
     *@param conditionTree
     *@Return int
     */
    @Override
    public int modifyConditionName(ConditionTree conditionTree) {
        LambdaUpdateWrapper<ConditionTree> updateWrapper=new LambdaUpdateWrapper<>();
        updateWrapper.set(ConditionTree::getName,conditionTree.getName()).eq(ConditionTree::getConditionId,conditionTree.getConditionId());
        int num=conditionTreeMapper.update(updateWrapper);
        return num;
    }


    /**
     *@Description 新增子级筛选条件
     *@Author fjh
     *@param conditionTree
     *@Return int
     */
    @Override
    @Transactional
    public boolean createNextLevelCondition(ConditionTree conditionTree) {
        conditionTree.setDelFlag(false);
        String nodeType=getNode(conditionTree.getParentId());
        int n1= conditionTreeMapper.insert(conditionTree);
        int n2=1;
        if (conditionTree.getType().equals("resource")){
            LambdaUpdateWrapper<ConditionTree> updateWrapper=new LambdaUpdateWrapper<>();
            updateWrapper.set(ConditionTree::getType,"rnode").eq(ConditionTree::getConditionId,conditionTree.getParentId());
            n2=conditionTreeMapper.update(updateWrapper);
        } else if (conditionTree.getType().equals("datatype") && !nodeType.equals("resource") ) {
            LambdaUpdateWrapper<ConditionTree> updateWrapper=new LambdaUpdateWrapper<>();
            updateWrapper.set(ConditionTree::getType,"dnode").eq(ConditionTree::getConditionId,conditionTree.getParentId());
            n2=conditionTreeMapper.update(updateWrapper);
        }

        return (n1 != 0) && (n2 != 0);
    }


    /**
     *@Description 删除筛选条件（伪删）
     *@Author fjh
     *@param conditionTree
     *@Return int
     */
    @Override
    public int removeCondition(ConditionTree conditionTree) {
        LambdaUpdateWrapper<ConditionTree> updateWrapper=new LambdaUpdateWrapper<>();
        updateWrapper.set(ConditionTree::isDelFlag,true).eq(ConditionTree::getConditionId,conditionTree.getConditionId());
        int n=conditionTreeMapper.update(updateWrapper);
        return n;
    }

    /**
     *@Description 获取资源条件筛选条件
     *@Author fjh
     *@param
     *@Return java.util.List<com.cug.qhfg.entity.ConditionTree>
     */
    @Override
    public List<ConditionTree> getResourceConditions() {
        LambdaQueryWrapper<ConditionTree> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper
                .select(ConditionTree::getConditionId,ConditionTree::getName,ConditionTree::getNameEn,ConditionTree::getParentId,ConditionTree::getType)
                .eq(ConditionTree::isDelFlag,false)
                .in(ConditionTree::getType,"rnode","resource")
                .orderByAsc(ConditionTree::getConditionId);

        List<ConditionTree> clist=conditionTreeMapper.selectList(queryWrapper);
        return clist;
    }


    /**
     *@Description 获取数据类型筛选条件
     *@Author fjh
     *@param conditionTree
     *@Return java.util.List<com.cug.qhfg.entity.ConditionTree>
     */
    @Override
    public List<ConditionTree> getDataTypeConditions(ConditionTree conditionTree) {
        List<ConditionTree> clist=conditionTreeMapper.getDataTypeConditions(conditionTree);

        clist.add(ConditionTree.builder()
                .conditionId(conditionTree.getConditionId())
                .name("")
                .type("")
                .parentId(-1)
                .build());
        return clist;
    }


    /**
     *@Description 获取该条件子条件
     *@Author fjh
     *@param conditionTree
     *@Return java.util.List<com.cug.qhfg.entity.ConditionTree>
     */
    @Override
    public List<ConditionTree> getChildConditions(ConditionTree conditionTree) {
        LambdaQueryWrapper<ConditionTree> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper
                .select(ConditionTree::getConditionId,ConditionTree::getName,ConditionTree::getNameEn,ConditionTree::getParentId,ConditionTree::getType)
                .eq(ConditionTree::isDelFlag,false)
                .eq(ConditionTree::getParentId,conditionTree.getConditionId())
                .orderByAsc(ConditionTree::getConditionId);

        List<ConditionTree> clist=conditionTreeMapper.selectList(queryWrapper);

        clist.add(ConditionTree.builder()
                .conditionId(conditionTree.getConditionId())
                        .name("")
                        .type("")
                        .parentId(-1)
                .build());
        return clist;
    }



}
