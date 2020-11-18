package com.gateway.connector.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "camel")
public class WebServiceConfig {
    private String dataFormat;

    private String listenerAddress;
    /**默认的监听端口*/
    private Long defaultPort;
    /**路由id前缀*/
    private String routeIdPrefx;
    /**重试计数*/
    private Integer retrytimes;
}
