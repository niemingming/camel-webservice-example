package com.gateway.connector.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gateway.connector.config.WebServiceConfig;
import com.gateway.connector.container.RouteContainer;
import com.gateway.connector.entity.ConnectorWebserviceRouteEntity;
import com.gateway.connector.service.ConnectorWebserviceRouteService;
import com.gateway.connector.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 *  前端控制器
 *
 * @author nmm
 * @since 2020-11-03
 */
@RestController
@RequestMapping("/api/v1/routes")
public class ConnectorWebserviceRouteController {

    @Autowired
    private WebServiceConfig webServiceConfig;
    @Autowired
    private RouteContainer routeContainer;

    @Autowired
    private ConnectorWebserviceRouteService connectorWebserviceRouteService;

    /**
     * 添加路由
     * @param routeEntity
     * @return
     */
    @PostMapping("/")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity addRoute(@RequestBody @Validated ConnectorWebserviceRouteEntity routeEntity) throws Exception {
        //设置默认值
        String wsdlUrl = Optional.ofNullable(routeEntity.getRouteServiceWsdlurl()).orElse(routeEntity.getRouteServiceAddress() + "?wsdl");
        String dataFormat = Optional.ofNullable(routeEntity.getRouteServiceDataformat()).orElse(webServiceConfig.getDataFormat());
        Long port = Optional.ofNullable(routeEntity.getRouteServicePort()).orElse(webServiceConfig.getDefaultPort());
        String routePath = routeEntity.getRouteServicePath();
        if (!routePath.startsWith("/")) {
            routePath = "/" + routePath;
        }
        routeEntity.setRouteServiceWsdlurl(wsdlUrl);
        routeEntity.setRouteServiceDataformat(dataFormat);
        routeEntity.setRouteServicePort(port);
        routeEntity.setRouteServicePath(routePath);
        //判断是否有重复
        LambdaQueryWrapper<ConnectorWebserviceRouteEntity> checkQuery = new LambdaQueryWrapper<>();
        checkQuery.eq(ConnectorWebserviceRouteEntity::getRouteCode,routeEntity.getRouteCode());
        int count = connectorWebserviceRouteService.count(checkQuery);
        if (count > 0) {
            return ResponseEntity.ok(R.error("路由编码已经存在！").toString());
        }
        if (connectorWebserviceRouteService.save(routeEntity)) {
            routeContainer.addRoute(routeEntity);
            return ResponseEntity.ok("success");
        }
        return ResponseEntity.ok(R.error("保存失败！").toString());
    }

    /**
     * 删除某个路由
     * @param id
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @DeleteMapping("/{id}")
    public ResponseEntity removeRoute(@PathVariable Long id) throws Exception {

        ConnectorWebserviceRouteEntity routeEntity = connectorWebserviceRouteService.getById(id);
        if (routeEntity == null) {
            return ResponseEntity.ok(R.error("未找到路由信息！").toString());
        }
        routeEntity.setIsValiable(false);
        //仅仅修改状态
        connectorWebserviceRouteService.updateById(routeEntity);
        routeContainer.removeRoute(id);
        return ResponseEntity.ok("success");
    }

    /**
     * 更新路由
     * @param routeEntity
     * @return
     */
    @PutMapping("/")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity updateRoute(@RequestBody ConnectorWebserviceRouteEntity routeEntity) throws Exception {
        Assert.notNull(routeEntity.getId(),"路由id不能为空！");

        ConnectorWebserviceRouteEntity oldEntity = connectorWebserviceRouteService.getById(routeEntity.getId());
        boolean shouldUpdateRoute = false;
        if(!routeEntity.getIsValiable() //不可用了
                || !Optional.ofNullable(routeEntity.getRouteServicePath()).orElse(oldEntity.getRouteServicePath()).equals(oldEntity.getRouteServicePath()) //监听路径改变
                || !Optional.ofNullable(routeEntity.getRouteServiceWsdlurl()).orElse(oldEntity.getRouteServiceWsdlurl()).equals(oldEntity.getRouteServiceWsdlurl()) //wsdl地址变更
                || !Optional.ofNullable(routeEntity.getRouteServiceOperation()).orElse(oldEntity.getRouteServiceOperation()).equals(oldEntity.getRouteServiceOperation()) //操作方法变更
                || !Optional.ofNullable(routeEntity.getRouteServiceTargetnamespace()).orElse(oldEntity.getRouteServiceTargetnamespace()).equals(oldEntity.getRouteServiceTargetnamespace()) //命名空间变更
                || !Optional.ofNullable(routeEntity.getRouteServiceAddress()).orElse(oldEntity.getRouteServiceAddress()).equals(oldEntity.getRouteServiceAddress())//服务地址变更
                || Optional.ofNullable(routeEntity.getRouteServiceInType()).orElse(oldEntity.getRouteServiceInType()) != oldEntity.getRouteServiceInType()//入参类型变更
        ) {
            shouldUpdateRoute = true;
        }
        boolean success = connectorWebserviceRouteService.updateById(routeEntity);
        if (success && shouldUpdateRoute) {
            routeContainer.removeRoute(routeEntity.getId());
        }
        return ResponseEntity.ok("success");
    }

    /**
     * 查询所有路由信息
     * @return
     */
    @GetMapping("/")
    public ResponseEntity listRoutes() {

        return null;
    }

}
