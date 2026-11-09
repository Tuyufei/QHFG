package com.cug.qhfg.service;

import com.cug.qhfg.entity.Description;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @Description TODO
 * @Author fjh
 * @Date 2025/6/9
 */
public interface DescriptionService {

    public int putInfo(Description description);

    public int upload(String dataName, List<MultipartFile> files, String developer) throws Exception;

    public Description getDescriptionZn(String map);

    public Description getDescriptionEn(String map);
}
