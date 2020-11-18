package com.gateway.connector.util;

import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.IOException;
import java.io.StringWriter;

/**
 * xml工具类
 */
public class XmlUtil {

    //输出xml字符串
    public static String xml2Str(Element element) throws IOException {
        StringWriter sw = new StringWriter();
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("UTF-8");
        XMLWriter writer = new XMLWriter(sw,format);
        try{
            writer.write(element);
        }finally {
            writer.close();
        }
        return sw.toString();
    }

    /**
     * 解析xml获取xml对象
     * @param xmlStr
     * @return
     */
    public static Element parseXmlStr(String xmlStr) throws DocumentException {
        return DocumentHelper.parseText(xmlStr).getRootElement();
    }

}
