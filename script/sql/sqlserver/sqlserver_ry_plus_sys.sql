create table sys_social
(
    id                 bigint            NOT NULL,
    user_id            bigint            NOT NULL,
    tenant_id          nvarchar(20)      DEFAULT ('000000') NULL,
    auth_id            nvarchar(255)     NOT NULL,
    source             nvarchar(255)     NOT NULL,
    open_id            nvarchar(255)     NULL,
    user_name          nvarchar(30)      NULL,
    nick_name          nvarchar(30)      DEFAULT ('')   NULL,
    email              nvarchar(255)     DEFAULT ('')   NULL,
    avatar             nvarchar(500)     DEFAULT ('')   NULL,
    access_token       nvarchar(255)     NOT NULL,
    expire_in          bigint            NULL,
    refresh_token      nvarchar(255)     NULL,
    access_code        nvarchar(255)     NULL,
    union_id           nvarchar(255)     NULL,
    scope              nvarchar(255)     NULL,
    token_type         nvarchar(255)     NULL,
    id_token           nvarchar(2000)    NULL,
    mac_algorithm      nvarchar(255)     NULL,
    mac_key            nvarchar(255)     NULL,
    code               nvarchar(255)     NULL,
    oauth_token        nvarchar(255)     NULL,
    oauth_token_secret nvarchar(255)     NULL,
    create_dept        bigint,
    create_by          bigint,
    create_time        datetime2(7),
    update_by          bigint,
    update_time        datetime2(7),
    is_deleted         nchar(1)      DEFAULT ('0')        NULL,
    CONSTRAINT PK__sys_social__B21E8F2427725F8A PRIMARY KEY CLUSTERED (id)
    WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
    ON [PRIMARY]
)
ON [PRIMARY]
GO

EXEC sys.sp_addextendedproperty
    'MS_Description', N'id' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_social',
    'COLUMN', N'id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'用户ID' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_social',
    'COLUMN', N'user_id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'租户id' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_social',
    'COLUMN', N'tenant_id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'平台+平台唯一id' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_social',
    'COLUMN', N'auth_id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'用户来源' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_social',
    'COLUMN', N'source'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'平台编号唯一id' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_social',
    'COLUMN', N'open_id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'登录账号' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_social',
    'COLUMN', N'user_name'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'用户昵称' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_social',
    'COLUMN', N'nick_name'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'用户邮箱' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_social',
    'COLUMN', N'email'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'头像地址' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_social',
    'COLUMN', N'avatar'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'用户的授权令牌' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_social',
    'COLUMN', N'access_token'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'用户的授权令牌的有效期，部分平台可能没有' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_social',
    'COLUMN', N'expire_in'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'刷新令牌，部分平台可能没有' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_social',
    'COLUMN', N'refresh_token'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'平台的授权信息，部分平台可能没有' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_social',
    'COLUMN', N'access_code'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'用户的 unionid' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_social',
    'COLUMN', N'union_id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'授予的权限，部分平台可能没有' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_social',
    'COLUMN', N'scope'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'个别平台的授权信息，部分平台可能没有' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_social',
    'COLUMN', N'token_type'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'id token，部分平台可能没有' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_social',
    'COLUMN', N'id_token'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'小米平台用户的附带属性，部分平台可能没有' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_social',
    'COLUMN', N'mac_algorithm'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'小米平台用户的附带属性，部分平台可能没有' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_social',
    'COLUMN', N'mac_key'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'用户的授权code，部分平台可能没有' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_social',
    'COLUMN', N'code'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'Twitter平台用户的附带属性，部分平台可能没有' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_social',
    'COLUMN', N'oauth_token'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'Twitter平台用户的附带属性，部分平台可能没有' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_social',
    'COLUMN', N'oauth_token_secret'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'是否删除' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_social',
    'COLUMN', N'is_deleted'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建部门' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_social',
    'COLUMN', N'create_dept'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建者' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_social',
    'COLUMN', N'create_by'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建时间' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_social',
    'COLUMN', N'create_time'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'更新者' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_social',
    'COLUMN', N'update_by'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'更新时间' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_social',
    'COLUMN', N'update_time'
GO
EXEC sp_addextendedproperty
    'MS_Description', N'社会化关系表',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_social'
GO

CREATE TABLE sys_tenant
(
    id                    bigint                          NOT NULL,
    tenant_id             nvarchar(20)                    NOT NULL,
    contact_user_name     nvarchar(20)                    NULL,
    contact_phone         nvarchar(20)                    NULL,
    company_name          nvarchar(30)                    NULL,
    license_number        nvarchar(30)                    NULL,
    address               nvarchar(200)                   NULL,
    intro                 nvarchar(200)                   NULL,
    domain                nvarchar(200)                   NULL,
    remark                nvarchar(200)                   NULL,
    package_id            bigint                          NULL,
    expire_time           datetime2(7)                    NULL,
    account_count         int             DEFAULT ((-1))  NULL,
    status                nchar(1)        DEFAULT ('1')   NULL,
    is_deleted            nchar(1)        DEFAULT ('0')   NULL,
    create_dept           bigint                          NULL,
    create_by             bigint                          NULL,
    create_time           datetime2(7)                    NULL,
    update_by             bigint                          NULL,
    update_time           datetime2(7)                    NULL,
    CONSTRAINT PK__sys_tenant__B21E8F2427725F8A PRIMARY KEY CLUSTERED (id)
        WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
        ON [PRIMARY]
)
ON [PRIMARY]
GO

EXEC sys.sp_addextendedproperty
    'MS_Description', N'id' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_tenant',
    'COLUMN', N'id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'租户id' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_tenant',
    'COLUMN', N'tenant_id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'联系人' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_tenant',
    'COLUMN', N'contact_user_name'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'联系电话' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_tenant',
    'COLUMN', N'contact_phone'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'企业名称' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_tenant',
    'COLUMN', N'company_name'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'统一社会信用代码' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_tenant',
    'COLUMN', N'license_number'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'地址' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_tenant',
    'COLUMN', N'address'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'企业简介' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_tenant',
    'COLUMN', N'intro'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'域名' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_tenant',
    'COLUMN', N'domain'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'备注' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_tenant',
    'COLUMN', N'remark'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'租户套餐编号' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_tenant',
    'COLUMN', N'package_id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'过期时间' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_tenant',
    'COLUMN', N'expire_time'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'用户数量（-1不限制）' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_tenant',
    'COLUMN', N'account_count'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'租户状态' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_tenant',
    'COLUMN', N'status'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'是否删除' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_tenant',
    'COLUMN', N'is_deleted'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建部门' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_tenant',
    'COLUMN', N'create_dept'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建者' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_tenant',
    'COLUMN', N'create_by'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建时间' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_tenant',
    'COLUMN', N'create_time'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'更新者' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_tenant',
    'COLUMN', N'update_by'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'更新时间' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_tenant',
    'COLUMN', N'update_time'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'租户表' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_tenant'
GO

INSERT sys_tenant VALUES (1, N'000000', N'管理组', N'15888888888', N'若依工作室', NULL, NULL, N'多租户通用后台管理管理系统', NULL, NULL, NULL, NULL, -1, N'0', N'0', 100, 1, getdate(), NULL, NULL)
GO


CREATE TABLE sys_tenant_package
(
    package_id            bigint                          NOT NULL,
    package_name          nvarchar(20)                    NOT NULL,
    menu_ids              nvarchar(2000)                    NULL,
    remark                nvarchar(200)                   NULL,
    menu_check_strictly   tinyint         DEFAULT ((1))   NULL,
    status                nchar(1)        DEFAULT ('1')   NULL,
    is_deleted            nchar(1)        DEFAULT ('0')   NULL,
    create_dept           bigint                          NULL,
    create_by             bigint                          NULL,
    create_time           datetime2(7)                    NULL,
    update_by             bigint                          NULL,
    update_time           datetime2(7)                    NULL,
    CONSTRAINT PK__sys_tenant_package__B21E8F2427725F8A PRIMARY KEY CLUSTERED (package_id)
        WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
        ON [PRIMARY]
)
ON [PRIMARY]
GO

EXEC sys.sp_addextendedproperty
    'MS_Description', N'租户套餐id' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_tenant_package',
    'COLUMN', N'package_id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'套餐名称' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_tenant_package',
    'COLUMN', N'package_name'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'关联菜单id' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_tenant_package',
    'COLUMN', N'menu_ids'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'备注' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_tenant_package',
    'COLUMN', N'remark'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'租户状态' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_tenant_package',
    'COLUMN', N'status'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'是否删除' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_tenant_package',
    'COLUMN', N'is_deleted'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建部门' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_tenant_package',
    'COLUMN', N'create_dept'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建者' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_tenant_package',
    'COLUMN', N'create_by'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建时间' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_tenant_package',
    'COLUMN', N'create_time'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'更新者' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_tenant_package',
    'COLUMN', N'update_by'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'更新时间' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_tenant_package',
    'COLUMN', N'update_time'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'租户套餐表' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_tenant_package'
GO


CREATE TABLE sys_gen_table
(
    table_id          bigint                         NOT NULL,
    data_name         nvarchar(200) DEFAULT ''       NULL,
    table_name        nvarchar(200) DEFAULT ''       NULL,
    table_comment     nvarchar(500) DEFAULT ''       NULL,
    sub_table_name    nvarchar(64)                   NULL,
    sub_table_fk_name nvarchar(64)                   NULL,
    class_name        nvarchar(100) DEFAULT ''       NULL,
    tpl_category      nvarchar(200) DEFAULT ('crud') NULL,
    package_name      nvarchar(100)                  NULL,
    module_name       nvarchar(30)                   NULL,
    business_name     nvarchar(30)                   NULL,
    function_name     nvarchar(50)                   NULL,
    function_author   nvarchar(50)                   NULL,
    gen_type          nchar(1)      DEFAULT ('0')    NULL,
    gen_path          nvarchar(200) DEFAULT ('/')    NULL,
    options           nvarchar(1000)                 NULL,
    create_dept       bigint                         NULL,
    create_by         bigint                         NULL,
    create_time       datetime2(7)                   NULL,
    update_by         bigint                         NULL,
    update_time       datetime2(7)                   NULL,
    remark            nvarchar(500)                  NULL,
    CONSTRAINT PK__gen_tabl__B21E8F2427725F8A PRIMARY KEY CLUSTERED (table_id)
        WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
        ON [PRIMARY]
)
ON [PRIMARY]
GO

EXEC sys.sp_addextendedproperty
    'MS_Description', N'编号' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table',
    'COLUMN', N'table_id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'数据源名称' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table',
    'COLUMN', N'data_name'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'表名称' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table',
    'COLUMN', N'table_name'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'表描述' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table',
    'COLUMN', N'table_comment'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'关联子表的表名' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table',
    'COLUMN', N'sub_table_name'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'子表关联的外键名' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table',
    'COLUMN', N'sub_table_fk_name'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'实体类名称' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table',
    'COLUMN', N'class_name'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'使用的模板（crud单表操作 tree树表操作）' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table',
    'COLUMN', N'tpl_category'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'生成包路径' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table',
    'COLUMN', N'package_name'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'生成模块名' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table',
    'COLUMN', N'module_name'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'生成业务名' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table',
    'COLUMN', N'business_name'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'生成功能名' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table',
    'COLUMN', N'function_name'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'生成功能作者' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table',
    'COLUMN', N'function_author'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'生成代码方式（0zip压缩包 1自定义路径）' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table',
    'COLUMN', N'gen_type'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'生成路径（不填默认项目路径）' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table',
    'COLUMN', N'gen_path'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'其它生成选项' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table',
    'COLUMN', N'options'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建部门' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table',
    'COLUMN', N'create_dept'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建者' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table',
    'COLUMN', N'create_by'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建时间' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table',
    'COLUMN', N'create_time'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'更新者' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table',
    'COLUMN', N'update_by'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'更新时间' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table',
    'COLUMN', N'update_time'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'备注' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table',
    'COLUMN', N'remark'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'代码生成业务表' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table'
GO

CREATE TABLE sys_gen_table_column
(
    column_id      bigint                       NOT NULL,
    table_id       bigint                       NULL,
    column_name    nvarchar(200)                NULL,
    column_comment nvarchar(500)                NULL,
    column_label   nvarchar(200)                NULL,
    column_type    nvarchar(100)                NULL,
    java_type      nvarchar(500)                NULL,
    java_field     nvarchar(200)                NULL,
    is_pk          nchar(1)                     NULL,
    is_increment   nchar(1)                     NULL,
    is_required    nchar(1)                     NULL,
    is_insert      nchar(1)                     NULL,
    is_edit        nchar(1)                     NULL,
    is_list        nchar(1)                     NULL,
    is_query       nchar(1)                     NULL,
    query_type     nvarchar(200) DEFAULT ('EQ') NULL,
    html_type      nvarchar(200)                NULL,
    dict_type      nvarchar(200) DEFAULT ''     NULL,
    column_default nvarchar(100) DEFAULT ''     NULL,
    sort           int                          NULL,
    create_dept    bigint                       NULL,
    create_by      bigint                       NULL,
    create_time    datetime2(7)                 NULL,
    update_by      bigint                       NULL,
    update_time    datetime2(7)                 NULL,
    CONSTRAINT PK__gen_tabl__E301851F2E68B4E8 PRIMARY KEY CLUSTERED (column_id)
        WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
        ON [PRIMARY]
)
ON [PRIMARY]
GO

EXEC sys.sp_addextendedproperty
    'MS_Description', N'编号' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table_column',
    'COLUMN', N'column_id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'归属表编号' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table_column',
    'COLUMN', N'table_id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'列名称' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table_column',
    'COLUMN', N'column_name'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'列描述' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table_column',
    'COLUMN', N'column_comment'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'字段标签（用于显示）' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table_column',
    'COLUMN', N'column_label'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'列类型' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table_column',
    'COLUMN', N'column_type'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'JAVA类型' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table_column',
    'COLUMN', N'java_type'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'JAVA字段名' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table_column',
    'COLUMN', N'java_field'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'是否主键（1是）' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table_column',
    'COLUMN', N'is_pk'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'是否自增（1是）' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table_column',
    'COLUMN', N'is_increment'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'是否必填（1是）' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table_column',
    'COLUMN', N'is_required'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'是否为插入字段（1是）' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table_column',
    'COLUMN', N'is_insert'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'是否编辑字段（1是）' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table_column',
    'COLUMN', N'is_edit'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'是否列表字段（1是）' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table_column',
    'COLUMN', N'is_list'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'是否查询字段（1是）' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table_column',
    'COLUMN', N'is_query'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'查询方式（等于、不等于、大于、小于、范围）' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table_column',
    'COLUMN', N'query_type'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table_column',
    'COLUMN', N'html_type'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'字典类型' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table_column',
    'COLUMN', N'dict_type'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'默认值' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table_column',
    'COLUMN', N'column_default'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'排序' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table_column',
    'COLUMN', N'sort'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建部门' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table_column',
    'COLUMN', N'create_dept'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建者' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table_column',
    'COLUMN', N'create_by'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建时间' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table_column',
    'COLUMN', N'create_time'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'更新者' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table_column',
    'COLUMN', N'update_by'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'更新时间' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table_column',
    'COLUMN', N'update_time'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'代码生成业务表字段' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_gen_table_column'
GO

CREATE TABLE sys_config
(
    config_id    bigint                      NOT NULL,
    tenant_id    nvarchar(20)  DEFAULT '000000'  NULL,
    config_name  nvarchar(100) DEFAULT ''    NULL,
    config_key   nvarchar(100) DEFAULT ''    NULL,
    config_value nvarchar(500) DEFAULT ''    NULL,
    config_type  nchar(1)      DEFAULT ('0') NULL,
    create_dept  bigint                      NULL,
    create_by    bigint                      NULL,
    create_time  datetime2(7)                NULL,
    update_by    bigint                      NULL,
    update_time  datetime2(7)                NULL,
    remark       nvarchar(500)               NULL,
    CONSTRAINT PK__sys_conf__4AD1BFF182643682 PRIMARY KEY CLUSTERED (config_id)
        WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
        ON [PRIMARY]
)
ON [PRIMARY]
GO

EXEC sys.sp_addextendedproperty
    'MS_Description', N'参数主键' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_config',
    'COLUMN', N'config_id'
GO
EXEC sys.sp_addextendedproperty
     'MS_Description', N'租户id' ,
     'SCHEMA', N'dbo',
     'TABLE', N'sys_config',
     'COLUMN', N'tenant_id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'参数名称' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_config',
    'COLUMN', N'config_name'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'参数键名' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_config',
    'COLUMN', N'config_key'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'参数键值' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_config',
    'COLUMN', N'config_value'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'系统内置（1是 0否）' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_config',
    'COLUMN', N'config_type'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建部门' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_config',
    'COLUMN', N'create_dept'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建者' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_config',
    'COLUMN', N'create_by'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建时间' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_config',
    'COLUMN', N'create_time'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'更新者' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_config',
    'COLUMN', N'update_by'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'更新时间' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_config',
    'COLUMN', N'update_time'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'备注' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_config',
    'COLUMN', N'remark'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'参数配置表' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_config'
GO

INSERT sys_config VALUES (1, N'000000', N'登录验证-验证码开关', N'system.security.captcha-enabled', N'true', N'1', 200, 1, getdate(), NULL, NULL, N'登录时是否开启验证码功能（true开启，false关闭）')
GO
INSERT sys_config VALUES (2, N'000000', N'用户管理-初始密码', N'system.user.initial-password', N'123456', N'1', 100, 1, getdate(), NULL, NULL, N'新用户创建时的默认初始密码')
GO
INSERT sys_config VALUES (3, N'000000', N'账号自助-注册功能开关', N'system.account.register-enabled', N'false', N'1', 300, 1, getdate(), NULL, NULL, N'是否允许用户自助注册账号（true开启，false关闭）')
GO
INSERT sys_config VALUES (4, N'000000', N'OSS功能-预览列表开关', N'system.oss.preview-enabled', N'true', N'1', 300, 1, getdate(), NULL, NULL, N'是否开启OSS资源预览列表功能（true开启，false关闭）')
GO
INSERT sys_config VALUES (5, N'000000', N'社交登录-自动注册开关', N'system.social.auto-register-enabled', N'false', N'1', 300, 1, getdate(), NULL, NULL, N'是否允许社交登录时自动注册账号（true开启false关闭）')
GO

CREATE TABLE sys_api_key
(
    id                 BIGINT            NOT NULL,
    tenant_id          NVARCHAR(20)      DEFAULT ('000000') NULL,
    app_name           NVARCHAR(100)     NOT NULL,
    app_key            NVARCHAR(64)      NOT NULL,
    app_secret         NVARCHAR(128)     NOT NULL,
    user_id            BIGINT            NULL,
    expire_time        DATETIME2(7)      NULL,
    status             NCHAR(1)          DEFAULT ('1') NULL,
    white_ips          NVARCHAR(500)     NULL,
    call_count         BIGINT            DEFAULT ((0)) NULL,
    last_call_time     DATETIME2(7)      NULL,
    create_dept        BIGINT            NULL,
    create_by          BIGINT            NULL,
    create_time        DATETIME2(7)      NULL,
    update_by          BIGINT            NULL,
    update_time        DATETIME2(7)      NULL,
    remark             NVARCHAR(500)     NULL,
    CONSTRAINT PK__sys_api_key PRIMARY KEY CLUSTERED (id)
        WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
        ON [PRIMARY],
    CONSTRAINT UK__sys_api_key_app_key UNIQUE (app_key)
)
    ON [PRIMARY]
    GO

CREATE INDEX idx_sys_api_key_user_id ON sys_api_key(user_id);
GO
CREATE INDEX idx_sys_api_key_status ON sys_api_key(status);
GO

EXEC sys.sp_addextendedproperty 'MS_Description', N'API密钥ID', 'SCHEMA', N'dbo', 'TABLE', N'sys_api_key', 'COLUMN', N'id'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'租户id', 'SCHEMA', N'dbo', 'TABLE', N'sys_api_key', 'COLUMN', N'tenant_id'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'应用名称', 'SCHEMA', N'dbo', 'TABLE', N'sys_api_key', 'COLUMN', N'app_name'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'AppKey(公开)', 'SCHEMA', N'dbo', 'TABLE', N'sys_api_key', 'COLUMN', N'app_key'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'AppSecret', 'SCHEMA', N'dbo', 'TABLE', N'sys_api_key', 'COLUMN', N'app_secret'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'关联用户ID', 'SCHEMA', N'dbo', 'TABLE', N'sys_api_key', 'COLUMN', N'user_id'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'过期时间', 'SCHEMA', N'dbo', 'TABLE', N'sys_api_key', 'COLUMN', N'expire_time'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'状态(0停用 1正常)', 'SCHEMA', N'dbo', 'TABLE', N'sys_api_key', 'COLUMN', N'status'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'IP白名单,逗号分隔', 'SCHEMA', N'dbo', 'TABLE', N'sys_api_key', 'COLUMN', N'white_ips'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'调用次数', 'SCHEMA', N'dbo', 'TABLE', N'sys_api_key', 'COLUMN', N'call_count'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'最后调用时间', 'SCHEMA', N'dbo', 'TABLE', N'sys_api_key', 'COLUMN', N'last_call_time'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'创建部门', 'SCHEMA', N'dbo', 'TABLE', N'sys_api_key', 'COLUMN', N'create_dept'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'创建者', 'SCHEMA', N'dbo', 'TABLE', N'sys_api_key', 'COLUMN', N'create_by'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'创建时间', 'SCHEMA', N'dbo', 'TABLE', N'sys_api_key', 'COLUMN', N'create_time'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'更新者', 'SCHEMA', N'dbo', 'TABLE', N'sys_api_key', 'COLUMN', N'update_by'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'更新时间', 'SCHEMA', N'dbo', 'TABLE', N'sys_api_key', 'COLUMN', N'update_time'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'备注', 'SCHEMA', N'dbo', 'TABLE', N'sys_api_key', 'COLUMN', N'remark'
GO
EXEC sys.sp_addextendedproperty 'MS_Description', N'API密钥管理表', 'SCHEMA', N'dbo', 'TABLE', N'sys_api_key'
GO

CREATE TABLE sys_dept
(
    dept_id     bigint                     NOT NULL,
    tenant_id   nvarchar(20) DEFAULT ('000000') NULL,
    parent_id   bigint       DEFAULT ((0)) NULL,
    ancestors   nvarchar(500)DEFAULT ''    NULL,
    dept_name   nvarchar(30)               NULL,
    dept_category nvarchar(100) DEFAULT '' NULL,
    order_num   int          DEFAULT ((0)) NULL,
    leader      bigint                     NULL,
    phone       nvarchar(11)               NULL,
    email       nvarchar(50)               NULL,
    area_code   nchar(6)                   NULL,
    status      nchar(1)     DEFAULT ('1') NULL,
    is_deleted  nchar(1)     DEFAULT ('0') NULL,
    create_dept bigint                     NULL,
    create_by   bigint                     NULL,
    create_time datetime2(7)               NULL,
    update_by   bigint                     NULL,
    update_time datetime2(7)               NULL,
    CONSTRAINT PK__sys_dept__DCA659747DE13804 PRIMARY KEY CLUSTERED (dept_id)
        WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
        ON [PRIMARY]
)
ON [PRIMARY]
GO

EXEC sys.sp_addextendedproperty
    'MS_Description', N'部门id' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dept',
    'COLUMN', N'dept_id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'租户id' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dept',
    'COLUMN', N'tenant_id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'父部门id' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dept',
    'COLUMN', N'parent_id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'祖级列表' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dept',
    'COLUMN', N'ancestors'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'部门名称' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dept',
    'COLUMN', N'dept_name'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'部门类别编码' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dept',
    'COLUMN', N'dept_category'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'显示顺序' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dept',
    'COLUMN', N'order_num'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'负责人' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dept',
    'COLUMN', N'leader'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'联系电话' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dept',
    'COLUMN', N'phone'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'邮箱' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dept',
    'COLUMN', N'email'
GO
EXEC sys.sp_addextendedproperty
   'MS_Description', N'区划代码' ,
   'SCHEMA', N'dbo',
   'TABLE', N'sys_dept',
   'COLUMN', N'area_code'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'部门状态' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dept',
    'COLUMN', N'status'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'是否删除' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dept',
    'COLUMN', N'is_deleted'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建部门' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dept',
    'COLUMN', N'create_dept'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建者' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dept',
    'COLUMN', N'create_by'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建时间' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dept',
    'COLUMN', N'create_time'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'更新者' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dept',
    'COLUMN', N'update_by'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'更新时间' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dept',
    'COLUMN', N'update_time'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'部门表' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dept'
GO

INSERT sys_dept VALUES (100, N'000000', 0, N'0', N'若依工作室', NULL, 0, NULL, N'15888888888', N'xxx@qq.com', NULL, N'1', N'0', 100, 1, getdate(), NULL, NULL)
GO
INSERT sys_dept VALUES (101, N'000000', 100, N'0,100', N'深圳总公司', NULL, 1, NULL, N'15888888888', N'xxx@qq.com', NULL, N'1', N'0', 100, 1, getdate(), NULL, NULL)
GO
INSERT sys_dept VALUES (102, N'000000', 100, N'0,100', N'长沙分公司', NULL, 2, NULL, N'15888888888', N'xxx@qq.com', NULL, N'1', N'0', 100, 1, getdate(), NULL, NULL)
GO
INSERT sys_dept VALUES (103, N'000000', 101, N'0,100,101', N'研发部门', NULL, 1, 1, N'15888888888', N'xxx@qq.com', NULL, N'1', N'0', 100, 1, getdate(), NULL, NULL)
GO
INSERT sys_dept VALUES (104, N'000000', 101, N'0,100,101', N'市场部门', NULL, 2, NULL, N'15888888888', N'xxx@qq.com', NULL, N'1', N'0', 100, 1, getdate(), NULL, NULL)
GO
INSERT sys_dept VALUES (105, N'000000', 101, N'0,100,101', N'测试部门', NULL, 3, NULL, N'15888888888', N'xxx@qq.com', NULL, N'1', N'0', 100, 1, getdate(), NULL, NULL)
GO
INSERT sys_dept VALUES (106, N'000000', 101, N'0,100,101', N'财务部门', NULL, 4, NULL, N'15888888888', N'xxx@qq.com', NULL, N'1', N'0', 100, 1, getdate(), NULL, NULL)
GO
INSERT sys_dept VALUES (107, N'000000', 101, N'0,100,101', N'运维部门', NULL, 5, NULL, N'15888888888', N'xxx@qq.com', NULL, N'1', N'0', 100, 1, getdate(), NULL, NULL)
GO
INSERT sys_dept VALUES (108, N'000000', 102, N'0,100,102', N'市场部门', NULL, 1, NULL, N'15888888888', N'xxx@qq.com', NULL, N'1', N'0', 100, 1, getdate(), NULL, NULL)
GO
INSERT sys_dept VALUES (109, N'000000', 102, N'0,100,102', N'财务部门', NULL, 2, NULL, N'15888888888', N'xxx@qq.com', NULL, N'1', N'0', 100, 1, getdate(), NULL, NULL)
GO


CREATE TABLE sys_dict_type
(
    dict_id          bigint                      NOT NULL,
    tenant_id        nvarchar(20)  DEFAULT ('000000') NULL,
    dict_name        nvarchar(100) DEFAULT ''    NULL,
    dict_type        nvarchar(100) DEFAULT ''    NULL,
    is_system        nchar(1)      DEFAULT ('0') NULL,
    status           nchar(1)      DEFAULT ('1') NULL,
    create_dept      bigint                      NULL,
    create_by        bigint                      NULL,
    create_time      datetime2(7)                NULL,
    update_by        bigint                      NULL,
    update_time      datetime2(7)                NULL,
    remark           nvarchar(500)               NULL,
    CONSTRAINT PK__sys_dict__3BD4186C409C5391 PRIMARY KEY CLUSTERED (dict_id)
        WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
        ON [PRIMARY]
)
ON [PRIMARY]
GO

CREATE NONCLUSTERED INDEX sys_dict_type_index1 ON sys_dict_type (tenant_id, dict_type)
GO

EXEC sys.sp_addextendedproperty
    'MS_Description', N'字典主键' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dict_type',
    'COLUMN', N'dict_id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'字典主键' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dict_type',
    'COLUMN', N'tenant_id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'字典名称' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dict_type',
    'COLUMN', N'dict_name'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'字典类型' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dict_type',
    'COLUMN', N'dict_type'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'是否系统级字典(0否 1是)' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dict_type',
    'COLUMN', N'is_system'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'状态' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dict_type',
    'COLUMN', N'status'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建部门' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dict_type',
    'COLUMN', N'create_dept'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建者' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dict_type',
    'COLUMN', N'create_by'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建时间' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dict_type',
    'COLUMN', N'create_time'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'更新者' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dict_type',
    'COLUMN', N'update_by'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'更新时间' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dict_type',
    'COLUMN', N'update_time'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'备注' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dict_type',
    'COLUMN', N'remark'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'字典类型表' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dict_type'
GO

-- 字典类型数据
INSERT sys_dict_type VALUES (1, N'000000', N'用户性别', N'sys_user_gender', '1', '1', 100, 1, getdate(), NULL, NULL, N'用户性别列表')
GO
INSERT sys_dict_type VALUES (2, N'000000', N'数据权限', N'sys_data_scope', '1', '1', 100, 1, getdate(), NULL, NULL, N'数据权限类型列表')
GO
INSERT sys_dict_type VALUES (3, N'000000', N'启用状态', N'sys_enable_status', '1', '1', 100, 1, getdate(), NULL, NULL, N'启用状态列表')
GO
INSERT sys_dict_type VALUES (4, N'000000', N'显示设置', N'sys_display_setting', '1', '1', 100, 1, getdate(), NULL, NULL, N'显示设置列表')
GO
INSERT sys_dict_type VALUES (5, N'000000', N'逻辑标志', N'sys_boolean_flag', '1', '1', 100, 1, getdate(), NULL, NULL, N'逻辑标志列表')
GO
INSERT sys_dict_type VALUES (6, N'000000', N'操作类型', N'sys_oper_type', '1', '1', 100, 1, getdate(), NULL, NULL, N'操作类型列表')
GO
INSERT sys_dict_type VALUES (7, N'000000', N'操作结果', N'sys_oper_result', '1', '1', 100, 1, getdate(), NULL, NULL, N'操作结果列表')
GO
INSERT sys_dict_type VALUES (8, N'000000', N'审核状态', N'sys_audit_status', '1', '1', 100, 1, getdate(), NULL, NULL, N'审核状态列表')
GO
INSERT sys_dict_type VALUES (9, N'000000', N'文件类型', N'sys_file_type', '1', '1', 100, 1, getdate(), NULL, NULL, N'文件类型列表')
GO
INSERT sys_dict_type VALUES (10, N'000000', N'消息类型', N'sys_message_type', '1', '1', 100, 1, getdate(), NULL, NULL, N'消息类型列表')
GO
INSERT sys_dict_type VALUES (11, N'000000', N'通知类型', N'sys_notice_type', '1', '1', 100, 1, getdate(), NULL, NULL, N'通知类型列表')
GO
INSERT sys_dict_type VALUES (12, N'000000', N'通知状态', N'sys_notice_status', '1', '1', 100, 1, getdate(), NULL, NULL, N'通知状态列表')
GO
INSERT sys_dict_type VALUES (20, N'000000', N'平台类型', N'sys_platform_type', '1', '1', 100, 1, getdate(), NULL, NULL, N'平台类型列表')
GO
INSERT sys_dict_type VALUES (21, N'000000', N'支付方式', N'sys_payment_method', '1', '1', 100, 1, getdate(), NULL, NULL, N'支付方式列表')
GO
INSERT sys_dict_type VALUES (22, N'000000', N'订单状态', N'sys_order_status', '1', '1', 100, 1, getdate(), NULL, NULL, N'订单状态列表')
GO
INSERT sys_dict_type VALUES (23, N'000000', N'商品规格类型', N'm_goods_spec_type', '0', '1', 100, 1, getdate(), NULL, NULL, N'商品规格类型(单规格/多规格)')
GO

-- 字典数据表
CREATE TABLE sys_dict_data
(
    dict_data_id   bigint                      NOT NULL,
    tenant_id   nvarchar(20)  DEFAULT ('000000') NULL,
    dict_sort   int           DEFAULT ((0)) NULL,
    dict_label  nvarchar(100) DEFAULT ''    NULL,
    dict_value  nvarchar(100) DEFAULT ''    NULL,
    dict_type   nvarchar(100) DEFAULT ''    NULL,
    css_class   nvarchar(100)               NULL,
    list_class  nvarchar(100)               NULL,
    is_default  nchar(1)      DEFAULT ('0') NULL,
    status      nchar(1)      DEFAULT ('1') NULL,
    create_dept bigint                      NULL,
    create_by   bigint                      NULL,
    create_time datetime2(7)                NULL,
    update_by   bigint                      NULL,
    update_time datetime2(7)                NULL,
    remark      nvarchar(500)               NULL,
    CONSTRAINT PK__sys_dict__19CBC34B661AF3B3 PRIMARY KEY CLUSTERED (dict_data_id)
        WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
        ON [PRIMARY]
)
ON [PRIMARY]
GO

    EXEC sys.sp_addextendedproperty
    'MS_Description', N'字典编码' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dict_data',
    'COLUMN', N'dict_data_id'
GO
    EXEC sys.sp_addextendedproperty
    'MS_Description', N'字典编码' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dict_data',
    'COLUMN', N'tenant_id'
GO
    EXEC sys.sp_addextendedproperty
    'MS_Description', N'字典排序' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dict_data',
    'COLUMN', N'dict_sort'
GO
    EXEC sys.sp_addextendedproperty
    'MS_Description', N'字典标签' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dict_data',
    'COLUMN', N'dict_label'
GO
    EXEC sys.sp_addextendedproperty
    'MS_Description', N'字典键值' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dict_data',
    'COLUMN', N'dict_value'
GO
    EXEC sys.sp_addextendedproperty
    'MS_Description', N'字典类型' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dict_data',
    'COLUMN', N'dict_type'
GO
    EXEC sys.sp_addextendedproperty
    'MS_Description', N'样式属性（其他样式扩展）' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dict_data',
    'COLUMN', N'css_class'
GO
    EXEC sys.sp_addextendedproperty
    'MS_Description', N'表格回显样式' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dict_data',
    'COLUMN', N'list_class'
GO
    EXEC sys.sp_addextendedproperty
    'MS_Description', N'是否默认' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dict_data',
    'COLUMN', N'is_default'
GO
    EXEC sys.sp_addextendedproperty
    'MS_Description', N'状态' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dict_data',
    'COLUMN', N'status'
GO
    EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建部门' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dict_data',
    'COLUMN', N'create_dept'
GO
    EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建者' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dict_data',
    'COLUMN', N'create_by'
GO
    EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建时间' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dict_data',
    'COLUMN', N'create_time'
GO
    EXEC sys.sp_addextendedproperty
    'MS_Description', N'更新者' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dict_data',
    'COLUMN', N'update_by'
GO
    EXEC sys.sp_addextendedproperty
    'MS_Description', N'更新时间' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dict_data',
    'COLUMN', N'update_time'
GO
    EXEC sys.sp_addextendedproperty
    'MS_Description', N'备注' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dict_data',
    'COLUMN', N'remark'
GO
    EXEC sys.sp_addextendedproperty
    'MS_Description', N'字典数据表' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_dict_data'
GO

-- 用户性别
INSERT sys_dict_data VALUES (1, N'000000', 0, N'女', N'0', N'sys_user_gender', N'', N'success', N'1', '1', 100, 1, getdate(), NULL, NULL, N'性别:女')
GO
INSERT sys_dict_data VALUES (2, N'000000', 1, N'男', N'1', N'sys_user_gender', N'', N'primary', N'0', '1', 100, 1, getdate(), NULL, NULL, N'性别:男')
GO
INSERT sys_dict_data VALUES (3, N'000000', 2, N'未知', N'2', N'sys_user_gender', N'', N'info', N'0', '1', 100, 1, getdate(), NULL, NULL, N'性别:未知')
GO

-- 数据权限
INSERT sys_dict_data VALUES (4, N'000000', 1, N'全部数据权限', N'1', N'sys_data_scope', N'', N'primary', N'0', '1', 100, 1, getdate(), NULL, NULL, N'数据权限:全部数据')
GO
INSERT sys_dict_data VALUES (5, N'000000', 2, N'自定义数据权限', N'2', N'sys_data_scope', N'', N'info', N'0', '1', 100, 1, getdate(), NULL, NULL, N'数据权限:自定义数据')
GO
INSERT sys_dict_data VALUES (6, N'000000', 3, N'部门数据权限', N'3', N'sys_data_scope', N'', N'success', N'0', '1', 100, 1, getdate(), NULL, NULL, N'数据权限:本部门数据')
GO
INSERT sys_dict_data VALUES (7, N'000000', 4, N'部门及以下数据权限', N'4', N'sys_data_scope', N'', N'warning', N'0', '1', 100, 1, getdate(), NULL, NULL, N'数据权限:部门及以下数据')
GO
INSERT sys_dict_data VALUES (8, N'000000', 5, N'仅本人数据权限', N'5', N'sys_data_scope', N'', N'danger', N'0', '1', 100, 1, getdate(), NULL, NULL, N'数据权限:仅本人数据')
GO
INSERT sys_dict_data VALUES (9, N'000000', 6, N'部门及以下或本人数据', N'6', N'sys_data_scope', N'', N'info', N'0', '1', 100, 1, getdate(), NULL, NULL, N'数据权限:部门及以下或本人数据')
GO

-- 启用状态
INSERT sys_dict_data VALUES (10, N'000000', 1, N'禁用', N'0', N'sys_enable_status', N'', N'danger', N'0', '1', 100, 1, getdate(), NULL, NULL, N'启用状态:禁用')
GO
INSERT sys_dict_data VALUES (11, N'000000', 2, N'启用', N'1', N'sys_enable_status', N'', N'primary', N'1', '1', 100, 1, getdate(), NULL, NULL, N'启用状态:启用')
GO

-- 显示设置
INSERT sys_dict_data VALUES (12, N'000000', 1, N'隐藏', N'0', N'sys_display_setting', N'', N'danger', N'0', '1', 100, 1, getdate(), NULL, NULL, N'显示设置:隐藏')
GO
INSERT sys_dict_data VALUES (13, N'000000', 2, N'显示', N'1', N'sys_display_setting', N'', N'primary', N'1', '1', 100, 1, getdate(), NULL, NULL, N'显示设置:显示')
GO

-- 逻辑标志
INSERT sys_dict_data VALUES (14, N'000000', 1, N'否', N'0', N'sys_boolean_flag', N'', N'danger', N'0', '1', 100, 1, getdate(), NULL, NULL, N'逻辑标志:否')
GO
INSERT sys_dict_data VALUES (15, N'000000', 2, N'是', N'1', N'sys_boolean_flag', N'', N'primary', N'1', '1', 100, 1, getdate(), NULL, NULL, N'逻辑标志:是')
GO

-- 操作类型
INSERT sys_dict_data VALUES (16, N'000000', 1, N'新增', N'1', N'sys_oper_type', N'', N'primary', N'0', '1', 100, 1, getdate(), NULL, NULL, N'操作:新增')
GO
INSERT sys_dict_data VALUES (17, N'000000', 2, N'修改', N'2', N'sys_oper_type', N'', N'info', N'0', '1', 100, 1, getdate(), NULL, NULL, N'操作:修改')
GO
INSERT sys_dict_data VALUES (18, N'000000', 3, N'删除', N'3', N'sys_oper_type', N'', N'danger', N'0', '1', 100, 1, getdate(), NULL, NULL, N'操作:删除')
GO
INSERT sys_dict_data VALUES (19, N'000000', 4, N'授权', N'4', N'sys_oper_type', N'', N'success', N'0', '1', 100, 1, getdate(), NULL, NULL, N'操作:授权')
GO
INSERT sys_dict_data VALUES (20, N'000000', 5, N'导出', N'5', N'sys_oper_type', N'', N'warning', N'0', '1', 100, 1, getdate(), NULL, NULL, N'操作:导出')
GO
INSERT sys_dict_data VALUES (21, N'000000', 6, N'导入', N'6', N'sys_oper_type', N'', N'warning', N'0', '1', 100, 1, getdate(), NULL, NULL, N'操作:导入')
GO
INSERT sys_dict_data VALUES (22, N'000000', 7, N'强退', N'7', N'sys_oper_type', N'', N'danger', N'0', '1', 100, 1, getdate(), NULL, NULL, N'操作:强退')
GO
INSERT sys_dict_data VALUES (23, N'000000', 8, N'生成代码', N'8', N'sys_oper_type', N'', N'success', N'0', '1', 100, 1, getdate(), NULL, NULL, N'操作:生成代码')
GO
INSERT sys_dict_data VALUES (24, N'000000', 9, N'清空数据', N'9', N'sys_oper_type', N'', N'danger', N'0', '1', 100, 1, getdate(), NULL, NULL, N'操作:清空数据')
GO
INSERT sys_dict_data VALUES (25, N'000000', 99, N'其他', N'99', N'sys_oper_type', N'', N'info', N'0', '1', 100, 1, getdate(), NULL, NULL, N'操作:其他')
GO

-- 操作结果
INSERT sys_dict_data VALUES (26, N'000000', 1, N'成功', N'1', N'sys_oper_result', N'', N'success', N'0', '1', 100, 1, getdate(), NULL, NULL, N'操作结果:成功')
GO
INSERT sys_dict_data VALUES (27, N'000000', 2, N'失败', N'0', N'sys_oper_result', N'', N'danger', N'0', '1', 100, 1, getdate(), NULL, NULL, N'操作结果:失败')
GO

-- 审核状态
INSERT sys_dict_data VALUES (28, N'000000', 0, N'待审核', N'0', N'sys_audit_status', N'', N'info', N'1', '1', 100, 1, getdate(), NULL, NULL, N'审核状态:待审核')
GO
INSERT sys_dict_data VALUES (29, N'000000', 1, N'通过', N'1', N'sys_audit_status', N'', N'success', N'0', '1', 100, 1, getdate(), NULL, NULL, N'审核状态:通过')
GO
INSERT sys_dict_data VALUES (30, N'000000', 2, N'驳回', N'2', N'sys_audit_status', N'', N'warning', N'0', '1', 100, 1, getdate(), NULL, NULL, N'审核状态:驳回')
GO
INSERT sys_dict_data VALUES (31, N'000000', 3, N'拒绝', N'3', N'sys_audit_status', N'', N'danger', N'0', '1', 100, 1, getdate(), NULL, NULL, N'审核状态:拒绝')
GO

-- 文件类型
INSERT sys_dict_data VALUES (32, N'000000', 0, N'图片', N'image', N'sys_file_type', N'', N'primary', N'0', '1', 100, 1, getdate(), NULL, NULL, N'文件类型:图片')
GO
INSERT sys_dict_data VALUES (33, N'000000', 1, N'文档', N'document', N'sys_file_type', N'', N'success', N'0', '1', 100, 1, getdate(), NULL, NULL, N'文件类型:文档')
GO
INSERT sys_dict_data VALUES (34, N'000000', 2, N'视频', N'video', N'sys_file_type', N'', N'info', N'0', '1', 100, 1, getdate(), NULL, NULL, N'文件类型:视频')
GO
INSERT sys_dict_data VALUES (35, N'000000', 3, N'音频', N'audio', N'sys_file_type', N'', N'warning', N'0', '1', 100, 1, getdate(), NULL, NULL, N'文件类型:音频')
GO
INSERT sys_dict_data VALUES (36, N'000000', 4, N'压缩包', N'archive', N'sys_file_type', N'', N'danger', N'0', '1', 100, 1, getdate(), NULL, NULL, N'文件类型:压缩包')
GO
INSERT sys_dict_data VALUES (37, N'000000', 5, N'其他', N'other', N'sys_file_type', N'', N'info', N'0', '1', 100, 1, getdate(), NULL, NULL, N'文件类型:其他')
GO

-- 消息类型
INSERT sys_dict_data VALUES (38, N'000000', 0, N'系统通知', N'system', N'sys_message_type', N'', N'danger', N'0', '1', 100, 1, getdate(), NULL, NULL, N'消息类型:系统通知')
GO
INSERT sys_dict_data VALUES (39, N'000000', 1, N'活动通知', N'activity', N'sys_message_type', N'', N'success', N'0', '1', 100, 1, getdate(), NULL, NULL, N'消息类型:活动通知')
GO
INSERT sys_dict_data VALUES (40, N'000000', 2, N'审核通知', N'audit', N'sys_message_type', N'', N'info', N'0', '1', 100, 1, getdate(), NULL, NULL, N'消息类型:审核通知')
GO
INSERT sys_dict_data VALUES (41, N'000000', 3, N'账户通知', N'account', N'sys_message_type', N'', N'warning', N'0', '1', 100, 1, getdate(), NULL, NULL, N'消息类型:账户通知')
GO
INSERT sys_dict_data VALUES (42, N'000000', 4, N'私信', N'private', N'sys_message_type', N'', N'primary', N'0', '1', 100, 1, getdate(), NULL, NULL, N'消息类型:私信')
GO

-- 通知类型
INSERT sys_dict_data VALUES (43, N'000000', 1, N'通知', N'1', N'sys_notice_type', N'', N'warning', N'1', '1', 100, 1, getdate(), NULL, NULL, N'通知类型:通知')
GO
INSERT sys_dict_data VALUES (44, N'000000', 2, N'公告', N'2', N'sys_notice_type', N'', N'success', N'0', '1', 100, 1, getdate(), NULL, NULL, N'通知类型:公告')
GO

-- 通知状态
INSERT sys_dict_data VALUES (45, N'000000', 1, N'立即发送', N'1', N'sys_notice_status', N'', N'success', N'1', '1', 100, 1, getdate(), NULL, NULL, N'通知状态:立即发送')
GO
INSERT sys_dict_data VALUES (46, N'000000', 2, N'保存为草稿', N'0', N'sys_notice_status', N'', N'danger', N'0', '1', 100, 1, getdate(), NULL, NULL, N'通知状态:保存为草稿')
GO

-- 平台类型
INSERT sys_dict_data VALUES (100, N'000000', 0, N'微信小程序', N'mp-weixin', N'sys_platform_type', N'', N'primary', N'0', '1', 100, 1, getdate(), NULL, NULL, N'平台:微信小程序')
GO
INSERT sys_dict_data VALUES (101, N'000000', 10, N'微信公众号', N'mp-official-account', N'sys_platform_type', N'', N'success', N'0', '1', 100, 1, getdate(), NULL, NULL, N'平台:微信公众号')
GO
INSERT sys_dict_data VALUES (102, N'000000', 20, N'QQ小程序', N'mp-qq', N'sys_platform_type', N'', N'info', N'0', '1', 100, 1, getdate(), NULL, NULL, N'平台:QQ小程序')
GO
INSERT sys_dict_data VALUES (103, N'000000', 30, N'支付宝小程序', N'mp-alipay', N'sys_platform_type', N'', N'warning', N'0', '1', 100, 1, getdate(), NULL, NULL, N'平台:支付宝小程序')
GO
INSERT sys_dict_data VALUES (104, N'000000', 40, N'京东小程序', N'mp-jd', N'sys_platform_type', N'', N'danger', N'0', '1', 100, 1, getdate(), NULL, NULL, N'平台:京东小程序')
GO
INSERT sys_dict_data VALUES (105, N'000000', 50, N'快手小程序', N'mp-kuaishou', N'sys_platform_type', N'', N'info', N'0', '1', 100, 1, getdate(), NULL, NULL, N'平台:快手小程序')
GO
INSERT sys_dict_data VALUES (106, N'000000', 60, N'飞书小程序', N'mp-lark', N'sys_platform_type', N'', N'primary', N'0', '1', 100, 1, getdate(), NULL, NULL, N'平台:飞书小程序')
GO
INSERT sys_dict_data VALUES (107, N'000000', 70, N'百度小程序', N'mp-baidu', N'sys_platform_type', N'', N'success', N'0', '1', 100, 1, getdate(), NULL, NULL, N'平台:百度小程序')
GO
INSERT sys_dict_data VALUES (108, N'000000', 80, N'头条小程序', N'mp-toutiao', N'sys_platform_type', N'', N'warning', N'0', '1', 100, 1, getdate(), NULL, NULL, N'平台:头条/抖音小程序')
GO
INSERT sys_dict_data VALUES (109, N'000000', 90, N'小红书小程序', N'mp-xhs', N'sys_platform_type', N'', N'danger', N'0', '1', 100, 1, getdate(), NULL, NULL, N'平台:小红书小程序')
GO

-- 支付方式
INSERT sys_dict_data VALUES (110, N'000000', 0, N'微信支付', N'wechat', N'sys_payment_method', N'', N'success', N'0', '1', 100, 1, getdate(), NULL, NULL, N'支付方式:微信支付')
GO
INSERT sys_dict_data VALUES (111, N'000000', 1, N'支付宝', N'alipay', N'sys_payment_method', N'', N'primary', N'0', '1', 100, 1, getdate(), NULL, NULL, N'支付方式:支付宝')
GO
INSERT sys_dict_data VALUES (112, N'000000', 2, N'银联', N'unionpay', N'sys_payment_method', N'', N'danger', N'0', '1', 100, 1, getdate(), NULL, NULL, N'支付方式:银联')
GO
INSERT sys_dict_data VALUES (113, N'000000', 3, N'余额支付', N'balance', N'sys_payment_method', N'', N'info', N'0', '1', 100, 1, getdate(), NULL, NULL, N'支付方式:余额支付')
GO
INSERT sys_dict_data VALUES (114, N'000000', 4, N'积分抵扣', N'points', N'sys_payment_method', N'', N'warning', N'0', '1', 100, 1, getdate(), NULL, NULL, N'支付方式:积分抵扣')
GO

-- 订单状态
INSERT sys_dict_data VALUES (120, N'000000', 0, N'待支付', N'pending', N'sys_order_status', N'', N'info', N'0', '1', 100, 1, getdate(), NULL, NULL, N'订单状态:待支付')
GO
INSERT sys_dict_data VALUES (121, N'000000', 1, N'已支付', N'paid', N'sys_order_status', N'', N'primary', N'0', '1', 100, 1, getdate(), NULL, NULL, N'订单状态:已支付')
GO
INSERT sys_dict_data VALUES (122, N'000000', 2, N'已发货', N'delivered', N'sys_order_status', N'', N'warning', N'0', '1', 100, 1, getdate(), NULL, NULL, N'订单状态:已发货')
GO
INSERT sys_dict_data VALUES (123, N'000000', 3, N'已完成', N'completed', N'sys_order_status', N'', N'success', N'0', '1', 100, 1, getdate(), NULL, NULL, N'订单状态:已完成')
GO
INSERT sys_dict_data VALUES (124, N'000000', 4, N'已取消', N'cancelled', N'sys_order_status', N'', N'info', N'0', '1', 100, 1, getdate(), NULL, NULL, N'订单状态:已取消')
GO
INSERT sys_dict_data VALUES (125, N'000000', 5, N'已退款', N'refunded', N'sys_order_status', N'', N'danger', N'0', '1', 100, 1, getdate(), NULL, NULL, N'订单状态:已退款')
GO
INSERT sys_dict_data VALUES (130, N'000000', 1, N'单规格', N'0', N'm_goods_spec_type', N'', N'success', N'0', '1', 100, 1, getdate(), NULL, NULL, N'单一规格商品')
GO
INSERT sys_dict_data VALUES (131, N'000000', 2, N'多规格', N'1', N'm_goods_spec_type', N'', N'primary', N'0', '1', 100, 1, getdate(), NULL, NULL, N'多规格商品')
GO

CREATE TABLE sys_login_log
(
    info_id             bigint                      NOT NULL,
    tenant_id           nvarchar(20)  DEFAULT ('000000') NULL,
    user_id             bigint                      NULL,
    user_name           nvarchar(50)  DEFAULT ''    NULL,
    device_type         nvarchar(32)  DEFAULT ''    NULL,
    ipaddr              nvarchar(128) DEFAULT ''    NULL,
    login_location      nvarchar(255) DEFAULT ''    NULL,
    browser             nvarchar(50)  DEFAULT ''    NULL,
    os                  nvarchar(50)  DEFAULT ''    NULL,
    status              nchar(1)      DEFAULT ('1') NULL,
    msg                 nvarchar(255) DEFAULT ''    NULL,
    login_time          datetime2(7)                NULL,
    CONSTRAINT PK__sys_logi__3D8A9C1A1854AE10 PRIMARY KEY CLUSTERED (info_id)
        WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
        ON [PRIMARY]
)
ON [PRIMARY]
GO

CREATE NONCLUSTERED INDEX idx_sys_login_log_s ON sys_login_log (status)
GO
CREATE NONCLUSTERED INDEX idx_sys_login_log_lt ON sys_login_log (login_time)
GO

EXEC sys.sp_addextendedproperty
    'MS_Description', N'访问ID' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_login_log',
    'COLUMN', N'info_id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'租户id' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_login_log',
    'COLUMN', N'tenant_id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'用户id' ,                          -- 新增注释
    'SCHEMA', N'dbo',
    'TABLE', N'sys_login_log',
    'COLUMN', N'user_id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'用户账号' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_login_log',
    'COLUMN', N'user_name'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'设备类型' ,                         -- 修改注释
    'SCHEMA', N'dbo',
    'TABLE', N'sys_login_log',
    'COLUMN', N'device_type'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'登录IP地址' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_login_log',
    'COLUMN', N'ipaddr'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'登录地点' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_login_log',
    'COLUMN', N'login_location'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'浏览器类型' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_login_log',
    'COLUMN', N'browser'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'操作系统' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_login_log',
    'COLUMN', N'os'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'登录状态' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_login_log',
    'COLUMN', N'status'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'提示消息' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_login_log',
    'COLUMN', N'msg'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'访问时间' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_login_log',
    'COLUMN', N'login_time'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'登录日志' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_login_log'
GO

CREATE TABLE sys_menu
(
    menu_id           bigint                      NOT NULL,
    menu_name         nvarchar(50)                NOT NULL,
    parent_id         bigint        DEFAULT ((0)) NULL,
    order_num         int           DEFAULT ((0)) NULL,
    path              nvarchar(200) DEFAULT ''    NULL,
    component         nvarchar(255)               NULL,
    query_param       nvarchar(255)               NULL,
    is_external_link  nchar(1)      DEFAULT ('0') NULL,
    is_cache          nchar(1)      DEFAULT ('1') NULL,
    menu_type         nchar(1)      DEFAULT ''    NULL,
    visible           nchar(1)      DEFAULT ('1') NULL,
    status            nchar(1)      DEFAULT ('1') NULL,
    perms             nvarchar(100)               NULL,
    icon              nvarchar(100) DEFAULT ('#') NULL,
    create_dept       bigint                      NULL,
    create_by         bigint                      NULL,
    create_time       datetime2(7)                NULL,
    update_by         bigint                      NULL,
    update_time       datetime2(7)                NULL,
    remark            nvarchar(500) DEFAULT ''    NULL,
    CONSTRAINT PK__sys_menu__4CA0FADCF8545C58 PRIMARY KEY CLUSTERED (menu_id)
        WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
        ON [PRIMARY]
)
ON [PRIMARY]
GO

EXEC sys.sp_addextendedproperty
    'MS_Description', N'菜单ID' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_menu',
    'COLUMN', N'menu_id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'菜单名称' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_menu',
    'COLUMN', N'menu_name'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'父菜单ID' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_menu',
    'COLUMN', N'parent_id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'显示顺序' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_menu',
    'COLUMN', N'order_num'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'路由地址' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_menu',
    'COLUMN', N'path'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'组件路径' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_menu',
    'COLUMN', N'component'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'路由参数' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_menu',
    'COLUMN', N'query_param'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'是否为外链' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_menu',
    'COLUMN', N'is_external_link'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'是否缓存' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_menu',
    'COLUMN', N'is_cache'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'菜单类型' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_menu',
    'COLUMN', N'menu_type'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'显示设置' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_menu',
    'COLUMN', N'visible'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'启用状态' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_menu',
    'COLUMN', N'status'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'权限标识' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_menu',
    'COLUMN', N'perms'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'菜单图标' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_menu',
    'COLUMN', N'icon'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建部门' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_menu',
    'COLUMN', N'create_dept'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建者' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_menu',
    'COLUMN', N'create_by'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建时间' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_menu',
    'COLUMN', N'create_time'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'更新者' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_menu',
    'COLUMN', N'update_by'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'更新时间' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_menu',
    'COLUMN', N'update_time'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'备注' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_menu',
    'COLUMN', N'remark'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'菜单权限表' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_menu'
GO

INSERT sys_menu VALUES (1, N'系统管理', 0, 1, N'system', NULL, N'', '0', '1', N'M', N'1', N'1', N'', N'system', 100, 1, getdate(), NULL, NULL, N'系统管理目录')
GO
INSERT sys_menu VALUES (6, N'租户管理', 0, 2, N'tenant', NULL, N'', '0', '1', N'M', N'1', N'1', N'', N'chart', 100, 1, getdate(), NULL, NULL, N'租户管理目录')
GO
INSERT sys_menu VALUES (2, N'系统监控', 0, 3, N'monitor', NULL, N'', '0', '1', N'M', N'1', N'1', N'', N'monitor', 100, 1, getdate(), NULL, NULL, N'系统监控目录')
GO
INSERT sys_menu VALUES (3, N'系统工具', 0, 4, N'tool', NULL, N'', '0', '1', N'M', N'1', N'1', N'', N'tool', 100, 1, getdate(), NULL, NULL, N'系统工具目录')
GO
INSERT sys_menu VALUES (100, N'用户管理', 1, 1, N'user', N'system/core/user/user', N'', '0', '1', N'C', N'1', N'1', N'system:user:view', N'user', 100, 1, getdate(), NULL, NULL, N'用户管理菜单')
GO
INSERT sys_menu VALUES (101, N'角色管理', 1, 2, N'role', N'system/core/role/role', N'', '0', '1', N'C', N'1', N'1', N'system:role:view', N'role', 100, 1, getdate(), NULL, NULL, N'角色管理菜单')
GO
INSERT sys_menu VALUES (102, N'菜单管理', 1, 3, N'menu', N'system/core/menu/menu', N'', '0', '1', N'C', N'1', N'1', N'system:menu:view', N'menu', 100, 1, getdate(), NULL, NULL, N'菜单管理菜单')
GO
INSERT sys_menu VALUES (103, N'部门管理', 1, 4, N'dept', N'system/core/dept/dept', N'', '0', '1', N'C', N'1', N'1', N'system:dept:view', N'department', 100, 1, getdate(), NULL, NULL, N'部门管理菜单')
GO
INSERT sys_menu VALUES (104, N'岗位管理', 1, 5, N'post', N'system/core/post/post', N'', '0', '1', N'C', N'1', N'1', N'system:post:view', N'post', 100, 1, getdate(), NULL, NULL, N'岗位管理菜单')
GO
INSERT sys_menu VALUES (105, N'字典管理', 1, 6, N'dict', N'system/dict/dictType', N'', '0', '1', N'C', N'1', N'1', N'system:dict:view', N'dict', 100, 1, getdate(), NULL, NULL, N'字典管理菜单')
GO
INSERT sys_menu VALUES (106, N'参数设置', 1, 7, N'config', N'system/config/config', N'', '0', '1', N'C', N'1', N'1', N'system:config:view', N'edit', 100, 1, getdate(), NULL, NULL, N'参数设置菜单')
GO
INSERT sys_menu VALUES (107, N'通知公告', 1, 9, N'notice', N'system/config/notice', N'', '0', '1', N'C', N'1', N'1', N'system:notice:view', N'notification', 100, 1, getdate(), NULL, NULL, N'通知公告菜单')
GO
INSERT sys_menu VALUES (108, N'日志管理', 1, 10, N'log', N'', N'', '0', '1', N'M', N'1', N'1', N'', N'log', 100, 1, getdate(), NULL, NULL, N'日志管理菜单')
GO
INSERT sys_menu VALUES (109, N'在线用户', 2, 1, N'online', N'system/monitor/online/online', N'', '0', '1', N'C', N'1', N'1', N'monitor:online:view', N'online', 100, 1, getdate(), NULL, NULL, N'在线用户菜单')
GO
INSERT sys_menu VALUES (113, N'缓存监控', 2, 2, N'cache', N'system/monitor/cache/cache', N'', '0', '1', N'C', N'1', N'1', N'monitor:cache:view', N'redis', 100, 1, getdate(), NULL, NULL, N'缓存监控菜单')
GO
INSERT sys_menu VALUES (115, N'代码生成', 3, 2, N'gen', N'tool/gen/gen', N'', '0', '1', N'C', N'1', N'1', N'tool:gen:view', N'code', 100, 1, getdate(), NULL, NULL, N'代码生成菜单')
GO
INSERT sys_menu VALUES (116, N'页面设计', 3, 3, N'pageDesigner', N'tool/pageDesigner/pageDesigner', N'', '0', '1', N'C', N'1', N'1', N'tool:page:view', N'dashboard', 100, 1, getdate(), NULL, NULL, N'页面设计菜单')
GO
INSERT sys_menu VALUES (121, N'租户管理', 6, 1, N'tenant', N'system/tenant/tenant', N'', '0', '1', N'C', N'1', N'1', N'system:tenant:view', N'users', 100, 1, getdate(), NULL, NULL, N'租户管理菜单')
GO
INSERT sys_menu VALUES (122, N'租户套餐', 6, 2, N'tenantPackage', N'system/tenant/tenantPackage', N'', '0', '1', N'C', N'1', N'1', N'system:tenantPackage:view', N'combination', 100, 1, getdate(), NULL, NULL, N'租户套餐菜单')
GO
INSERT sys_menu VALUES (117, N'admin监控', 2, 5, N'admin', N'system/monitor/admin/admin', N'', '0', '1', N'C', N'1', N'1', N'monitor:admin:view', N'dashboard', 100, 1, getdate(), NULL, NULL, N'Admin监控菜单');
GO
INSERT sys_menu VALUES (118, N'文件管理', 1, 11, N'oss', N'system/oss/oss', N'', '0', '1', N'C', '1', N'1', N'system:oss:view', N'upload', 100, 1, getdate(), NULL, NULL, N'文件管理菜单');
GO
INSERT sys_menu VALUES (120, N'任务调度', 2, 5, N'snailjob', N'system/monitor/snailjob/snailjob', N'', '0', '1', N'C', N'1', N'1', N'monitor:snailjob:view', N'job', 100, 1, getdate(), NULL, NULL, N'SnailJob控制台菜单');
GO
INSERT sys_menu VALUES (500, N'操作日志', 108, 1, N'operLog', N'system/monitor/operLog/operLog', N'', '0', '1', N'C', N'1', N'1', N'monitor:operLog:view', N'tool', 100, 1, getdate(), NULL, NULL, N'操作日志菜单')
GO
INSERT sys_menu VALUES (501, N'登录日志', 108, 2, N'loginLog', N'system/monitor/loginLog/loginLog', N'', '0', '1', N'C', N'1', N'1', N'monitor:loginLog:view', N'login-info', 100, 1, getdate(), NULL, NULL, N'登录日志菜单')
GO
INSERT sys_menu VALUES (1644, N'错误日志', 108, 3, N'errorLog', N'system/monitor/errorLog/errorLog', N'', '0', '1', N'C', N'1', N'1', N'monitor:errorLog:view', N'bug', 100, 1, getdate(), NULL, NULL, N'错误日志菜单')
GO
INSERT sys_menu VALUES (1001, N'用户查询', 100, 1, N'', N'', N'', '0', '1', N'F', N'1', N'1', N'system:user:query', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1002, N'用户新增', 100, 2, N'', N'', N'', '0', '1', N'F', N'1', N'1', N'system:user:add', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1003, N'用户修改', 100, 3, N'', N'', N'', '0', '1', N'F', N'1', N'1', N'system:user:update', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1004, N'用户删除', 100, 4, N'', N'', N'', '0', '1', N'F', N'1', N'1', N'system:user:delete', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1005, N'用户导出', 100, 5, N'', N'', N'', '0', '1', N'F', N'1', N'1', N'system:user:export', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1006, N'用户导入', 100, 6, N'', N'', N'', '0', '1', N'F', N'1', N'1', N'system:user:import', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1007, N'重置密码', 100, 7, N'', N'', N'', '0', '1', N'F', N'1', N'1', N'system:user:resetPwd', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1008, N'角色查询', 101, 1, N'', N'', N'', '0', '1', N'F', N'1', N'1', N'system:role:query', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1009, N'角色新增', 101, 2, N'', N'', N'', '0', '1', N'F', N'1', N'1', N'system:role:add', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1010, N'角色修改', 101, 3, N'', N'', N'', '0', '1', N'F', N'1', N'1', N'system:role:update', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1011, N'角色删除', 101, 4, N'', N'', N'', '0', '1', N'F', N'1', N'1', N'system:role:delete', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1012, N'角色导出', 101, 5, N'', N'', N'', '0', '1', N'F', N'1', N'1', N'system:role:export', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1013, N'菜单查询', 102, 1, N'', N'', N'', '0', '1', N'F', N'1', N'1', N'system:menu:query', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1014, N'菜单新增', 102, 2, N'', N'', N'', '0', '1', N'F', N'1', N'1', N'system:menu:add', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1015, N'菜单修改', 102, 3, N'', N'', N'', '0', '1', N'F', N'1', N'1', N'system:menu:update', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1016, N'菜单删除', 102, 4, N'', N'', N'', '0', '1', N'F', N'1', N'1', N'system:menu:delete', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1017, N'部门查询', 100, 1, N'', N'', N'', '0', '1', N'F', N'1', N'1', N'system:dept:query', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1018, N'部门新增', 100, 2, N'', N'', N'', '0', '1', N'F', N'1', N'1', N'system:dept:add', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1019, N'部门修改', 100, 3, N'', N'', N'', '0', '1', N'F', N'1', N'1', N'system:dept:update', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1020, N'部门删除', 100, 4, N'', N'', N'', '0', '1', N'F', N'1', N'1', N'system:dept:delete', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1021, N'岗位查询', 104, 1, N'', N'', N'', '0', '1', N'F', N'1', N'1', N'system:post:query', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1022, N'岗位新增', 104, 2, N'', N'', N'', '0', '1', N'F', N'1', N'1', N'system:post:add', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1023, N'岗位修改', 104, 3, N'', N'', N'', '0', '1', N'F', N'1', N'1', N'system:post:update', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1024, N'岗位删除', 104, 4, N'', N'', N'', '0', '1', N'F', N'1', N'1', N'system:post:delete', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1025, N'岗位导出', 104, 5, N'', N'', N'', '0', '1', N'F', N'1', N'1', N'system:post:export', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1026, N'字典查询', 105, 1, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'system:dict:query', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1027, N'字典新增', 105, 2, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'system:dict:add', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1028, N'字典修改', 105, 3, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'system:dict:update', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1029, N'字典删除', 105, 4, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'system:dict:delete', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1030, N'字典导出', 105, 5, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'system:dict:export', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1031, N'参数查询', 106, 1, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'system:config:query', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1032, N'参数新增', 106, 2, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'system:config:add', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1033, N'参数修改', 106, 3, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'system:config:update', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1034, N'参数删除', 106, 4, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'system:config:delete', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1035, N'参数导出', 106, 5, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'system:config:export', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1036, N'公告查询', 107, 1, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'system:notice:query', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1037, N'公告新增', 107, 2, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'system:notice:add', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1038, N'公告修改', 107, 3, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'system:notice:update', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1039, N'公告删除', 107, 4, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'system:notice:delete', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1040, N'操作查询', 500, 1, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'monitor:operLog:query', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1041, N'操作删除', 500, 2, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'monitor:operLog:delete', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1042, N'日志导出', 500, 4, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'monitor:operLog:export', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1043, N'登录查询', 501, 1, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'monitor:loginLog:query', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1044, N'登录删除', 501, 2, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'monitor:loginLog:delete', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1045, N'日志导出', 501, 3, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'monitor:loginLog:export', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1050, N'账户解锁', 501, 4, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'monitor:loginLog:unlock',  N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1645, N'错误日志查询', 1644, 1, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'monitor:errorLog:query', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1646, N'错误日志更新', 1644, 2, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'monitor:errorLog:update', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1647, N'错误日志删除', 1644, 3, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'monitor:errorLog:delete', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1648, N'错误日志导出', 1644, 4, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'monitor:errorLog:export', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1046, N'在线查询', 109, 1, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'monitor:online:query', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1047, N'批量强退', 109, 2, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'monitor:online:batchLogout', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1048, N'单条强退', 109, 3, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'monitor:online:forceLogout', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1055, N'生成查询', 115, 1, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'tool:gen:query', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1056, N'生成修改', 115, 2, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'tool:gen:update', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1057, N'生成删除', 115, 3, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'tool:gen:delete', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1058, N'导入代码', 115, 2, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'tool:gen:import', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1059, N'预览代码', 115, 4, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'tool:gen:preview', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_menu VALUES (1060, N'生成代码', 115, 5, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'tool:gen:code', N'#', 100, 1, getdate(), NULL, NULL, N'')
GO
-- oss相关按钮
INSERT sys_menu VALUES (1600, N'文件查询', 118, 1, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'system:oss:query', N'#', 100, 1, getdate(), NULL, NULL, N'');
GO
INSERT sys_menu VALUES (1601, N'文件上传', 118, 2, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'system:oss:upload', N'#', 100, 1, getdate(), NULL, NULL, N'');
GO
INSERT sys_menu VALUES (1602, N'文件下载', 118, 3, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'system:oss:download', N'#', 100, 1, getdate(), NULL, NULL, N'');
GO
INSERT sys_menu VALUES (1603, N'文件删除', 118, 4, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'system:oss:delete', N'#', 100, 1, getdate(), NULL, NULL, N'');
GO
INSERT sys_menu VALUES (1620, N'配置列表', 118, 5, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'system:ossConfig:view', N'#', 100, 1, getdate(), NULL, NULL, N'');
GO
INSERT sys_menu VALUES (1621, N'配置添加', 118, 6, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'system:ossConfig:add', N'#', 100, 1, getdate(), NULL, NULL, N'');
GO
INSERT sys_menu VALUES (1622, N'配置编辑', 118, 7, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'system:ossConfig:update', N'#', 100, 1, getdate(), NULL, NULL, N'');
GO
INSERT sys_menu VALUES (1623, N'配置删除', 118, 8, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'system:ossConfig:delete', N'#', 100, 1, getdate(), NULL, NULL, N'');
GO
-- OSS目录权限按钮
INSERT sys_menu VALUES (1630, N'目录查询', 118, 9, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'system:ossDirectory:query', N'#', 100, 1, getdate(), NULL, NULL, N'');
GO
INSERT sys_menu VALUES (1631, N'目录新增', 118, 10, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'system:ossDirectory:add', N'#', 100, 1, getdate(), NULL, NULL, N'');
GO
INSERT sys_menu VALUES (1632, N'目录修改', 118, 11, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'system:ossDirectory:update', N'#', 100, 1, getdate(), NULL, NULL, N'');
GO
INSERT sys_menu VALUES (1633, N'目录删除', 118, 12, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'system:ossDirectory:delete', N'#', 100, 1, getdate(), NULL, NULL, N'');
GO
-- 租户管理相关按钮
INSERT sys_menu VALUES (1606, N'租户查询', 121, 1, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'system:tenant:query', N'#', 100, 1, getdate(), NULL, NULL, N'');
GO
INSERT sys_menu VALUES (1607, N'租户新增', 121, 2, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'system:tenant:add', N'#', 100, 1, getdate(), NULL, NULL, N'');
GO
INSERT sys_menu VALUES (1608, N'租户修改', 121, 3, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'system:tenant:update', N'#', 100, 1, getdate(), NULL, NULL, N'');
GO
INSERT sys_menu VALUES (1609, N'租户删除', 121, 4, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'system:tenant:delete', N'#', 100, 1, getdate(), NULL, NULL, N'');
GO
INSERT sys_menu VALUES (1610, N'租户导出', 121, 5, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'system:tenant:export', N'#', 100, 1, getdate(), NULL, NULL, N'');
GO
-- 租户套餐相关按钮
INSERT sys_menu VALUES (1611, N'租户套餐查询', 122, 1, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'system:tenantPackage:query', N'#', 100, 1, getdate(), NULL, NULL, N'');
GO
INSERT sys_menu VALUES (1612, N'租户套餐新增', 122, 2, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'system:tenantPackage:add', N'#', 100, 1, getdate(), NULL, NULL, N'');
GO
INSERT sys_menu VALUES (1613, N'租户套餐修改', 122, 3, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'system:tenantPackage:update', N'#', 100, 1, getdate(), NULL, NULL, N'');
GO
INSERT sys_menu VALUES (1614, N'租户套餐删除', 122, 4, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'system:tenantPackage:delete', N'#', 100, 1, getdate(), NULL, NULL, N'');
GO
INSERT sys_menu VALUES (1615, N'租户套餐导出', 122, 5, N'#', N'', N'', '0', '1', N'F', N'1', N'1', N'system:tenantPackage:export', N'#', 100, 1, getdate(), NULL, NULL, N'');
GO

INSERT sys_menu VALUES (1120, N'开放平台', 1, 8, N'openApi', N'', N'', '0', '0', N'M', N'1', N'1', N'', N'key', 103, 0, getdate(), NULL, NULL, N'API开放平台管理');
GO
INSERT sys_menu VALUES (1121, N'API密钥', 1120, 1, N'apiKey', N'system/openApi/apiKey/apiKey', N'', '0', '1', N'C', N'1', N'1', N'system:apiKey:view', N'password', 103, 0, getdate(), NULL, NULL, N'API密钥管理');
GO
INSERT sys_menu VALUES (1122, N'API密钥查询', 1121, 1, N'', N'', N'', '0', '1', N'F', N'1', N'1', N'system:apiKey:query', N'#', 103, 0, getdate(), NULL, NULL, N'');
GO
INSERT sys_menu VALUES (1123, N'API密钥新增', 1121, 2, N'', N'', N'', '0', '1', N'F', N'1', N'1', N'system:apiKey:add', N'#', 103, 0, getdate(), NULL, NULL, N'');
GO
INSERT sys_menu VALUES (1124, N'API密钥修改', 1121, 3, N'', N'', N'', '0', '1', N'F', N'1', N'1', N'system:apiKey:update', N'#', 103, 0, getdate(), NULL, NULL, N'');
GO
INSERT sys_menu VALUES (1125, N'API密钥删除', 1121, 4, N'', N'', N'', '0', '1', N'F', N'1', N'1', N'system:apiKey:delete', N'#', 103, 0, getdate(), NULL, NULL, N'');
GO
INSERT sys_menu VALUES (1126, N'API密钥导出', 1121, 5, N'', N'', N'', '0', '1', N'F', N'1', N'1', N'system:apiKey:export', N'#', 103, 0, getdate(), NULL, NULL, N'');
GO


CREATE TABLE sys_notice
(
    notice_id       bigint                     NOT NULL,
    tenant_id       nvarchar(20) DEFAULT ('000000') NULL,
    notice_type     nchar(1)                   NOT NULL,
    target_config   nvarchar(max)              NULL,
   target_user_ids nvarchar(max)              NULL,
   read_user_ids   nvarchar(max)              NULL,
   notice_title    nvarchar(50)               NOT NULL,
   notice_content  nvarchar(max)              NULL,
   status          nchar(1)     DEFAULT ('1') NULL,
   create_dept     bigint                     NULL,
   create_by       bigint                     NULL,
   create_time     datetime2(7)               NULL,
   update_by       bigint                     NULL,
   update_time     datetime2(7)               NULL,
   remark          nvarchar(255)              NULL,
   CONSTRAINT PK__sys_noti__3E82A5DB0EC94801 PRIMARY KEY CLUSTERED (notice_id)
       WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
       ON [PRIMARY]
)
    ON [PRIMARY]
TEXTIMAGE_ON [PRIMARY]
GO

EXEC sys.sp_addextendedproperty
   'MS_Description', N'公告ID' ,
   'SCHEMA', N'dbo',
   'TABLE', N'sys_notice',
   'COLUMN', N'notice_id'
GO
EXEC sys.sp_addextendedproperty
   'MS_Description', N'租户id' ,
   'SCHEMA', N'dbo',
   'TABLE', N'sys_notice',
   'COLUMN', N'tenant_id'
GO
EXEC sys.sp_addextendedproperty
   'MS_Description', N'公告类型（1通知 2公告）' ,
   'SCHEMA', N'dbo',
   'TABLE', N'sys_notice',
   'COLUMN', N'notice_type'
GO
EXEC sys.sp_addextendedproperty
   'MS_Description', N'推送配置JSON' ,
   'SCHEMA', N'dbo',
   'TABLE', N'sys_notice',
   'COLUMN', N'target_config'
GO
EXEC sys.sp_addextendedproperty
   'MS_Description', N'目标用户ID列表' ,
   'SCHEMA', N'dbo',
   'TABLE', N'sys_notice',
   'COLUMN', N'target_user_ids'
GO
EXEC sys.sp_addextendedproperty
   'MS_Description', N'已读用户ID列表' ,
   'SCHEMA', N'dbo',
   'TABLE', N'sys_notice',
   'COLUMN', N'read_user_ids'
GO
EXEC sys.sp_addextendedproperty
   'MS_Description', N'公告标题' ,
   'SCHEMA', N'dbo',
   'TABLE', N'sys_notice',
   'COLUMN', N'notice_title'
GO
EXEC sys.sp_addextendedproperty
   'MS_Description', N'公告内容' ,
   'SCHEMA', N'dbo',
   'TABLE', N'sys_notice',
   'COLUMN', N'notice_content'
GO
EXEC sys.sp_addextendedproperty
   'MS_Description', N'公告状态' ,
   'SCHEMA', N'dbo',
   'TABLE', N'sys_notice',
   'COLUMN', N'status'
GO
EXEC sys.sp_addextendedproperty
   'MS_Description', N'创建部门' ,
   'SCHEMA', N'dbo',
   'TABLE', N'sys_notice',
   'COLUMN', N'create_dept'
GO
EXEC sys.sp_addextendedproperty
   'MS_Description', N'创建者' ,
   'SCHEMA', N'dbo',
   'TABLE', N'sys_notice',
   'COLUMN', N'create_by'
GO
EXEC sys.sp_addextendedproperty
   'MS_Description', N'创建时间' ,
   'SCHEMA', N'dbo',
   'TABLE', N'sys_notice',
   'COLUMN', N'create_time'
GO
EXEC sys.sp_addextendedproperty
   'MS_Description', N'更新者' ,
   'SCHEMA', N'dbo',
   'TABLE', N'sys_notice',
   'COLUMN', N'update_by'
GO
EXEC sys.sp_addextendedproperty
   'MS_Description', N'更新时间' ,
   'SCHEMA', N'dbo',
   'TABLE', N'sys_notice',
   'COLUMN', N'update_time'
GO
EXEC sys.sp_addextendedproperty
   'MS_Description', N'备注' ,
   'SCHEMA', N'dbo',
   'TABLE', N'sys_notice',
   'COLUMN', N'remark'
GO
EXEC sys.sp_addextendedproperty
   'MS_Description', N'通知公告表' ,
   'SCHEMA', N'dbo',
   'TABLE', N'sys_notice'
GO

INSERT sys_notice VALUES (1, N'000000', N'2', N'{"type": "all"}', NULL, NULL, N'温馨提醒：2018-07-01 若依新版本发布啦', N'新版本内容', N'1', 100, 1, getdate(), NULL, NULL, N'管理员')
GO
INSERT sys_notice VALUES (2, N'000000', N'1', N'{"type": "all"}', NULL, NULL, N'维护通知：2018-07-01 若依系统凌晨维护', N'维护内容', N'1', 100, 1, getdate(), NULL, NULL, N'管理员')
GO

CREATE TABLE sys_oper_log
(
    oper_id        bigint                       NOT NULL,
    tenant_id      nvarchar(20)   DEFAULT ('000000') NULL,
    title          nvarchar(50)   DEFAULT ''    NULL,
    oper_type      nvarchar(8)    DEFAULT ('1') NULL,
    method         nvarchar(100)  DEFAULT ''    NULL,
    request_method nvarchar(10)   DEFAULT ''    NULL,
    operator_type  nvarchar(20)   DEFAULT ''    NULL,
    oper_name      nvarchar(50)   DEFAULT ''    NULL,
    dept_name      nvarchar(50)   DEFAULT ''    NULL,
    oper_url       nvarchar(255)  DEFAULT ''    NULL,
    oper_ip        nvarchar(128)  DEFAULT ''    NULL,
    oper_location  nvarchar(255)  DEFAULT ''    NULL,
    oper_param     nvarchar(max)                NULL,
    json_result    nvarchar(max)                NULL,
    status         nchar(1)       DEFAULT ('1') NULL,
    error_msg      nvarchar(max)                NULL,
    oper_time      datetime2(7)                 NULL,
    cost_time      bigint         DEFAULT ((0)) NULL,
    CONSTRAINT PK__sys_oper__34723BF9BD954573 PRIMARY KEY CLUSTERED (oper_id)
        WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
        ON [PRIMARY]
)
ON [PRIMARY]
GO

CREATE NONCLUSTERED INDEX idx_sys_oper_log_bt ON sys_oper_log (oper_type)
GO
CREATE NONCLUSTERED INDEX idx_sys_oper_log_s ON sys_oper_log (status)
GO
CREATE NONCLUSTERED INDEX idx_sys_oper_log_ot ON sys_oper_log (oper_time)
GO

EXEC sys.sp_addextendedproperty
    'MS_Description', N'日志主键' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oper_log',
    'COLUMN', N'oper_id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'租户id' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oper_log',
    'COLUMN', N'tenant_id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'模块标题' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oper_log',
    'COLUMN', N'title'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'操作类型' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oper_log',
    'COLUMN', N'oper_type'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'方法名称' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oper_log',
    'COLUMN', N'method'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'请求方式' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oper_log',
    'COLUMN', N'request_method'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'操作用户类别' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oper_log',
    'COLUMN', N'operator_type'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'操作人员' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oper_log',
    'COLUMN', N'oper_name'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'部门名称' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oper_log',
    'COLUMN', N'dept_name'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'请求URL' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oper_log',
    'COLUMN', N'oper_url'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'主机地址' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oper_log',
    'COLUMN', N'oper_ip'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'操作地点' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oper_log',
    'COLUMN', N'oper_location'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'请求参数' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oper_log',
    'COLUMN', N'oper_param'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'返回参数' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oper_log',
    'COLUMN', N'json_result'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'操作结果' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oper_log',
    'COLUMN', N'status'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'错误消息' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oper_log',
    'COLUMN', N'error_msg'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'操作时间' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oper_log',
    'COLUMN', N'oper_time'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'消耗时间' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oper_log',
    'COLUMN', N'cost_time'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'操作日志' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oper_log'
GO

CREATE TABLE sys_post
(
    post_id     bigint                          NOT NULL,
    tenant_id   nvarchar(20) DEFAULT ('000000') NULL,
    dept_id     bigint                          NOT NULL,
    post_code   nvarchar(64)                    NOT NULL,
    post_category nvarchar(100)                 NULL,
    post_name   nvarchar(50)                    NOT NULL,
    post_sort   int                             NOT NULL,
    status      nchar(1)          DEFAULT ('1') NULL,
    create_dept bigint                          NULL,
    create_by   bigint                          NULL,
    create_time datetime2(7)                    NULL,
    update_by   bigint                          NULL,
    update_time datetime2(7)                    NULL,
    remark      nvarchar(500)                   NULL,
    CONSTRAINT PK__sys_post__3ED7876668E2D081 PRIMARY KEY CLUSTERED (post_id)
        WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
        ON [PRIMARY]
)
ON [PRIMARY]
GO

EXEC sys.sp_addextendedproperty
    'MS_Description', N'岗位ID' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_post',
    'COLUMN', N'post_id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'租户id' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_post',
    'COLUMN', N'tenant_id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'部门id' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_post',
    'COLUMN', N'dept_id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'岗位编码' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_post',
    'COLUMN', N'post_code'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'岗位类别编码' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_post',
    'COLUMN', N'post_category'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'岗位名称' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_post',
    'COLUMN', N'post_name'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'显示顺序' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_post',
    'COLUMN', N'post_sort'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'状态' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_post',
    'COLUMN', N'status'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建部门' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_post',
    'COLUMN', N'create_dept'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建者' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_post',
    'COLUMN', N'create_by'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建时间' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_post',
    'COLUMN', N'create_time'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'更新者' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_post',
    'COLUMN', N'update_by'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'更新时间' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_post',
    'COLUMN', N'update_time'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'备注' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_post',
    'COLUMN', N'remark'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'岗位信息表' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_post'
GO

INSERT sys_post VALUES (1, N'000000', 100, N'ceo', NULL,  N'董事长', 1, N'1', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_post VALUES (2, N'000000', 100, N'se', NULL,  N'项目经理', 2, N'1', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_post VALUES (3, N'000000', 100, N'hr', NULL,  N'人力资源', 3, N'1', 100, 1, getdate(), NULL, NULL, N'')
GO
INSERT sys_post VALUES (4, N'000000', 100, N'user', NULL,  N'普通员工', 4, N'1', 100, 1, getdate(), NULL, NULL, N'')
GO

CREATE TABLE sys_role
(
    role_id             bigint                     NOT NULL,
    tenant_id           nvarchar(20) DEFAULT ('000000') NULL,
    role_name           nvarchar(30)               NOT NULL,
    role_key            nvarchar(100)              NOT NULL,
    role_sort           int                        NOT NULL,
    data_scope          nchar(1)     DEFAULT ('1') NULL,
    menu_check_strictly tinyint      DEFAULT ((1)) NULL,
    dept_check_strictly tinyint      DEFAULT ((1)) NULL,
    status              nchar(1)     DEFAULT ('1') NULL,
    is_deleted          nchar(1)     DEFAULT ('0') NULL,
    create_dept         bigint                     NULL,
    create_by           bigint                     NULL,
    create_time         datetime2(7)               NULL,
    update_by           bigint                     NULL,
    update_time         datetime2(7)               NULL,
    remark              nvarchar(500)              NULL,
    CONSTRAINT PK__sys_role__760965CCF9383145 PRIMARY KEY CLUSTERED (role_id)
        WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
        ON [PRIMARY]
)
ON [PRIMARY]
GO

EXEC sys.sp_addextendedproperty
    'MS_Description', N'角色ID' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_role',
    'COLUMN', N'role_id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'租户id' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_role',
    'COLUMN', N'tenant_id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'角色名称' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_role',
    'COLUMN', N'role_name'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'角色权限字符串' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_role',
    'COLUMN', N'role_key'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'显示顺序' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_role',
    'COLUMN', N'role_sort'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限 5：仅本人数据权限 6：部门及以下或本人数据权限）' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_role',
    'COLUMN', N'data_scope'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'菜单树选择项是否关联显示' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_role',
    'COLUMN', N'menu_check_strictly'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'部门树选择项是否关联显示' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_role',
    'COLUMN', N'dept_check_strictly'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'角色状态' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_role',
    'COLUMN', N'status'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'是否删除' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_role',
    'COLUMN', N'is_deleted'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建部门' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_role',
    'COLUMN', N'create_dept'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建者' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_role',
    'COLUMN', N'create_by'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建时间' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_role',
    'COLUMN', N'create_time'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'更新者' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_role',
    'COLUMN', N'update_by'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'更新时间' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_role',
    'COLUMN', N'update_time'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'备注' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_role',
    'COLUMN', N'remark'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'角色信息表' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_role'
GO

INSERT sys_role VALUES (1, N'000000', N'超级管理员', N'superadmin', 1, N'1', 1, 1, N'1', N'0', 100, 1, getdate(), NULL, NULL, N'超级管理员')
GO
INSERT sys_role VALUES (2, N'000000', N'PC端普通用户', N'pc_user', 2, N'1', 1, 1, N'1', N'0', 100, 1, getdate(), NULL, NULL, N'');
GO
INSERT sys_role VALUES (3, N'000000', N'移动端普通用户', N'app_user', 3, N'1', 1, 1, N'1', N'0', 100, 1, getdate(), NULL, NULL, N'');
GO

CREATE TABLE sys_role_dept
(
    role_id bigint NOT NULL,
    dept_id bigint NOT NULL,
    CONSTRAINT PK__sys_role__2BC3005BABBCA08A PRIMARY KEY CLUSTERED (role_id, dept_id)
        WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
        ON [PRIMARY]
)
ON [PRIMARY]
GO

EXEC sys.sp_addextendedproperty
    'MS_Description', N'角色ID' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_role_dept',
    'COLUMN', N'role_id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'部门ID' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_role_dept',
    'COLUMN', N'dept_id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'角色和部门关联表' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_role_dept'
GO

CREATE TABLE sys_role_menu
(
    role_id bigint NOT NULL,
    menu_id bigint NOT NULL,
    CONSTRAINT PK__sys_role__A2C36A6187BA4B17 PRIMARY KEY CLUSTERED (role_id, menu_id)
        WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
        ON [PRIMARY]
)
ON [PRIMARY]
GO

EXEC sys.sp_addextendedproperty
    'MS_Description', N'角色ID' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_role_menu',
    'COLUMN', N'role_id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'菜单ID' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_role_menu',
    'COLUMN', N'menu_id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'角色和菜单关联表' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_role_menu'
GO

-- 初始化-角色和菜单关联表数据
INSERT sys_role_menu VALUES (3, 1601);
GO


CREATE TABLE sys_user
(
    user_id     bigint                             NOT NULL,
    tenant_id   nvarchar(20)  DEFAULT ('000000')   NULL,
    dept_id     bigint                             NULL,
    user_name   nvarchar(30)                       NOT NULL,
    nick_name   nvarchar(30)                       NULL,
    user_type   nvarchar(20)  DEFAULT 'pc_user'    NULL,
    email       nvarchar(50)  DEFAULT ''           NULL,
    phone nvarchar(11)  DEFAULT ''           NULL,
    gender      nchar(1)                           NULL,
    avatar      nvarchar(255) DEFAULT ''           NULL,
    password    nvarchar(100) DEFAULT ''           NULL,
    status      nchar(1)      DEFAULT ('1')        NULL,
    is_deleted  nchar(1)      DEFAULT ('0')        NULL,
    login_ip    nvarchar(128) DEFAULT ''           NULL,
    login_date  datetime2(7)                       NULL,
    create_dept bigint                             NULL,
    create_by   bigint                             NULL,
    create_time datetime2(7)                       NULL,
    update_by   bigint                             NULL,
    update_time datetime2(7)                       NULL,
    remark      nvarchar(500)                      NULL,
    CONSTRAINT PK__sys_user__B9BE370F79170B6A PRIMARY KEY CLUSTERED (user_id)
        WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
        ON [PRIMARY]
)
ON [PRIMARY]
GO

EXEC sys.sp_addextendedproperty
    'MS_Description', N'用户ID' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_user',
    'COLUMN', N'user_id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'租户id' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_user',
    'COLUMN', N'tenant_id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'部门ID' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_user',
    'COLUMN', N'dept_id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'用户账号' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_user',
    'COLUMN', N'user_name'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'用户昵称' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_user',
    'COLUMN', N'nick_name'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'用户类型' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_user',
    'COLUMN', N'user_type'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'用户邮箱' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_user',
    'COLUMN', N'email'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'手机号码' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_user',
    'COLUMN', N'phone'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'用户性别' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_user',
    'COLUMN', N'gender'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'头像地址' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_user',
    'COLUMN', N'avatar'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'密码' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_user',
    'COLUMN', N'password'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'帐号状态' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_user',
    'COLUMN', N'status'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'是否删除' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_user',
    'COLUMN', N'is_deleted'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'最后登录IP' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_user',
    'COLUMN', N'login_ip'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'最后登录时间' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_user',
    'COLUMN', N'login_date'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建部门' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_user',
    'COLUMN', N'create_dept'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建者' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_user',
    'COLUMN', N'create_by'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建时间' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_user',
    'COLUMN', N'create_time'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'更新者' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_user',
    'COLUMN', N'update_by'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'更新时间' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_user',
    'COLUMN', N'update_time'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'备注' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_user',
    'COLUMN', N'remark'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'用户信息表' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_user'
GO

INSERT sys_user VALUES (1, N'000000', 100,  N'superadmin', N'抓蛙师', N'sys_user', N'770492966@qq.com', N'15888888888', N'1', NULL, N'$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', N'1', N'0', N'127.0.0.1', getdate(), 100, 1, getdate(), NULL, NULL, N'管理员')
GO
INSERT sys_user VALUES (3, N'000000', 108, N'test', N'本部门及以下 密码666666', N'sys_user', N'', N'', N'0', NULL, N'$2a$10$b8yUzN0C71sbz.PhNOCgJe.Tu1yWC3RNrTyjSQ8p1W0.aaUXUJ.Ne', N'1', N'0', N'127.0.0.1', getdate(), 100, 1, getdate(), 3, getdate(), NULL);
GO
INSERT sys_user VALUES (4, N'000000', 102, N'test1', N'仅本人 密码666666', N'sys_user', N'', N'', N'0', NULL, N'$2a$10$b8yUzN0C71sbz.PhNOCgJe.Tu1yWC3RNrTyjSQ8p1W0.aaUXUJ.Ne', N'1', N'0', N'127.0.0.1', getdate(), 100, 1, getdate(), 4, getdate(), NULL);
GO

CREATE TABLE sys_user_post
(
    user_id bigint NOT NULL,
    post_id bigint NOT NULL,
    CONSTRAINT PK__sys_user__CA534F799C04589B PRIMARY KEY CLUSTERED (user_id, post_id)
        WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
        ON [PRIMARY]
)
ON [PRIMARY]
GO

EXEC sys.sp_addextendedproperty
    'MS_Description', N'用户ID' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_user_post',
    'COLUMN', N'user_id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'岗位ID' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_user_post',
    'COLUMN', N'post_id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'用户与岗位关联表' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_user_post'
GO

INSERT sys_user_post VALUES (1, 1)
GO

CREATE TABLE sys_user_role
(
    user_id bigint NOT NULL,
    role_id bigint NOT NULL,
    CONSTRAINT PK__sys_user__6EDEA153FB34D8F0 PRIMARY KEY CLUSTERED (user_id, role_id)
        WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
        ON [PRIMARY]
)
ON [PRIMARY]
GO

EXEC sys.sp_addextendedproperty
    'MS_Description', N'用户ID' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_user_role',
    'COLUMN', N'user_id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'角色ID' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_user_role',
    'COLUMN', N'role_id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'用户和角色关联表' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_user_role'
GO

INSERT sys_user_role VALUES (1, 1)
GO
INSERT sys_user_role VALUES (3, 3);
GO
INSERT sys_user_role VALUES (4, 3);
GO

CREATE TABLE sys_oss
(
    oss_id        bigint                          NOT NULL,
    tenant_id     nvarchar(20)  DEFAULT ('000000') NULL,
    directory_id  bigint                          NULL,
    file_name     nvarchar(255) DEFAULT ''        NOT NULL,
    original_name nvarchar(255) DEFAULT ''        NOT NULL,
    file_suffix   nvarchar(10)  DEFAULT ''        NOT NULL,
    file_size     bigint                          NULL,
    url           nvarchar(500)                   NOT NULL,
    ext1          nvarchar(500) DEFAULT ''        NULL,
    create_dept   bigint                          NULL,
    create_time   datetime2(7)                    NULL,
    create_by     bigint                          NULL,
    update_time   datetime2(7)                    NULL,
    update_by     bigint                          NULL,
    service       nvarchar(20)  DEFAULT ('minio') NOT NULL,
    CONSTRAINT PK__sys_oss__91241EA442389F0D PRIMARY KEY CLUSTERED (oss_id)
        WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
        ON [PRIMARY]
)
ON [PRIMARY]
GO

EXEC sp_addextendedproperty
    'MS_Description', N'对象存储主键',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss',
    'COLUMN', N'oss_id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'租户id' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss',
    'COLUMN', N'tenant_id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'所属目录ID' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss',
    'COLUMN', N'directory_id'
GO
EXEC sp_addextendedproperty
    'MS_Description', N'文件名',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss',
    'COLUMN', N'file_name'
GO
EXEC sp_addextendedproperty
    'MS_Description', N'原名',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss',
    'COLUMN', N'original_name'
GO
EXEC sp_addextendedproperty
    'MS_Description', N'文件后缀名',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss',
    'COLUMN', N'file_suffix'
GO
EXEC sp_addextendedproperty
    'MS_Description', N'文件大小(字节)',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss',
    'COLUMN', N'file_size'
GO
EXEC sp_addextendedproperty
    'MS_Description', N'URL地址',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss',
    'COLUMN', N'url'
GO
EXEC sp_addextendedproperty
    'MS_Description', N'扩展字段',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss',
    'COLUMN', N'ext1'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建部门' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss',
    'COLUMN', N'create_dept'
GO
EXEC sp_addextendedproperty
    'MS_Description', N'创建时间',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss',
    'COLUMN', N'create_time'
GO
EXEC sp_addextendedproperty
    'MS_Description', N'上传人',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss',
    'COLUMN', N'create_by'
GO
EXEC sp_addextendedproperty
    'MS_Description', N'更新时间',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss',
    'COLUMN', N'update_time'
GO
EXEC sp_addextendedproperty
    'MS_Description', N'更新人',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss',
    'COLUMN', N'update_by'
GO
EXEC sp_addextendedproperty
    'MS_Description', N'服务商',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss',
    'COLUMN', N'service'
GO
EXEC sp_addextendedproperty
    'MS_Description', N'OSS对象存储表',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss'
GO

CREATE TABLE sys_oss_config
(
    oss_config_id bigint                      NOT NULL,
    tenant_id     nvarchar(20)  DEFAULT ('000000') NULL,
    config_key    nvarchar(20)  DEFAULT ''    NOT NULL,
    access_key    nvarchar(255) DEFAULT ''    NULL,
    secret_key    nvarchar(255) DEFAULT ''    NULL,
    bucket_name   nvarchar(255) DEFAULT ''    NULL,
    prefix        nvarchar(255) DEFAULT ''    NULL,
    endpoint      nvarchar(255) DEFAULT ''    NULL,
    domain        nvarchar(255) DEFAULT ''    NULL,
    is_https      nchar(1)      DEFAULT ('0') NULL,
    region        nvarchar(255) DEFAULT ''    NULL,
    access_policy nchar(1)      DEFAULT ('1') NOT NULL,
    status        nchar(1)      DEFAULT ('1') NULL,
    ext1          nvarchar(255) DEFAULT ''    NULL,
    create_dept   bigint                      NULL,
    create_by     bigint                      NULL,
    create_time   datetime2(7)                NULL,
    update_by     bigint                      NULL,
    update_time   datetime2(7)                NULL,
    remark        nvarchar(500)               NULL,
    CONSTRAINT PK__sys_oss___BFBDE87009ED2882 PRIMARY KEY CLUSTERED (oss_config_id)
        WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
        ON [PRIMARY]
)
ON [PRIMARY]
GO

EXEC sp_addextendedproperty
    'MS_Description', N'主键',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss_config',
    'COLUMN', N'oss_config_id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'租户id' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss_config',
    'COLUMN', N'tenant_id'
GO
EXEC sp_addextendedproperty
    'MS_Description', N'配置key',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss_config',
    'COLUMN', N'config_key'
GO
EXEC sp_addextendedproperty
    'MS_Description', N'accessKey',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss_config',
    'COLUMN', N'access_key'
GO
EXEC sp_addextendedproperty
    'MS_Description', N'秘钥',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss_config',
    'COLUMN', N'secret_key'
GO
EXEC sp_addextendedproperty
    'MS_Description', N'桶名称',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss_config',
    'COLUMN', N'bucket_name'
GO
EXEC sp_addextendedproperty
    'MS_Description', N'前缀',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss_config',
    'COLUMN', N'prefix'
GO
EXEC sp_addextendedproperty
    'MS_Description', N'访问站点',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss_config',
    'COLUMN', N'endpoint'
GO
EXEC sp_addextendedproperty
     'MS_Description', N'自定义域名',
     'SCHEMA', N'dbo',
     'TABLE', N'sys_oss_config',
     'COLUMN', N'domain'
GO
EXEC sp_addextendedproperty
    'MS_Description', N'是否https（1=是,0=否）',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss_config',
    'COLUMN', N'is_https'
GO
EXEC sp_addextendedproperty
    'MS_Description', N'域',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss_config',
    'COLUMN', N'region'
GO
EXEC sp_addextendedproperty
     'MS_Description', N'桶权限类型(0=private 1=public 2=custom)',
     'SCHEMA', N'dbo',
     'TABLE', N'sys_oss_config',
     'COLUMN', N'access_policy'
GO
EXEC sp_addextendedproperty
    'MS_Description', N'启用状态',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss_config',
    'COLUMN', N'status'
GO
EXEC sp_addextendedproperty
    'MS_Description', N'扩展字段',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss_config',
    'COLUMN', N'ext1'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建部门' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss_config',
    'COLUMN', N'create_dept'
GO
EXEC sp_addextendedproperty
    'MS_Description', N'创建者',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss_config',
    'COLUMN', N'create_by'
GO
EXEC sp_addextendedproperty
    'MS_Description', N'创建时间',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss_config',
    'COLUMN', N'create_time'
GO
EXEC sp_addextendedproperty
    'MS_Description', N'更新者',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss_config',
    'COLUMN', N'update_by'
GO
EXEC sp_addextendedproperty
    'MS_Description', N'更新时间',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss_config',
    'COLUMN', N'update_time'
GO
EXEC sp_addextendedproperty
    'MS_Description', N'备注',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss_config',
    'COLUMN', N'remark'
GO
EXEC sp_addextendedproperty
    'MS_Description', N'对象存储配置表',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss_config'
GO

INSERT INTO sys_oss_config VALUES (N'1', N'000000', N'minio', N'ruoyi',            N'ruoyi123',        N'ruoyi',            N'erp_sys', N'127.0.0.1:9000',                    N'',N'0', N'',           N'1', N'0', N'', 100, 1, getdate(), 1, getdate(), NULL)
GO
INSERT INTO sys_oss_config VALUES (N'2', N'000000', N'qiniu', N'XXXXXXXXXXXXXXXX', N'XXXXXXXXXXXXXXX', N'ruoyi',            N'erp_sys', N's3-cn-north-1.qiniucs.com',         N'',N'0', N'',           N'1', N'0', N'', 100, 1, getdate(), 1, getdate(), NULL)
GO
INSERT INTO sys_oss_config VALUES (N'3', N'000000', N'aliyun', N'XXXXXXXXXXXXXXX', N'XXXXXXXXXXXXXXX', N'ruoyi',            N'erp_sys', N'oss-cn-beijing.aliyuncs.com',       N'',N'0', N'',           N'1', N'0', N'', 100, 1, getdate(), 1, getdate(), NULL)
GO
INSERT INTO sys_oss_config VALUES (N'4', N'000000', N'qcloud', N'XXXXXXXXXXXXXXX', N'XXXXXXXXXXXXXXX', N'ruoyi-1250000000', N'erp_sys', N'cos.ap-beijing.myqcloud.com',       N'',N'0', N'ap-beijing', N'0', N'0', N'', 100, 1, getdate(), 1, getdate(), NULL)
GO
INSERT INTO sys_oss_config VALUES (N'5', N'000000', N'image',  N'ruoyi',           N'ruoyi123',        N'ruoyi',            N'erp_sys', N'127.0.0.1:9000',               N'',N'0', N'',           N'0', N'0', N'', 100, 1, getdate(), 1, getdate(), NULL)
GO
INSERT INTO sys_oss_config VALUES (N'6', N'000000', N'local',  N'local',           N'local',        N'local',            N'erp_sys', N'127.0.0.1',               N'',N'0', N'',           N'1', N'1', N'', 100, 1, getdate(), 1, getdate(), '支持配置自定义域名和是否https，自定义域名不要加前缀，自定义域名如果做了反向代理，需要带上反向代理路径,如: xxx.ruoyikj.top/xxx')
GO

-- -------------------------
-- OSS目录表
-- -------------------------
    IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[sys_oss_directory]') AND type in (N'U'))
DROP TABLE [dbo].[sys_oss_directory]
GO

CREATE TABLE [dbo].[sys_oss_directory]
(
    [directory_id]    BIGINT                         NOT NULL,
    [tenant_id]       NVARCHAR(20)  DEFAULT ('000000') NULL,
    [parent_id]       BIGINT        DEFAULT ((0))    NULL,
    [ancestors]       NVARCHAR(500) DEFAULT ('')     NULL,
    [directory_name]  NVARCHAR(255) DEFAULT ('')     NOT NULL,
    [directory_path]  NVARCHAR(500) DEFAULT ('')     NULL,
    [order_num]       INT           DEFAULT ((0))    NULL,
    [status]          CHAR(1)       DEFAULT ('1')    NULL,
    [is_default]      CHAR(1)       DEFAULT ('0')    NULL,
    [create_dept]     BIGINT                         NULL,
    [create_by]       BIGINT                         NULL,
    [create_time]     DATETIME2(7)                   NULL,
    [update_by]       BIGINT                         NULL,
    [update_time]     DATETIME2(7)                   NULL,
    [remark]          NVARCHAR(500)                  NULL,
    CONSTRAINT [PK_sys_oss_directory] PRIMARY KEY CLUSTERED ([directory_id])
    WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
    ON [PRIMARY]
    )
    ON [PRIMARY]
GO

    EXEC sp_addextendedproperty
    'MS_Description', N'目录ID',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss_directory',
    'COLUMN', N'directory_id'
GO

    EXEC sp_addextendedproperty
    'MS_Description', N'租户id',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss_directory',
    'COLUMN', N'tenant_id'
GO

    EXEC sp_addextendedproperty
    'MS_Description', N'父目录ID',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss_directory',
    'COLUMN', N'parent_id'
GO

    EXEC sp_addextendedproperty
    'MS_Description', N'祖级列表',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss_directory',
    'COLUMN', N'ancestors'
GO

    EXEC sp_addextendedproperty
    'MS_Description', N'目录名称',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss_directory',
    'COLUMN', N'directory_name'
GO

    EXEC sp_addextendedproperty
    'MS_Description', N'目录路径',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss_directory',
    'COLUMN', N'directory_path'
GO

    EXEC sp_addextendedproperty
    'MS_Description', N'显示顺序',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss_directory',
    'COLUMN', N'order_num'
GO

    EXEC sp_addextendedproperty
    'MS_Description', N'目录状态',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss_directory',
    'COLUMN', N'status'
GO

    EXEC sp_addextendedproperty
    'MS_Description', N'是否默认目录',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss_directory',
    'COLUMN', N'is_default'
GO

    EXEC sp_addextendedproperty
    'MS_Description', N'创建部门',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss_directory',
    'COLUMN', N'create_dept'
GO

    EXEC sp_addextendedproperty
    'MS_Description', N'创建者',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss_directory',
    'COLUMN', N'create_by'
GO

    EXEC sp_addextendedproperty
    'MS_Description', N'创建时间',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss_directory',
    'COLUMN', N'create_time'
GO

    EXEC sp_addextendedproperty
    'MS_Description', N'更新者',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss_directory',
    'COLUMN', N'update_by'
GO

    EXEC sp_addextendedproperty
    'MS_Description', N'更新时间',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss_directory',
    'COLUMN', N'update_time'
GO

    EXEC sp_addextendedproperty
    'MS_Description', N'备注',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss_directory',
    'COLUMN', N'remark'
GO

    EXEC sp_addextendedproperty
    'MS_Description', N'OSS目录',
    'SCHEMA', N'dbo',
    'TABLE', N'sys_oss_directory'
GO

-- 模板表
CREATE TABLE a_temp
(
    id            bigint                      NOT NULL,
    tenant_id     nvarchar(20)  DEFAULT ('000000') NULL,
    status        nchar(1)      DEFAULT ('1') NULL,
    create_dept   bigint                      NULL,
    create_by     bigint                      NULL,
    create_time   datetime2(7)                NULL,
    update_by     bigint                      NULL,
    update_time   datetime2(7)                NULL,
    remark        nvarchar(255)               NULL,
    is_deleted    nchar(1)      DEFAULT ('0') NULL,
    CONSTRAINT PK__a_temp__3213E83F PRIMARY KEY CLUSTERED (id)
        WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
    ON [PRIMARY]
)
    ON [PRIMARY]
GO

EXEC sys.sp_addextendedproperty
    'MS_Description', N'id' ,
    'SCHEMA', N'dbo',
    'TABLE', N'a_temp',
    'COLUMN', N'id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'租户id' ,
    'SCHEMA', N'dbo',
    'TABLE', N'a_temp',
    'COLUMN', N'tenant_id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'状态' ,
    'SCHEMA', N'dbo',
    'TABLE', N'a_temp',
    'COLUMN', N'status'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建部门' ,
    'SCHEMA', N'dbo',
    'TABLE', N'a_temp',
    'COLUMN', N'create_dept'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建人' ,
    'SCHEMA', N'dbo',
    'TABLE', N'a_temp',
    'COLUMN', N'create_by'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建时间' ,
    'SCHEMA', N'dbo',
    'TABLE', N'a_temp',
    'COLUMN', N'create_time'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'修改人' ,
    'SCHEMA', N'dbo',
    'TABLE', N'a_temp',
    'COLUMN', N'update_by'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'更新时间' ,
    'SCHEMA', N'dbo',
    'TABLE', N'a_temp',
    'COLUMN', N'update_time'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'备注' ,
    'SCHEMA', N'dbo',
    'TABLE', N'a_temp',
    'COLUMN', N'remark'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'是否删除' ,
    'SCHEMA', N'dbo',
    'TABLE', N'a_temp',
    'COLUMN', N'is_deleted'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'模板' ,
    'SCHEMA', N'dbo',
    'TABLE', N'a_temp'
GO

-- SQL Server 数据库索引创建语句

-- 1. sys_social 第三方平台授权表
CREATE NONCLUSTERED INDEX idx_sys_social_tenant_user ON sys_social (tenant_id, user_id);
CREATE NONCLUSTERED INDEX idx_sys_social_auth_id ON sys_social (auth_id);
CREATE NONCLUSTERED INDEX idx_sys_social_source ON sys_social (source);

-- 2. sys_tenant 租户表
CREATE NONCLUSTERED INDEX idx_sys_tenant_tenant_id ON sys_tenant (tenant_id);

-- 4. sys_dept 部门表
CREATE NONCLUSTERED INDEX idx_sys_dept_tenant_id ON sys_dept (tenant_id);
CREATE NONCLUSTERED INDEX idx_sys_dept_parent_id ON sys_dept (parent_id);

-- 5. sys_user 用户信息表
CREATE NONCLUSTERED INDEX idx_sys_user_tenant_id ON sys_user (tenant_id);
CREATE NONCLUSTERED INDEX idx_sys_user_user_name ON sys_user (user_name);

-- 6. sys_post 岗位信息表
CREATE NONCLUSTERED INDEX idx_sys_post_tenant_id ON sys_post (tenant_id);
CREATE NONCLUSTERED INDEX idx_sys_post_dept_id ON sys_post (dept_id);

-- 7. sys_role 角色信息表
CREATE NONCLUSTERED INDEX idx_sys_role_tenant_id ON sys_role (tenant_id);
CREATE NONCLUSTERED INDEX idx_sys_role_role_key ON sys_role (role_key);

-- 8. sys_menu 菜单权限表
CREATE NONCLUSTERED INDEX idx_sys_menu_parent_id ON sys_menu (parent_id);
CREATE NONCLUSTERED INDEX idx_sys_menu_menu_type ON sys_menu (menu_type);

-- 9. sys_user_role 用户和角色关联表
CREATE NONCLUSTERED INDEX idx_sys_user_role_role_id ON sys_user_role (role_id);

-- 10. sys_role_menu 角色和菜单关联表
CREATE NONCLUSTERED INDEX idx_sys_role_menu_menu_id ON sys_role_menu (menu_id);

-- 11. sys_role_dept 角色和部门关联表
CREATE NONCLUSTERED INDEX idx_sys_role_dept_dept_id ON sys_role_dept (dept_id);

-- 12. sys_user_post 用户与岗位关联表
CREATE NONCLUSTERED INDEX idx_sys_user_post_post_id ON sys_user_post (post_id);

-- 13. sys_oper_log 操作日志
CREATE NONCLUSTERED INDEX idx_sys_oper_log_tenant_id ON sys_oper_log (tenant_id);

-- 14. sys_dict_type 字典类型表
CREATE NONCLUSTERED INDEX idx_sys_dict_type_tenant_id ON sys_dict_type (tenant_id);
CREATE NONCLUSTERED INDEX idx_sys_dict_type_dict_type ON sys_dict_type (dict_type);

-- 15. sys_dict_data 字典数据表
CREATE NONCLUSTERED INDEX idx_sys_dict_data_tenant_id ON sys_dict_data (tenant_id);
CREATE NONCLUSTERED INDEX idx_sys_dict_data_dict_type ON sys_dict_data (dict_type);

-- 16. sys_config 参数配置表
CREATE NONCLUSTERED INDEX idx_sys_config_tenant_id ON sys_config (tenant_id);
CREATE NONCLUSTERED INDEX idx_sys_config_config_key ON sys_config (config_key);

-- 17. sys_login_log 登录日志
CREATE NONCLUSTERED INDEX idx_sys_login_log_tenant_id ON sys_login_log (tenant_id);
CREATE NONCLUSTERED INDEX idx_sys_login_log_user_id ON sys_login_log (user_id);

-- 18. sys_notice 通知公告表
CREATE NONCLUSTERED INDEX idx_sys_notice_tenant_id ON sys_notice (tenant_id);

-- 21. sys_oss OSS对象存储表
CREATE NONCLUSTERED INDEX idx_sys_oss_tenant_id ON sys_oss (tenant_id);
CREATE NONCLUSTERED INDEX idx_sys_oss_directory_id ON sys_oss (directory_id);

-- 23. sys_oss_directory OSS目录
CREATE NONCLUSTERED INDEX idx_sys_oss_directory_tenant_id ON sys_oss_directory (tenant_id);
CREATE NONCLUSTERED INDEX idx_sys_oss_directory_parent_id ON sys_oss_directory (parent_id);

-- ============================================================================
-- 错误日志表 (sys_error_log)
-- 用途: 记录系统运行时的错误、异常、SQL错误等，便于快速排查和统计分析
-- ============================================================================
create table sys_error_log
(
    -- ========== 主键与租户 ==========
    id              bigint            NOT NULL,
    tenant_id       nvarchar(20)      DEFAULT ('000000') NULL,

    -- ========== 错误基本信息 ==========
    error_level     nvarchar(20)      NOT NULL,
    error_type      nvarchar(255)     NOT NULL,
    error_code      nvarchar(50)      NULL,
    error_message   nvarchar(max)     NOT NULL,
    error_stack     nvarchar(max)     NULL,

    -- ========== 请求信息 ==========
    trace_id        nvarchar(50)      NULL,
    request_uri     nvarchar(500)     NULL,
    request_pattern nvarchar(500)     NULL,
    request_method  nvarchar(10)      NULL,
    request_params  nvarchar(max)     NULL,
    request_ip      nvarchar(50)      NULL,
    user_agent      nvarchar(500)     NULL,

    -- ========== 用户信息 ==========
    user_id         bigint            NULL,
    user_name       nvarchar(100)     NULL,
    dept_id         bigint            NULL,

    -- ========== SQL错误专项信息 ==========
    sql_statement   nvarchar(max)     NULL,
    sql_params      nvarchar(max)     NULL,
    sql_duration    int               NULL,

    -- ========== 业务上下文 ==========
    module_name     nvarchar(100)     NULL,
    business_type   nvarchar(50)      NULL,
    business_key    nvarchar(200)     NULL,

    -- ========== 客户端信息（前端错误上报） ==========
    client_type     nvarchar(20)      NULL,
    client_version  nvarchar(50)      NULL,
    os_type         nvarchar(20)      NULL,

    -- ========== 服务器环境信息 ==========
    server_name     nvarchar(100)     NULL,
    server_ip       nvarchar(50)      NULL,
    app_version     nvarchar(20)      NULL,
    thread_name     nvarchar(100)     NULL,

    -- ========== 处理状态 ==========
    handle_status   nchar(1)          DEFAULT ('0') NULL,
    handle_by       bigint            NULL,
    handle_time     datetime2(7)      NULL,
    handle_remark   nvarchar(500)     NULL,

    -- ========== 统计信息（去重机制） ==========
    occurrence_count int              DEFAULT (1) NULL,
    first_time      datetime2(7)      NULL,
    last_time       datetime2(7)      NULL,

    -- ========== 审计字段（项目规范） ==========
    create_dept     bigint            NULL,
    create_by       bigint            NULL,
    create_time     datetime2(7)      DEFAULT GETDATE() NULL,
    update_by       bigint            NULL,
    update_time     datetime2(7)      DEFAULT GETDATE() NULL,
    remark          nvarchar(500)     NULL,

    CONSTRAINT PK__sys_error_log PRIMARY KEY CLUSTERED (id)
    WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
    ON [PRIMARY]
)
ON [PRIMARY]
GO

-- 添加表注释
EXEC sys.sp_addextendedproperty
    'MS_Description', N'错误日志表' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_error_log'
GO

-- 主键与租户
EXEC sys.sp_addextendedproperty
    'MS_Description', N'主键ID' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_error_log',
    'COLUMN', N'id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'租户ID' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_error_log',
    'COLUMN', N'tenant_id'
GO

-- 错误基本信息
EXEC sys.sp_addextendedproperty
    'MS_Description', N'严重级别(ERROR/WARN/FATAL)' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_error_log',
    'COLUMN', N'error_level'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'异常类名(ServiceException/SQLException等)' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_error_log',
    'COLUMN', N'error_type'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'业务错误码' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_error_log',
    'COLUMN', N'error_code'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'错误消息' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_error_log',
    'COLUMN', N'error_message'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'异常堆栈(限制5000字符)' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_error_log',
    'COLUMN', N'error_stack'
GO

-- 请求信息
EXEC sys.sp_addextendedproperty
    'MS_Description', N'链路追踪ID(MDC中的traceId)' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_error_log',
    'COLUMN', N'trace_id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'请求URI' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_error_log',
    'COLUMN', N'request_uri'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'请求路径模板' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_error_log',
    'COLUMN', N'request_pattern'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'请求方法(GET/POST)' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_error_log',
    'COLUMN', N'request_method'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'请求参数(JSON格式，已脱敏)' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_error_log',
    'COLUMN', N'request_params'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'请求IP' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_error_log',
    'COLUMN', N'request_ip'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'User-Agent' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_error_log',
    'COLUMN', N'user_agent'
GO

-- 用户信息
EXEC sys.sp_addextendedproperty
    'MS_Description', N'操作用户ID' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_error_log',
    'COLUMN', N'user_id'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'操作用户名' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_error_log',
    'COLUMN', N'user_name'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'所属部门ID' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_error_log',
    'COLUMN', N'dept_id'
GO

-- SQL错误专项信息
EXEC sys.sp_addextendedproperty
    'MS_Description', N'执行的SQL语句' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_error_log',
    'COLUMN', N'sql_statement'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'SQL参数(JSON格式)' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_error_log',
    'COLUMN', N'sql_params'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'SQL执行耗时(ms)' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_error_log',
    'COLUMN', N'sql_duration'
GO

-- 业务上下文
EXEC sys.sp_addextendedproperty
    'MS_Description', N'业务模块(base/mall/iot/crm)' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_error_log',
    'COLUMN', N'module_name'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'业务类型(查询/新增/修改/删除)' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_error_log',
    'COLUMN', N'business_type'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'业务关键字(订单号/商品ID/设备ID等)' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_error_log',
    'COLUMN', N'business_key'
GO

-- 客户端信息
EXEC sys.sp_addextendedproperty
    'MS_Description', N'平台类型(PC/H5/MINIAPP/APP)' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_error_log',
    'COLUMN', N'client_type'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'客户端版本' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_error_log',
    'COLUMN', N'client_version'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'操作系统(Windows/Mac/Android/iOS)' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_error_log',
    'COLUMN', N'os_type'
GO

-- 服务器环境信息
EXEC sys.sp_addextendedproperty
    'MS_Description', N'服务器名称' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_error_log',
    'COLUMN', N'server_name'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'服务器IP' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_error_log',
    'COLUMN', N'server_ip'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'应用版本' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_error_log',
    'COLUMN', N'app_version'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'线程名称' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_error_log',
    'COLUMN', N'thread_name'
GO

-- 处理状态
EXEC sys.sp_addextendedproperty
    'MS_Description', N'处理状态(0未处理 1已处理)' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_error_log',
    'COLUMN', N'handle_status'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'处理人' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_error_log',
    'COLUMN', N'handle_by'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'处理时间' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_error_log',
    'COLUMN', N'handle_time'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'处理备注' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_error_log',
    'COLUMN', N'handle_remark'
GO

-- 统计信息（去重机制）
EXEC sys.sp_addextendedproperty
    'MS_Description', N'发生次数' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_error_log',
    'COLUMN', N'occurrence_count'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'首次发生时间' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_error_log',
    'COLUMN', N'first_time'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'最后发生时间' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_error_log',
    'COLUMN', N'last_time'
GO

-- 审计字段（项目规范）
EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建部门' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_error_log',
    'COLUMN', N'create_dept'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建者' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_error_log',
    'COLUMN', N'create_by'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'创建时间' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_error_log',
    'COLUMN', N'create_time'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'更新者' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_error_log',
    'COLUMN', N'update_by'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'更新时间' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_error_log',
    'COLUMN', N'update_time'
GO
EXEC sys.sp_addextendedproperty
    'MS_Description', N'备注' ,
    'SCHEMA', N'dbo',
    'TABLE', N'sys_error_log',
    'COLUMN', N'remark'
GO

-- ============================================================================
-- 错误日志表索引
-- ============================================================================
CREATE NONCLUSTERED INDEX idx_sys_error_log_tenant_id ON sys_error_log (tenant_id);
GO
CREATE NONCLUSTERED INDEX idx_sys_error_log_error_level ON sys_error_log (error_level);
GO
CREATE NONCLUSTERED INDEX idx_sys_error_log_error_type ON sys_error_log (error_type);
GO
CREATE NONCLUSTERED INDEX idx_sys_error_log_trace_id ON sys_error_log (trace_id);
GO
CREATE NONCLUSTERED INDEX idx_sys_error_log_user_id ON sys_error_log (user_id);
GO
CREATE NONCLUSTERED INDEX idx_sys_error_log_module_name ON sys_error_log (module_name);
GO
CREATE NONCLUSTERED INDEX idx_sys_error_log_create_time ON sys_error_log (create_time);
GO
CREATE NONCLUSTERED INDEX idx_sys_error_log_handle_status ON sys_error_log (handle_status);
GO
