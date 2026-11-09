package com.qiyun.qhfg.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.qiyun.qhfg.entity.Point2D;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@CrossOrigin
public class MapServiceController {

    @Value("${serverPath}")
    private String QGISServerPath;

    @Value("${qgisServerUrl}")
    private String qgisServerUrl;


    @Value("${qgisDataDir}")
    private String qgisDataDir;

    /**
     * 服务能力获取
     *
     * @param REQUEST  请求类型，包括：GetCapabilities
     * @param SERVICE  服务类型，包括：WMS\WFS\WCS\WMTS
     * @param VERSION  版本
     * @param response --
     */
    @RequestMapping("/MapServiceV1")
    public void Capabilities(String REQUEST, String SERVICE, String VERSION, HttpServletResponse response) {
        try {

            if (REQUEST == null || REQUEST.isEmpty()) {
                ReponseNotSupportSerivce(response);
            } else {
                if (VERSION == null || VERSION.isEmpty()) {
                    VERSION = "1.1.0";
                }
                if (SERVICE == null || SERVICE.isEmpty()) {
                    SERVICE = "";
                }
                if (REQUEST.compareTo("GetCapabilities") == 0) {
                    //List<String> listReq = new AbstractList<String>() {  "","",""  };

                    String url = QGISServerPath + "?" + URLUtil.encode("SERVICE=" + SERVICE + "&REQUEST=" + REQUEST + "&VERSION=" + VERSION);
                    HttpResponse httpResponse = HttpRequest.get(url).timeout(10000).execute();
                    InputStream inputStream = httpResponse.bodyStream();
                    try {
                        ServletOutputStream outputStream = response.getOutputStream();
                        byte[] bytes = IOUtils.toByteArray(inputStream);
                        if (httpResponse.getStatus() == 200) {
                            response.setContentType("text/xml");

                            String resStr = new String(bytes);
                            if (resStr.length() < 1000 && resStr.indexOf("Service unknown or unsupported. Current supported") >= 0) {
                                ReponseNotSupportSerivce(response);
                                return;
                            } else {

                                // 需要替换文本 , 用数据库中的配置路径替换 [MapServiceURL]  和 QGISServerPath
                                // 注意超大文本的效率问题
                                //String qgisServerUrl = sysConfigService.getConfig("QGIS_SERVER_URL");
                                if (!qgisServerUrl.startsWith("http://")) {
                                    qgisServerUrl = "http://" + qgisServerUrl;
                                }
                                resStr = resStr.replace("[MapServiceURL]", qgisServerUrl);
                                resStr = resStr.replace(QGISServerPath, qgisServerUrl);

                                outputStream.write(resStr.getBytes());
                            }
                        } else {
                            response.setContentType("text/xml");
                            outputStream.write(bytes);
                        }
                        outputStream.flush();
                        outputStream.close();

                    } catch (Exception ex) {
                        ReponseXMLError(" error ", response);
                        return;
                    }
                } else {
                    ReponseNotSupportSerivce(response);
                    return;
                }
            }


        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }



    /**
     * WMS服务
     *
     * @param REQUEST
     * @param VERSION
     * @param LAYERS
     * @param STYLES
     * @param SRS
     * @param BBOX
     * @param WIDTH
     * @param HEIGHT
     * @param FORMAT
     * @param TRANSPARENT
     * @param SLD
     * @param SLD_BODY
     * @param DPI
     * @param TILED
     * @param OUTFORMAT
     * @param response
     */
    @RequestMapping("/MapServiceV1/WMS")
    public void GetWMS(String REQUEST, String VERSION, String LAYERS,
                       String STYLES, String SRS, String BBOX, String WIDTH, String HEIGHT,
                       String FORMAT, String TRANSPARENT, String SLD, String SLD_BODY,
                       String DPI, String TILED,

                       String OUTFORMAT, HttpServletResponse response) {
        String SERVICE = "WMS";
        try {

            if (REQUEST == null || REQUEST.isEmpty()) {
                Capabilities("GetCapabilities", SERVICE, VERSION, response);
            } else {
                if (FORMAT == null || FORMAT.isEmpty()) {
                    FORMAT = "image/png";
                }
                if (VERSION == null || VERSION.isEmpty()) {
                    VERSION = "1.1.0";
                }

                if (REQUEST.compareTo("GetCapabilities") == 0) {
                    Capabilities("GetCapabilities", SERVICE, VERSION, response);
                } else if (REQUEST.compareTo("GetMap") == 0) {
                    if (StrUtil.isBlank(LAYERS)) {
                        noticeRequestParam(response, "请输入LAYERS参数");
                        return;
                    }
                    if (StrUtil.isBlank(SRS)) {
                        noticeRequestParam(response, "请输入SRS参数");
                        return;
                    }
                    if (StrUtil.isBlank(WIDTH)) {
                        noticeRequestParam(response, "请输入WIDTH参数");
                        return;
                    }
                    if (StrUtil.isBlank(HEIGHT)) {
                        noticeRequestParam(response, "请输入HEIGHT参数");
                        return;
                    }
                    if (StrUtil.isBlank(BBOX)) {
                        noticeRequestParam(response, "请输入BBOX参数");
                        return;
                    }
                    String[] mapLayers = LAYERS.split(":");
                    if (mapLayers.length != 2) {
                        ReponseCannotFindLayers(response);
                        return;
                    }
                    //此处从数据库获取配置太慢，需要改为后台缓存模式
                    //String qgisServerUrl = sysConfigService.getConfig("QGIS_SERVER_URL");
                    //String qgisDataDir = sysConfigService.getConfig("QGIS_DATA_DIR");
                    //先改为配置方式获取
                    String qgsMap = qgisDataDir + "/" + mapLayers[0] + ".qgs";
                    qgsMap = qgsMap.replace("\\", "/").replace("//", "/");
                    String qgsLayers = mapLayers[1];
                    //必要参数
                    String paramURL = "SERVICE=" + SERVICE + "&REQUEST=" + REQUEST + "&VERSION=" + VERSION +
                            "&MAP=" + qgsMap + "&LAYERS=" + URLUtil.encode(qgsLayers) +
                            "&SRS=" + SRS + "&WIDTH=" + WIDTH + "&HEIGHT=" + HEIGHT + "&BBOX=" + BBOX;
                    //可选参数
                    if (STYLES != null && !STYLES.isEmpty()) {
                        paramURL += "&STYLES=" + paramURL;
                    }
                    if (FORMAT != null && !FORMAT.isEmpty()) {
                        paramURL += "&FORMAT=" + FORMAT;
                    }
                    if (TRANSPARENT != null && !TRANSPARENT.isEmpty()) {
                        paramURL += "&TRANSPARENT=" + TRANSPARENT;
                    }
                    if (SLD != null && !SLD.isEmpty()) {
                        paramURL += "&SLD=" + SLD;
                    }
                    if (SLD_BODY != null && !SLD_BODY.isEmpty()) {
                        paramURL += "&SLD_BODY=" + SLD_BODY;
                    }
                    if (DPI != null && !DPI.isEmpty()) {
                        paramURL += "&DPI=" + DPI;
                    }
                    //URLUtil.encode(paramURL)
                    String url = QGISServerPath + "?" + paramURL;
                    HttpResponse httpResponse = HttpRequest.get(url).timeout(10000).execute();
                    InputStream inputStream = httpResponse.bodyStream();
                    try {
                        ServletOutputStream outputStream = response.getOutputStream();
                        byte[] bytes = IOUtils.toByteArray(inputStream);
                        if (httpResponse.getStatus() == 200) {
                            response.setContentType(FORMAT);
                            //String resStr = new String(bytes);
                            outputStream.write(bytes);
                        } else {
                            response.setContentType("text/xml");

                            String resStr = new String(bytes);
                            if (resStr.length() < 300 && resStr.indexOf("valid QGIS project file") >= 0) {
                                ReponseXMLError("没有找到工作空间信息", response);
                                return;
                            } else {

                            }
                            outputStream.write(bytes);
                        }
                        outputStream.flush();
                        outputStream.close();

                    } catch (Exception ex) {
                        ReponseXMLError(" error ", response);
                        return;
                    }

                    return;
                } else if (REQUEST.compareTo("GetFeatureInfo") == 0 || REQUEST.compareTo("GetStyle") == 0 || REQUEST.compareTo("GetStyles") == 0) {
                    //实现剩余函数
                    ReponseNotSupportFunction(response);
                } else {
                    ReponseNotSupportFunction(response);
                }
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    @RequestMapping("/MapServiceV1/WMTS")
    public void GetWMTS(String REQUEST, String VERSION, String LAYER, String FORMAT, String STYLE,
                        String TILEMATRIXSET, String TILEMATRIX, String TILEROW, String TILECOL, HttpServletResponse response) {
        String SERVICE = "WMTS";
        if (VERSION == null || VERSION.isEmpty()) {
            VERSION = "1.3.0";
        }
        try {

            if (REQUEST == null || REQUEST.isEmpty()) {
                Capabilities("GetCapabilities", SERVICE, VERSION, response);
            } else {
                if (FORMAT == null || FORMAT.isEmpty()) {
                    FORMAT = "image/png";
                }
                if (STYLE == null || STYLE.isEmpty()) {
                    STYLE = "";
                }

                if (REQUEST.compareTo("GetCapabilities") == 0) {
                    Capabilities("GetCapabilities", SERVICE, VERSION, response);
                } else if (REQUEST.compareTo("GetTile") == 0) {
                    if (StrUtil.isBlank(LAYER)) {
                        noticeRequestParam(response, "请输入LAYER参数");
                        return;
                    }

                    String[] mapLayers = LAYER.split(":");
                    if (mapLayers.length != 2) {
                        ReponseCannotFindLayers(response);
                        return;
                    }
                    //此处从数据库获取配置太慢，需要改为后台缓存模式
                    //String qgisServerUrl = sysConfigService.getConfig("QGIS_SERVER_URL");
                    //String qgisDataDir = sysConfigService.getConfig("QGIS_DATA_DIR");
                    //先改为配置方式获取
                    String qgsMap = qgisDataDir + "/" + mapLayers[0] + ".qgs";
                    qgsMap = qgsMap.replace("\\", "/").replace("//", "/");
                    String qgsLayers = mapLayers[1];

                    boolean fromWMS = true;

                    // 直接调用QGIS的服务
                    if (fromWMS) {

                        // todo: 补充缓存机制

                        int crs = 0;
                        if (TILEMATRIXSET.contains("3857") || TILEMATRIXSET.contains("900913") || TILEMATRIXSET.contains("google")) {
                            crs = 3857;
                        } else if (TILEMATRIXSET.contains("4326") || TILEMATRIXSET.contains("4490")) {
                            crs = 4326;
                        } else {
                            ReponseXMLError("can not support " + TILEMATRIXSET, response);
                            return;
                        }
                        int x = 0;
                        int y = 0;
                        int z = 0;
                        try {
                            x = Integer.parseInt(TILECOL);
                        } catch (Exception e) {
                            ReponseXMLError("TILEROW must be int , input = " + TILEMATRIXSET, response);
                            return;
                        }
                        try {
                            y = Integer.parseInt(TILEROW);
                        } catch (Exception e) {
                            ReponseXMLError("TILEROW must be int , input = " + TILEMATRIXSET, response);
                            return;
                        }
                        try {
                            z = Integer.parseInt(TILEMATRIX);
                        } catch (Exception e) {
                            ReponseXMLError("TILEMATRIX must be int , input = " + TILEMATRIXSET, response);
                            return;
                        }
                        //
                        String version = "1.3.0";
                        //String map = serviceMains.get(0).getQgsPath();
                        //String layers =  serviceMains.get(0).getAllLayers();
                        //String qgisServer = serviceMains.get(0).getServerUrl();
                        String styles = STYLE;
                        String width = "256";
                        String height = "256";

                        if (TILEMATRIXSET.indexOf("x2") > 0) {
                            width = "512";
                            height = "512";
                        }
                        String srs = "EPSG:" + String.valueOf(crs);

                        Point2D pnt1 = toPoint(x, y, z, crs);
                        Point2D pnt2 = toPoint(x + 1, y + 1, z, crs);

                        double xmin = Math.min(pnt1.getX(), pnt2.getX());
                        double xmax = Math.max(pnt1.getX(), pnt2.getX());
                        double ymin = Math.min(pnt1.getY(), pnt2.getY());
                        double ymax = Math.max(pnt1.getY(), pnt2.getY());
                        String bbox = String.format("%f,%f,%f,%f", xmin, ymin, xmax, ymax);
                        String url = QGISServerPath + "?SERVICE=WMS&REQUEST=GetMap&MAP=" + qgsMap +
                                "&VERSION=1.1.1" + "&LAYERS=" + URLUtil.encode(qgsLayers) + "&STYLES=" + styles + "&FORMAT=image/png&TRANSPARENT=true&WIDTH=" + width + "&HEIGHT=" + height + "&SRS=" + srs + "&BBOX=" + bbox;

                        HttpResponse httpResponse = HttpRequest.get(url).timeout(10000).execute();
                        InputStream inputStream = httpResponse.bodyStream();
                        try {
                            ServletOutputStream outputStream = response.getOutputStream();
                            byte[] bytes = IOUtils.toByteArray(inputStream);
                            if (httpResponse.getStatus() == 200) {
                                response.setContentType("image/png");
                                outputStream.write(bytes);

                            } else {
                                response.setContentType("text/xml");
                                outputStream.write(bytes);
                            }
                            outputStream.flush();
                            outputStream.close();

                        /*     //保存到MongoDB
                            Tiles tiles1 = new Tiles();
                            tiles1.setId(key);
                            tiles1.setContent(new Binary(bytes));
                            mongo.save(tiles1,name);*/

                        } catch (Exception ex) {
                            ReponseXMLError("wmts error", response);
                            return;
                        }

                    } else {
                        //必要参数
                        String paramURL = "SERVICE=" + SERVICE + "&REQUEST=" + REQUEST + "&VERSION=" + VERSION +
                                "&MAP=" + qgsMap + "&LAYER=" + URLUtil.encode(qgsLayers) +
                                "&TILEMATRIXSET=" + TILEMATRIXSET + "&TILEMATRIX=" + TILEMATRIX + "&TILEROW=" + TILEROW + "&TILECOL=" + TILECOL;
                        //可选参数

                        if (FORMAT != null && !FORMAT.isEmpty()) {
                            paramURL += "&FORMAT=" + FORMAT;
                        }

                        //URLUtil.encode(paramURL)
                        String url = QGISServerPath + "?" + paramURL;
                        HttpResponse httpResponse = HttpRequest.get(url).timeout(10000).execute();
                        InputStream inputStream = httpResponse.bodyStream();
                        try {
                            ServletOutputStream outputStream = response.getOutputStream();
                            byte[] bytes = IOUtils.toByteArray(inputStream);
                            if (httpResponse.getStatus() == 200) {
                                response.setContentType(FORMAT);
                                //String resStr = new String(bytes);
                                outputStream.write(bytes);
                            } else {
                                response.setContentType("text/xml");

                                String resStr = new String(bytes);
                                if (resStr.length() < 300 && resStr.indexOf("valid QGIS project file") >= 0) {
                                    ReponseXMLError("没有找到工作空间信息", response);
                                    return;
                                } else {

                                }
                                outputStream.write(resStr.getBytes());
                            }
                            outputStream.flush();
                            outputStream.close();

                        } catch (Exception ex) {
                            ReponseXMLError(" error ", response);
                            return;
                        }

                    }


                    return;
                } else if (REQUEST.compareTo("GetFeatureInfo") == 0 || REQUEST.compareTo("GetStyle") == 0 || REQUEST.compareTo("GetStyles") == 0) {
                    ////实现剩余函数
                    ReponseNotSupportFunction(response);
                } else {
                    ReponseNotSupportFunction(response);
                }
            }
        } catch (Exception ex) {
            ReponseXMLError(ex.getMessage(), response);
            log.error(ex.getMessage());
        }
    }


    public static void ReponseXMLError(String error, HttpServletResponse response) {
        try {
            String content = String.format("<ServiceExceptionReport xmlns=\"http://www.opengis.net/ogc\" version=\"1.0.0\">\n" +
                    "<ServiceException code=\"OperationNotSupported\">%s</ServiceException>\n" +
                    "</ServiceExceptionReport>", error);
            response.setContentType("text/xml;charset=UTF-8");
            ServletOutputStream outputStream = response.getOutputStream();
            outputStream.write(content.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();
        } catch (Exception ex) {

        }
    }


    private void ReponseNotSupportSerivce(HttpServletResponse response) {
        try {
            String content = "\n" +
                    "<ServiceExceptionReport xmlns=\"http://www.opengis.net/ogc\" version=\"1.3.0\">\n" +
                    " <ServiceException code=\"Service configuration error\">服务类型不能为空. 当前支持的服务类型包括(区分大小写): WMS WFS WCS WMTS SampleService</ServiceException>\n" +
                    "</ServiceExceptionReport>\n";
            response.setContentType("text/xml;charset=UTF-8");
            ServletOutputStream outputStream = response.getOutputStream();
            outputStream.write(content.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();
        } catch (Exception ex) {

        }
    }

    private void ReponseNotSupportFunction(HttpServletResponse response) {
        try {
            String content = "\n" +
                    "<ServiceExceptionReport xmlns=\"http://www.opengis.net/ogc\" version=\"1.3.0\">\n" +
                    " <ServiceException code=\"Service support error\">此函数未实现</ServiceException>\n" +
                    "</ServiceExceptionReport>\n";
            response.setContentType("text/xml;charset=UTF-8");
            ServletOutputStream outputStream = response.getOutputStream();
            outputStream.write(content.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();
        } catch (Exception ex) {

        }
    }

    private void ReponseCannotFindLayers(HttpServletResponse response) {
        try {
            String content = "\n" +
                    "<ServiceExceptionReport xmlns=\"http://www.opengis.net/ogc\" version=\"1.3.0\">\n" +
                    " <ServiceException code=\"Service support error\">无法找到图层</ServiceException>\n" +
                    "</ServiceExceptionReport>\n";
            response.setContentType("text/xml;charset=UTF-8");
            ServletOutputStream outputStream = response.getOutputStream();
            outputStream.write(content.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();
        } catch (Exception ex) {

        }
    }

    /**
     * 提示输入参数
     *
     * @param response
     * @param msg      提示语句
     */
    private void noticeRequestParam(HttpServletResponse response, String msg) {
        try {
            String content = "\n" +
                    "<ServiceExceptionReport xmlns=\"http://www.opengis.net/ogc\" version=\"1.3.0\">\n" +
                    " <ServiceException code=\"Service support error\">" + msg + "</ServiceException>\n" +
                    "</ServiceExceptionReport>\n";
            response.setContentType("text/xml;charset=UTF-8");
            ServletOutputStream outputStream = response.getOutputStream();
            outputStream.write(content.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();
        } catch (Exception ex) {

        }
    }


    protected Point2D toPoint(int x, int y, int z, int epsg) {
        if (epsg == 3857) {
            int n = (int) (Math.pow((double) 2, (double) z) + 0.01);
            double lon = (double) x / (double) n * 360.0 - 180.0;
            double lat = Math.atan(Math.sinh(Math.PI * (1.0 - 2.0 * (double) y / (double) n))) * 180.0 / Math.PI;
            return lonLat2WebMercator(new Point2D(lon, lat));
        } else if (epsg == 4326) {
            double lon = 0.0, lat = 0.0;
            double resolution = 360.0 / 256.0 / Math.pow(2.0, z);
            lon = x * 256.0 * resolution - 180.0;
            lat = 90 - y * 256.0 * resolution;

            return new Point2D(lon, lat);
        }
        return null;

    }


    protected Point2D lonLat2WebMercator(Point2D lonLat) {
        double x = lonLat.getX() * 20037508.34 / 180.0;
        double y = Math.log(Math.tan((90.0 + lonLat.getY()) * Math.PI / 360.0)) / (Math.PI / 180.0);
        y = y * 20037508.34 / 180;
        Point2D mercator = new Point2D(x, y);
        return mercator;
    }



}


