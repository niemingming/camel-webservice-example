package com.gateway.connector.processor;


import com.gateway.connector.entity.ConnectorWebserviceRouteEntity;
import com.gateway.connector.util.XmlUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.http.common.HttpMessage;
import org.dom4j.Element;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Optional;

/**
 * 参数processor，根据参数key-value生成数据结构
 */
@Slf4j
public class ParameterProcessor extends BaseInProcessor {

    public ParameterProcessor(ConnectorWebserviceRouteEntity connectorWebserviceRouteEntity) {
        super(connectorWebserviceRouteEntity);
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        HttpMessage message = (HttpMessage) exchange.getMessage();
        Element root = getRootElement();
        //获取所有入参进行数据格式组装
        HttpServletRequest request = message.getRequest();
        Enumeration<String> names = request.getParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            root.addElement(name).addText(Optional.ofNullable(request.getParameter(name)).orElse(""));
        }
        try{
            message.setBody(XmlUtil.xml2Str(root));
        } catch (Exception e) {
            throw new RuntimeException("解析xml字符失败！");
        }
    }
}
