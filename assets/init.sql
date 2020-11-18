/**webservice配置主表，用于配置路由信息*/

create table connector_webservice_route (
    id int(18) auto_increment primary key comment '主键',
    route_name varchar(64) comment '规则名称',
    route_code varchar(64) not null comment '规则编码',
    route_service_address varchar(512) not null comment '路由目标服务地址，即webservice服务地址',
    route_service_wsdlurl varchar(512) not null comment '路由的wsdl文档地址',
    route_service_operation varchar(128) not null comment '路由要调用的webservice方法',
    route_service_port int(5) default 8888 not null comment '路由监听的端口',
    route_service_path varchar(256) not null comment '监听的路径',
    route_service_targetnamespace varchar(512) not null comment '路由webservice的命名空间',
    route_service_dataformat varchar(32) not null default 'PAYLOAD' comment '路由后端的数据类型',
    route_service_in_type int(2) not null default 1 comment '路由入参处理方式，1：表单参数处理，2：json格式处理，3：自定义，暂不支持',
    route_service_out_type int (2) not null default 1 comment '路由出参处理，1：默认返回json格式/字符串',
    is_valiable tinyint(1) default 1 comment '是否启用'
) comment '路由规则配置表';