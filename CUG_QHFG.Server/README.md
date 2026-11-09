# ce_visualization_system
# 鉴权实现：spring security + JWT
## 简单说明：
和鉴权认证相关的文件包括，文件夹security中的所有、文件夹filter中的JwtAuthenticationTokenFilter、文件夹service中的LoginService、
文件夹util.jwt中的所有文件。
### 相关的表
用户表：sys_user (用户表包含有一个 role 字段（字符串类型），用来表示该用户角色，密码加密存储)

角色权限表：sys_role_permission (表示某个 role 拥有某个 permission)

目前表内有两个账号。

账号1：clj 密码：123456 角色：admin 权限：sys、normal

账号2：user 密码：123455 角色：user 权限：normal

### 使用说明
用到了redis，需要先在本地开启redis服务。 可以使用注解的方法来限制接口范围，或者统一配置。

使用注解的方法：参考HelloController，注解为    @PreAuthorize("hasAuthority('normal')")  其中的normal对应角色权限表中permission字段

统一配置的方法：在文件夹security 下的 SecurityConfig文件 的 configure方法中配置。


### 流程说明：
用户调用登录接口（LoginController），controller中调用loginService.login()

在loginService.login()方法中，先是调用框架的认证方法，验证用户名和密码是否正确。如果认证通过，则将用户信息封装成LoginUser对象，
该类对象包含一个权限List用来表示用户拥有那些权限。将封装的LoginUser对象序列化后存入redis缓存，当用户请求其他接口时，直接从redis中获得用户权限信息。
loginService.login()方法中会使用用户的id和username字段来生成token，返回给前端。

用户请求其他接口时，先经过JwtAuthenticationTokenFilter来判断是否携带token。。。。。。

### 后期完善
注册邮箱验证。。
