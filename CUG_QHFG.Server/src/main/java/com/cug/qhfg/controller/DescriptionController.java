package com.cug.qhfg.controller;

import com.cug.qhfg.common.Result;
import com.cug.qhfg.entity.Description;
import com.cug.qhfg.service.DescriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @Description TODO
 * @Author fjh
 * @Date 2025/6/9
 */
@RestController
@RequestMapping("/i")
public class DescriptionController {

    @Autowired
    private DescriptionService descriptionService;

    @PostMapping("/putinfo")
    public Result insertInfo(@RequestBody Description description){

        int n=descriptionService.putInfo(description);
        if (n!=0){
            return Result.success("插入成功！");
        }else {
            return Result.error("插入失败！");
        }

    }

    @PostMapping("/uploaddescription")
    public Result putDocx(@RequestParam("files") List<MultipartFile> files, @RequestParam("developer") String developer) throws Exception{

        String dataName = files.get(0).getOriginalFilename().replaceFirst("[.][^.]+$", "");

        int n=descriptionService.upload(dataName,files,developer);
        if (n!=0){
            return Result.success("上传成功！");
        }else {
            return Result.error("上传失败！");
        }

    }

    @GetMapping("/getdescription")
    public Result getDesc(@RequestParam("map") String map,@RequestParam("language") String language){

        Description description=new Description();

        if (language.equals("chinese")){
            description=descriptionService.getDescriptionZn(map);
        }else if (language.equals("english")){
            description=descriptionService.getDescriptionEn(map);
        }


        if (description==null){
            return Result.error("无数据");
        }else {
            return Result.success(description);
        }
    }
}
