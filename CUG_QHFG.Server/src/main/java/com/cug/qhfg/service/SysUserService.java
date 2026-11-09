package com.cug.qhfg.service;

import com.cug.qhfg.entity.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author DELL
* @description 针对表【sys_user】的数据库操作Service
* @createDate 2025-05-10 14:20:19
*/
public interface SysUserService extends IService<SysUser> {

    public boolean hasUser(String email);

    public boolean sendModifyCode(String email);

    public int modifyPwd(String email,String password);

}
