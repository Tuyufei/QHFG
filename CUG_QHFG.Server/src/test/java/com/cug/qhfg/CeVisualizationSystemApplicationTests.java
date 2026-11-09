package com.cug.qhfg;

import com.cug.qhfg.mapper.StatisticalDataMapper;
import com.cug.qhfg.mapper.SysRolePermissionMapper;
import com.cug.qhfg.service.FetchStatisticalDataService;
import com.cug.qhfg.service.SysUserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;

@SpringBootTest
class CeVisualizationSystemApplicationTests {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysRolePermissionMapper sysRolePermissionMapper;

    @Autowired
    private FetchStatisticalDataService fetchStatisticalDataService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    StatisticalDataMapper statisticalDataMapper;

    @Test
    public void test() {
        String map = "1发电侧/风力发电/低空风电/陆上风电/潜力数据/6_5MW/装机潜力/25km/";
        String timeScale = "年";
        String year = "2021";
        boolean isGlobal = true;
        String level = "次区域";
        String area = "七星关区";


//        String tableName = fetchStatisticalDataService.parseMapToTableName(map, timeScale, year, isGlobal, level);
//        // 打印结果
//        System.out.println("生成的表名: " + tableName);
//
//        //根据表名查询全部
//        List<Map<String, Object>> maps = fetchStatisticalDataService.queryAllByTableName(tableName);
//        System.out.println(maps);

        Map<String, Object> stringObjectMap = fetchStatisticalDataService.queryAll(map, timeScale, year, isGlobal, level);
        System.out.println(stringObjectMap);


    }

//        fetchStatisticalDataService.parseMapToTableName(map, true, "cntry");
//        System.out.println(fetchStatisticalDataService.parseMapToTableName(map, true, "cntry"));

//        List<Map<String, Object>> maps = fetchStatisticalDataService.queryStatisticalDataByArea(map, true, "cntry", "中国");
//        System.out.println(maps);
   // }


//    @Test
//    public void test() {
//        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
//       String encode = bCryptPasswordEncoder.encode("123456");
////        String encode1 = bCryptPasswordEncoder.encode("1234");
////        System.out.println(encode);
////        System.out.println(encode1);
//       // boolean matches = bCryptPasswordEncoder.matches("123456", "$2a$10$NyvC02DJ0rlIeA0lkplDFuX/rRMc775bdxuyKIE.pIrZOMdrt2z7.");
//        System.out.println(encode);
//    }
//
//
//
//    @Test
//    void testMapper() throws JsonProcessingException {
//        String redisKey = "login:" + 1;
//        String jsonLoginUser = stringRedisTemplate.opsForValue().get(redisKey);
//        LoginUser loginUser = JSON.parseObject(jsonLoginUser, LoginUser.class);
//        System.out.println(loginUser);
//    }
//
//    @Test
//    void test2() {
//        List<String> permissionList = sysRolePermissionMapper.selectRolePermissionByUsername("clj");
//        System.out.println(permissionList);
//    }
//
//    @Test
//    void contextLoads() {
//        QueryWrapper<SysUser> sysUserQueryWrapper = new QueryWrapper<>();
//        sysUserQueryWrapper.eq("username", "clj");
//        SysUser one = sysUserService.getOne(sysUserQueryWrapper);
//        System.out.println(one);
//    }

}
