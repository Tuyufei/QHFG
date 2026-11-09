package com.qiyun.qhfg.service;

import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qiyun.qhfg.entity.SysUser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * (SysUser)表服务接口
 *
 * @author makejava
 * @since 2024-12-26 10:28:35
 */
public interface SysUserService extends IService<SysUser> {

    List<SysUser> queryUser(String userName, String passWord);

    SaResult login(String userName, String passWord, HttpServletRequest request, HttpServletResponse response) throws Exception;

    SaResult register(String userName, String passWord, boolean role);

    SaResult delete(Integer id);

    SaResult update(Integer id, Map<String, Object> map);

    SaResult list(Integer pageSize, Integer pageIndex);

    SaResult queryUserById(Integer id);
}

