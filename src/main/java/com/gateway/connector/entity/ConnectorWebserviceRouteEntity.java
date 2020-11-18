package com.gateway.connector.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * connector_webservice_route
 * @author nmm
 * @since 2020-11-03
 */
@Data
@TableName("connector_webservice_route")
public class ConnectorWebserviceRouteEntity {

    /**
    * 主键
    */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
    * 规则名称
    */
    @NotBlank(message = "路由名称不能为空")
    private String routeName;
    /**
    * 规则编码
    */
    @NotBlank(message = "路由编码不能为空")
    private String routeCode;
    /**
    * 路由目标服务地址，即webservice服务地址
    */
    @NotBlank(message = "服务地址不能为空")
    private String routeServiceAddress;
    /**
    * 路由的wsdl文档地址
    */
    private String routeServiceWsdlurl;
    /**
    * 路由要调用的webservice方法
    */
    @NotBlank(message = "调用方法不能为空")
    private String routeServiceOperation;
    /**
     * 监听地址
     */
    @NotBlank(message = "监听地址不能为空")
    private String routeServicePath;
    /**
    * 路由监听的端口
    */
    private Long routeServicePort;
    /**
    * 路由webservice的命名空间
    */
    @NotBlank(message = "目标命名空间不能为空")
    private String routeServiceTargetnamespace;
    /**
    * 路由后端的数据类型
    */
    private String routeServiceDataformat;
    /**
    * 路由入参处理方式，1：表单参数处理，2：json格式处理，3：自定义，暂不支持
    */
    private Long routeServiceInType = 1l;
    /**
    * 路由出参处理，1：默认返回json格式/字符串
    */
    private Long routeServiceOutType = 1l;
    /**
     * 是否启用
     */
    private Boolean isValiable = true;

}
