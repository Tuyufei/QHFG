package com.cug.qhfg.util.utils;

import com.cug.qhfg.entity.ConditionTree;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description TODO
 * @Author fjh
 * @Date 2024/12/20
 */


public class CreateConditionTree {

    @Data
    static class TreeNode {
        int id;                // 结点ID
        String name;           // 结点名称（中文）
        String nameEn;         //结点名称（英文）
        int parentId;         //所属的结点ID
        String type;          //结点类型
        List<TreeNode> children;  // 子节点列表

        // 构造函数
        public TreeNode(int id, String name,String nameEn,int parentId, String type) {
            this.id = id;
            this.name = name;
            this.nameEn=nameEn;
            this.parentId=parentId;
            this.type = type;
            this.children=new ArrayList<>();
        }
        // 添加子节点
        public void addChild(TreeNode child) {
            this.children.add(child);
        }

    }

    public static TreeNode cTree(List<ConditionTree> clist) {

        // 构建树
        Map<Integer, TreeNode> nodeMap = new HashMap<>();
        TreeNode root = null;

        for (ConditionTree item : clist) {
            int id = item.getConditionId();
            String name = item.getName();
            String nameEn=item.getNameEn();
            int parentId=item.getParentId();
            String type=item.getType();

            nodeMap.put(id, new TreeNode(id, name,nameEn,parentId,type));
        }

        for (ConditionTree item : clist) {
            int id = item.getConditionId();
            int parentId = item.getParentId();
            if (parentId == -1) {
                root = nodeMap.get(id);
            } else {
                TreeNode parent = nodeMap.get(parentId);
                TreeNode child = nodeMap.get(id);
                if (parent != null) {
                    parent.addChild(child);
                }
            }
        }
        return root;

    }

}
