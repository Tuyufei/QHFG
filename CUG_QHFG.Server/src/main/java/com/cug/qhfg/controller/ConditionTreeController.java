package com.cug.qhfg.controller;

import com.cug.qhfg.common.Result;
import com.cug.qhfg.entity.ConditionTree;
import com.cug.qhfg.service.ConditionTreeService;
import com.cug.qhfg.util.utils.CreateConditionTree;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description TODO
 * @Author fjh
 */


@RestController
@RequestMapping("/list")
@PreAuthorize("hasAuthority('normal')")
public class ConditionTreeController {

    @Autowired
    private ConditionTreeService conditionTreeService;


    @GetMapping("/getdatalist")
    public Result getDataList(){
        List<ConditionTree> datalist=conditionTreeService.getConditionTree();


        return new Result<>().success(CreateConditionTree.cTree(datalist));
    }

    @PostMapping("/updatecondition")
    public Result updateConditionName(@RequestBody ConditionTree conditionTree){

        int n=conditionTreeService.modifyConditionName(conditionTree);
        if (n>0){
            return new Result<>().success("修改成功！");
        }else {
            return new Result<>().error("修改失败！");
        }
    }


    @PostMapping("/addChild")
    public Result addNextLevelCondition(@RequestBody ConditionTree conditionTree){

        boolean f=conditionTreeService.createNextLevelCondition(conditionTree);
        if (f){
            return new Result<>().success("添加成功！");
        }else {
            return new Result<>().error("添加失败！");
        }
    }

    @PostMapping("/delcondition")
    public Result deleteConditon(@RequestBody ConditionTree conditionTree){
        int n=conditionTreeService.removeCondition(conditionTree);
        if (n>0){
            return new Result<>().success("删除成功！");
        }else {
            return new Result<>().error("删除失败！");
        }
    }

    @GetMapping("/getresourcelist")
    public Result getDefaultList(){
        List<ConditionTree> datalist=conditionTreeService.getResourceConditions();

        return new Result<>().success(CreateConditionTree.cTree(datalist));
    }

    @GetMapping("/getdatatypelist")
    public Result getResourceList(ConditionTree conditionTree){
        List<ConditionTree> datalist=conditionTreeService.getDataTypeConditions(conditionTree);

        return new Result<>().success(CreateConditionTree.cTree(datalist));
    }

    @GetMapping("getchild")
    public Result getChildC(ConditionTree conditionTree){
        List<ConditionTree> datalist=conditionTreeService.getChildConditions(conditionTree);

        return new Result<>().success(CreateConditionTree.cTree(datalist));
    }



}
