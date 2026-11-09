package com.qiyun.qhfg.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qiyun.qhfg.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * (SysUser)表数据库访问层
 *
 * @author makejava
 * @since 2024-12-26 10:28:33
 */
@Mapper
public interface SysUserDao extends BaseMapper<SysUser> {
    List<SysUser> queryUser(String userName, String passWord);

    Integer queryUserMaxId();

    Integer addUser(Integer newId, String userName, String passWord, String roleName);

    List<Map<String, Object>> queryUserByName(String userName);

    Integer delete(Integer id);

    List<Map<String, Object>> queryUserById(Integer id);

    Integer update(Map<String, Object> map);

    List<Map<String, Object>> list(Integer pageIndex, Integer pageSize);
}

