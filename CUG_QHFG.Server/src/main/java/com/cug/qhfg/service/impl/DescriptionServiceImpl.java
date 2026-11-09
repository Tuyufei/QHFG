package com.cug.qhfg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.cug.qhfg.entity.Description;
import com.cug.qhfg.mapper.DescriptionMapper;
import com.cug.qhfg.service.DescriptionService;
import com.cug.qhfg.util.utils.DocxToHtmlConverter;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


/**
 * @Description TODO
 * @Author fjh
 * @Date 2025/6/9
 */
@Service
public class DescriptionServiceImpl implements DescriptionService {

    @Autowired
    private DescriptionMapper descriptionMapper;

    @Override
    public int putInfo(Description description) {

        return descriptionMapper.insert(description);
    }

    @Override
    public int upload(String dataName, List<MultipartFile> files, String developer) throws Exception {


        File tempFileZn = File.createTempFile("temp1-", ".docx");
        files.get(0).transferTo(tempFileZn);

        File tempFileEn = File.createTempFile("temp2-", ".docx");
        files.get(1).transferTo(tempFileEn);



        String htmlZn = DocxToHtmlConverter.convertDocxFileToHtml(tempFileZn);
        String htmlEn = DocxToHtmlConverter.convertDocxFileToHtml(tempFileEn);

        // 过滤不安全html标签，防止XSS攻击
        String safeHtmlZn = Jsoup.clean(htmlZn, Safelist.relaxed());
        String safeHtmlEn = Jsoup.clean(htmlEn, Safelist.relaxed());

        LocalDateTime localDateTime=LocalDateTime.now();
        LambdaUpdateWrapper<Description> wrapper=new LambdaUpdateWrapper<>();
        wrapper.set(Description::getDescriptionZn,safeHtmlZn).set(Description::getDescriptionEn,safeHtmlEn).set(Description::getTime,localDateTime).set(Description::getDeveloper,developer)
                .eq(Description::getDataName,dataName);

        return descriptionMapper.update(wrapper);
    }

    @Override
    public Description getDescriptionZn(String map) {

        LambdaQueryWrapper<Description> wrapper=new LambdaQueryWrapper<>();
        wrapper.select(Description::getDescriptionZn).eq(Description::getMap,map);

        Description description=descriptionMapper.selectOne(wrapper);
        if (description==null){
            return null;
        }else {
            return description;
        }
    }

    @Override
    public Description getDescriptionEn(String map) {
        LambdaQueryWrapper<Description> wrapper=new LambdaQueryWrapper<>();
        wrapper.select(Description::getDescriptionEn).eq(Description::getMap,map);

        Description description=descriptionMapper.selectOne(wrapper);
        if (description==null){
            return null;
        }else {
            return description;
        }
    }
}
