package com.cug.qhfg.util.utils;

import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.convert.out.html.HtmlExporterNG2;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

import java.io.StringWriter;

/**
 * @Description TODO
 * @Author fjh
 * @Date 2025/6/9
 */
public class DocxToHtmlConverter {
    public static String convertDocxFileToHtml(File file) throws Exception {
        // 加载 Word 文件
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(file);

        // 设置 HTML 导出配置
        HTMLSettings htmlSettings = new HTMLSettings();
        htmlSettings.setWmlPackage(wordMLPackage);

        // HTML 输出结果
        StringWriter stringWriter = new StringWriter();
        StreamResult result = new StreamResult(stringWriter);

        // 执行转换
        HtmlExporterNG2 exporter = new HtmlExporterNG2();
        exporter.html(wordMLPackage, result, htmlSettings);

        // 返回 HTML 字符串
        return stringWriter.toString();
    }
}
