package com.gateway.connector.processor;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gateway.connector.util.XmlUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.dom4j.Element;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 默认的输出处理类
 */
@Slf4j
@Component
public class DefaultOutProcessor implements Processor {

    private String returnName = "return";

    @Override
    public void process(Exchange exchange) throws Exception {
        Message message = exchange.getMessage();
        String body = message.getBody(String.class);
        log.info("获取响应信息：{}",body);
        //尝试转换信息
        Element element = XmlUtil.parseXmlStr(body);
        if (element.elements(returnName) != null && element.elements(returnName).size() > 0) {
            List<Element> results = element.elements(returnName);
            if (results.size() == 1) {
                //返回值是个对象
                if (results.get(0).isTextOnly()) {
                    message.setBody(results.get(0).getTextTrim());
                } else {
                    //有子元素
                    JSONObject res = new JSONObject();
                    addChildren(res,results.get(0));
                    message.setBody(res.toJSONString());
                }
            } else  {
                //返回值是个list
                JSONArray res = new JSONArray();
                for (Element result : results) {
                    if (result.isTextOnly()) {
                        res.add(result.getTextTrim());
                    } else {
                        JSONObject cres = new JSONObject();
                        res.add(cres);
                        addChildren(cres,result);
                    }
                }
                message.setBody(res.toJSONString());
            }
        } else {
            log.info("没有响应信息！");
            message.setBody("");
        }
    }

    /**
     * 添加属性
     * @param res
     * @param element
     */
    private void addChildren(JSONObject res, Element element) {
        for (Element child : element.elements()) {
            String name = child.getName();
            if (element.elements(name).size() > 1) {
                //有多个，说明属性是集合
                res.put(name,new JSONArray());
                if (child.isTextOnly()) {
                    res.getJSONArray(name).add(child.getTextTrim());
                } else {
                    JSONObject cres = new JSONObject();
                    res.getJSONArray(name).add(cres);
                    addChildren(cres,child);
                }
            } else {
                //单个属性,要么是字符串，要么是嵌套对象
                if (child.isTextOnly()) {
                    res.put(name,child.getTextTrim());
                } else {
                    JSONObject cres = new JSONObject();
                    res.put(name,cres);
                    addChildren(cres,child);
                }
            }
        }
    }
}
