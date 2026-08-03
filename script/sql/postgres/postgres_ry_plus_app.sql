-- app移动端 抓蛙师

-- 1、广告配置表
create table if not exists b_ad
(
    id           int8 not null,
    tenant_id    varchar(20)  default '000000'::varchar,
    appid        varchar(50)  default null::varchar,
    ad_unit_id   varchar(100) default null::varchar,
    ad_name      varchar(100) not null,
    ad_type      varchar(20)  not null,
    position     varchar(50)  default null::varchar,
    img          varchar(500) default null::varchar,
    description  varchar(255) default null::varchar,
    jump_appid   varchar(50)  default null::varchar,
    jump_path    varchar(255) default null::varchar,
    style_config varchar(255) default null::varchar,
    sort_order   int4         default 999,
    status       char         default '1'::bpchar,
    create_dept  int8         default null,
    create_by    int8         default null,
    create_time  timestamp    default now(),
    update_by    int8         default null,
    update_time  timestamp    default now(),
    remark       varchar(255) default null::varchar,
    is_deleted   char         default '0'::bpchar,
    constraint "b_ad_pk" primary key (id)
);

comment on table b_ad                   is '广告配置表';
comment on column b_ad.id               is '主键id';
comment on column b_ad.tenant_id        is '租户id';
comment on column b_ad.appid            is 'appid';
comment on column b_ad.ad_unit_id       is '广告位id';
comment on column b_ad.ad_name          is '广告名称';
comment on column b_ad.ad_type          is '广告类型';
comment on column b_ad.position         is '投放位置';
comment on column b_ad.img              is '广告图片';
comment on column b_ad.description      is '描述';
comment on column b_ad.jump_appid       is '跳转appid';
comment on column b_ad.jump_path        is '跳转路径';
comment on column b_ad.style_config     is '样式配置';
comment on column b_ad.sort_order       is '排序值';
comment on column b_ad.status           is '状态';
comment on column b_ad.create_dept      is '创建部门';
comment on column b_ad.create_by        is '创建人';
comment on column b_ad.create_time      is '创建时间';
comment on column b_ad.update_by        is '更新人';
comment on column b_ad.update_time      is '更新时间';
comment on column b_ad.remark           is '备注';
comment on column b_ad.is_deleted       is '是否删除';

-- 2、账号绑定表
create table if not exists b_bind
(
    id            int8,
    tenant_id     varchar(20)  default '000000'::varchar,
    user_id       int8         default null,
    platform_type varchar(20)  default null::varchar,
    appid         varchar(30)  default null::varchar,
    unionid       varchar(30)  default null::varchar,
    openid        varchar(30)  default null::varchar,
    extra_data    varchar(255) default null::varchar,
    create_dept   int8         default null,
    create_by     int8         default null,
    create_time   timestamp    default null,
    update_by     int8         default null,
    update_time   timestamp    default null,
    remark        varchar(255) default null::varchar,
    is_deleted    char         default '0'::bpchar,
    constraint "b_bind_pk" primary key (id)
);

comment on table b_bind                     is '账号绑定表';
comment on column b_bind.id                 is '账号绑定id';
comment on column b_bind.tenant_id          is '租户id';
comment on column b_bind.user_id            is '用户id';
comment on column b_bind.platform_type      is '平台类型';
comment on column b_bind.appid              is 'appid';
comment on column b_bind.unionid            is 'unionid';
comment on column b_bind.openid             is 'openid';
comment on column b_bind.extra_data         is '扩展数据';
comment on column b_bind.create_dept        is '创建部门';
comment on column b_bind.create_by          is '创建人';
comment on column b_bind.create_time        is '创建时间';
comment on column b_bind.update_by          is '修改人';
comment on column b_bind.update_time        is '更新时间';
comment on column b_bind.remark             is '备注';
comment on column b_bind.is_deleted         is '是否删除';

-- 3、支付配置表
create table if not exists b_payment
(
    id                 int8,
    tenant_id          varchar(20)   default '000000'::varchar,
    type               varchar(30)   default null::varchar,
    mch_name           varchar(30)   default null::varchar,
    mch_id             varchar(20)   default null::varchar,
    mch_key            varchar(50)   default null::varchar,
    api_v3_key         varchar(50)   default null::varchar,
    cert_path          varchar(2000) default null::varchar,
    key_path           varchar(2000) default null::varchar,
    platform_cert_path varchar(2000) default null::varchar,
    cert_serial_no     varchar(100)  default null::varchar,
    public_key_id      varchar(100)  default null::varchar,
    p12_cert_path      varchar(500)  default null::varchar,
    status             char          default '1'::bpchar,
    create_dept        int8          default null,
    create_by          int8          default null,
    create_time        timestamp     default null,
    update_by          int8          default null,
    update_time        timestamp     default null,
    remark             varchar(255)  default null::varchar,
    is_deleted         char          default '0'::bpchar,
    constraint "b_payment_pk" primary key (id)
);

comment on table b_payment                    is '支付配置表';
comment on column b_payment.id                 is '支付配置id';
comment on column b_payment.tenant_id          is '租户id';
comment on column b_payment.type               is '商户类型';
comment on column b_payment.mch_name           is '商户名称';
comment on column b_payment.mch_id             is '商户号';
comment on column b_payment.mch_key            is '商户密钥';
comment on column b_payment.api_v3_key         is 'APIv3密钥';
comment on column b_payment.cert_path          is '证书路径(V3 PEM格式)';
comment on column b_payment.key_path           is '密钥路径(V3 PEM格式)';
comment on column b_payment.platform_cert_path is '平台证书路径(V3 PEM格式)';
comment on column b_payment.cert_serial_no     is '证书序列号';
comment on column b_payment.public_key_id      is '微信支付公钥ID(V3公钥模式专用,格式 PUB_KEY_ID_xxx)';
comment on column b_payment.p12_cert_path      is 'p12证书路径(V2退款专用)';
comment on column b_payment.status             is '状态';
comment on column b_payment.create_dept        is '创建部门';
comment on column b_payment.create_by          is '创建人';
comment on column b_payment.create_time        is '创建时间';
comment on column b_payment.update_by          is '修改人';
comment on column b_payment.update_time        is '更新时间';
comment on column b_payment.remark             is '备注';
comment on column b_payment.is_deleted         is '是否删除';

-- 4、平台配置表
create table if not exists b_platform
(
    id               int8,
    tenant_id        varchar(20)   default '000000'::varchar,
    type             varchar(20)   default null::varchar,
    name             varchar(20)   default null::varchar,
    appid            varchar(30)   default null::varchar,
    secret           varchar(50)   default null::varchar,
    token            varchar(50)   default null::varchar,
    aeskey           varchar(50)   default null::varchar,
    payment_ids      varchar(100)  default null::varchar,
    template_configs varchar(2000) default null::varchar,
    status           char          default '1'::bpchar,
    create_dept      int8          default null,
    create_by        int8          default null,
    create_time      timestamp     default null,
    update_by        int8          default null,
    update_time      timestamp     default null,
    remark           varchar(255)  default null::varchar,
    is_deleted       char          default '0'::bpchar,
    constraint "b_platform_pk" primary key (id)
);

comment on table b_platform                       is '平台配置表';
comment on column b_platform.id                   is '平台配置id';
comment on column b_platform.tenant_id            is '租户id';
comment on column b_platform.type                 is '平台类型';
comment on column b_platform.name                 is '名称';
comment on column b_platform.appid                is 'appid';
comment on column b_platform.secret               is '密钥';
comment on column b_platform.token                is '接口token';
comment on column b_platform.aeskey               is '加密密钥';
comment on column b_platform.payment_ids          is '关联支付配置';
comment on column b_platform.template_configs     is '模板配置';
comment on column b_platform.status               is '状态';
comment on column b_platform.create_dept          is '创建部门';
comment on column b_platform.create_by            is '创建人';
comment on column b_platform.create_time          is '创建时间';
comment on column b_platform.update_by            is '修改人';
comment on column b_platform.update_time          is '更新时间';
comment on column b_platform.remark               is '备注';
comment on column b_platform.is_deleted           is '逻辑删除';

-- 5、商品表(SPU - 标准产品单位)
create table if not exists m_goods
(
    id             int8 not null,
    tenant_id      varchar(20)    default '000000'::varchar,
    category       varchar(30)    default null::varchar,
    code           varchar(30)    default null::varchar,
    name           varchar(200)   not null,
    img            varchar(255)   default null::varchar,
    imgs           varchar(1000)  default null::varchar,
    spec_type      char           default '0'::bpchar,
    original_price decimal(10,2)  default null,
    discount       decimal(3,2)   default 1.00,
    price          decimal(10,2)  default null,
    description    text           default null,
    stock          int4           default null,
    sales_count    int4           default 0,
    status         char           default '1'::bpchar,
    sort_order     int4           default 999,
    create_dept    int8           default null,
    create_by      int8           default null,
    create_time    timestamp      default now(),
    update_by      int8           default null,
    update_time    timestamp      default now(),
    remark         varchar(255)   default null::varchar,
    is_deleted     char           default '0'::bpchar,
    constraint "m_goods_pk" primary key (id)
    );

create index if not exists idx_m_goods_category on m_goods (category);
create index if not exists idx_m_goods_status_deleted on m_goods (status, is_deleted);
create index if not exists idx_m_goods_spec_type on m_goods (spec_type);

comment on table m_goods                   is '商品表(SPU)';
comment on column m_goods.id               is '商品ID';
comment on column m_goods.tenant_id        is '租户ID';
comment on column m_goods.category         is '商品分类';
comment on column m_goods.code             is '商品编码';
comment on column m_goods.name             is '商品名称';
comment on column m_goods.img              is '商品主图';
comment on column m_goods.imgs             is '商品图片集(多图,逗号分隔)';
comment on column m_goods.spec_type        is '规格类型 (0单规格 1多规格)';
comment on column m_goods.original_price   is '原价(单规格时使用)';
comment on column m_goods.discount         is '折扣';
comment on column m_goods.price            is '价格(单规格时使用/多规格时为最低价)';
comment on column m_goods.description      is '商品描述';
comment on column m_goods.stock            is '库存(单规格时使用/多规格时为总库存)';
comment on column m_goods.sales_count      is '总销量';
comment on column m_goods.status           is '状态 (0下架 1上架)';
comment on column m_goods.sort_order       is '排序';
comment on column m_goods.create_dept      is '创建部门';
comment on column m_goods.create_by        is '创建人';
comment on column m_goods.create_time      is '创建时间';
comment on column m_goods.update_by        is '修改人';
comment on column m_goods.update_time      is '更新时间';
comment on column m_goods.remark           is '备注';
comment on column m_goods.is_deleted       is '是否删除';

-- 5.1、商品SKU表(库存量单位 - 具体规格)
create table if not exists m_goods_sku
(
    id             int8 not null,
    tenant_id      varchar(20)    default '000000'::varchar,
    goods_id       int8           not null,
    sku_code       varchar(50)    default null::varchar,
    sku_name       varchar(200)   default null::varchar,
    spec_values    varchar(500)   not null,
    original_price decimal(10,2)  default null,
    price          decimal(10,2)  not null,
    stock          int4           default 0,
    sales_count    int4           default 0,
    img            varchar(255)   default null::varchar,
    status         char           default '1'::bpchar,
    sort_order     int4           default 999,
    is_default     char           default '0'::bpchar,
    create_dept    int8           default null,
    create_by      int8           default null,
    create_time    timestamp      default now(),
    update_by      int8           default null,
    update_time    timestamp      default now(),
    remark         varchar(255)   default null::varchar,
    is_deleted     char           default '0'::bpchar,
    constraint "m_goods_sku_pk" primary key (id),
    constraint "uk_m_goods_sku_code" unique (sku_code)
    );

create index if not exists idx_m_goods_sku_goods_id on m_goods_sku (goods_id);
create index if not exists idx_m_goods_sku_price on m_goods_sku (price);
create index if not exists idx_m_goods_sku_status_deleted on m_goods_sku (status, is_deleted);

comment on table m_goods_sku                   is '商品SKU表';
comment on column m_goods_sku.id               is 'SKU ID';
comment on column m_goods_sku.tenant_id        is '租户ID';
comment on column m_goods_sku.goods_id         is '商品ID(关联m_goods.id)';
comment on column m_goods_sku.sku_code         is 'SKU编码(自动生成或手动)';
comment on column m_goods_sku.sku_name         is 'SKU名称(如:红色-S码)';
comment on column m_goods_sku.spec_values      is '规格值JSON(如:{"颜色":"红色","尺码":"S"})';
comment on column m_goods_sku.original_price   is '原价';
comment on column m_goods_sku.price            is '价格';
comment on column m_goods_sku.stock            is '库存';
comment on column m_goods_sku.sales_count      is '销量';
comment on column m_goods_sku.img              is 'SKU图片(可继承商品主图)';
comment on column m_goods_sku.status           is '状态 (0停用 1启用)';
comment on column m_goods_sku.sort_order       is '排序';
comment on column m_goods_sku.is_default       is '是否默认SKU (0否 1是)';
comment on column m_goods_sku.create_dept      is '创建部门';
comment on column m_goods_sku.create_by        is '创建人';
comment on column m_goods_sku.create_time      is '创建时间';
comment on column m_goods_sku.update_by        is '修改人';
comment on column m_goods_sku.update_time      is '更新时间';
comment on column m_goods_sku.remark           is '备注';
comment on column m_goods_sku.is_deleted       is '是否删除';

-- 6、订单表
create table if not exists m_order
(
    id             int8 not null,
    tenant_id      varchar(20)    default '000000'::varchar,
    order_no       varchar(32)    not null,
    user_id        int8           default null,
    goods_id       int8           not null,
    sku_id         int8           default null,
    goods_name     varchar(200)   default null::varchar,
    sku_name       varchar(200)   default null::varchar,
    spec_values    varchar(500)   default null::varchar,
    goods_img      varchar(255)   default null::varchar,
    price          decimal(10,2)  not null,
    quantity       int4           not null,
    total_amount   decimal(10,2)  not null,
    actual_amount  decimal(10,2)  default null,
    order_status   varchar(20)    default 'pending'::varchar,
    payment_method varchar(20)    default null::varchar,
    payment_time   timestamp      default null,
    transaction_id varchar(64)    default null::varchar,
    buyer_remark   varchar(500)   default null::varchar,
    order_ext_info varchar(500)   default null::varchar,
    receiver_info  varchar(500)   default null::varchar,
    shipping_info  varchar(1000)   default null::varchar,
    create_dept    int8           default null,
    create_by      int8           default null,
    create_time    timestamp      default now(),
    update_by      int8           default null,
    update_time    timestamp      default now(),
    remark         varchar(255)   default null::varchar,
    is_deleted     char           default '0'::bpchar,
    constraint "m_order_pk" primary key (id),
    constraint "uk_m_order_no" unique (order_no)
);

create index if not exists idx_m_order_user_id on m_order (user_id);
create index if not exists idx_m_order_goods_id on m_order (goods_id);
create index if not exists idx_m_order_sku_id on m_order (sku_id);
create index if not exists idx_m_order_status on m_order (order_status);
create index if not exists idx_m_order_create_time on m_order (create_time);

comment on table m_order                      is '订单表';
comment on column m_order.id                  is '订单ID';
comment on column m_order.tenant_id           is '租户ID';
comment on column m_order.order_no            is '订单编号';
comment on column m_order.user_id             is '用户ID';
comment on column m_order.goods_id            is '商品ID(SPU)';
comment on column m_order.sku_id              is 'SKU ID(规格)';
comment on column m_order.goods_name          is '商品名称';
comment on column m_order.sku_name            is 'SKU名称(如:红色-S码)';
comment on column m_order.spec_values         is '规格值JSON';
comment on column m_order.goods_img           is '商品图片';
comment on column m_order.price               is '商品单价';
comment on column m_order.quantity            is '购买数量';
comment on column m_order.total_amount        is '订单总金额';
comment on column m_order.actual_amount       is '实付金额';
comment on column m_order.order_status        is '订单状态';
comment on column m_order.payment_method      is '支付方式';
comment on column m_order.payment_time        is '支付时间';
comment on column m_order.transaction_id      is '交易流水号';
comment on column m_order.buyer_remark        is '买家备注';
comment on column m_order.order_ext_info      is '订单扩展信息';
comment on column m_order.receiver_info       is '收货信息';
comment on column m_order.shipping_info       is '物流信息';
comment on column m_order.create_dept         is '创建部门';
comment on column m_order.create_by           is '创建人';
comment on column m_order.create_time         is '创建时间';
comment on column m_order.update_by           is '修改人';
comment on column m_order.update_time         is '更新时间';
comment on column m_order.remark              is '备注';
comment on column m_order.is_deleted          is '是否删除';

-- 初始化-菜单信息表数据

-- APP配置主菜单
-- noinspection SqlResolve
insert into sys_menu values('2000', 'APP配置', '0', '95', 'appConfiguration', null, null, '0', '1', 'M', '1', '1', null, 'app', 103, 1, now(), 1, now(), 'APP配置目录');

-- 商城管理主菜单
-- noinspection SqlResolve
insert into sys_menu values('2001', '商城管理', '0', '90', 'mallManage', null, null, '0', '1', 'M', '1', '1', null, 'store', 103, 1, now(), 1, now(), '商城管理目录');

-- 平台配置菜单
-- noinspection SqlResolve
insert into sys_menu values('2010', '平台配置', '2000', '10', 'platform', 'business/base/platform/platform', null, '0', '1', 'C', '1', '1', 'base:platform:view', 'slider', 103, 1, now(), null, null, '平台配置菜单');
-- noinspection SqlResolve
insert into sys_menu values('2011', '平台配置查询', '2010', '1', '#', '', null, '0', '1', 'F', '1', '1', 'base:platform:query', '#', 103, 1, now(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu values('2012', '平台配置新增', '2010', '2', '#', '', null, '0', '1', 'F', '1', '1', 'base:platform:add', '#', 103, 1, now(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu values('2013', '平台配置修改', '2010', '3', '#', '', null, '0', '1', 'F', '1', '1', 'base:platform:update', '#', 103, 1, now(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu values('2014', '平台配置删除', '2010', '4', '#', '', null, '0', '1', 'F', '1', '1', 'base:platform:delete', '#', 103, 1, now(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu values('2015', '平台配置导出', '2010', '5', '#', '', null, '0', '1', 'F', '1', '1', 'base:platform:export', '#', 103, 1, now(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu values('2016', '平台配置导入', '2010', '6', '#', '', null, '0', '1', 'F', '1', '1', 'base:platform:import', '#', 103, 1, now(), null, null, '');

-- 支付配置菜单
-- noinspection SqlResolve
insert into sys_menu values('2020', '支付配置', '2000', '20', 'payment', 'business/base/payment/payment', null, '0', '1', 'C', '1', '1', 'base:payment:view', 'payment', 103, 1, now(), null, null, '支付配置菜单');
-- noinspection SqlResolve
insert into sys_menu values('2021', '支付配置查询', '2020', '1', '#', '', null, '0', '1', 'F', '1', '1', 'base:payment:query', '#', 103, 1, now(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu values('2022', '支付配置新增', '2020', '2', '#', '', null, '0', '1', 'F', '1', '1', 'base:payment:add', '#', 103, 1, now(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu values('2023', '支付配置修改', '2020', '3', '#', '', null, '0', '1', 'F', '1', '1', 'base:payment:update', '#', 103, 1, now(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu values('2024', '支付配置删除', '2020', '4', '#', '', null, '0', '1', 'F', '1', '1', 'base:payment:delete', '#', 103, 1, now(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu values('2025', '支付配置导出', '2020', '5', '#', '', null, '0', '1', 'F', '1', '1', 'base:payment:export', '#', 103, 1, now(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu values('2026', '支付配置导入', '2020', '6', '#', '', null, '0', '1', 'F', '1', '1', 'base:payment:import', '#', 103, 1, now(), null, null, '');

-- 账号绑定菜单
-- noinspection SqlResolve
insert into sys_menu values('2030', '账号绑定', '2000', '1', 'bind', 'business/base/bind/bind', null, '0', '1', 'C', '1', '1', 'base:bind:view', 'link', 103, 1, now(), null, null, '账号绑定菜单');
-- noinspection SqlResolve
insert into sys_menu values('2031', '账号绑定查询', '2030', '1', '#', '', null, '0', '1', 'F', '1', '1', 'base:bind:query', '#', 103, 1, now(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu values('2032', '账号绑定新增', '2030', '2', '#', '', null, '0', '1', 'F', '1', '1', 'base:bind:add', '#', 103, 1, now(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu values('2033', '账号绑定修改', '2030', '3', '#', '', null, '0', '1', 'F', '1', '1', 'base:bind:update', '#', 103, 1, now(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu values('2034', '账号绑定删除', '2030', '4', '#', '', null, '0', '1', 'F', '1', '1', 'base:bind:delete', '#', 103, 1, now(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu values('2035', '账号绑定导出', '2030', '5', '#', '', null, '0', '1', 'F', '1', '1', 'base:bind:export', '#', 103, 1, now(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu values('2036', '账号绑定导入', '2030', '6', '#', '', null, '0', '1', 'F', '1', '1', 'base:bind:import', '#', 103, 1, now(), null, null, '');

-- 广告配置菜单
-- noinspection SqlResolve
insert into sys_menu values('2040', '广告配置', '2000', '30', 'ad', 'business/base/ad/ad', null, '0', '1', 'C', '1', '1', 'base:ad:view', 'wishlist', 103, 1, now(), null, null, '广告配置菜单');
-- noinspection SqlResolve
insert into sys_menu values('2041', '广告配置查询', '2040', '1', '#', '', null, '0', '1', 'F', '1', '1', 'base:ad:query', '#', 103, 1, now(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu values('2042', '广告配置新增', '2040', '2', '#', '', null, '0', '1', 'F', '1', '1', 'base:ad:add', '#', 103, 1, now(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu values('2043', '广告配置修改', '2040', '3', '#', '', null, '0', '1', 'F', '1', '1', 'base:ad:update', '#', 103, 1, now(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu values('2044', '广告配置删除', '2040', '4', '#', '', null, '0', '1', 'F', '1', '1', 'base:ad:delete', '#', 103, 1, now(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu values('2045', '广告配置导出', '2040', '5', '#', '', null, '0', '1', 'F', '1', '1', 'base:ad:export', '#', 103, 1, now(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu values('2046', '广告配置导入', '2040', '6', '#', '', null, '0', '1', 'F', '1', '1', 'base:ad:import', '#', 103, 1, now(), null, null, '');

-- 商品菜单
-- noinspection SqlResolve
insert into sys_menu values('2110', '商品', '2001', '1', 'goods', 'business/mall/goods/goods', null, '0', '1', 'C', '1', '1', 'mall:goods:view', 'bag', 103, 1, now(), null, null, '商品菜单');
-- noinspection SqlResolve
insert into sys_menu values('2111', '商品查询', '2110', '1', '#', '', null, '0', '1', 'F', '1', '1', 'mall:goods:query', '#', 103, 1, now(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu values('2112', '商品新增', '2110', '2', '#', '', null, '0', '1', 'F', '1', '1', 'mall:goods:add', '#', 103, 1, now(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu values('2113', '商品修改', '2110', '3', '#', '', null, '0', '1', 'F', '1', '1', 'mall:goods:update', '#', 103, 1, now(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu values('2114', '商品删除', '2110', '4', '#', '', null, '0', '1', 'F', '1', '1', 'mall:goods:delete', '#', 103, 1, now(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu values('2115', '商品导出', '2110', '5', '#', '', null, '0', '1', 'F', '1', '1', 'mall:goods:export', '#', 103, 1, now(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu values('2116', '商品导入', '2110', '6', '#', '', null, '0', '1', 'F', '1', '1', 'mall:goods:import', '#', 103, 1, now(), null, null, '');

-- 订单菜单
-- noinspection SqlResolve
insert into sys_menu values('2120', '订单', '2001', '10', 'order', 'business/mall/order/order', null, '0', '1', 'C', '1', '1', 'mall:order:view', 'order', 103, 1, now(), null, null, '订单菜单');
-- noinspection SqlResolve
insert into sys_menu values('2121', '订单查询', '2120', '1', '#', '', null, '0', '1', 'F', '1', '1', 'mall:order:query', '#', 103, 1, now(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu values('2122', '订单新增', '2120', '2', '#', '', null, '0', '1', 'F', '1', '1', 'mall:order:add', '#', 103, 1, now(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu values('2123', '订单修改', '2120', '3', '#', '', null, '0', '1', 'F', '1', '1', 'mall:order:update', '#', 103, 1, now(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu values('2124', '订单删除', '2120', '4', '#', '', null, '0', '1', 'F', '1', '1', 'mall:order:delete', '#', 103, 1, now(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu values('2125', '订单导出', '2120', '5', '#', '', null, '0', '1', 'F', '1', '1', 'mall:order:export', '#', 103, 1, now(), null, null, '');
-- noinspection SqlResolve
insert into sys_menu values('2126', '订单导入', '2120', '6', '#', '', null, '0', '1', 'F', '1', '1', 'mall:order:import', '#', 103, 1, now(), null, null, '');
