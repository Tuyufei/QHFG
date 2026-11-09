package com.cug.qhfg.service;

import com.cug.qhfg.common.Result;
import com.cug.qhfg.entity.SysUser;

public interface LoginService {
    Result login(SysUser sysUser);

    Result logout();
}
