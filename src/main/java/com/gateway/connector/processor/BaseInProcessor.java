package com.gateway.connector.processor;

import com.gateway.connector.entity.ConnectorWebserviceRouteEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.camel.Processor;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;


/**
 * 通用入参处理类
 */
@Data
@AllArgsConstructor
public abstract class BaseInProcessor implements Processor {

    private ConnectorWebserviceRouteEntity connectorWebserviceRouteEntity;

    protected static String prefix = "ser";


    public Element getRootElement() {
        return DocumentHelper.createElement(prefix + ":" + connectorWebserviceRouteEntity.getRouteServiceOperation())
                .addNamespace(prefix,connectorWebserviceRouteEntity.getRouteServiceTargetnamespace());
    }

}
