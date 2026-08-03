-- app移动端 抓蛙师

-- 1、广告配置表
CREATE TABLE b_ad
(
    id           bigint        NOT NULL,
    tenant_id    nvarchar(20)  DEFAULT ('000000') NULL,
    appid        nvarchar(50)  NULL,
    ad_unit_id   nvarchar(100) NULL,
    ad_name      nvarchar(100) NOT NULL,
    ad_type      nvarchar(20)  NOT NULL,
    position     nvarchar(50)  NULL,
    img          nvarchar(500) NULL,
    description  nvarchar(255) NULL,
    jump_appid   nvarchar(50)  NULL,
    jump_path    nvarchar(255) NULL,
    style_config nvarchar(255) NULL,
    sort_order   int           DEFAULT ((999)) NULL,
    status       nchar(1)      DEFAULT ('1') NULL,
    create_dept  bigint        NULL,
    create_by    bigint        NULL,
    create_time  datetime2(7)  DEFAULT (getdate()) NULL,
    update_by    bigint        NULL,
    update_time  datetime2(7)  DEFAULT (getdate()) NULL,
    remark       nvarchar(255) NULL,
    is_deleted   nchar(1)      DEFAULT ('0') NULL,
    CONSTRAINT PK_b_ad PRIMARY KEY CLUSTERED (id)
        WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
    ON [PRIMARY]
)
    ON [PRIMARY]
GO

-- 广告配置表字段注释
EXEC sys.sp_addextendedproperty 'MS_Description', N'主键id', 'SCHEMA', N'dbo', 'TABLE', N'b_ad', 'COLUMN', N'id'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'租户id', 'SCHEMA', N'dbo', 'TABLE', N'b_ad', 'COLUMN', N'tenant_id'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'appid', 'SCHEMA', N'dbo', 'TABLE', N'b_ad', 'COLUMN', N'appid'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'广告位id', 'SCHEMA', N'dbo', 'TABLE', N'b_ad', 'COLUMN', N'ad_unit_id'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'广告名称', 'SCHEMA', N'dbo', 'TABLE', N'b_ad', 'COLUMN', N'ad_name'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'广告类型', 'SCHEMA', N'dbo', 'TABLE', N'b_ad', 'COLUMN', N'ad_type'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'投放位置', 'SCHEMA', N'dbo', 'TABLE', N'b_ad', 'COLUMN', N'position'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'广告图片', 'SCHEMA', N'dbo', 'TABLE', N'b_ad', 'COLUMN', N'img'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'描述', 'SCHEMA', N'dbo', 'TABLE', N'b_ad', 'COLUMN', N'description'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'跳转appid', 'SCHEMA', N'dbo', 'TABLE', N'b_ad', 'COLUMN', N'jump_appid'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'跳转路径', 'SCHEMA', N'dbo', 'TABLE', N'b_ad', 'COLUMN', N'jump_path'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'样式配置', 'SCHEMA', N'dbo', 'TABLE', N'b_ad', 'COLUMN', N'style_config'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'排序值', 'SCHEMA', N'dbo', 'TABLE', N'b_ad', 'COLUMN', N'sort_order'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'状态', 'SCHEMA', N'dbo', 'TABLE', N'b_ad', 'COLUMN', N'status'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'创建部门', 'SCHEMA', N'dbo', 'TABLE', N'b_ad', 'COLUMN', N'create_dept'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'创建人', 'SCHEMA', N'dbo', 'TABLE', N'b_ad', 'COLUMN', N'create_by'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'创建时间', 'SCHEMA', N'dbo', 'TABLE', N'b_ad', 'COLUMN', N'create_time'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'更新人', 'SCHEMA', N'dbo', 'TABLE', N'b_ad', 'COLUMN', N'update_by'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'更新时间', 'SCHEMA', N'dbo', 'TABLE', N'b_ad', 'COLUMN', N'update_time'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'备注', 'SCHEMA', N'dbo', 'TABLE', N'b_ad', 'COLUMN', N'remark'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'是否删除', 'SCHEMA', N'dbo', 'TABLE', N'b_ad', 'COLUMN', N'is_deleted'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'广告配置表', 'SCHEMA', N'dbo', 'TABLE', N'b_ad'
GO

-- 2、账号绑定表
CREATE TABLE b_bind
(
    id            bigint        NOT NULL,
    tenant_id     nvarchar(20)  DEFAULT ('000000') NULL,
    user_id       bigint        NULL,
    platform_type nvarchar(20)  NULL,
    appid         nvarchar(30)  NULL,
    unionid       nvarchar(30)  NULL,
    openid        nvarchar(30)  NULL,
    extra_data    nvarchar(255) NULL,
    create_dept   bigint        NULL,
    create_by     bigint        NULL,
    create_time   datetime2(7)  NULL,
    update_by     bigint        NULL,
    update_time   datetime2(7)  NULL,
    remark        nvarchar(255) NULL,
    is_deleted    nchar(1)      DEFAULT ('0') NULL,
    CONSTRAINT PK_b_bind PRIMARY KEY CLUSTERED (id)
        WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
    ON [PRIMARY]
)
    ON [PRIMARY]
GO

-- 账号绑定表字段注释
EXEC sys.sp_addextendedproperty 'MS_Description', N'账号绑定id', 'SCHEMA', N'dbo', 'TABLE', N'b_bind', 'COLUMN', N'id'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'租户id', 'SCHEMA', N'dbo', 'TABLE', N'b_bind', 'COLUMN', N'tenant_id'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'用户id', 'SCHEMA', N'dbo', 'TABLE', N'b_bind', 'COLUMN', N'user_id'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'平台类型', 'SCHEMA', N'dbo', 'TABLE', N'b_bind', 'COLUMN', N'platform_type'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'appid', 'SCHEMA', N'dbo', 'TABLE', N'b_bind', 'COLUMN', N'appid'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'unionid', 'SCHEMA', N'dbo', 'TABLE', N'b_bind', 'COLUMN', N'unionid'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'openid', 'SCHEMA', N'dbo', 'TABLE', N'b_bind', 'COLUMN', N'openid'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'扩展数据', 'SCHEMA', N'dbo', 'TABLE', N'b_bind', 'COLUMN', N'extra_data'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'创建部门', 'SCHEMA', N'dbo', 'TABLE', N'b_bind', 'COLUMN', N'create_dept'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'创建人', 'SCHEMA', N'dbo', 'TABLE', N'b_bind', 'COLUMN', N'create_by'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'创建时间', 'SCHEMA', N'dbo', 'TABLE', N'b_bind', 'COLUMN', N'create_time'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'修改人', 'SCHEMA', N'dbo', 'TABLE', N'b_bind', 'COLUMN', N'update_by'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'更新时间', 'SCHEMA', N'dbo', 'TABLE', N'b_bind', 'COLUMN', N'update_time'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'备注', 'SCHEMA', N'dbo', 'TABLE', N'b_bind', 'COLUMN', N'remark'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'是否删除', 'SCHEMA', N'dbo', 'TABLE', N'b_bind', 'COLUMN', N'is_deleted'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'账号绑定表', 'SCHEMA', N'dbo', 'TABLE', N'b_bind'
GO

-- 3、支付配置表
CREATE TABLE b_payment
(
    id                 bigint         NOT NULL,
    tenant_id          nvarchar(20)   DEFAULT ('000000') NULL,
    type               nvarchar(30)   NULL,
    mch_name           nvarchar(30)   NULL,
    mch_id             nvarchar(20)   NULL,
    mch_key            nvarchar(50)   NULL,
    api_v3_key         nvarchar(50)   NULL,
    cert_path          nvarchar(2000) NULL,
    key_path           nvarchar(2000) NULL,
    platform_cert_path nvarchar(2000) NULL,
    cert_serial_no     nvarchar(100)  NULL,
    public_key_id      nvarchar(100)  NULL,
    p12_cert_path      nvarchar(500)  NULL,
    status             nchar(1)       DEFAULT ('1') NULL,
    create_dept        bigint         NULL,
    create_by          bigint         NULL,
    create_time        datetime2(7)   NULL,
    update_by          bigint         NULL,
    update_time        datetime2(7)   NULL,
    remark             nvarchar(255)  NULL,
    is_deleted         nchar(1)       DEFAULT ('0') NULL,
    CONSTRAINT PK_b_payment PRIMARY KEY CLUSTERED (id)
        WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
    ON [PRIMARY]
)
    ON [PRIMARY]
GO

-- 支付配置表字段注释
EXEC sys.sp_addextendedproperty 'MS_Description', N'支付配置id', 'SCHEMA', N'dbo', 'TABLE', N'b_payment', 'COLUMN', N'id'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'租户id', 'SCHEMA', N'dbo', 'TABLE', N'b_payment', 'COLUMN', N'tenant_id'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'商户类型', 'SCHEMA', N'dbo', 'TABLE', N'b_payment', 'COLUMN', N'type'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'商户名称', 'SCHEMA', N'dbo', 'TABLE', N'b_payment', 'COLUMN', N'mch_name'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'商户号', 'SCHEMA', N'dbo', 'TABLE', N'b_payment', 'COLUMN', N'mch_id'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'商户密钥', 'SCHEMA', N'dbo', 'TABLE', N'b_payment', 'COLUMN', N'mch_key'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'APIv3密钥', 'SCHEMA', N'dbo', 'TABLE', N'b_payment', 'COLUMN', N'api_v3_key'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'证书路径(V3 PEM格式)', 'SCHEMA', N'dbo', 'TABLE', N'b_payment', 'COLUMN', N'cert_path'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'密钥路径(V3 PEM格式)', 'SCHEMA', N'dbo', 'TABLE', N'b_payment', 'COLUMN', N'key_path'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'平台证书路径(V3 PEM格式)', 'SCHEMA', N'dbo', 'TABLE', N'b_payment', 'COLUMN', N'platform_cert_path'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'证书序列号', 'SCHEMA', N'dbo', 'TABLE', N'b_payment', 'COLUMN', N'cert_serial_no'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'微信支付公钥ID(V3公钥模式专用,格式 PUB_KEY_ID_xxx)', 'SCHEMA', N'dbo', 'TABLE', N'b_payment', 'COLUMN', N'public_key_id'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'p12证书路径(V2退款专用)', 'SCHEMA', N'dbo', 'TABLE', N'b_payment', 'COLUMN', N'p12_cert_path'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'状态', 'SCHEMA', N'dbo', 'TABLE', N'b_payment', 'COLUMN', N'status'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'创建部门', 'SCHEMA', N'dbo', 'TABLE', N'b_payment', 'COLUMN', N'create_dept'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'创建人', 'SCHEMA', N'dbo', 'TABLE', N'b_payment', 'COLUMN', N'create_by'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'创建时间', 'SCHEMA', N'dbo', 'TABLE', N'b_payment', 'COLUMN', N'create_time'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'修改人', 'SCHEMA', N'dbo', 'TABLE', N'b_payment', 'COLUMN', N'update_by'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'更新时间', 'SCHEMA', N'dbo', 'TABLE', N'b_payment', 'COLUMN', N'update_time'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'备注', 'SCHEMA', N'dbo', 'TABLE', N'b_payment', 'COLUMN', N'remark'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'是否删除', 'SCHEMA', N'dbo', 'TABLE', N'b_payment', 'COLUMN', N'is_deleted'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'支付配置表', 'SCHEMA', N'dbo', 'TABLE', N'b_payment'
GO

-- 4、平台配置表
CREATE TABLE b_platform
(
    id               bigint         NOT NULL,
    tenant_id        nvarchar(20)   DEFAULT ('000000') NULL,
    type             nvarchar(20)   NULL,
    name             nvarchar(20)   NULL,
    appid            nvarchar(30)   NULL,
    secret           nvarchar(50)   NULL,
    token            nvarchar(50)   NULL,
    aeskey           nvarchar(50)   NULL,
    payment_ids      nvarchar(100)  NULL,
    template_configs nvarchar(2000) NULL,
    status           nchar(1)       DEFAULT ('1') NULL,
    create_dept      bigint         NULL,
    create_by        bigint         NULL,
    create_time      datetime2(7)   NULL,
    update_by        bigint         NULL,
    update_time      datetime2(7)   NULL,
    remark           nvarchar(255)  NULL,
    is_deleted       nchar(1)       DEFAULT ('0') NULL,
    CONSTRAINT PK_b_platform PRIMARY KEY CLUSTERED (id)
        WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
    ON [PRIMARY]
)
    ON [PRIMARY]
GO

-- 平台配置表字段注释
EXEC sys.sp_addextendedproperty 'MS_Description', N'平台配置id', 'SCHEMA', N'dbo', 'TABLE', N'b_platform', 'COLUMN', N'id'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'租户id', 'SCHEMA', N'dbo', 'TABLE', N'b_platform', 'COLUMN', N'tenant_id'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'平台类型', 'SCHEMA', N'dbo', 'TABLE', N'b_platform', 'COLUMN', N'type'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'名称', 'SCHEMA', N'dbo', 'TABLE', N'b_platform', 'COLUMN', N'name'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'appid', 'SCHEMA', N'dbo', 'TABLE', N'b_platform', 'COLUMN', N'appid'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'密钥', 'SCHEMA', N'dbo', 'TABLE', N'b_platform', 'COLUMN', N'secret'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'接口token', 'SCHEMA', N'dbo', 'TABLE', N'b_platform', 'COLUMN', N'token'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'加密密钥', 'SCHEMA', N'dbo', 'TABLE', N'b_platform', 'COLUMN', N'aeskey'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'关联支付配置', 'SCHEMA', N'dbo', 'TABLE', N'b_platform', 'COLUMN', N'payment_ids'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'模板配置', 'SCHEMA', N'dbo', 'TABLE', N'b_platform', 'COLUMN', N'template_configs'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'状态', 'SCHEMA', N'dbo', 'TABLE', N'b_platform', 'COLUMN', N'status'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'创建部门', 'SCHEMA', N'dbo', 'TABLE', N'b_platform', 'COLUMN', N'create_dept'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'创建人', 'SCHEMA', N'dbo', 'TABLE', N'b_platform', 'COLUMN', N'create_by'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'创建时间', 'SCHEMA', N'dbo', 'TABLE', N'b_platform', 'COLUMN', N'create_time'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'修改人', 'SCHEMA', N'dbo', 'TABLE', N'b_platform', 'COLUMN', N'update_by'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'更新时间', 'SCHEMA', N'dbo', 'TABLE', N'b_platform', 'COLUMN', N'update_time'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'备注', 'SCHEMA', N'dbo', 'TABLE', N'b_platform', 'COLUMN', N'remark'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'逻辑删除', 'SCHEMA', N'dbo', 'TABLE', N'b_platform', 'COLUMN', N'is_deleted'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'平台配置表', 'SCHEMA', N'dbo', 'TABLE', N'b_platform'
GO

-- 5、商品表(SPU - 标准产品单位)
CREATE TABLE m_goods
(
    id             bigint         NOT NULL,
    tenant_id      nvarchar(20)   DEFAULT ('000000') NULL,
    category       nvarchar(30)   NULL,
    code           nvarchar(30)   NULL,
    name           nvarchar(200)  NOT NULL,
    img            nvarchar(255)  NULL,
    imgs           nvarchar(1000) NULL,
    spec_type      nchar(1)       DEFAULT ('0') NULL,
    original_price decimal(10,2)  NULL,
    discount       decimal(3,2)   DEFAULT ((1.00)) NULL,
    price          decimal(10,2)  NULL,
    description    nvarchar(max)  NULL,
    stock          int            NULL,
    sales_count    int            DEFAULT ((0)) NULL,
    status         nchar(1)       DEFAULT ('1') NULL,
    sort_order     int            DEFAULT ((999)) NULL,
    create_dept    bigint         NULL,
    create_by      bigint         NULL,
    create_time    datetime2(7)   DEFAULT (getdate()) NULL,
    update_by      bigint         NULL,
    update_time    datetime2(7)   DEFAULT (getdate()) NULL,
    remark         nvarchar(255)  NULL,
    is_deleted     nchar(1)       DEFAULT ('0') NULL,
    CONSTRAINT PK_m_goods PRIMARY KEY CLUSTERED (id)
        WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
        ON [PRIMARY]
)
ON [PRIMARY]
GO

CREATE INDEX idx_m_goods_category ON m_goods (category)
GO
CREATE INDEX idx_m_goods_status_deleted ON m_goods (status, is_deleted)
GO
CREATE INDEX idx_m_goods_spec_type ON m_goods (spec_type)
GO

-- 商品表字段注释
EXEC sys.sp_addextendedproperty 'MS_Description', N'商品ID', 'SCHEMA', N'dbo', 'TABLE', N'm_goods', 'COLUMN', N'id'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'租户ID', 'SCHEMA', N'dbo', 'TABLE', N'm_goods', 'COLUMN', N'tenant_id'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'商品分类', 'SCHEMA', N'dbo', 'TABLE', N'm_goods', 'COLUMN', N'category'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'商品编码', 'SCHEMA', N'dbo', 'TABLE', N'm_goods', 'COLUMN', N'code'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'商品名称', 'SCHEMA', N'dbo', 'TABLE', N'm_goods', 'COLUMN', N'name'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'商品主图', 'SCHEMA', N'dbo', 'TABLE', N'm_goods', 'COLUMN', N'img'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'商品图片集(多图,逗号分隔)', 'SCHEMA', N'dbo', 'TABLE', N'm_goods', 'COLUMN', N'imgs'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'规格类型 (0单规格 1多规格)', 'SCHEMA', N'dbo', 'TABLE', N'm_goods', 'COLUMN', N'spec_type'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'原价(单规格时使用)', 'SCHEMA', N'dbo', 'TABLE', N'm_goods', 'COLUMN', N'original_price'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'折扣', 'SCHEMA', N'dbo', 'TABLE', N'm_goods', 'COLUMN', N'discount'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'价格(单规格时使用/多规格时为最低价)', 'SCHEMA', N'dbo', 'TABLE', N'm_goods', 'COLUMN', N'price'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'商品描述', 'SCHEMA', N'dbo', 'TABLE', N'm_goods', 'COLUMN', N'description'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'库存(单规格时使用/多规格时为总库存)', 'SCHEMA', N'dbo', 'TABLE', N'm_goods', 'COLUMN', N'stock'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'总销量', 'SCHEMA', N'dbo', 'TABLE', N'm_goods', 'COLUMN', N'sales_count'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'状态 (0下架 1上架)', 'SCHEMA', N'dbo', 'TABLE', N'm_goods', 'COLUMN', N'status'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'排序', 'SCHEMA', N'dbo', 'TABLE', N'm_goods', 'COLUMN', N'sort_order'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'创建部门', 'SCHEMA', N'dbo', 'TABLE', N'm_goods', 'COLUMN', N'create_dept'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'创建人', 'SCHEMA', N'dbo', 'TABLE', N'm_goods', 'COLUMN', N'create_by'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'创建时间', 'SCHEMA', N'dbo', 'TABLE', N'm_goods', 'COLUMN', N'create_time'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'修改人', 'SCHEMA', N'dbo', 'TABLE', N'm_goods', 'COLUMN', N'update_by'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'更新时间', 'SCHEMA', N'dbo', 'TABLE', N'm_goods', 'COLUMN', N'update_time'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'备注', 'SCHEMA', N'dbo', 'TABLE', N'm_goods', 'COLUMN', N'remark'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'是否删除', 'SCHEMA', N'dbo', 'TABLE', N'm_goods', 'COLUMN', N'is_deleted'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'商品表(SPU)', 'SCHEMA', N'dbo', 'TABLE', N'm_goods'
GO

-- 5.1、商品SKU表(库存量单位 - 具体规格)
CREATE TABLE m_goods_sku
(
    id             bigint         NOT NULL,
    tenant_id      nvarchar(20)   DEFAULT ('000000') NULL,
    goods_id       bigint         NOT NULL,
    sku_code       nvarchar(50)   NULL,
    sku_name       nvarchar(200)  NULL,
    spec_values    nvarchar(500)  NOT NULL,
    original_price decimal(10,2)  NULL,
    price          decimal(10,2)  NOT NULL,
    stock          int            DEFAULT ((0)) NULL,
    sales_count    int            DEFAULT ((0)) NULL,
    img            nvarchar(255)  NULL,
    status         nchar(1)       DEFAULT ('1') NULL,
    sort_order     int            DEFAULT ((999)) NULL,
    is_default     nchar(1)       DEFAULT ('0') NULL,
    create_dept    bigint         NULL,
    create_by      bigint         NULL,
    create_time    datetime2(7)   DEFAULT (getdate()) NULL,
    update_by      bigint         NULL,
    update_time    datetime2(7)   DEFAULT (getdate()) NULL,
    remark         nvarchar(255)  NULL,
    is_deleted     nchar(1)       DEFAULT ('0') NULL,
    CONSTRAINT PK_m_goods_sku PRIMARY KEY CLUSTERED (id)
        WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
        ON [PRIMARY],
    CONSTRAINT UK_m_goods_sku_code UNIQUE (sku_code)
)
ON [PRIMARY]
GO

CREATE INDEX idx_m_goods_sku_goods_id ON m_goods_sku (goods_id)
GO
CREATE INDEX idx_m_goods_sku_price ON m_goods_sku (price)
GO
CREATE INDEX idx_m_goods_sku_status_deleted ON m_goods_sku (status, is_deleted)
GO

-- 商品SKU表字段注释
EXEC sys.sp_addextendedproperty 'MS_Description', N'SKU ID', 'SCHEMA', N'dbo', 'TABLE', N'm_goods_sku', 'COLUMN', N'id'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'租户ID', 'SCHEMA', N'dbo', 'TABLE', N'm_goods_sku', 'COLUMN', N'tenant_id'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'商品ID(关联m_goods.id)', 'SCHEMA', N'dbo', 'TABLE', N'm_goods_sku', 'COLUMN', N'goods_id'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'SKU编码(自动生成或手动)', 'SCHEMA', N'dbo', 'TABLE', N'm_goods_sku', 'COLUMN', N'sku_code'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'SKU名称(如:红色-S码)', 'SCHEMA', N'dbo', 'TABLE', N'm_goods_sku', 'COLUMN', N'sku_name'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'规格值JSON(如:{"颜色":"红色","尺码":"S"})', 'SCHEMA', N'dbo', 'TABLE', N'm_goods_sku', 'COLUMN', N'spec_values'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'原价', 'SCHEMA', N'dbo', 'TABLE', N'm_goods_sku', 'COLUMN', N'original_price'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'价格', 'SCHEMA', N'dbo', 'TABLE', N'm_goods_sku', 'COLUMN', N'price'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'库存', 'SCHEMA', N'dbo', 'TABLE', N'm_goods_sku', 'COLUMN', N'stock'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'销量', 'SCHEMA', N'dbo', 'TABLE', N'm_goods_sku', 'COLUMN', N'sales_count'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'SKU图片(可继承商品主图)', 'SCHEMA', N'dbo', 'TABLE', N'm_goods_sku', 'COLUMN', N'img'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'状态 (0停用 1启用)', 'SCHEMA', N'dbo', 'TABLE', N'm_goods_sku', 'COLUMN', N'status'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'排序', 'SCHEMA', N'dbo', 'TABLE', N'm_goods_sku', 'COLUMN', N'sort_order'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'是否默认SKU (0否 1是)', 'SCHEMA', N'dbo', 'TABLE', N'm_goods_sku', 'COLUMN', N'is_default'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'创建部门', 'SCHEMA', N'dbo', 'TABLE', N'm_goods_sku', 'COLUMN', N'create_dept'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'创建人', 'SCHEMA', N'dbo', 'TABLE', N'm_goods_sku', 'COLUMN', N'create_by'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'创建时间', 'SCHEMA', N'dbo', 'TABLE', N'm_goods_sku', 'COLUMN', N'create_time'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'修改人', 'SCHEMA', N'dbo', 'TABLE', N'm_goods_sku', 'COLUMN', N'update_by'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'更新时间', 'SCHEMA', N'dbo', 'TABLE', N'm_goods_sku', 'COLUMN', N'update_time'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'备注', 'SCHEMA', N'dbo', 'TABLE', N'm_goods_sku', 'COLUMN', N'remark'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'是否删除', 'SCHEMA', N'dbo', 'TABLE', N'm_goods_sku', 'COLUMN', N'is_deleted'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'商品SKU表', 'SCHEMA', N'dbo', 'TABLE', N'm_goods_sku'
GO

-- 6、订单表
CREATE TABLE m_order
(
    id             bigint         NOT NULL,
    tenant_id      nvarchar(20)   DEFAULT ('000000') NULL,
    order_no       nvarchar(32)   NOT NULL,
    user_id        bigint         NULL,
    goods_id       bigint         NOT NULL,
    sku_id         bigint         NULL,
    goods_name     nvarchar(200)  NULL,
    sku_name       nvarchar(200)  NULL,
    spec_values    nvarchar(500)  NULL,
    goods_img      nvarchar(255)  NULL,
    price          decimal(10,2)  NOT NULL,
    quantity       int            NOT NULL,
    total_amount   decimal(10,2)  NOT NULL,
    actual_amount  decimal(10,2)  NULL,
    order_status   nvarchar(20)   DEFAULT ('pending') NULL,
    payment_method nvarchar(20)   NULL,
    payment_time   datetime2(7)   NULL,
    transaction_id nvarchar(64)   NULL,
    buyer_remark   nvarchar(500)  NULL,
    order_ext_info nvarchar(500)  NULL,
    receiver_info  nvarchar(500)  NULL,
    shipping_info  nvarchar(1000)  NULL,
    create_dept    bigint         NULL,
    create_by      bigint         NULL,
    create_time    datetime2(7)   DEFAULT (getdate()) NULL,
    update_by      bigint         NULL,
    update_time    datetime2(7)   DEFAULT (getdate()) NULL,
    remark         nvarchar(255)  NULL,
    is_deleted     nchar(1)       DEFAULT ('0') NULL,
    CONSTRAINT PK_m_order PRIMARY KEY CLUSTERED (id)
        WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
    ON [PRIMARY],
    CONSTRAINT UK_m_order_no UNIQUE (order_no)
)
    ON [PRIMARY]
GO

CREATE INDEX idx_m_order_user_id ON m_order (user_id)
GO
CREATE INDEX idx_m_order_goods_id ON m_order (goods_id)
GO
CREATE INDEX idx_m_order_sku_id ON m_order (sku_id)
GO
CREATE INDEX idx_m_order_status ON m_order (order_status)
GO
CREATE INDEX idx_m_order_create_time ON m_order (create_time)
GO

-- 订单表字段注释
EXEC sys.sp_addextendedproperty 'MS_Description', N'订单ID', 'SCHEMA', N'dbo', 'TABLE', N'm_order', 'COLUMN', N'id'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'租户ID', 'SCHEMA', N'dbo', 'TABLE', N'm_order', 'COLUMN', N'tenant_id'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'订单编号', 'SCHEMA', N'dbo', 'TABLE', N'm_order', 'COLUMN', N'order_no'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'用户ID', 'SCHEMA', N'dbo', 'TABLE', N'm_order', 'COLUMN', N'user_id'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'商品ID(SPU)', 'SCHEMA', N'dbo', 'TABLE', N'm_order', 'COLUMN', N'goods_id'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'SKU ID(规格)', 'SCHEMA', N'dbo', 'TABLE', N'm_order', 'COLUMN', N'sku_id'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'商品名称', 'SCHEMA', N'dbo', 'TABLE', N'm_order', 'COLUMN', N'goods_name'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'SKU名称(如:红色-S码)', 'SCHEMA', N'dbo', 'TABLE', N'm_order', 'COLUMN', N'sku_name'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'规格值JSON', 'SCHEMA', N'dbo', 'TABLE', N'm_order', 'COLUMN', N'spec_values'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'商品图片', 'SCHEMA', N'dbo', 'TABLE', N'm_order', 'COLUMN', N'goods_img'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'商品单价', 'SCHEMA', N'dbo', 'TABLE', N'm_order', 'COLUMN', N'price'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'购买数量', 'SCHEMA', N'dbo', 'TABLE', N'm_order', 'COLUMN', N'quantity'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'订单总金额', 'SCHEMA', N'dbo', 'TABLE', N'm_order', 'COLUMN', N'total_amount'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'实付金额', 'SCHEMA', N'dbo', 'TABLE', N'm_order', 'COLUMN', N'actual_amount'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'订单状态', 'SCHEMA', N'dbo', 'TABLE', N'm_order', 'COLUMN', N'order_status'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'支付方式', 'SCHEMA', N'dbo', 'TABLE', N'm_order', 'COLUMN', N'payment_method'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'支付时间', 'SCHEMA', N'dbo', 'TABLE', N'm_order', 'COLUMN', N'payment_time'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'交易流水号', 'SCHEMA', N'dbo', 'TABLE', N'm_order', 'COLUMN', N'transaction_id'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'买家备注', 'SCHEMA', N'dbo', 'TABLE', N'm_order', 'COLUMN', N'buyer_remark'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'订单扩展信息', 'SCHEMA', N'dbo', 'TABLE', N'm_order', 'COLUMN', N'order_ext_info'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'收货信息', 'SCHEMA', N'dbo', 'TABLE', N'm_order', 'COLUMN', N'receiver_info'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'物流信息', 'SCHEMA', N'dbo', 'TABLE', N'm_order', 'COLUMN', N'shipping_info'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'创建部门', 'SCHEMA', N'dbo', 'TABLE', N'm_order', 'COLUMN', N'create_dept'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'创建人', 'SCHEMA', N'dbo', 'TABLE', N'm_order', 'COLUMN', N'create_by'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'创建时间', 'SCHEMA', N'dbo', 'TABLE', N'm_order', 'COLUMN', N'create_time'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'修改人', 'SCHEMA', N'dbo', 'TABLE', N'm_order', 'COLUMN', N'update_by'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'更新时间', 'SCHEMA', N'dbo', 'TABLE', N'm_order', 'COLUMN', N'update_time'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'备注', 'SCHEMA', N'dbo', 'TABLE', N'm_order', 'COLUMN', N'remark'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'是否删除', 'SCHEMA', N'dbo', 'TABLE', N'm_order', 'COLUMN', N'is_deleted'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'订单表', 'SCHEMA', N'dbo', 'TABLE', N'm_order'
GO

-- 初始化-菜单信息表数据

-- APP配置主菜单
-- noinspection SqlResolve
INSERT sys_menu VALUES (2000, N'APP配置', 0, 95, N'appConfiguration', NULL, NULL, '0', '1', N'M', N'1', N'1', NULL, N'app', 103, 1, getdate(), 1, getdate(), N'APP配置目录')
   ;

-- 商城管理主菜单
-- noinspection SqlResolve
INSERT sys_menu VALUES (2001, N'商城管理', 0, 90, N'mallManage', NULL, NULL, '0', '1', N'M', N'1', N'1', NULL, N'store', 103, 1, getdate(), 1, getdate(), N'商城管理目录')
   ;

-- 平台配置菜单
-- noinspection SqlResolve
INSERT sys_menu VALUES (2010, N'平台配置', 2000, 10, N'platform', N'business/base/platform/platform', NULL, '0', '1', N'C', N'1', N'1', N'base:platform:view', N'slider', 103, 1, getdate(), NULL, NULL, N'平台配置菜单')
   ;
-- noinspection SqlResolve
INSERT sys_menu VALUES (2011, N'平台配置查询', 2010, 1, N'#', N'', NULL, '0', '1', N'F', N'1', N'1', N'base:platform:query', N'#', 103, 1, getdate(), NULL, NULL, N'')
   ;
-- noinspection SqlResolve
INSERT sys_menu VALUES (2012, N'平台配置新增', 2010, 2, N'#', N'', NULL, '0', '1', N'F', N'1', N'1', N'base:platform:add', N'#', 103, 1, getdate(), NULL, NULL, N'')
   ;
-- noinspection SqlResolve
INSERT sys_menu VALUES (2013, N'平台配置修改', 2010, 3, N'#', N'', NULL, '0', '1', N'F', N'1', N'1', N'base:platform:update', N'#', 103, 1, getdate(), NULL, NULL, N'')
   ;
-- noinspection SqlResolve
INSERT sys_menu VALUES (2014, N'平台配置删除', 2010, 4, N'#', N'', NULL, '0', '1', N'F', N'1', N'1', N'base:platform:delete', N'#', 103, 1, getdate(), NULL, NULL, N'')
   ;
-- noinspection SqlResolve
INSERT sys_menu VALUES (2015, N'平台配置导出', 2010, 5, N'#', N'', NULL, '0', '1', N'F', N'1', N'1', N'base:platform:export', N'#', 103, 1, getdate(), NULL, NULL, N'')
   ;
-- noinspection SqlResolve
INSERT sys_menu VALUES (2016, N'平台配置导入', 2010, 6, N'#', N'', NULL, '0', '1', N'F', N'1', N'1', N'base:platform:import', N'#', 103, 1, getdate(), NULL, NULL, N'')
   ;

-- 支付配置菜单
-- noinspection SqlResolve
INSERT sys_menu VALUES (2020, N'支付配置', 2000, 20, N'payment', N'business/base/payment/payment', NULL, '0', '1', N'C', N'1', N'1', N'base:payment:view', N'payment', 103, 1, getdate(), NULL, NULL, N'支付配置菜单')
   ;
-- noinspection SqlResolve
INSERT sys_menu VALUES (2021, N'支付配置查询', 2020, 1, N'#', N'', NULL, '0', '1', N'F', N'1', N'1', N'base:payment:query', N'#', 103, 1, getdate(), NULL, NULL, N'')
   ;
-- noinspection SqlResolve
INSERT sys_menu VALUES (2022, N'支付配置新增', 2020, 2, N'#', N'', NULL, '0', '1', N'F', N'1', N'1', N'base:payment:add', N'#', 103, 1, getdate(), NULL, NULL, N'')
   ;
-- noinspection SqlResolve
INSERT sys_menu VALUES (2023, N'支付配置修改', 2020, 3, N'#', N'', NULL, '0', '1', N'F', N'1', N'1', N'base:payment:update', N'#', 103, 1, getdate(), NULL, NULL, N'')
   ;
-- noinspection SqlResolve
INSERT sys_menu VALUES (2024, N'支付配置删除', 2020, 4, N'#', N'', NULL, '0', '1', N'F', N'1', N'1', N'base:payment:delete', N'#', 103, 1, getdate(), NULL, NULL, N'')
   ;
-- noinspection SqlResolve
INSERT sys_menu VALUES (2025, N'支付配置导出', 2020, 5, N'#', N'', NULL, '0', '1', N'F', N'1', N'1', N'base:payment:export', N'#', 103, 1, getdate(), NULL, NULL, N'')
   ;
-- noinspection SqlResolve
INSERT sys_menu VALUES (2026, N'支付配置导入', 2020, 6, N'#', N'', NULL, '0', '1', N'F', N'1', N'1', N'base:payment:import', N'#', 103, 1, getdate(), NULL, NULL, N'')
   ;

-- 账号绑定菜单
-- noinspection SqlResolve
INSERT sys_menu VALUES (2030, N'账号绑定', 2000, 1, N'bind', N'business/base/bind/bind', NULL, '0', '1', N'C', N'1', N'1', N'base:bind:view', N'link', 103, 1, getdate(), NULL, NULL, N'账号绑定菜单')
   ;
-- noinspection SqlResolve
INSERT sys_menu VALUES (2031, N'账号绑定查询', 2030, 1, N'#', N'', NULL, '0', '1', N'F', N'1', N'1', N'base:bind:query', N'#', 103, 1, getdate(), NULL, NULL, N'')
   ;
-- noinspection SqlResolve
INSERT sys_menu VALUES (2032, N'账号绑定新增', 2030, 2, N'#', N'', NULL, '0', '1', N'F', N'1', N'1', N'base:bind:add', N'#', 103, 1, getdate(), NULL, NULL, N'')
   ;
-- noinspection SqlResolve
INSERT sys_menu VALUES (2033, N'账号绑定修改', 2030, 3, N'#', N'', NULL, '0', '1', N'F', N'1', N'1', N'base:bind:update', N'#', 103, 1, getdate(), NULL, NULL, N'')
   ;
-- noinspection SqlResolve
-- noinspection SqlResolve
INSERT sys_menu VALUES (2034, N'账号绑定删除', 2030, 4, N'#', N'', NULL, '0', '1', N'F', N'1', N'1', N'base:bind:delete', N'#', 103, 1, getdate(), NULL, NULL, N'')
   ;
-- noinspection SqlResolve
INSERT sys_menu VALUES (2035, N'账号绑定导出', 2030, 5, N'#', N'', NULL, '0', '1', N'F', N'1', N'1', N'base:bind:export', N'#', 103, 1, getdate(), NULL, NULL, N'')
   ;
-- noinspection SqlResolve
INSERT sys_menu VALUES (2036, N'账号绑定导入', 2030, 6, N'#', N'', NULL, '0', '1', N'F', N'1', N'1', N'base:bind:import', N'#', 103, 1, getdate(), NULL, NULL, N'')
   ;

-- 广告配置菜单
-- noinspection SqlResolve
INSERT sys_menu VALUES (2040, N'广告配置', 2000, 30, N'ad', N'business/base/ad/ad', NULL, '0', '1', N'C', N'1', N'1', N'base:ad:view', N'wishlist', 103, 1, getdate(), NULL, NULL, N'广告配置菜单')
   ;
-- noinspection SqlResolve
INSERT sys_menu VALUES (2041, N'广告配置查询', 2040, 1, N'#', N'', NULL, '0', '1', N'F', N'1', N'1', N'base:ad:query', N'#', 103, 1, getdate(), NULL, NULL, N'')
   ;
-- noinspection SqlResolve
INSERT sys_menu VALUES (2042, N'广告配置新增', 2040, 2, N'#', N'', NULL, '0', '1', N'F', N'1', N'1', N'base:ad:add', N'#', 103, 1, getdate(), NULL, NULL, N'')
   ;
-- noinspection SqlResolve
INSERT sys_menu VALUES (2043, N'广告配置修改', 2040, 3, N'#', N'', NULL, '0', '1', N'F', N'1', N'1', N'base:ad:update', N'#', 103, 1, getdate(), NULL, NULL, N'')
   ;
-- noinspection SqlResolve
INSERT sys_menu VALUES (2044, N'广告配置删除', 2040, 4, N'#', N'', NULL, '0', '1', N'F', N'1', N'1', N'base:ad:delete', N'#', 103, 1, getdate(), NULL, NULL, N'')
   ;
-- noinspection SqlResolve
INSERT sys_menu VALUES (2045, N'广告配置导出', 2040, 5, N'#', N'', NULL, '0', '1', N'F', N'1', N'1', N'base:ad:export', N'#', 103, 1, getdate(), NULL, NULL, N'')
   ;
-- noinspection SqlResolve
INSERT sys_menu VALUES (2046, N'广告配置导入', 2040, 6, N'#', N'', NULL, '0', '1', N'F', N'1', N'1', N'base:ad:import', N'#', 103, 1, getdate(), NULL, NULL, N'')
   ;

-- 商品菜单
-- noinspection SqlResolve
INSERT sys_menu VALUES (2110, N'商品', 2001, 1, N'goods', N'business/mall/goods/goods', NULL, '0', '1', N'C', N'1', N'1', N'mall:goods:view', N'bag', 103, 1, getdate(), NULL, NULL, N'商品菜单')
   ;
-- noinspection SqlResolve
INSERT sys_menu VALUES (2111, N'商品查询', 2110, 1, N'#', N'', NULL, '0', '1', N'F', N'1', N'1', N'mall:goods:query', N'#', 103, 1, getdate(), NULL, NULL, N'')
   ;
-- noinspection SqlResolve
INSERT sys_menu VALUES (2112, N'商品新增', 2110, 2, N'#', N'', NULL, '0', '1', N'F', N'1', N'1', N'mall:goods:add', N'#', 103, 1, getdate(), NULL, NULL, N'')
   ;
-- noinspection SqlResolve
INSERT sys_menu VALUES (2113, N'商品修改', 2110, 3, N'#', N'', NULL, '0', '1', N'F', N'1', N'1', N'mall:goods:update', N'#', 103, 1, getdate(), NULL, NULL, N'')
   ;
-- noinspection SqlResolve
INSERT sys_menu VALUES (2114, N'商品删除', 2110, 4, N'#', N'', NULL, '0', '1', N'F', N'1', N'1', N'mall:goods:delete', N'#', 103, 1, getdate(), NULL, NULL, N'')
   ;
-- noinspection SqlResolve
INSERT sys_menu VALUES (2115, N'商品导出', 2110, 5, N'#', N'', NULL, '0', '1', N'F', N'1', N'1', N'mall:goods:export', N'#', 103, 1, getdate(), NULL, NULL, N'')
   ;
-- noinspection SqlResolve
INSERT sys_menu VALUES (2116, N'商品导入', 2110, 6, N'#', N'', NULL, '0', '1', N'F', N'1', N'1', N'mall:goods:import', N'#', 103, 1, getdate(), NULL, NULL, N'')
   ;

-- 订单菜单
-- noinspection SqlResolve
INSERT sys_menu VALUES (2120, N'订单', 2001, 10, N'order', N'business/mall/order/order', NULL, '0', '1', N'C', N'1', N'1', N'mall:order:view', N'order', 103, 1, getdate(), NULL, NULL, N'订单菜单')
   ;
-- noinspection SqlResolve
INSERT sys_menu VALUES (2121, N'订单查询', 2120, 1, N'#', N'', NULL, '0', '1', N'F', N'1', N'1', N'mall:order:query', N'#', 103, 1, getdate(), NULL, NULL, N'')
   ;
-- noinspection SqlResolve
INSERT sys_menu VALUES (2122, N'订单新增', 2120, 2, N'#', N'', NULL, '0', '1', N'F', N'1', N'1', N'mall:order:add', N'#', 103, 1, getdate(), NULL, NULL, N'')
   ;
-- noinspection SqlResolve
INSERT sys_menu VALUES (2123, N'订单修改', 2120, 3, N'#', N'', NULL, '0', '1', N'F', N'1', N'1', N'mall:order:update', N'#', 103, 1, getdate(), NULL, NULL, N'')
   ;
-- noinspection SqlResolve
INSERT sys_menu VALUES (2124, N'订单删除', 2120, 4, N'#', N'', NULL, '0', '1', N'F', N'1', N'1', N'mall:order:delete', N'#', 103, 1, getdate(), NULL, NULL, N'')
   ;
-- noinspection SqlResolve
INSERT sys_menu VALUES (2125, N'订单导出', 2120, 5, N'#', N'', NULL, '0', '1', N'F', N'1', N'1', N'mall:order:export', N'#', 103, 1, getdate(), NULL, NULL, N'')
   ;
-- noinspection SqlResolve
INSERT sys_menu VALUES (2126, N'订单导入', 2120, 6, N'#', N'', NULL, '0', '1', N'F', N'1', N'1', N'mall:order:import', N'#', 103, 1, getdate(), NULL, NULL, N'')
   ;
