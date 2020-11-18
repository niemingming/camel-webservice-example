package com.gateway.connector.processor;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gateway.connector.entity.ConnectorWebserviceRouteEntity;
import com.gateway.connector.util.XmlUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.dom4j.Element;

import java.math.BigDecimal;

/**
 * 请求body体的Json格式处理。
 */
@Slf4j
public class BodyProcessor extends BaseInProcessor {
    public BodyProcessor(ConnectorWebserviceRouteEntity connectorWebserviceRouteEntity) {
        super(connectorWebserviceRouteEntity);
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        Message message = exchange.getMessage();
        String body = message.getBody(String.class);
        log.info("获取请求体：{}",body);
        Element root = getRootElement();
        //转换
        try{
            // 根节点必须是对象，参数可以是数组
            JSONObject obj = JSONObject.parseObject(body);
            dealWithObject(root,obj);
            String xmlStr = XmlUtil.xml2Str(root);
            log.info("xml字符：{}",xmlStr);
            message.setBody(xmlStr);
        }catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("请求体格式不正确.",e);
        }
    }

    /**
     * 处理对象数据
     * @param root
     * @param obj
     */
    private void dealWithObject(Element root, JSONObject obj) {
        for (String key : obj.keySet()) {
            Object cobj = obj.get(key);
            if (cobj instanceof JSONObject) {
                dealWithObject(root.addElement(key), (JSONObject) cobj);
            }else if (cobj instanceof JSONArray) {
                dealWithArray(root, (JSONArray) cobj,key);
            }else if (cobj instanceof BigDecimal) {//dobule
                root.addElement(key).addText(obj.getBigDecimal(key).doubleValue() + "");
            } else {// Boolean,integer,string等按照字符串处理
                root.addElement(key).addText(obj.getString(key) + "");
            }
        }
    }


    /**
     * 处理数组数据
     * <key>value</key>
     * <key>value</key>
     * <key>value</key>
     * <key>value</key>
     * @param root
     * @param obj
     */
    private void dealWithArray(Element root, JSONArray obj,String key) {
        for (int i = 0; i < obj.size(); i++) {
            Object cobj = obj.get(i);
            if (cobj instanceof JSONObject) {
                dealWithObject(root.addElement(key), (JSONObject) cobj);
            }else if (cobj instanceof JSONArray) {
                throw new RuntimeException("数组不能嵌套数组！");
            }else if (cobj instanceof BigDecimal) {//dobule
                root.addElement(key).addText(obj.getBigDecimal(i).doubleValue() + "");
            } else {// Boolean,integer,string等按照字符串处理
                root.addElement(key).addText(obj.get(i) + "");
            }
        }
    }
}
