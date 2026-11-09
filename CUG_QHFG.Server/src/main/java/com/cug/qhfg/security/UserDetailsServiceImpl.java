package com.cug.qhfg.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cug.qhfg.entity.SysUser;
import com.cug.qhfg.mapper.SysRolePermissionMapper;
import com.cug.qhfg.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * spring security, 查询用户信息，查询权限信息
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysRolePermissionMapper sysRolePermissionMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 查询用户信息
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUsername,username);
        SysUser sysUser = sysUserMapper.selectOne(queryWrapper);

        if(Objects.isNull(sysUser)){
            throw new UsernameNotFoundException(username);
        }

        //todo 查询对应权限 可以在这里获取用户的权限，然后封装到loginuser里面，需要在loginuser类里面加一个表示权限的list
        // 是然后将loginuser做缓存。（done）
        // 下一步去jwt过滤器
        List<String> permissionList = sysRolePermissionMapper.selectRolePermissionByUsername(username);


        // 把数据封装成UserDetails
        return new LoginUser(sysUser,permissionList);
    }
}
