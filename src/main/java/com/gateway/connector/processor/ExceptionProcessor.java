package com.gateway.connector.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.http.common.HttpMessage;
import org.apache.camel.support.DefaultExchange;
import org.apache.cxf.binding.soap.SoapFault;
import org.springframework.stereotype.Component;

import java.net.ConnectException;

/**
 * 异常处理类
 */
@Slf4j
@Component
public class ExceptionProcessor implements Processor {

    private String textType = "text/plain";

    @Override
    public void process(Exchange exchange) throws Exception {
        String message = exchange.getException().getMessage();
        Exception exception = exchange.getException();
        //设置响应头
        exchange.getMessage().setHeader(Exchange.CONTENT_TYPE,textType);
        exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE,500);
        if (exception instanceof SoapFault) { //判断是服务端异常，还是本地异常
            String part = ((SoapFault)exception).getFaultCode().getLocalPart();
            if ("server".equalsIgnoreCase(part)) {
                message = "服务端发生异常：" + message;
            } else {
                message = "调用服务发生异常，请检查服务配置：" + message;
            }
        } else if(exception.getCause() != null && exception.getCause() instanceof ConnectException) {
            message = "无法连接到服务地址：" + exception.getCause().getMessage();
            ((HttpMessage)exchange.getMessage()).getResponse().setStatus(500);
        }
        exchange.getMessage().setBody(message);
        //打印异常栈
        exception.printStackTrace();
        log.error("发生异常：{}",message);
        //已经处理后续不需要处理
        ((DefaultExchange) exchange).setErrorHandlerHandled(true);
    }
}
