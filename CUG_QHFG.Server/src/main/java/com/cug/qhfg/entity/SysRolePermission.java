package com.cug.qhfg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 
 * @TableName sys_role_permission
 */
@Data
@TableName(value ="sys_role_permission")
public class SysRolePermission implements Serializable {
    /**
     * 
     */
    @TableId
    private Integer id;

    /**
     * 
     */
    private String role;

    /**
     * 
     */
    private String permission;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    public Integer getId() {
        return id;
    }

    /**
     * 
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 
     */
    public String getRole() {
        return role;
    }

    /**
     * 
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * 
     */
    public String getPermission() {
        return permission;
    }

    /**
     * 
     */
    public void setPermission(String permission) {
        this.permission = permission;
    }
}