package com.cug.qhfg.service;

import com.cug.qhfg.common.Result;
import org.springframework.http.HttpHeaders;

import java.util.Map;
import java.util.Objects;

/**
 * @Description TODO
 * @Author fjh
 * @Date 2024/12/23
 */
public interface FetchGISMapService {

    public byte[] fetchMap(Map<String, Object> params);

    Double getPointValue(double longitude, double latitude, String map, String layers);

    byte[] fetchBoundary(Map<String, Object> params);

    Map<String, Object> extractBasicInfo(String url,
                                         Map<String, Object> params,
                                         HttpHeaders headers,
                                         String[] keys,
                                         boolean useColumnMap);

    Map<String, Object> getAreaOfPoint(double longitude, double latitude);
}
