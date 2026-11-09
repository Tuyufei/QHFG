package com.cug.qhfg.mapper;

import com.cug.qhfg.entity.SysRolePermission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* @author fairy
* @description 针对表【sys_role_permission】的数据库操作Mapper
* @createDate 2024-12-17 11:22:21
* @Entity com.cug.qhfg.entity.SysRolePermission
*/
@Mapper
public interface SysRolePermissionMapper extends BaseMapper<SysRolePermission> {
    List<String> selectRolePermissionByUsername(String username);
}




