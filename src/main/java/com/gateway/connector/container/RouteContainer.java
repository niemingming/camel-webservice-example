package com.gateway.connector.container;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gateway.connector.config.WebServiceConfig;
import com.gateway.connector.entity.ConnectorWebserviceRouteEntity;
import com.gateway.connector.processor.*;
import com.gateway.connector.service.ConnectorWebserviceRouteService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Route;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 路由初始化容器
 */
@Data
@Slf4j
@Component
@EnableScheduling
public class RouteContainer {
    @Autowired
    private CamelContext camelContext;
    @Autowired
    private ConnectorWebserviceRouteService routeService;
    @Autowired
    private WebServiceConfig webServiceConfig;
    @Autowired
    private DefaultOutProcessor defaultOutProcessor;
    @Autowired
    private ExceptionProcessor exceptionProcessor;

    private String webserviceSchema = "cxf:";
    /**重试次数*/
    private ConcurrentHashMap<String,Integer> retryTimes = new ConcurrentHashMap<>();

    /**
     * 废弃该方法，该方法在异常时导致系统无法启动
     * @throws Exception
     */
    @Deprecated
    public void afterPropertiesSet() throws Exception {
        //加载数据
        LambdaQueryWrapper<ConnectorWebserviceRouteEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConnectorWebserviceRouteEntity::getIsValiable,true);
        List<ConnectorWebserviceRouteEntity> routeEntities = routeService.list(queryWrapper);
        addRoutes(routeEntities);
    }

    /**
     * 动态添加单个路由
     * @param routeEntity
     * @throws Exception
     */
    public void addRoute(ConnectorWebserviceRouteEntity routeEntity) throws Exception {
        addRoutes(Arrays.asList(routeEntity));
    }

    /**
     * 删除某个路由
     */
    public void removeRoute(Long id) throws Exception {
        String routeId = webServiceConfig.getRouteIdPrefx() + id;
        log.info("尝试删除路由：{}",routeId);
        if (camelContext.getRoute(routeId) == null) {
            log.info("路由未启动！");
            return;
        }
        //尝试停止路由，并删除
        camelContext.getRouteController().stopRoute(routeId);
        if (camelContext.removeRoute(routeId)) {
            return;
        }
        throw new RuntimeException("删除路由失败");
    }

    /**
     * 添加路由
     * @param routeEntities
     * @throws Exception
     */
    public void addRoutes(List<ConnectorWebserviceRouteEntity> routeEntities) {
        if (routeEntities.isEmpty()) {
            return;
        }
        RouteBuilder r = new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                errorHandler(defaultErrorHandler().onExceptionOccurred(exceptionProcessor));

                for (ConnectorWebserviceRouteEntity routeEntity : routeEntities) {
                    //路由id
                    String routeId = webServiceConfig.getRouteIdPrefx() + routeEntity.getId();
                    log.info("尝试添加路由：{}",routeId);
                    //监听地址
                    String listenerAddress = webServiceConfig.getListenerAddress() + ":" + routeEntity.getRouteServicePort() + routeEntity.getRouteServicePath();
                    //转换地址
                    String cxfurl = new StringBuilder(webserviceSchema).append(routeEntity.getRouteServiceAddress())
                            .append("?wsdlURL=").append(routeEntity.getRouteServiceWsdlurl())
                            .append("&defaultOperationName=").append(routeEntity.getRouteServiceOperation())
                            .append("&dataFormat=").append(Optional.ofNullable(routeEntity.getRouteServiceDataformat()).orElse(webServiceConfig.getDataFormat()))
                            .toString();
                    // 暂时只处理请求参数和请求体json串
                    BaseInProcessor processor = routeEntity.getRouteServiceInType() == 1 ? new ParameterProcessor(routeEntity) : new BodyProcessor(routeEntity);
                    //添加路由
                    from(listenerAddress)
                            .process(processor).routeId(routeId)
                            .to(cxfurl)
                            .process(defaultOutProcessor)
                            ;
                    retryTimes.put(routeId,retryTimes.getOrDefault(routeId,-1) + 1 );
                }
            }
        };
        //添加路由集合
        try {
            camelContext.addRoutes(r);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Scheduled(fixedDelayString = "${camel.refreshPeriod}")
    public void refresh(){
        log.info("开始同步路由信息！");
        //打印所有路由状态
        List<String> routeId = camelContext.getRoutes().stream().map(Route::getRouteId).collect(Collectors.toList());
        //表示成功了，我们就删除重试计数
        for (String id : routeId) {
            retryTimes.remove(id);
        }
        List<String> alertRoute = new ArrayList<>();
        if (webServiceConfig.getRetrytimes() > 0 ) {
            alertRoute = retryTimes.entrySet().stream().filter(e -> e.getValue() >= webServiceConfig.getRetrytimes())
                    .map(Map.Entry::getKey).collect(Collectors.toList());
            //给出提示
            if (!alertRoute.isEmpty()) {
                log.error("下列路由重试{}次，仍未启动成功：{}",webServiceConfig.getRetrytimes(),alertRoute);
            }
        }
        //已经启动的Route
        List<Long> startedRoute = routeId.stream().map(id -> Long.parseLong(id.substring(webServiceConfig.getRouteIdPrefx().length()))).collect(Collectors.toList());
        //需要废弃的路由
        List<Long> fqRoute = alertRoute.stream().map(id -> Long.parseLong(id.substring(webServiceConfig.getRouteIdPrefx().length()))).collect(Collectors.toList());


        LambdaQueryWrapper<ConnectorWebserviceRouteEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConnectorWebserviceRouteEntity::getIsValiable,true);
        List<ConnectorWebserviceRouteEntity> routeEntities = routeService.list(queryWrapper);
        //后补添加的routes
        List<ConnectorWebserviceRouteEntity> addRoutes = routeEntities.stream()
                .filter( routeEntity -> !startedRoute.contains(routeEntity.getId()) && !fqRoute.contains(routeEntity.getId()))
                .collect(Collectors.toList());
        try {
            addRoutes(addRoutes);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("同步路由信息失败！");
        }
        //删除routes
        List<Long> ids = routeEntities.stream()
                .map(ConnectorWebserviceRouteEntity::getId).collect(Collectors.toList());
        List<Long> removes = startedRoute.stream().filter(id -> !ids.contains(id)).collect(Collectors.toList());
        for (Long removeId : removes) {
            try {
                if (!camelContext.removeRoute(webserviceSchema + routeId)) {
                    log.warn("删除路由信息失败！");
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("删除路由信息失败！");
            }
        }
        log.info("完成同步路由信息！");
    }
}
