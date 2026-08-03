-- app移动端 抓蛙师

set names utf8mb4;
set foreign_key_checks = 0;

-- 1、广告配置表
create table b_ad
(
    id           bigint(20)   not null comment '主键id',
    tenant_id    varchar(20)  default '000000' comment '租户id',
    appid        varchar(50)  default null comment 'appid',
    ad_unit_id   varchar(100) default null comment '广告位id',
    ad_name      varchar(100) not null comment '广告名称',
    ad_type      varchar(20)  not null comment '广告类型',
    position     varchar(50)  default null comment '投放位置',
    img          varchar(500) default null comment '广告图片',
    description  varchar(255) default null comment '描述',
    jump_appid   varchar(50)  default null comment '跳转appid',
    jump_path    varchar(255) default null comment '跳转路径',
    style_config varchar(255) default null comment '样式配置',
    sort_order   int(4)       default 999 comment '排序值',
    status       char(1)      default '1' comment '状态',
    create_dept  bigint(20)   default null comment '创建部门',
    create_by    bigint(20)   default null comment '创建人',
    create_time  datetime     default current_timestamp comment '创建时间',
    update_by    bigint(20)   default null comment '更新人',
    update_time  datetime     default current_timestamp on update current_timestamp comment '更新时间',
    remark       varchar(255) default null comment '备注',
    is_deleted   char(1)      default '0' comment '是否删除',
    primary key (id)
) engine = innodb comment = '广告配置表';

-- 2、账号绑定表
create table b_bind
(
    id            bigint(20)   not null comment '账号绑定id',
    tenant_id     varchar(20)  default '000000' comment '租户id',
    user_id       bigint(20)   default null comment '用户id',
    platform_type varchar(20)  default null comment '平台类型',
    appid         varchar(30)  default null comment 'appid',
    unionid       varchar(30)  default null comment 'unionid',
    openid        varchar(30)  default null comment 'openid',
    extra_data    varchar(255) default null comment '扩展数据',
    create_dept   bigint(20)   default null comment '创建部门',
    create_by     bigint(20)   default null comment '创建人',
    create_time   datetime     default null comment '创建时间',
    update_by     bigint(20)   default null comment '修改人',
    update_time   datetime     default null comment '更新时间',
    remark        varchar(255) default null comment '备注',
    is_deleted    char(1)      default '0' comment '是否删除',
    primary key (id)
) engine = innodb comment = '账号绑定表';

-- 3、支付配置表
create table b_payment
(
    id                 bigint(20)    not null comment '支付配置id',
    tenant_id          varchar(20)   default '000000' comment '租户id',
    type               varchar(30)   default null comment '商户类型',
    mch_name           varchar(30)   default null comment '商户名称',
    mch_id             varchar(20)   default null comment '商户号',
    mch_key            varchar(50)   default null comment '商户密钥',
    api_v3_key         varchar(50)   default null comment 'APIv3密钥',
    cert_path          varchar(2000) default null comment '证书路径(V3 PEM格式)',
    key_path           varchar(2000) default null comment '密钥路径(V3 PEM格式)',
    platform_cert_path varchar(2000) default null comment '平台证书路径(V3 PEM格式)',
    cert_serial_no     varchar(100)  default null comment '证书序列号',
    public_key_id      varchar(100)  default null comment '微信支付公钥ID(V3公钥模式专用,格式 PUB_KEY_ID_xxx)',
    p12_cert_path      varchar(500)  default null comment 'p12证书路径(V2退款专用)',
    status             char(1)       default '1' comment '状态',
    create_dept        bigint(20)    default null comment '创建部门',
    create_by          bigint(20)    default null comment '创建人',
    create_time        datetime      default null comment '创建时间',
    update_by          bigint(20)    default null comment '修改人',
    update_time        datetime      default null comment '更新时间',
    remark             varchar(255)  default null comment '备注',
    is_deleted         char(1)       default '0' comment '是否删除',
    primary key (id)
) engine = innodb comment = '支付配置表';

-- 4、平台配置表
create table b_platform
(
    id               bigint(20)    not null comment '平台配置id',
    tenant_id        varchar(20)   default '000000' comment '租户id',
    type             varchar(20)   default null comment '平台类型',
    name             varchar(20)   default null comment '名称',
    appid            varchar(30)   default null comment 'appid',
    secret           varchar(50)   default null comment '密钥',
    token            varchar(50)   default null comment '接口token',
    aeskey           varchar(50)   default null comment '加密密钥',
    payment_ids      varchar(100)  default null comment '关联支付配置',
    template_configs varchar(2000) default null comment '模板配置',
    status           char(1)       default '1' comment '状态',
    create_dept      bigint(20)    default null comment '创建部门',
    create_by        bigint(20)    default null comment '创建人',
    create_time      datetime      default null comment '创建时间',
    update_by        bigint(20)    default null comment '修改人',
    update_time      datetime      default null comment '更新时间',
    remark           varchar(255)  default null comment '备注',
    is_deleted       char(1)       default '0' comment '逻辑删除',
    primary key (id)
) engine = innodb comment = '平台配置表';

-- 5、商品表(SPU - 标准产品单位)
create table m_goods
(
    id            bigint(20)     not null comment '商品ID',
    tenant_id     varchar(20)    default '000000' comment '租户ID',
    category      varchar(30)    default null comment '商品分类',
    code          varchar(30)    default null comment '商品编码',
    name          varchar(200)   not null comment '商品名称',
    img           varchar(255)   default null comment '商品主图',
    imgs          varchar(1000)  default null comment '商品图片集(多图,逗号分隔)',
    spec_type     char(1)        default '0' comment '规格类型 (0单规格 1多规格)',
    original_price decimal(10, 2) default null comment '原价(单规格时使用)',
    discount      decimal(3, 2)  default 1.00 comment '折扣',
    price         decimal(10, 2) default null comment '价格(单规格时使用/多规格时为最低价)',
    description   text           default null comment '商品描述',
    stock         int(11)        default null comment '库存(单规格时使用/多规格时为总库存)',
    sales_count   int(11)        default 0 comment '总销量',
    status        char(1)        default '1' comment '状态 (0下架 1上架)',
    sort_order    int(4)         default 999 comment '排序',
    create_dept   bigint(20)     default null comment '创建部门',
    create_by     bigint(20)     default null comment '创建人',
    create_time   datetime       default current_timestamp comment '创建时间',
    update_by     bigint(20)     default null comment '修改人',
    update_time   datetime       default current_timestamp on update current_timestamp comment '更新时间',
    remark        varchar(255)   default null comment '备注',
    is_deleted    char(1)        default '0' comment '是否删除',
    primary key (id),
    key idx_category (category),
    key idx_status_deleted (status, is_deleted),
    key idx_spec_type (spec_type)
) engine = innodb comment = '商品表(SPU)';

-- 5.1、商品SKU表(库存量单位 - 具体规格)
create table m_goods_sku
(
    id             bigint(20)     not null comment 'SKU ID',
    tenant_id      varchar(20)    default '000000' comment '租户ID',
    goods_id       bigint(20)     not null comment '商品ID(关联m_goods.id)',
    sku_code       varchar(50)    default null comment 'SKU编码(自动生成或手动)',
    sku_name       varchar(200)   default null comment 'SKU名称(如:红色-S码)',
    spec_values    varchar(500)   not null comment '规格值JSON(如:{"颜色":"红色","尺码":"S"})',
    original_price decimal(10, 2) default null comment '原价',
    price          decimal(10, 2) not null comment '价格',
    stock          int(11)        default 0 comment '库存',
    sales_count    int(11)        default 0 comment '销量',
    img            varchar(255)   default null comment 'SKU图片(可继承商品主图)',
    is_default     char(1)        default '0' comment '是否默认SKU (0否 1是)',
    status         char(1)        default '1' comment '状态 (0停用 1启用)',
    sort_order     int(4)         default 999 comment '排序',
    create_dept    bigint(20)     default null comment '创建部门',
    create_by      bigint(20)     default null comment '创建人',
    create_time    datetime       default current_timestamp comment '创建时间',
    update_by      bigint(20)     default null comment '修改人',
    update_time    datetime       default current_timestamp on update current_timestamp comment '更新时间',
    remark         varchar(255)   default null comment '备注',
    is_deleted     char(1)        default '0' comment '是否删除',
    primary key (id),
    unique key uk_sku_code (sku_code),
    key idx_goods_id (goods_id),
    key idx_price (price),
    key idx_status_deleted (status, is_deleted)
) engine = innodb comment = '商品SKU表';

-- 6、订单表
create table m_order
(
    id             bigint(20)     not null comment '订单ID',
    tenant_id      varchar(20)    default '000000' comment '租户ID',
    order_no       varchar(32)    not null comment '订单编号',
    user_id        bigint(20)     default null comment '用户ID',
    goods_id       bigint(20)     not null comment '商品ID(SPU)',
    sku_id         bigint(20)     default null comment 'SKU ID(规格)',
    goods_name     varchar(200)   default null comment '商品名称',
    sku_name       varchar(200)   default null comment 'SKU名称(如:红色-S码)',
    spec_values    varchar(500)   default null comment '规格值JSON',
    goods_img      varchar(255)   default null comment '商品图片',
    price          decimal(10, 2) not null comment '商品单价',
    quantity       int(4)         not null comment '购买数量',
    total_amount   decimal(10, 2) not null comment '订单总金额',
    actual_amount  decimal(10, 2) default null comment '实付金额',
    order_status   varchar(20)    default 'pending' comment '订单状态',
    payment_method varchar(20)    default null comment '支付方式',
    payment_time   datetime       default null comment '支付时间',
    transaction_id varchar(64)    default null comment '交易流水号',
    buyer_remark   varchar(500)   default null comment '买家备注',
    order_ext_info varchar(500)   default null comment '订单扩展信息',
    receiver_info  varchar(500)   default null comment '收货信息',
    shipping_info  varchar(1000)  default null comment '物流信息',
    create_dept    bigint(20)     default null comment '创建部门',
    create_by      bigint(20)     default null comment '创建人',
    create_time    datetime       default current_timestamp comment '创建时间',
    update_by      bigint(20)     default null comment '修改人',
    update_time    datetime       default current_timestamp on update current_timestamp comment '更新时间',
    remark         varchar(255)   default null comment '备注',
    is_deleted     char(1)        default '0' comment '是否删除',
    primary key (id),
    unique key uk_order_no (order_no),
    key idx_user_id (user_id),
    key idx_goods_id (goods_id),
    key idx_sku_id (sku_id),
    key idx_order_status (order_status),
    key idx_create_time (create_time)
) engine = innodb comment = '订单表';

-- 初始化-菜单信息表数据

-- APP配置主菜单
-- noinspection SqlResolve
insert into sys_menu
values (2000, 'APP配置', 0, 95, 'appConfiguration', null, null, '0', '1', 'M', '1', '1', null, 'app', 103, 1,
        sysdate(), 1, sysdate(), 'APP配置目录');

-- 商城管理主菜单
-- noinspection SqlResolve
insert into sys_menu
values (2001, '商城管理', 0, 90, 'mallManage', null, null, '0', '1', 'M', '1', '1', null, 'store', 103, 1,
        sysdate(), 1, sysdate(), '商城管理目录');

-- 平台配置菜单
-- noinspection SqlResolve
insert into sys_menu
values (2010, '平台配置', 2000, 10, 'platform', 'business/base/platform/platform', null, '0', '1', 'C', '1', '1',
        'base:platform:view', 'slider', 103, 1, sysdate(), null, null, '平台配置菜单');
-- noinspection SqlResolve
insert into sys_menu
values (2011, '平台配置查询', 2010, 1, '#', '', null, '0', '1', 'F', '1', '1', 'base:platform:query', '#', 103, 1,
        sysdate(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu
values (2012, '平台配置新增', 2010, 2, '#', '', null, '0', '1', 'F', '1', '1', 'base:platform:add', '#', 103, 1,
        sysdate(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu
values (2013, '平台配置修改', 2010, 3, '#', '', null, '0', '1', 'F', '1', '1', 'base:platform:update', '#', 103, 1,
        sysdate(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu
values (2014, '平台配置删除', 2010, 4, '#', '', null, '0', '1', 'F', '1', '1', 'base:platform:delete', '#', 103, 1,
        sysdate(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu
values (2015, '平台配置导出', 2010, 5, '#', '', null, '0', '1', 'F', '1', '1', 'base:platform:export', '#', 103, 1,
        sysdate(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu
values (2016, '平台配置导入', 2010, 6, '#', '', null, '0', '1', 'F', '1', '1', 'base:platform:import', '#', 103, 1,
        sysdate(), null, null, '');

-- 支付配置菜单
-- noinspection SqlResolve
insert into sys_menu
values (2020, '支付配置', 2000, 20, 'payment', 'business/base/payment/payment', null, '0', '1', 'C', '1', '1',
        'base:payment:view', 'payment', 103, 1, sysdate(), null, null, '支付配置菜单');
-- noinspection SqlResolve
insert into sys_menu
values (2021, '支付配置查询', 2020, 1, '#', '', null, '0', '1', 'F', '1', '1', 'base:payment:query', '#', 103, 1,
        sysdate(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu
values (2022, '支付配置新增', 2020, 2, '#', '', null, '0', '1', 'F', '1', '1', 'base:payment:add', '#', 103, 1,
        sysdate(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu
values (2023, '支付配置修改', 2020, 3, '#', '', null, '0', '1', 'F', '1', '1', 'base:payment:update', '#', 103, 1,
        sysdate(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu
values (2024, '支付配置删除', 2020, 4, '#', '', null, '0', '1', 'F', '1', '1', 'base:payment:delete', '#', 103, 1,
        sysdate(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu
values (2025, '支付配置导出', 2020, 5, '#', '', null, '0', '1', 'F', '1', '1', 'base:payment:export', '#', 103, 1,
        sysdate(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu
values (2026, '支付配置导入', 2020, 6, '#', '', null, '0', '1', 'F', '1', '1', 'base:payment:import', '#', 103, 1,
        sysdate(), null, null, '');

-- 账号绑定菜单
-- noinspection SqlResolve
insert into sys_menu
values (2030, '账号绑定', 2000, 1, 'bind', 'business/base/bind/bind', null, '0', '1', 'C', '1', '1',
        'base:bind:view', 'link', 103, 1, sysdate(), null, null, '账号绑定菜单');
-- noinspection SqlResolve
insert into sys_menu
values (2031, '账号绑定查询', 2030, 1, '#', '', null, '0', '1', 'F', '1', '1', 'base:bind:query', '#', 103, 1,
        sysdate(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu
values (2032, '账号绑定新增', 2030, 2, '#', '', null, '0', '1', 'F', '1', '1', 'base:bind:add', '#', 103, 1,
        sysdate(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu
values (2033, '账号绑定修改', 2030, 3, '#', '', null, '0', '1', 'F', '1', '1', 'base:bind:update', '#', 103, 1,
        sysdate(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu
values (2034, '账号绑定删除', 2030, 4, '#', '', null, '0', '1', 'F', '1', '1', 'base:bind:delete', '#', 103, 1,
        sysdate(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu
values (2035, '账号绑定导出', 2030, 5, '#', '', null, '0', '1', 'F', '1', '1', 'base:bind:export', '#', 103, 1,
        sysdate(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu
values (2036, '账号绑定导入', 2030, 6, '#', '', null, '0', '1', 'F', '1', '1', 'base:bind:import', '#', 103, 1,
        sysdate(), null, null, '');

-- 广告配置菜单
-- noinspection SqlResolve
insert into sys_menu
values (2040, '广告配置', 2000, 30, 'ad', 'business/base/ad/ad', null, '0', '1', 'C', '1', '1', 'base:ad:view',
        'wishlist', 103, 1, sysdate(), null, null, '广告配置菜单');
-- noinspection SqlResolve
insert into sys_menu
values (2041, '广告配置查询', 2040, 1, '#', '', null, '0', '1', 'F', '1', '1', 'base:ad:query', '#', 103, 1,
        sysdate(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu
values (2042, '广告配置新增', 2040, 2, '#', '', null, '0', '1', 'F', '1', '1', 'base:ad:add', '#', 103, 1,
        sysdate(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu
values (2043, '广告配置修改', 2040, 3, '#', '', null, '0', '1', 'F', '1', '1', 'base:ad:update', '#', 103, 1,
        sysdate(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu
values (2044, '广告配置删除', 2040, 4, '#', '', null, '0', '1', 'F', '1', '1', 'base:ad:delete', '#', 103, 1,
        sysdate(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu
values (2045, '广告配置导出', 2040, 5, '#', '', null, '0', '1', 'F', '1', '1', 'base:ad:export', '#', 103, 1,
        sysdate(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu
values (2046, '广告配置导入', 2040, 6, '#', '', null, '0', '1', 'F', '1', '1', 'base:ad:import', '#', 103, 1,
        sysdate(), null, null, '');

-- 商品菜单
-- noinspection SqlResolve
insert into sys_menu
values (2110, '商品', 2001, 1, 'goods', 'business/mall/goods/goods', null, '0', '1', 'C', '1', '1',
        'mall:goods:view', 'bag', 103, 1, sysdate(), null, null, '商品菜单');
-- noinspection SqlResolve
insert into sys_menu
values (2111, '商品查询', 2110, 1, '#', '', null, '0', '1', 'F', '1', '1', 'mall:goods:query', '#', 103, 1,
        sysdate(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu
values (2112, '商品新增', 2110, 2, '#', '', null, '0', '1', 'F', '1', '1', 'mall:goods:add', '#', 103, 1,
        sysdate(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu
values (2113, '商品修改', 2110, 3, '#', '', null, '0', '1', 'F', '1', '1', 'mall:goods:update', '#', 103, 1,
        sysdate(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu
values (2114, '商品删除', 2110, 4, '#', '', null, '0', '1', 'F', '1', '1', 'mall:goods:delete', '#', 103, 1,
        sysdate(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu
values (2115, '商品导出', 2110, 5, '#', '', null, '0', '1', 'F', '1', '1', 'mall:goods:export', '#', 103, 1,
        sysdate(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu
values (2116, '商品导入', 2110, 6, '#', '', null, '0', '1', 'F', '1', '1', 'mall:goods:import', '#', 103, 1,
        sysdate(), null, null, '');

-- 订单菜单
-- noinspection SqlResolve
insert into sys_menu
values (2120, '订单', 2001, 10, 'order', 'business/mall/order/order', null, '0', '1', 'C', '1', '1',
        'mall:order:view', 'order', 103, 1, sysdate(), null, null, '订单菜单');
-- noinspection SqlResolve
insert into sys_menu
values (2121, '订单查询', 2120, 1, '#', '', null, '0', '1', 'F', '1', '1', 'mall:order:query', '#', 103, 1,
        sysdate(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu
values (2122, '订单新增', 2120, 2, '#', '', null, '0', '1', 'F', '1', '1', 'mall:order:add', '#', 103, 1,
        sysdate(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu
values (2123, '订单修改', 2120, 3, '#', '', null, '0', '1', 'F', '1', '1', 'mall:order:update', '#', 103, 1,
        sysdate(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu
values (2124, '订单删除', 2120, 4, '#', '', null, '0', '1', 'F', '1', '1', 'mall:order:delete', '#', 103, 1,
        sysdate(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu
values (2125, '订单导出', 2120, 5, '#', '', null, '0', '1', 'F', '1', '1', 'mall:order:export', '#', 103, 1,
        sysdate(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu
values (2126, '订单导入', 2120, 6, '#', '', null, '0', '1', 'F', '1', '1', 'mall:order:import', '#', 103, 1,
        sysdate(), null, null, '');

set foreign_key_checks = 1;
