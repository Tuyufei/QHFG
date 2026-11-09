package com.qiyun.qhfg.service.Impl;

import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiyun.qhfg.dao.SysUserDao;
import com.qiyun.qhfg.entity.SysUser;
import com.qiyun.qhfg.entity.SysUser;
import com.qiyun.qhfg.service.SysUserService;
import com.qiyun.qhfg.utils.EncryptAndDecrypte;
import com.qiyun.qhfg.utils.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * (SysUser)表服务实现类
 *
 * @author makejava
 * @since 2024-12-26 10:28:37
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserDao, SysUser> implements SysUserService {
    @Autowired
    SysUserDao userMapper;

    @Value("${encryption.key}")
    private String encryptionKey;

    @Value("${encryption.iv}")
    private String encryptionIv;

    @Override
    public List<SysUser> queryUser(String userName, String passWord) {
        return userMapper.queryUser(userName, passWord);
    }

    @Override
    public SaResult login(String userName, String passWord, HttpServletRequest request, HttpServletResponse response) throws Exception {
        clearUserCookies(request,response);
        //通过账号密码查询用户
        List<SysUser> userList = userMapper.queryUser(userName, passWord);
        SaResult saResult;
        if (userList.isEmpty()) {
            saResult = ResultUtil.error("账号名或密码有误");
            saResult.set("status", "error");
            saResult.set("error", "账号名或密码有误");
            saResult.set("role", "");
            return saResult;
        } else {
            SysUser user = userList.get(0);
            //清除密码
            user.setPassWord("");
            //查询用户信息转JSON对象
            String userStr = user.toString();
            String token = EncryptAndDecrypte.encryptString(userStr,encryptionKey,encryptionIv);
            saResult = ResultUtil.data("登陆成功");
            saResult.set("status", "ok");
            saResult.set("message", "登陆成功");
            saResult.set("id", user.getId());
            saResult.set("name", user.getUserName());
            saResult.set("token", token);
            saResult.set("role", user.getRole());
            //cookies 添加token
            Cookie cookie = new Cookie("token", token);
            // 将Cookie添加到响应头中
            cookie.setPath("/");
            response.addCookie(cookie);
        }
        return saResult;
    }

    @Override
    public SaResult register(String userName, String passWord, boolean role) {
        //查询用户
        List<Map<String, Object>> listUser = userMapper.queryUserByName(userName);
        if (!listUser.isEmpty()) {
            return ResultUtil.error("该用户已存在");
        }
        //查询用户表中最大的id并+1
        Integer newId = userMapper.queryUserMaxId();
        //新增用户
        String roleName = role ? "管理员" : "普通用户";
        Integer addNum = userMapper.addUser(newId, userName, passWord, roleName);
        if (addNum > 0) {
            return ResultUtil.data("注册成功");
        } else {
            return ResultUtil.error("访问接口异常");
        }
    }

    @Override
    public SaResult delete(Integer id) {
        Integer delNum = userMapper.delete(id);
        if (delNum > 0) {
            return ResultUtil.data("删除成功");
        } else {
            return ResultUtil.error("访问接口异常");
        }
    }

    @Override
    public SaResult update(Integer id, Map<String, Object> map) {
        //通过id查询用户信息
        List<Map<String, Object>> userList = userMapper.queryUserById(id);
        if (userList.isEmpty()) {
            return ResultUtil.error("用户id不存在！");
        }
        if (!Objects.isNull(map.get("role")) && map.get("role") instanceof Boolean) {
            String roleName = (Boolean) map.get("role") ? "管理员" : "普通用户";
            map.put("role", roleName);
        }
        Map<String, Object> user = userList.get(0);
        if (!Objects.isNull(map.get("oldPassWord")) && !Objects.isNull(user.get("PassWord"))) {
            if (user.get("PassWord").toString().equals(map.get("oldPassWord").toString())) {
                map.put("passWord",map.get("newPassWord"));
            } else {
                return ResultUtil.error("旧密码不正确");
            }
        }
        map.put("id",id);
        Integer updateNum = userMapper.update(map);
        if(updateNum>0){
            return ResultUtil.data("修改成功");
        }else {
            return ResultUtil.error("访问接口异常");
        }
    }

    @Override
    public SaResult list(Integer pageSize, Integer pageIndex) {
        List<Map<String, Object>> userList = userMapper.list(pageIndex,pageSize);
        SaResult saResult = ResultUtil.data(userList);
        saResult.set("page", pageIndex);
        saResult.set(ResultUtil.PAGE_SIZE, pageSize);
        saResult = ResultUtil.setTotal(saResult,userList);
        return saResult;
    }

    @Override
    public SaResult queryUserById(Integer id) {
        List<Map<String, Object>> userList = userMapper.queryUserById(id);
        if(userList.isEmpty()){
            return ResultUtil.data(null);
        }else {
            Map<String, Object> user = userList.get(0);
            user.put("PassWord","******");
            return ResultUtil.data(userList.get(0));
        }
    }

    private void clearUserCookies(HttpServletRequest request,HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (Objects.isNull(cookies)) {
            return;
        }
        for (Cookie cookie : cookies) {
            String name = cookie.getName();
            if (name.equals("userName") || name.equals("token")) {
                // cookies失效
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }
    }

}

