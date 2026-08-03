-- 主数据库

-- 第三方平台授权表
create table sys_social
(
    id                 bigint       not null comment '主键',
    user_id            bigint       not null comment '用户ID',
    tenant_id          varchar(20)   default '000000' comment '租户id',
    auth_id            varchar(255) not null comment '平台+平台唯一id',
    source             varchar(255) not null comment '用户来源',
    open_id            varchar(255)  default null comment '平台编号唯一id',
    user_name          varchar(30)  not null comment '登录账号',
    nick_name          varchar(30)   default '' comment '用户昵称',
    email              varchar(255)  default '' comment '用户邮箱',
    avatar             varchar(500)  default '' comment '头像地址',
    access_token       varchar(255) not null comment '用户的授权令牌',
    expire_in          int           default null comment '用户的授权令牌的有效期，部分平台可能没有',
    refresh_token      varchar(255)  default null comment '刷新令牌，部分平台可能没有',
    access_code        varchar(255)  default null comment '平台的授权信息，部分平台可能没有',
    union_id           varchar(255)  default null comment '用户的 unionid',
    scope              varchar(255)  default null comment '授予的权限，部分平台可能没有',
    token_type         varchar(255)  default null comment '个别平台的授权信息，部分平台可能没有',
    id_token           varchar(2000) default null comment 'id token，部分平台可能没有',
    mac_algorithm      varchar(255)  default null comment '小米平台用户的附带属性，部分平台可能没有',
    mac_key            varchar(255)  default null comment '小米平台用户的附带属性，部分平台可能没有',
    code               varchar(255)  default null comment '用户的授权code，部分平台可能没有',
    oauth_token        varchar(255)  default null comment 'Twitter平台用户的附带属性，部分平台可能没有',
    oauth_token_secret varchar(255)  default null comment 'Twitter平台用户的附带属性，部分平台可能没有',
    create_dept        bigint(20) comment '创建部门',
    create_by          bigint(20) comment '创建者',
    create_time        datetime comment '创建时间',
    update_by          bigint(20) comment '更新者',
    update_time        datetime comment '更新时间',
    is_deleted         char(1)       default '0' comment '是否删除',
    PRIMARY KEY (id)
) engine = innodb comment = '社会化关系表';


-- 租户表
create table sys_tenant
(
    id                bigint(20)  not null comment 'id',
    tenant_id         varchar(20) not null comment '租户id',
    contact_user_name varchar(20) comment '联系人',
    contact_phone     varchar(20) comment '联系电话',
    company_name      varchar(30) comment '企业名称',
    license_number    varchar(30) comment '统一社会信用代码',
    address           varchar(200) comment '地址',
    intro             varchar(200) comment '企业简介',
    domain            varchar(200) comment '域名',
    remark            varchar(200) comment '备注',
    package_id        bigint(20) comment '租户套餐编号',
    expire_time       datetime comment '过期时间',
    account_count     int     default -1 comment '用户数量（-1不限制）',
    status            char(1) default '1' comment '租户状态',
    is_deleted        char(1) default '0' comment '是否删除',
    create_dept       bigint(20) comment '创建部门',
    create_by         bigint(20) comment '创建者',
    create_time       datetime comment '创建时间',
    update_by         bigint(20) comment '更新者',
    update_time       datetime comment '更新时间',
    primary key (id)
) engine = innodb comment = '租户表';


-- 初始化-租户表数据

insert into sys_tenant
values (1, '000000', '管理组', '15888888888', '若依工作室', null, null, '多租户通用后台管理管理系统', null, null, null,
        null, -1, '1', '0', 100, 1, sysdate(), null, null);


-- 租户套餐表
create table sys_tenant_package
(
    package_id          bigint(20) not null comment '租户套餐id',
    package_name        varchar(20) comment '套餐名称',
    menu_ids            varchar(3000) comment '关联菜单id',
    remark              varchar(200) comment '备注',
    menu_check_strictly tinyint(1) default 1 comment '菜单树选择项是否关联显示',
    status              char(1)    default '1' comment '状态',
    is_deleted          char(1)    default '0' comment '是否删除',
    create_dept         bigint(20) comment '创建部门',
    create_by           bigint(20) comment '创建者',
    create_time         datetime comment '创建时间',
    update_by           bigint(20) comment '更新者',
    update_time         datetime comment '更新时间',
    primary key (package_id)
) engine = innodb comment = '租户套餐表';


-- 1、部门表
create table sys_dept
(
    dept_id       bigint(20) not null comment '部门id',
    tenant_id     varchar(20)  default '000000' comment '租户id',
    parent_id     bigint(20)   default 0 comment '父部门id',
    ancestors     varchar(500) default '' comment '祖级列表',
    dept_name     varchar(30)  default '' comment '部门名称',
    dept_category varchar(100) default null comment '部门类别编码',
    order_num     int(4)       default 0 comment '显示顺序',
    leader        bigint(20)   default null comment '负责人',
    phone         varchar(11)  default null comment '联系电话',
    email         varchar(50)  default null comment '邮箱',
    area_code     char(6)      default null comment '区划代码',
    status        char(1)      default '1' comment '部门状态',
    is_deleted    char(1)      default '0' comment '是否删除',
    create_dept   bigint(20)   default null comment '创建部门',
    create_by     bigint(20)   default null comment '创建者',
    create_time   datetime comment '创建时间',
    update_by     bigint(20)   default null comment '更新者',
    update_time   datetime comment '更新时间',
    primary key (dept_id)
) engine = innodb comment = '部门表';

-- 初始化-部门表数据

insert into sys_dept
values (100, '000000', 0, '0', '若依工作室', null, 0, null, '15888888888', 'xxx@qq.com', null, '1', '0', 100, 1,
        sysdate(),
        null, null);
insert into sys_dept
values (101, '000000', 100, '0,100', '深圳总公司', null, 1, null, '15888888888', 'xxx@qq.com', null, '1', '0', 100, 1,
        sysdate(), null, null);
insert into sys_dept
values (102, '000000', 100, '0,100', '长沙分公司', null, 2, null, '15888888888', 'xxx@qq.com', null, '1', '0', 100, 1,
        sysdate(), null, null);
insert into sys_dept
values (103, '000000', 101, '0,100,101', '研发部门', null, 1, 1, '15888888888', 'xxx@qq.com', null, '1', '0', 100, 1,
        sysdate(), null, null);
insert into sys_dept
values (104, '000000', 101, '0,100,101', '市场部门', null, 2, null, '15888888888', 'xxx@qq.com', null, '1', '0', 100, 1,
        sysdate(), null, null);
insert into sys_dept
values (105, '000000', 101, '0,100,101', '测试部门', null, 3, null, '15888888888', 'xxx@qq.com', null, '1', '0', 100, 1,
        sysdate(), null, null);
insert into sys_dept
values (106, '000000', 101, '0,100,101', '财务部门', null, 4, null, '15888888888', 'xxx@qq.com', null, '1', '0', 100, 1,
        sysdate(), null, null);
insert into sys_dept
values (107, '000000', 101, '0,100,101', '运维部门', null, 5, null, '15888888888', 'xxx@qq.com', null, '1', '0', 100, 1,
        sysdate(), null, null);
insert into sys_dept
values (108, '000000', 102, '0,100,102', '市场部门', null, 1, null, '15888888888', 'xxx@qq.com', null, '1', '0', 100, 1,
        sysdate(), null, null);
insert into sys_dept
values (109, '000000', 102, '0,100,102', '财务部门', null, 2, null, '15888888888', 'xxx@qq.com', null, '1', '0', 100, 1,
        sysdate(), null, null);


-- 2、用户信息表
create table sys_user
(
    user_id     bigint(20) not null comment '用户ID',
    tenant_id   varchar(20)  default '000000' comment '租户id',
    dept_id     bigint(20)   default null comment '部门ID',
    user_name   varchar(30)  default null comment '用户账号',
    nick_name   varchar(30)  default null comment '用户昵称',
    user_type   varchar(20)  default 'pc_user' comment '用户类型',
    email       varchar(50)  default '' comment '用户邮箱',
    phone       varchar(11)  default '' comment '手机号码',
    gender      char(1)      default null comment '用户性别',
    avatar      varchar(255) default '' comment '头像地址',
    password    varchar(100) default '' comment '密码',
    status      char(1)      default '1' comment '帐号状态',
    is_deleted  char(1)      default '0' comment '是否删除',
    login_ip    varchar(128) default '' comment '最后登录IP',
    login_date  datetime comment '最后登录时间',
    create_dept bigint(20)   default null comment '创建部门',
    create_by   bigint(20)   default null comment '创建者',
    create_time datetime comment '创建时间',
    update_by   bigint(20)   default null comment '更新者',
    update_time datetime comment '更新时间',
    remark      varchar(500) default null comment '备注',
    primary key (user_id)
) engine = innodb comment = '用户信息表';

-- 初始化-用户信息表数据
insert into sys_user
values (1, '000000', 100, 'superadmin', '抓蛙师', 'pc_user', '770492966@qq.com', '15888888888', '1', null,
        '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '1', '0', '127.0.0.1', sysdate(), 100, 1,
        sysdate(), null, null, '管理员');
insert into sys_user
values (3, '000000', 108, 'test', '本部门及以下 密码666666', 'pc_user', '', '', '0', null,
        '$2a$10$b8yUzN0C71sbz.PhNOCgJe.Tu1yWC3RNrTyjSQ8p1W0.aaUXUJ.Ne', '1', '0', '127.0.0.1', sysdate(), 100, 1,
        sysdate(), 3, sysdate(), null);
insert into sys_user
values (4, '000000', 102, 'test1', '仅本人 密码666666', 'pc_user', '', '', '0', null,
        '$2a$10$b8yUzN0C71sbz.PhNOCgJe.Tu1yWC3RNrTyjSQ8p1W0.aaUXUJ.Ne', '1', '0', '127.0.0.1', sysdate(), 100, 1,
        sysdate(), 4, sysdate(), null);

-- 3、岗位信息表
create table sys_post
(
    post_id       bigint(20)  not null comment '岗位ID',
    tenant_id     varchar(20)  default '000000' comment '租户id',
    dept_id       bigint(20)  not null comment '部门id',
    post_code     varchar(64) not null comment '岗位编码',
    post_category varchar(100) default null comment '岗位类别编码',
    post_name     varchar(50) not null comment '岗位名称',
    post_sort     int(4)      not null comment '显示顺序',
    status        char(1)      default '1' comment '状态',
    create_dept   bigint(20)   default null comment '创建部门',
    create_by     bigint(20)   default null comment '创建者',
    create_time   datetime comment '创建时间',
    update_by     bigint(20)   default null comment '更新者',
    update_time   datetime comment '更新时间',
    remark        varchar(500) default null comment '备注',
    primary key (post_id)
) engine = innodb comment = '岗位信息表';

-- 初始化-岗位信息表数据
insert into sys_post
values (1, '000000', 100, 'ceo', null, '董事长', 1, '1', 100, 1, sysdate(), null, null, '');
insert into sys_post
values (2, '000000', 100, 'se', null, '项目经理', 2, '1', 100, 1, sysdate(), null, null, '');
insert into sys_post
values (3, '000000', 100, 'hr', null, '人力资源', 3, '1', 100, 1, sysdate(), null, null, '');
insert into sys_post
values (4, '000000', 100, 'user', null, '普通员工', 4, '1', 100, 1, sysdate(), null, null, '');


-- 4、角色信息表
create table sys_role
(
    role_id             bigint(20)   not null comment '角色ID',
    tenant_id           varchar(20)  default '000000' comment '租户id',
    role_name           varchar(30)  not null comment '角色名称',
    role_key            varchar(100) not null comment '角色权限字符串',
    role_sort           int(4)       not null comment '显示顺序',
    data_scope          char(1)      default '1' comment '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限 5：仅本人数据权限 6：部门及以下或本人数据权限）',
    menu_check_strictly tinyint(1)   default 1 comment '菜单树选择项是否关联显示',
    dept_check_strictly tinyint(1)   default 1 comment '部门树选择项是否关联显示',
    status              char(1)      default '1' comment '角色状态',
    is_deleted          char(1)      default '0' comment '是否删除',
    create_dept         bigint(20)   default null comment '创建部门',
    create_by           bigint(20)   default null comment '创建者',
    create_time         datetime comment '创建时间',
    update_by           bigint(20)   default null comment '更新者',
    update_time         datetime comment '更新时间',
    remark              varchar(500) default null comment '备注',
    primary key (role_id)
) engine = innodb comment = '角色信息表';

-- 初始化-角色信息表数据
insert into sys_role
values (1, '000000', '超级管理员', 'superadmin', 1, 1, 1, 1, '1', '0', 100, 1, sysdate(), null, null, '超级管理员');
insert into sys_role
values (2, '000000', 'PC端普通用户', 'pc_user', 2, 1, 1, 1, '1', '0', 100, 1, sysdate(), null, null, '');
insert into sys_role
values (3, '000000', '移动端普通用户', 'app_user', 3, 1, 1, 1, '1', '0', 100, 1, sysdate(), null, null, '');

-- 5、菜单权限表
create table sys_menu
(
    menu_id          bigint(20)  not null comment '菜单ID',
    menu_name        varchar(50) not null comment '菜单名称',
    parent_id        bigint(20)   default 0 comment '父菜单ID',
    order_num        int(4)       default 0 comment '显示顺序',
    path             varchar(200) default '' comment '路由地址',
    component        varchar(255) default null comment '组件路径',
    query_param      varchar(255) default null comment '路由参数',
    is_external_link char(1)      default '0' comment '是否为外链',
    is_cache         char(1)      default '1' comment '是否缓存',
    menu_type        char(1)      default '' comment '菜单类型',
    visible          char(1)      default '1' comment '显示设置',
    status           char(1)      default '1' comment '启用状态',
    perms            varchar(100) default null comment '权限标识',
    icon             varchar(100) default '#' comment '菜单图标',
    create_dept      bigint(20)   default null comment '创建部门',
    create_by        bigint(20)   default null comment '创建者',
    create_time      datetime comment '创建时间',
    update_by        bigint(20)   default null comment '更新者',
    update_time      datetime comment '更新时间',
    remark           varchar(500) default '' comment '备注',
    primary key (menu_id)
) engine = innodb comment = '菜单权限表';

-- 初始化-菜单信息表数据
-- 一级菜单
insert into sys_menu
values ('1', '系统管理', '0', '97', 'system', null, '', '0', '1', 'M', '1', '1', '', 'setting', 100, 1, sysdate(), null,
        null, '系统管理目录');
insert into sys_menu
values ('6', '租户管理', '0', '98', 'tenant', null, '', '0', '1', 'M', '1', '1', '', 'company', 100, 1, sysdate(), null,
        null, '租户管理目录');
insert into sys_menu
values ('2', '系统监控', '0', '99', 'monitor', null, '', '0', '1', 'M', '1', '1', '', 'monitor', 100, 1, sysdate(),
        null, null, '系统监控目录');
insert into sys_menu
values ('3', '系统工具', '0', '100', 'tool', null, '', '0', '1', 'M', '1', '1', '', 'tool', 100, 1, sysdate(), null,
        null, '系统工具目录');
-- 二级菜单
insert into sys_menu
values ('100', '用户管理', '1', '1', 'user', 'system/core/user/user', '', '0', '1', 'C', '1', '1', 'system:user:view',
        'user', 100, 1, sysdate(), null, null, '用户管理菜单');
insert into sys_menu
values ('101', '角色管理', '1', '2', 'role', 'system/core/role/role', '', '0', '1', 'C', '1', '1', 'system:role:view',
        'role', 100, 1, sysdate(), null, null, '角色管理菜单');
insert into sys_menu
values ('102', '菜单管理', '1', '3', 'menu', 'system/core/menu/menu', '', '0', '1', 'C', '1', '1', 'system:menu:view',
        'menu', 100, 1, sysdate(), null, null, '菜单管理菜单');
insert into sys_menu
values ('103', '部门管理', '1', '4', 'dept', 'system/core/dept/dept', '', '0', '1', 'C', '1', '1', 'system:dept:view',
        'department', 100, 1, sysdate(), null, null, '部门管理菜单');
insert into sys_menu
values ('104', '岗位管理', '1', '5', 'post', 'system/core/post/post', '', '0', '1', 'C', '1', '1', 'system:post:view',
        'post', 100, 1, sysdate(), null, null, '岗位管理菜单');
insert into sys_menu
values ('105', '字典管理', '1', '6', 'dict', 'system/dict/dictType', '', '0', '1', 'C', '1', '1', 'system:dict:view',
        'dict', 100, 1, sysdate(), null, null, '字典管理菜单');
insert into sys_menu
values ('106', '参数设置', '1', '7', 'config', 'system/config/config', '', '0', '1', 'C', '1', '1',
        'system:config:view', 'edit', 100, 1, sysdate(), null, null, '参数设置菜单');
insert into sys_menu
values ('107', '通知公告', '1', '9', 'notice', 'system/config/notice', '', '0', '1', 'C', '1', '1',
        'system:notice:view', 'notification', 100, 1, sysdate(), null, null, '通知公告菜单');
insert into sys_menu
values ('108', '日志管理', '1', '10', 'log', '', '', '0', '1', 'M', '1', '1', '', 'log', 100, 1, sysdate(), null, null,
        '日志管理菜单');
insert into sys_menu
values ('109', '在线用户', '2', '1', 'online', 'system/monitor/online/online', '', '0', '1', 'C', '1', '1',
        'monitor:online:view', 'online', 100, 1, sysdate(), null, null, '在线用户菜单');
insert into sys_menu
values ('113', '缓存监控', '2', '2', 'cache', 'system/monitor/cache/cache', '', '0', '1', 'C', '1', '1',
        'monitor:cache:view', 'redis', 100, 1, sysdate(), null, null, '缓存监控菜单');
insert into sys_menu
values ('115', '代码生成', '3', '2', 'gen', 'tool/gen/gen', '', '0', '1', 'C', '1', '1', 'tool:gen:view', 'code', 100,
        1, sysdate(), null, null, '代码生成菜单');
insert into sys_menu
values ('116', '页面设计', '3', '3', 'pageDesigner', 'tool/pageDesigner/pageDesigner', '', '0', '1', 'C', '1', '1',
        'tool:page:view', 'dashboard', 100, 1, sysdate(), null, null, '页面设计菜单');
insert into sys_menu
values ('121', '租户管理', '6', '1', 'tenant', 'system/tenant/tenant', '', '0', '1', 'C', '1', '1',
        'system:tenant:view', 'users', 100, 1, sysdate(), null, null, '租户管理菜单');
insert into sys_menu
values ('122', '租户套餐', '6', '2', 'tenantPackage', 'system/tenant/tenantPackage', '', '0', '1', 'C', '1', '1',
        'system:tenantPackage:view', 'combination', 100, 1, sysdate(), null, null, '租户套餐菜单');

-- springboot-admin监控
insert into sys_menu
values ('117', 'admin监控', '2', '5', 'http://127.0.0.1:9090/admin', 'system/monitor/admin/admin', '', '1', '1', 'C', '1', '1',
        'monitor:admin:view', 'dashboard', 100, 1, sysdate(), null, null, 'Admin监控菜单');
-- oss菜单
insert into sys_menu
values ('118', '文件管理', '1', '11', 'oss', 'system/oss/oss', '', '0', '1', 'C', '1', '1', 'system:oss:view', 'upload',
        100, 1, sysdate(), null, null, '文件管理菜单');
-- snail-job server控制台
insert into sys_menu
values ('120', '任务调度', '2', '6', 'snailjob', 'system/monitor/snailjob/snailjob', '', '0', '1', 'C', '1', '1',
        'monitor:snailjob:view', 'job', 100, 1, sysdate(), null, null, 'SnailJob控制台菜单');

-- 三级菜单
insert into sys_menu
values ('500', '操作日志', '108', '1', 'operLog', 'system/monitor/operLog/operLog', '', '0', '1', 'C', '1', '1',
        'monitor:operLog:view', 'tool', 100, 1, sysdate(), null, null, '操作日志菜单');
insert into sys_menu
values ('501', '登录日志', '108', '2', 'loginLog', 'system/monitor/loginLog/loginLog', '', '0', '1', 'C', '1', '1',
        'monitor:loginLog:view', 'login-info', 100, 1, sysdate(), null, null, '登录日志菜单');
insert into sys_menu
values ('1644', '错误日志', '108', '3', 'errorLog', 'system/monitor/errorLog/errorLog', '', '0', '1', 'C', '1', '1',
        'monitor:errorLog:view', 'bug', 100, 1, sysdate(), null, null, '错误日志菜单');
-- 用户管理按钮
insert into sys_menu
values ('1001', '用户查询', '100', '1', '', '', '', '0', '1', 'F', '1', '1', 'system:user:query', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1002', '用户新增', '100', '2', '', '', '', '0', '1', 'F', '1', '1', 'system:user:add', '#', 100, 1, sysdate(),
        null, null, '');
insert into sys_menu
values ('1003', '用户修改', '100', '3', '', '', '', '0', '1', 'F', '1', '1', 'system:user:update', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1004', '用户删除', '100', '4', '', '', '', '0', '1', 'F', '1', '1', 'system:user:delete', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1005', '用户导出', '100', '5', '', '', '', '0', '1', 'F', '1', '1', 'system:user:export', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1006', '用户导入', '100', '6', '', '', '', '0', '1', 'F', '1', '1', 'system:user:import', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1007', '重置密码', '100', '7', '', '', '', '0', '1', 'F', '1', '1', 'system:user:resetPwd', '#', 100, 1,
        sysdate(), null, null, '');
-- 角色管理按钮
insert into sys_menu
values ('1008', '角色查询', '101', '1', '', '', '', '0', '1', 'F', '1', '1', 'system:role:query', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1009', '角色新增', '101', '2', '', '', '', '0', '1', 'F', '1', '1', 'system:role:add', '#', 100, 1, sysdate(),
        null, null, '');
insert into sys_menu
values ('1010', '角色修改', '101', '3', '', '', '', '0', '1', 'F', '1', '1', 'system:role:update', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1011', '角色删除', '101', '4', '', '', '', '0', '1', 'F', '1', '1', 'system:role:delete', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1012', '角色导出', '101', '5', '', '', '', '0', '1', 'F', '1', '1', 'system:role:export', '#', 100, 1,
        sysdate(), null, null, '');
-- 菜单管理按钮
insert into sys_menu
values ('1013', '菜单查询', '102', '1', '', '', '', '0', '1', 'F', '1', '1', 'system:menu:query', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1014', '菜单新增', '102', '2', '', '', '', '0', '1', 'F', '1', '1', 'system:menu:add', '#', 100, 1, sysdate(),
        null, null, '');
insert into sys_menu
values ('1015', '菜单修改', '102', '3', '', '', '', '0', '1', 'F', '1', '1', 'system:menu:update', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1016', '菜单删除', '102', '4', '', '', '', '0', '1', 'F', '1', '1', 'system:menu:delete', '#', 100, 1,
        sysdate(), null, null, '');
-- 部门管理按钮
insert into sys_menu
values ('1017', '部门查询', '103', '1', '', '', '', '0', '1', 'F', '1', '1', 'system:dept:query', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1018', '部门新增', '103', '2', '', '', '', '0', '1', 'F', '1', '1', 'system:dept:add', '#', 100, 1, sysdate(),
        null, null, '');
insert into sys_menu
values ('1019', '部门修改', '103', '3', '', '', '', '0', '1', 'F', '1', '1', 'system:dept:update', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1020', '部门删除', '103', '4', '', '', '', '0', '1', 'F', '1', '1', 'system:dept:delete', '#', 100, 1,
        sysdate(), null, null, '');
-- 岗位管理按钮
insert into sys_menu
values ('1021', '岗位查询', '104', '1', '', '', '', '0', '1', 'F', '1', '1', 'system:post:query', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1022', '岗位新增', '104', '2', '', '', '', '0', '1', 'F', '1', '1', 'system:post:add', '#', 100, 1, sysdate(),
        null, null, '');
insert into sys_menu
values ('1023', '岗位修改', '104', '3', '', '', '', '0', '1', 'F', '1', '1', 'system:post:update', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1024', '岗位删除', '104', '4', '', '', '', '0', '1', 'F', '1', '1', 'system:post:delete', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1025', '岗位导出', '104', '5', '', '', '', '0', '1', 'F', '1', '1', 'system:post:export', '#', 100, 1,
        sysdate(), null, null, '');
-- 字典管理按钮
insert into sys_menu
values ('1026', '字典查询', '105', '1', '#', '', '', '0', '1', 'F', '1', '1', 'system:dict:query', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1027', '字典新增', '105', '2', '#', '', '', '0', '1', 'F', '1', '1', 'system:dict:add', '#', 100, 1, sysdate(),
        null, null, '');
insert into sys_menu
values ('1028', '字典修改', '105', '3', '#', '', '', '0', '1', 'F', '1', '1', 'system:dict:update', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1029', '字典删除', '105', '4', '#', '', '', '0', '1', 'F', '1', '1', 'system:dict:delete', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1030', '字典导出', '105', '5', '#', '', '', '0', '1', 'F', '1', '1', 'system:dict:export', '#', 100, 1,
        sysdate(), null, null, '');
-- 参数设置按钮
insert into sys_menu
values ('1031', '参数查询', '106', '1', '#', '', '', '0', '1', 'F', '1', '1', 'system:config:query', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1032', '参数新增', '106', '2', '#', '', '', '0', '1', 'F', '1', '1', 'system:config:add', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1033', '参数修改', '106', '3', '#', '', '', '0', '1', 'F', '1', '1', 'system:config:update', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1034', '参数删除', '106', '4', '#', '', '', '0', '1', 'F', '1', '1', 'system:config:delete', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1035', '参数导出', '106', '5', '#', '', '', '0', '1', 'F', '1', '1', 'system:config:export', '#', 100, 1,
        sysdate(), null, null, '');
-- 通知公告按钮
insert into sys_menu
values ('1036', '公告查询', '107', '1', '#', '', '', '0', '1', 'F', '1', '1', 'system:notice:query', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1037', '公告新增', '107', '2', '#', '', '', '0', '1', 'F', '1', '1', 'system:notice:add', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1038', '公告修改', '107', '3', '#', '', '', '0', '1', 'F', '1', '1', 'system:notice:update', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1039', '公告删除', '107', '4', '#', '', '', '0', '1', 'F', '1', '1', 'system:notice:delete', '#', 100, 1,
        sysdate(), null, null, '');
-- 操作日志按钮
insert into sys_menu
values ('1040', '操作查询', '500', '1', '#', '', '', '0', '1', 'F', '1', '1', 'monitor:operLog:query', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1041', '操作删除', '500', '2', '#', '', '', '0', '1', 'F', '1', '1', 'monitor:operLog:delete', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1042', '日志导出', '500', '4', '#', '', '', '0', '1', 'F', '1', '1', 'monitor:operLog:export', '#', 100, 1,
        sysdate(), null, null, '');
-- 登录日志按钮
insert into sys_menu
values ('1043', '登录查询', '501', '1', '#', '', '', '0', '1', 'F', '1', '1', 'monitor:loginLog:query', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1044', '登录删除', '501', '2', '#', '', '', '0', '1', 'F', '1', '1', 'monitor:loginLog:delete', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1045', '日志导出', '501', '3', '#', '', '', '0', '1', 'F', '1', '1', 'monitor:loginLog:export', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1050', '账户解锁', '501', '4', '#', '', '', '0', '1', 'F', '1', '1', 'monitor:loginLog:unlock', '#', 100, 1,
        sysdate(), null, null, '');
-- 错误日志按钮
insert into sys_menu
values ('1645', '错误日志查询', '1644', '1', '#', '', '', '0', '1', 'F', '1', '1', 'monitor:errorLog:query', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1646', '错误日志更新', '1644', '2', '#', '', '', '0', '1', 'F', '1', '1', 'monitor:errorLog:update', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1647', '错误日志删除', '1644', '3', '#', '', '', '0', '1', 'F', '1', '1', 'monitor:errorLog:delete', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1648', '错误日志导出', '1644', '4', '#', '', '', '0', '1', 'F', '1', '1', 'monitor:errorLog:export', '#', 100, 1,
        sysdate(), null, null, '');
-- 在线用户按钮
insert into sys_menu
values ('1046', '在线查询', '109', '1', '#', '', '', '0', '1', 'F', '1', '1', 'monitor:online:query', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1047', '批量强退', '109', '2', '#', '', '', '0', '1', 'F', '1', '1', 'monitor:online:batchLogout', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1048', '单条强退', '109', '3', '#', '', '', '0', '1', 'F', '1', '1', 'monitor:online:forceLogout', '#', 100, 1,
        sysdate(), null, null, '');
-- 代码生成按钮
insert into sys_menu
values ('1055', '生成查询', '115', '1', '#', '', '', '0', '1', 'F', '1', '1', 'tool:gen:query', '#', 100, 1, sysdate(),
        null, null, '');
insert into sys_menu
values ('1056', '生成修改', '115', '2', '#', '', '', '0', '1', 'F', '1', '1', 'tool:gen:update', '#', 100, 1, sysdate(),
        null, null, '');
insert into sys_menu
values ('1057', '生成删除', '115', '3', '#', '', '', '0', '1', 'F', '1', '1', 'tool:gen:delete', '#', 100, 1, sysdate(),
        null, null, '');
insert into sys_menu
values ('1058', '导入代码', '115', '2', '#', '', '', '0', '1', 'F', '1', '1', 'tool:gen:import', '#', 100, 1, sysdate(),
        null, null, '');
insert into sys_menu
values ('1059', '预览代码', '115', '4', '#', '', '', '0', '1', 'F', '1', '1', 'tool:gen:preview', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1060', '生成代码', '115', '5', '#', '', '', '0', '1', 'F', '1', '1', 'tool:gen:code', '#', 100, 1, sysdate(),
        null, null, '');
-- oss相关按钮
insert into sys_menu
values ('1600', '文件查询', '118', '1', '#', '', '', '0', '1', 'F', '1', '1', 'system:oss:query', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1601', '文件上传', '118', '2', '#', '', '', '0', '1', 'F', '1', '1', 'system:oss:upload', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1602', '文件下载', '118', '3', '#', '', '', '0', '1', 'F', '1', '1', 'system:oss:download', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1603', '文件删除', '118', '4', '#', '', '', '0', '1', 'F', '1', '1', 'system:oss:delete', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1620', '配置列表', '118', '5', '#', '', '', '0', '1', 'F', '1', '1', 'system:ossConfig:view', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1621', '配置添加', '118', '6', '#', '', '', '0', '1', 'F', '1', '1', 'system:ossConfig:add', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1622', '配置编辑', '118', '7', '#', '', '', '0', '1', 'F', '1', '1', 'system:ossConfig:update', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1623', '配置删除', '118', '8', '#', '', '', '0', '1', 'F', '1', '1', 'system:ossConfig:delete', '#', 100, 1,
        sysdate(), null, null, '');
-- OSS目录权限按钮
insert into sys_menu
values ('1640', '目录查询', '118', '9', '#', '', '', '0', '1', 'F', '1', '1', 'system:ossDirectory:query', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1641', '目录新增', '118', '10', '#', '', '', '0', '1', 'F', '1', '1', 'system:ossDirectory:add', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1642', '目录修改', '118', '11', '#', '', '', '0', '1', 'F', '1', '1', 'system:ossDirectory:update', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1643', '目录删除', '118', '12', '#', '', '', '0', '1', 'F', '1', '1', 'system:ossDirectory:delete', '#', 100, 1,
        sysdate(), null, null, '');

-- 租户管理相关按钮
insert into sys_menu
values ('1606', '租户查询', '121', '1', '#', '', '', '0', '1', 'F', '1', '1', 'system:tenant:query', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1607', '租户新增', '121', '2', '#', '', '', '0', '1', 'F', '1', '1', 'system:tenant:add', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1608', '租户修改', '121', '3', '#', '', '', '0', '1', 'F', '1', '1', 'system:tenant:update', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1609', '租户删除', '121', '4', '#', '', '', '0', '1', 'F', '1', '1', 'system:tenant:delete', '#', 100, 1,
        sysdate(), null, null, '');
insert into sys_menu
values ('1610', '租户导出', '121', '5', '#', '', '', '0', '1', 'F', '1', '1', 'system:tenant:export', '#', 100, 1,
        sysdate(), null, null, '');
-- 租户套餐相关按钮
insert into sys_menu
values ('1611', '租户套餐查询', '122', '1', '#', '', '', '0', '1', 'F', '1', '1', 'system:tenantPackage:query', '#',
        100, 1, sysdate(), null, null, '');
insert into sys_menu
values ('1612', '租户套餐新增', '122', '2', '#', '', '', '0', '1', 'F', '1', '1', 'system:tenantPackage:add', '#', 100,
        1, sysdate(), null, null, '');
insert into sys_menu
values ('1613', '租户套餐修改', '122', '3', '#', '', '', '0', '1', 'F', '1', '1', 'system:tenantPackage:update', '#',
        100, 1, sysdate(), null, null, '');
insert into sys_menu
values ('1614', '租户套餐删除', '122', '4', '#', '', '', '0', '1', 'F', '1', '1', 'system:tenantPackage:delete', '#',
        100, 1, sysdate(), null, null, '');
insert into sys_menu
values ('1615', '租户套餐导出', '122', '5', '#', '', '', '0', '1', 'F', '1', '1', 'system:tenantPackage:export', '#',
        100, 1, sysdate(), null, null, '');

-- 开放平台菜单
insert into sys_menu
values ('1120', '开放平台', '1', '8', 'openApi', '', '', '0', '0', 'M', '1', '1', '', 'key', 103, 0, sysdate(), null, null, 'API开放平台管理');
insert into sys_menu
values ('1121', 'API密钥', '1120', '1', 'apiKey', 'system/openApi/apiKey/apiKey', '', '0', '1', 'C', '1', '1', 'system:apiKey:view', 'password', 103, 0, sysdate(), null, null, 'API密钥管理');
insert into sys_menu
values ('1122', 'API密钥查询', '1121', '1', '', '', '', '0', '1', 'F', '1', '1', 'system:apiKey:query', '#', 103, 0, sysdate(), null, null, '');
insert into sys_menu
values ('1123', 'API密钥新增', '1121', '2', '', '', '', '0', '1', 'F', '1', '1', 'system:apiKey:add', '#', 103, 0, sysdate(), null, null, '');
insert into sys_menu
values ('1124', 'API密钥修改', '1121', '3', '', '', '', '0', '1', 'F', '1', '1', 'system:apiKey:update', '#', 103, 0, sysdate(), null, null, '');
insert into sys_menu
values ('1125', 'API密钥删除', '1121', '4', '', '', '', '0', '1', 'F', '1', '1', 'system:apiKey:delete', '#', 103, 0, sysdate(), null, null, '');
insert into sys_menu
values ('1126', 'API密钥导出', '1121', '5', '', '', '', '0', '1', 'F', '1', '1', 'system:apiKey:export', '#', 103, 0, sysdate(), null, null, '');

-- 6、用户和角色关联表  用户N-1角色
create table sys_user_role
(
    user_id bigint(20) not null comment '用户ID',
    role_id bigint(20) not null comment '角色ID',
    primary key (user_id, role_id)
) engine = innodb comment = '用户和角色关联表';

-- 初始化-用户和角色关联表数据
insert into sys_user_role
values ('1', '1');
insert into sys_user_role
values ('3', '3');
insert into sys_user_role
values ('4', '3');

-- 7、角色和菜单关联表  角色1-N菜单
create table sys_role_menu
(
    role_id bigint(20) not null comment '角色ID',
    menu_id bigint(20) not null comment '菜单ID',
    primary key (role_id, menu_id)
) engine = innodb comment = '角色和菜单关联表';

-- 初始化-角色和菜单关联表数据
insert into sys_role_menu
values ('3', '1601');

-- 8、角色和部门关联表  角色1-N部门
create table sys_role_dept
(
    role_id bigint(20) not null comment '角色ID',
    dept_id bigint(20) not null comment '部门ID',
    primary key (role_id, dept_id)
) engine = innodb comment = '角色和部门关联表';

-- 9、用户与岗位关联表  用户1-N岗位
create table sys_user_post
(
    user_id bigint(20) not null comment '用户ID',
    post_id bigint(20) not null comment '岗位ID',
    primary key (user_id, post_id)
) engine = innodb comment = '用户与岗位关联表';

-- 初始化-用户与岗位关联表数据
insert into sys_user_post
values ('1', '1');

-- 10、操作日志
create table sys_oper_log
(
    oper_id        bigint(20) not null comment '日志主键',
    tenant_id      varchar(20)  default '000000' comment '租户id',
    title          varchar(50)  default '' comment '模块标题',
    oper_type      varchar(8)   default '' comment '操作类型',
    method         varchar(100) default '' comment '方法名称',
    request_method varchar(10)  default '' comment '请求方式',
    operator_type  varchar(20)  default '' comment '操作用户类别',
    oper_name      varchar(50)  default '' comment '操作人员',
    dept_name      varchar(50)  default '' comment '部门名称',
    oper_url       varchar(255) default '' comment '请求URL',
    oper_ip        varchar(128) default '' comment '主机地址',
    oper_location  varchar(255) default '' comment '操作地点',
    oper_param     text         default null comment '请求参数',
    json_result    text         default null comment '返回参数',
    status         char(1)      default '1' comment '操作结果',
    error_msg      text         default null comment '错误消息',
    oper_time      datetime comment '操作时间',
    cost_time      bigint(20)   default 0 comment '消耗时间',
    primary key (oper_id),
    key idx_sys_oper_log_bt (oper_type),
    key idx_sys_oper_log_s (status),
    key idx_sys_oper_log_ot (oper_time)
) engine = innodb comment = '操作日志';


-- 11、字典类型表
create table sys_dict_type
(
    dict_id     bigint(20) not null comment '字典主键',
    tenant_id   varchar(20)  default '000000' comment '租户id',
    dict_name   varchar(100) default '' comment '字典名称',
    dict_type   varchar(100) default '' comment '字典类型',
    is_system   char(1)      default '0' comment '是否系统级字典(0否 1是)',
    status      char(1)      default '1' comment '状态',
    create_dept bigint(20)   default null comment '创建部门',
    create_by   bigint(20)   default null comment '创建者',
    create_time datetime comment '创建时间',
    update_by   bigint(20)   default null comment '更新者',
    update_time datetime comment '更新时间',
    remark      varchar(500) default null comment '备注',
    primary key (dict_id),
    unique (tenant_id, dict_type)
) engine = innodb comment = '字典类型表';

-- 字典类型
insert into sys_dict_type
values (1, '000000', '用户性别', 'sys_user_gender', '1', '1', 100, 1, sysdate(), null, null, '用户性别列表');
insert into sys_dict_type
values (2, '000000', '数据权限', 'sys_data_scope', '1', '1', 100, 1, sysdate(), null, null, '数据权限类型列表');
insert into sys_dict_type
values (3, '000000', '启用状态', 'sys_enable_status', '1', '1', 100, 1, sysdate(), null, null, '启用状态列表');
insert into sys_dict_type
values (4, '000000', '显示设置', 'sys_display_setting', '1', '1', 100, 1, sysdate(), null, null, '显示设置列表');
insert into sys_dict_type
values (5, '000000', '逻辑标志', 'sys_boolean_flag', '1', '1', 100, 1, sysdate(), null, null, '逻辑标志列表');
insert into sys_dict_type
values (6, '000000', '操作类型', 'sys_oper_type', '1', '1', 100, 1, sysdate(), null, null, '操作类型列表');
insert into sys_dict_type
values (7, '000000', '操作结果', 'sys_oper_result', '1', '1', 100, 1, sysdate(), null, null, '操作结果列表');
insert into sys_dict_type
values (8, '000000', '审核状态', 'sys_audit_status', '1', '1', 100, 1, sysdate(), null, null, '审核状态列表');
insert into sys_dict_type
values (9, '000000', '文件类型', 'sys_file_type', '1', '1', 100, 1, sysdate(), null, null, '文件类型列表');
insert into sys_dict_type
values (10, '000000', '消息类型', 'sys_message_type', '1', '1', 100, 1, sysdate(), null, null, '消息类型列表');
insert into sys_dict_type
values (11, '000000', '通知类型', 'sys_notice_type', '1', '1', 100, 1, sysdate(), null, null, '通知类型列表');
insert into sys_dict_type
values (12, '000000', '通知状态', 'sys_notice_status', '1', '1', 100, 1, sysdate(), null, null, '通知状态列表');
insert into sys_dict_type
values (20, '000000', '平台类型', 'sys_platform_type', '1', '1', 100, 1, sysdate(), null, null, '平台类型列表');
insert into sys_dict_type
values (21, '000000', '支付方式', 'sys_payment_method', '1', '1', 100, 1, sysdate(), null, null, '支付方式列表');
insert into sys_dict_type
values (22, '000000', '订单状态', 'sys_order_status', '1', '1', 100, 1, sysdate(), null, null, '订单状态列表');
insert into sys_dict_type
values (23, '000000', '商品规格类型', 'm_goods_spec_type', '0', '1', 100, 1, sysdate(), null, null, '商品规格类型(单规格/多规格)');


-- 12、字典数据表
create table sys_dict_data
(
    dict_data_id bigint(20) not null comment '字典编码',
    tenant_id    varchar(20)  default '000000' comment '租户id',
    dict_sort    int(4)       default 0 comment '字典排序',
    dict_label   varchar(100) default '' comment '字典标签',
    dict_value   varchar(100) default '' comment '字典键值',
    dict_type    varchar(100) default '' comment '字典类型',
    css_class    varchar(100) default null comment '样式属性（其他样式扩展）',
    list_class   varchar(100) default null comment '表格回显样式',
    is_default   char(1)      default '0' comment '是否默认',
    status       char(1)      default '1' comment '状态',
    create_dept  bigint(20)   default null comment '创建部门',
    create_by    bigint(20)   default null comment '创建者',
    create_time  datetime comment '创建时间',
    update_by    bigint(20)   default null comment '更新者',
    update_time  datetime comment '更新时间',
    remark       varchar(500) default null comment '备注',
    primary key (dict_data_id)
) engine = innodb comment = '字典数据表';

-- 用户性别
insert into sys_dict_data
values (1, '000000', 0, '女', '0', 'sys_user_gender', '', 'success', '1', '1', 100, 1, sysdate(), null, null,
        '性别:女');
insert into sys_dict_data
values (2, '000000', 1, '男', '1', 'sys_user_gender', '', 'primary', '0', '1', 100, 1, sysdate(), null, null,
        '性别:男');
insert into sys_dict_data
values (3, '000000', 2, '未知', '2', 'sys_user_gender', '', 'info', '0', '1', 100, 1, sysdate(), null, null,
        '性别:未知');

-- 数据权限
insert into sys_dict_data
values (4, '000000', 1, '全部数据权限', '1', 'sys_data_scope', '', 'primary', '0', '1', 100, 1, sysdate(), null, null,
        '数据权限:全部数据');
insert into sys_dict_data
values (5, '000000', 2, '自定义数据权限', '2', 'sys_data_scope', '', 'info', '0', '1', 100, 1, sysdate(), null, null,
        '数据权限:自定义数据');
insert into sys_dict_data
values (6, '000000', 3, '部门数据权限', '3', 'sys_data_scope', '', 'success', '0', '1', 100, 1, sysdate(), null, null,
        '数据权限:本部门数据');
insert into sys_dict_data
values (7, '000000', 4, '部门及以下数据权限', '4', 'sys_data_scope', '', 'warning', '0', '1', 100, 1, sysdate(), null,
        null, '数据权限:部门及以下数据');
insert into sys_dict_data
values (8, '000000', 5, '仅本人数据权限', '5', 'sys_data_scope', '', 'danger', '0', '1', 100, 1, sysdate(), null, null,
        '数据权限:仅本人数据');
insert into sys_dict_data
values (9, '000000', 6, '部门及以下或本人数据', '6', 'sys_data_scope', '', 'info', '0', '1', 100, 1, sysdate(), null,
        null, '数据权限:部门及以下或本人数据');

-- 启用状态
insert into sys_dict_data
values (10, '000000', 1, '禁用', '0', 'sys_enable_status', '', 'danger', '0', '1', 100, 1, sysdate(), null, null,
        '启用状态:禁用');
insert into sys_dict_data
values (11, '000000', 2, '启用', '1', 'sys_enable_status', '', 'primary', '1', '1', 100, 1, sysdate(), null, null,
        '启用状态:启用');

-- 显示设置
insert into sys_dict_data
values (12, '000000', 1, '隐藏', '0', 'sys_display_setting', '', 'danger', '0', '1', 100, 1, sysdate(), null, null,
        '显示设置:隐藏');
insert into sys_dict_data
values (13, '000000', 2, '显示', '1', 'sys_display_setting', '', 'primary', '1', '1', 100, 1, sysdate(), null, null,
        '显示设置:显示');

-- 逻辑标志
insert into sys_dict_data
values (14, '000000', 1, '否', '0', 'sys_boolean_flag', '', 'danger', '0', '1', 100, 1, sysdate(), null, null,
        '逻辑标志:否');
insert into sys_dict_data
values (15, '000000', 2, '是', '1', 'sys_boolean_flag', '', 'primary', '1', '1', 100, 1, sysdate(), null, null,
        '逻辑标志:是');

-- 操作类型
insert into sys_dict_data
values (16, '000000', 1, '新增', '1', 'sys_oper_type', '', 'primary', '0', '1', 100, 1, sysdate(), null, null,
        '操作:新增');
insert into sys_dict_data
values (17, '000000', 2, '修改', '2', 'sys_oper_type', '', 'info', '0', '1', 100, 1, sysdate(), null, null,
        '操作:修改');
insert into sys_dict_data
values (18, '000000', 3, '删除', '3', 'sys_oper_type', '', 'danger', '0', '1', 100, 1, sysdate(), null, null,
        '操作:删除');
insert into sys_dict_data
values (19, '000000', 4, '授权', '4', 'sys_oper_type', '', 'success', '0', '1', 100, 1, sysdate(), null, null,
        '操作:授权');
insert into sys_dict_data
values (20, '000000', 5, '导出', '5', 'sys_oper_type', '', 'warning', '0', '1', 100, 1, sysdate(), null, null,
        '操作:导出');
insert into sys_dict_data
values (21, '000000', 6, '导入', '6', 'sys_oper_type', '', 'warning', '0', '1', 100, 1, sysdate(), null, null,
        '操作:导入');
insert into sys_dict_data
values (22, '000000', 7, '强退', '7', 'sys_oper_type', '', 'danger', '0', '1', 100, 1, sysdate(), null, null,
        '操作:强退');
insert into sys_dict_data
values (23, '000000', 8, '生成代码', '8', 'sys_oper_type', '', 'success', '0', '1', 100, 1, sysdate(), null, null,
        '操作:生成代码');
insert into sys_dict_data
values (24, '000000', 9, '清空数据', '9', 'sys_oper_type', '', 'danger', '0', '1', 100, 1, sysdate(), null, null,
        '操作:清空数据');
insert into sys_dict_data
values (25, '000000', 99, '其他', '99', 'sys_oper_type', '', 'info', '0', '1', 100, 1, sysdate(), null, null,
        '操作:其他');

-- 操作结果
insert into sys_dict_data
values (26, '000000', 1, '成功', '1', 'sys_oper_result', '', 'success', '0', '1', 100, 1, sysdate(), null, null,
        '操作结果:成功');
insert into sys_dict_data
values (27, '000000', 2, '失败', '0', 'sys_oper_result', '', 'danger', '0', '1', 100, 1, sysdate(), null, null,
        '操作结果:失败');

-- 审核状态
insert into sys_dict_data
values (28, '000000', 0, '待审核', '0', 'sys_audit_status', '', 'info', '1', '1', 100, 1, sysdate(), null, null,
        '审核状态:待审核');
insert into sys_dict_data
values (29, '000000', 1, '通过', '1', 'sys_audit_status', '', 'success', '0', '1', 100, 1, sysdate(), null, null,
        '审核状态:通过');
insert into sys_dict_data
values (30, '000000', 2, '驳回', '2', 'sys_audit_status', '', 'warning', '0', '1', 100, 1, sysdate(), null, null,
        '审核状态:驳回');
insert into sys_dict_data
values (31, '000000', 3, '拒绝', '3', 'sys_audit_status', '', 'danger', '0', '1', 100, 1, sysdate(), null, null,
        '审核状态:拒绝');

-- 文件类型
insert into sys_dict_data
values (32, '000000', 0, '图片', 'image', 'sys_file_type', '', 'primary', '0', '1', 100, 1, sysdate(), null, null,
        '文件类型:图片');
insert into sys_dict_data
values (33, '000000', 1, '文档', 'document', 'sys_file_type', '', 'success', '0', '1', 100, 1, sysdate(), null, null,
        '文件类型:文档');
insert into sys_dict_data
values (34, '000000', 2, '视频', 'video', 'sys_file_type', '', 'info', '0', '1', 100, 1, sysdate(), null, null,
        '文件类型:视频');
insert into sys_dict_data
values (35, '000000', 3, '音频', 'audio', 'sys_file_type', '', 'warning', '0', '1', 100, 1, sysdate(), null, null,
        '文件类型:音频');
insert into sys_dict_data
values (36, '000000', 4, '压缩包', 'archive', 'sys_file_type', '', 'danger', '0', '1', 100, 1, sysdate(), null, null,
        '文件类型:压缩包');
insert into sys_dict_data
values (37, '000000', 5, '其他', 'other', 'sys_file_type', '', 'info', '0', '1', 100, 1, sysdate(), null, null,
        '文件类型:其他');

-- 消息类型
insert into sys_dict_data
values (38, '000000', 0, '系统通知', 'system', 'sys_message_type', '', 'danger', '0', '1', 100, 1, sysdate(), null,
        null, '消息类型:系统通知');
insert into sys_dict_data
values (39, '000000', 1, '活动通知', 'activity', 'sys_message_type', '', 'success', '0', '1', 100, 1, sysdate(), null,
        null, '消息类型:活动通知');
insert into sys_dict_data
values (40, '000000', 2, '审核通知', 'audit', 'sys_message_type', '', 'info', '0', '1', 100, 1, sysdate(), null, null,
        '消息类型:审核通知');
insert into sys_dict_data
values (41, '000000', 3, '账户通知', 'account', 'sys_message_type', '', 'warning', '0', '1', 100, 1, sysdate(), null,
        null, '消息类型:账户通知');
insert into sys_dict_data
values (42, '000000', 4, '私信', 'private', 'sys_message_type', '', 'primary', '0', '1', 100, 1, sysdate(), null, null,
        '消息类型:私信');

-- 通知类型
insert into sys_dict_data
values (43, '000000', 1, '通知', '1', 'sys_notice_type', '', 'warning', '1', '1', 100, 1, sysdate(), null, null,
        '通知类型:通知');
insert into sys_dict_data
values (44, '000000', 2, '公告', '2', 'sys_notice_type', '', 'success', '0', '1', 100, 1, sysdate(), null, null,
        '通知类型:公告');

-- 通知状态
insert into sys_dict_data
values (45, '000000', 1, '立即发送', '1', 'sys_notice_status', '', 'success', '1', '1', 100, 1, sysdate(), null, null,
        '通知状态:立即发送');
insert into sys_dict_data
values (46, '000000', 2, '保存为草稿', '0', 'sys_notice_status', '', 'danger', '0', '1', 100, 1, sysdate(), null, null,
        '通知状态:保存为草稿');

-- 平台类型
insert into sys_dict_data
values (100, '000000', 0, '微信小程序', 'mp-weixin', 'sys_platform_type', '', 'primary', '0', '1', 100, 1, sysdate(),
        null, null, '平台:微信小程序');
insert into sys_dict_data
values (101, '000000', 10, '微信公众号', 'mp-official-account', 'sys_platform_type', '', 'success', '0', '1', 100, 1,
        sysdate(), null, null, '平台:微信公众号');
insert into sys_dict_data
values (102, '000000', 20, 'QQ小程序', 'mp-qq', 'sys_platform_type', '', 'info', '0', '1', 100, 1, sysdate(), null, null,
        '平台:QQ小程序');
insert into sys_dict_data
values (103, '000000', 30, '支付宝小程序', 'mp-alipay', 'sys_platform_type', '', 'warning', '0', '1', 100, 1, sysdate(),
        null, null, '平台:支付宝小程序');
insert into sys_dict_data
values (104, '000000', 40, '京东小程序', 'mp-jd', 'sys_platform_type', '', 'danger', '0', '1', 100, 1, sysdate(), null,
        null, '平台:京东小程序');
insert into sys_dict_data
values (105, '000000', 50, '快手小程序', 'mp-kuaishou', 'sys_platform_type', '', 'info', '0', '1', 100, 1, sysdate(),
        null, null, '平台:快手小程序');
insert into sys_dict_data
values (106, '000000', 60, '飞书小程序', 'mp-lark', 'sys_platform_type', '', 'primary', '0', '1', 100, 1, sysdate(),
        null, null, '平台:飞书小程序');
insert into sys_dict_data
values (107, '000000', 70, '百度小程序', 'mp-baidu', 'sys_platform_type', '', 'success', '0', '1', 100, 1, sysdate(),
        null, null, '平台:百度小程序');
insert into sys_dict_data
values (108, '000000', 80, '头条小程序', 'mp-toutiao', 'sys_platform_type', '', 'warning', '0', '1', 100, 1, sysdate(),
        null, null, '平台:头条/抖音小程序');
insert into sys_dict_data
values (109, '000000', 90, '小红书小程序', 'mp-xhs', 'sys_platform_type', '', 'danger', '0', '1', 100, 1, sysdate(),
        null, null, '平台:小红书小程序');

-- 支付方式
insert into sys_dict_data
values (110, '000000', 0, '微信支付', 'wechat', 'sys_payment_method', '', 'success', '0', '1', 100, 1, sysdate(), null,
        null, '支付方式:微信支付');
insert into sys_dict_data
values (111, '000000', 1, '支付宝', 'alipay', 'sys_payment_method', '', 'primary', '0', '1', 100, 1, sysdate(), null,
        null, '支付方式:支付宝');
insert into sys_dict_data
values (112, '000000', 2, '银联', 'unionpay', 'sys_payment_method', '', 'danger', '0', '1', 100, 1, sysdate(), null,
        null, '支付方式:银联');
insert into sys_dict_data
values (113, '000000', 3, '余额支付', 'balance', 'sys_payment_method', '', 'info', '0', '1', 100, 1, sysdate(), null,
        null, '支付方式:余额支付');
insert into sys_dict_data
values (114, '000000', 4, '积分抵扣', 'points', 'sys_payment_method', '', 'warning', '0', '1', 100, 1, sysdate(), null,
        null, '支付方式:积分抵扣');

-- 订单状态
insert into sys_dict_data
values (120, '000000', 0, '待支付', 'pending', 'sys_order_status', '', 'info', '0', '1', 100, 1, sysdate(), null, null,
        '订单状态:待支付');
insert into sys_dict_data
values (121, '000000', 1, '已支付', 'paid', 'sys_order_status', '', 'primary', '0', '1', 100, 1, sysdate(), null, null,
        '订单状态:已支付');
insert into sys_dict_data
values (122, '000000', 2, '已发货', 'delivered', 'sys_order_status', '', 'warning', '0', '1', 100, 1, sysdate(), null, null,
        '订单状态:已发货');
insert into sys_dict_data
values (123, '000000', 3, '已完成', 'completed', 'sys_order_status', '', 'success', '0', '1', 100, 1, sysdate(), null, null,
        '订单状态:已完成');
insert into sys_dict_data
values (124, '000000', 4, '已取消', 'cancelled', 'sys_order_status', '', 'info', '0', '1', 100, 1, sysdate(), null, null,
        '订单状态:已取消');
insert into sys_dict_data
values (125, '000000', 5, '已退款', 'refunded', 'sys_order_status', '', 'danger', '0', '1', 100, 1, sysdate(), null, null,
        '订单状态:已退款');

-- 商品规格类型
insert into sys_dict_data
values (130, '000000', 1, '单规格', '0', 'm_goods_spec_type', '', 'success', '0', '1', 100, 1, sysdate(), null, null, '单一规格商品');
insert into sys_dict_data
values (131, '000000', 2, '多规格', '1', 'm_goods_spec_type', '', 'primary', '0', '1', 100, 1, sysdate(), null, null, '多规格商品');

-- 13、参数配置表
create table sys_config
(
    config_id    bigint(20) not null comment '参数主键',
    tenant_id    varchar(20)  default '000000' comment '租户id',
    config_name  varchar(100) default '' comment '参数名称',
    config_key   varchar(100) default '' comment '参数键名',
    config_value varchar(500) default '' comment '参数键值',
    config_type  char(1)      default '0' comment '系统内置（1是 0否）',
    create_dept  bigint(20)   default null comment '创建部门',
    create_by    bigint(20)   default null comment '创建者',
    create_time  datetime comment '创建时间',
    update_by    bigint(20)   default null comment '更新者',
    update_time  datetime comment '更新时间',
    remark       varchar(500) default null comment '备注',
    primary key (config_id)
) engine = innodb comment = '参数配置表';

insert into sys_config
values (1, '000000', '登录验证-验证码开关', 'system.security.captcha-enabled', 'true', '1', 200, 1, sysdate(), null,
        null, '登录时是否开启验证码功能（true开启，false关闭）');
insert into sys_config
values (2, '000000', '用户管理-初始密码', 'system.user.initial-password', '123456', '1', 100, 1, sysdate(), null, null,
        '新用户创建时的默认初始密码');
insert into sys_config
values (3, '000000', '账号自助-注册功能开关', 'system.account.register-enabled', 'false', '1', 300, 1, sysdate(), null,
        null, '是否允许用户自助注册账号（true开启，false关闭）');
insert into sys_config
values (4, '000000', 'OSS功能-预览列表开关', 'system.oss.preview-enabled', 'true', '1', 300, 1, sysdate(), null, null,
        '是否开启OSS资源预览列表功能（true开启，false关闭）');
insert into sys_config
values (5, '000000', '社交登录-自动注册开关', 'system.social.auto-register-enabled', 'false', '1', 300, 1, sysdate(), null, null,
        '是否允许社交登录时自动注册账号（true开启false关闭）');

-- API密钥管理表
create table sys_api_key
(
    id                 bigint(20)   not null comment 'API密钥ID',
    tenant_id          varchar(20)  default '000000' comment '租户id',
    app_name           varchar(100) not null comment '应用名称',
    app_key            varchar(64)  not null comment 'AppKey(公开)',
    app_secret         varchar(128) not null comment 'AppSecret',
    user_id            bigint(20)   default null comment '关联用户ID',
    expire_time        datetime      default null comment '过期时间',
    status             char(1)       default '1' comment '状态(0停用 1正常)',
    white_ips          varchar(500)  default null comment 'IP白名单,逗号分隔',
    call_count         bigint(20)    default 0 comment '调用次数',
    last_call_time     datetime      default null comment '最后调用时间',
    create_dept        bigint(20)    default null comment '创建部门',
    create_by          bigint(20)    default null comment '创建者',
    create_time        datetime comment '创建时间',
    update_by          bigint(20)    default null comment '更新者',
    update_time        datetime comment '更新时间',
    remark             varchar(500)  default null comment '备注',
    primary key (id),
    unique key uk_app_key (app_key),
    key idx_user_id (user_id),
    key idx_status (status)
) engine = innodb comment = 'API密钥管理表';

-- 14、登录日志
create table sys_login_log
(
    info_id        bigint(20) not null comment '访问ID',
    tenant_id      varchar(20)  default '000000' comment '租户id',
    user_id        bigint(20)   default null comment '用户id',
    user_name      varchar(50)  default '' comment '用户账号',
    device_type    varchar(32)  default '' comment '设备类型',
    ipaddr         varchar(128) default '' comment '登录IP地址',
    login_location varchar(255) default '' comment '登录地点',
    browser        varchar(50)  default '' comment '浏览器类型',
    os             varchar(50)  default '' comment '操作系统',
    status         char(1)      default '1' comment '登录状态',
    msg            varchar(255) default '' comment '提示消息',
    login_time     datetime comment '访问时间',
    primary key (info_id),
    key idx_sys_login_log_s (status),
    key idx_sys_login_log_lt (login_time)
) engine = innodb comment = '登录日志';


-- 17、通知公告表
CREATE TABLE sys_notice
(
    notice_id       BIGINT      NOT NULL COMMENT '公告ID',
    tenant_id       VARCHAR(20)  DEFAULT '000000' COMMENT '租户id',
    notice_type     CHAR(1)     NOT NULL COMMENT '公告类型（1通知 2公告）',
    target_config   TEXT         DEFAULT NULL COMMENT '推送配置JSON',
    target_user_ids TEXT         DEFAULT NULL COMMENT '目标用户ID列表',
    read_user_ids   TEXT         DEFAULT NULL COMMENT '已读用户ID列表',
    notice_title    VARCHAR(50) NOT NULL COMMENT '公告标题',
    notice_content  TEXT         DEFAULT NULL COMMENT '公告内容',
    status          CHAR(1)      DEFAULT '1' COMMENT '公告状态',
    create_dept     BIGINT       DEFAULT NULL COMMENT '创建部门',
    create_by       BIGINT       DEFAULT NULL COMMENT '创建者',
    create_time     DATETIME COMMENT '创建时间',
    update_by       BIGINT       DEFAULT NULL COMMENT '更新者',
    update_time     DATETIME COMMENT '更新时间',
    remark          VARCHAR(255) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (notice_id)
) ENGINE = INNODB COMMENT = '通知公告表';

-- 初始化-公告信息表数据
INSERT INTO sys_notice
VALUES ('1', '000000', '2', '{"type": "all"}', 1, NULL, '温馨提醒：2018-07-01 新版本发布啦', '新版本内容', '1', 100, 1,
        sysdate(), null, null, '管理员');
INSERT INTO sys_notice
VALUES ('2', '000000', '1', '{"type": "all"}', 1, NULL, '维护通知：2018-07-01 系统凌晨维护', '维护内容', '1', 100, 1,
        sysdate(), null, null, '管理员');

-- 18、代码生成业务表
create table sys_gen_table
(
    table_id          bigint(20) not null comment '编号',
    data_name         varchar(200) default '' comment '数据源名称',
    table_name        varchar(200) default '' comment '表名称',
    table_comment     varchar(500) default '' comment '表描述',
    sub_table_name    varchar(64)  default null comment '关联子表的表名',
    sub_table_fk_name varchar(64)  default null comment '子表关联的外键名',
    class_name        varchar(100) default '' comment '实体类名称',
    tpl_category      varchar(200) default 'crud' comment '使用的模板（crud单表操作 tree树表操作）',
    package_name      varchar(100) comment '生成包路径',
    module_name       varchar(30) comment '生成模块名',
    business_name     varchar(30) comment '生成业务名',
    function_name     varchar(50) comment '生成功能名',
    function_author   varchar(50) comment '生成功能作者',
    gen_type          char(1)      default '0' comment '生成代码方式（0zip压缩包 1自定义路径）',
    gen_path          varchar(200) default '/' comment '生成路径（不填默认项目路径）',
    options           varchar(1000) comment '其它生成选项',
    create_dept       bigint(20)   default null comment '创建部门',
    create_by         bigint(20)   default null comment '创建者',
    create_time       datetime comment '创建时间',
    update_by         bigint(20)   default null comment '更新者',
    update_time       datetime comment '更新时间',
    remark            varchar(500) default null comment '备注',
    primary key (table_id)
) engine = innodb comment = '代码生成业务表';


-- 19、代码生成业务表字段
create table sys_gen_table_column
(
    column_id      bigint(20) not null comment '编号',
    table_id       bigint(20) comment '归属表编号',
    column_name    varchar(200) comment '列名称',
    column_comment varchar(500) comment '列描述',
    column_label   varchar(200) comment '字段标签（用于显示）',
    column_type    varchar(100) comment '列类型',
    java_type      varchar(500) comment 'JAVA类型',
    java_field     varchar(200) comment 'JAVA字段名',
    is_pk          char(1) comment '是否主键（1是）',
    is_increment   char(1) comment '是否自增（1是）',
    is_required    char(1) comment '是否必填（1是）',
    is_insert      char(1) comment '是否为插入字段（1是）',
    is_edit        char(1) comment '是否编辑字段（1是）',
    is_list        char(1) comment '是否列表字段（1是）',
    is_query       char(1) comment '是否查询字段（1是）',
    query_type     varchar(200) default 'EQ' comment '查询方式（等于、不等于、大于、小于、范围）',
    html_type      varchar(200) comment '显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）',
    dict_type      varchar(200) default '' comment '字典类型',
    column_default varchar(100) default '' comment '默认值',
    sort           int comment '排序',
    create_dept    bigint(20)   default null comment '创建部门',
    create_by      bigint(20)   default null comment '创建者',
    create_time    datetime comment '创建时间',
    update_by      bigint(20)   default null comment '更新者',
    update_time    datetime comment '更新时间',
    primary key (column_id)
) engine = innodb comment = '代码生成业务表字段';

-- OSS对象存储表
create table sys_oss
(
    oss_id        bigint(20)   not null comment '对象存储主键',
    tenant_id     varchar(20)           default '000000' comment '租户id',
    directory_id  bigint       null     default NULL COMMENT '所属目录ID',
    file_name     varchar(255) not null default '' comment '文件名',
    original_name varchar(255) not null default '' comment '原名',
    file_suffix   varchar(10)  not null default '' comment '文件后缀名',
    file_size     bigint(20)            default null comment '文件大小(字节)',
    url           varchar(500) not null comment 'URL地址',
    ext1          text                  default null comment '扩展字段',
    create_dept   bigint(20)            default null comment '创建部门',
    create_time   datetime              default null comment '创建时间',
    create_by     bigint(20)            default null comment '上传人',
    update_time   datetime              default null comment '更新时间',
    update_by     bigint(20)            default null comment '更新人',
    service       varchar(20)  not null default 'minio' comment '服务商',
    primary key (oss_id)
) engine = innodb comment ='OSS对象存储表';

-- OSS对象存储动态配置表
create table sys_oss_config
(
    oss_config_id bigint(20)  not null comment '主键',
    tenant_id     varchar(20)          default '000000' comment '租户id',
    config_key    varchar(20) not null default '' comment '配置key',
    access_key    varchar(255)         default '' comment 'accessKey',
    secret_key    varchar(255)         default '' comment '秘钥',
    bucket_name   varchar(255)         default '' comment '桶名称',
    prefix        varchar(255)         default '' comment '前缀',
    endpoint      varchar(255)         default '' comment '访问站点',
    domain        varchar(255)         default '' comment '自定义域名',
    is_https      char(1)              default '0' comment '是否https（1=是,0=否）',
    region        varchar(255)         default '' comment '域',
    access_policy char(1)     not null default '1' comment '桶权限类型(0=private 1=public 2=custom)',
    status        char(1)              default '1' comment '启用状态',
    ext1          varchar(255)         default '' comment '扩展字段',
    create_dept   bigint(20)           default null comment '创建部门',
    create_by     bigint(20)           default null comment '创建者',
    create_time   datetime             default null comment '创建时间',
    update_by     bigint(20)           default null comment '更新者',
    update_time   datetime             default null comment '更新时间',
    remark        varchar(500)         default null comment '备注',
    primary key (oss_config_id)
) engine = innodb comment ='对象存储配置表';

insert into sys_oss_config
values (1, '000000', 'minio', 'ruoyi', 'ruoyi123', 'ruoyi', 'erp_sys', '127.0.0.1:9000', '', '0', '', '1', '0', '',
        100, 1, sysdate(), 1, sysdate(), null);
insert into sys_oss_config
values (2, '000000', 'qiniu', 'XXXXXXXXXXXXXXX', 'XXXXXXXXXXXXXXX', 'ruoyi', 'erp_sys', 's3-cn-north-1.qiniucs.com',
        '', '0', '', '1', '0', '', 100, 1, sysdate(), 1, sysdate(), null);
insert into sys_oss_config
values (3, '000000', 'aliyun', 'XXXXXXXXXXXXXXX', 'XXXXXXXXXXXXXXX', 'ruoyi', 'erp_sys',
        'oss-cn-beijing.aliyuncs.com', '', '0', '', '1', '0', '', 100, 1, sysdate(), 1, sysdate(), null);
insert into sys_oss_config
values (4, '000000', 'qcloud', 'XXXXXXXXXXXXXXX', 'XXXXXXXXXXXXXXX', 'ruoyi-1240000000', 'erp_sys',
        'cos.ap-beijing.myqcloud.com', '', '0', 'ap-beijing', '1', '0', '', 100, 1, sysdate(), 1, sysdate(), null);
insert into sys_oss_config
values (5, '000000', 'image', 'ruoyi', 'ruoyi123', 'ruoyi', 'erp_sys', '127.0.0.1:9000', '', '0', '', '1', '0', '',
        100, 1, sysdate(), 1, sysdate(), null);
insert into sys_oss_config
values (6, '000000', 'local', 'local', 'local', 'local', 'erp_sys', '127.0.0.1', '', '0', '', '1', '1', '', 100, 1,
        sysdate(), 1, sysdate(),
        '支持配置自定义域名和是否https，自定义域名不要加前缀，自定义域名如果做了反向代理，需要带上反向代理路径,如: xxx.ruoyikj.top/xxx');

-- OSS目录
create table sys_oss_directory
(
    directory_id   bigint(20)   not null comment '目录ID',
    tenant_id      varchar(20)           default '000000' comment '租户id',
    parent_id      bigint(20)            default 0 comment '父目录ID',
    ancestors      varchar(500)          default '' comment '祖级列表',
    directory_name varchar(255) not null default '' comment '目录名称',
    directory_path varchar(500)          default '' comment '目录路径',
    order_num      int                   default 0 comment '显示顺序',
    status         char(1)               default '1' comment '目录状态',
    is_default     char(1)               default '0' comment '是否默认目录（0=是,1=否）',
    create_dept    bigint(20)            default null comment '创建部门',
    create_by      bigint(20)            default null comment '创建者',
    create_time    datetime              default null comment '创建时间',
    update_by      bigint(20)            default null comment '更新者',
    update_time    datetime              default null comment '更新时间',
    remark         varchar(500)          default null comment '备注',
    primary key (directory_id)
) engine = innodb comment ='OSS目录';

-- 模板表
create table if not exists a_temp
(
    id          bigint(20) not null comment 'id',
    tenant_id   varchar(20)  default '000000' comment '租户id',
    status      char(1)      default '1' comment '状态',
    create_dept bigint(20)   default null comment '创建部门',
    create_by   bigint(20)   default null comment '创建人',
    create_time datetime comment '创建时间',
    update_by   bigint(20)   default null comment '修改人',
    update_time datetime comment '更新时间',
    remark      varchar(255) default null comment '备注',
    is_deleted  char(1)      default '0' comment '是否删除',
    primary key (id)
) engine = innodb comment = '模板';

-- ============================================================================
-- 错误日志表 (sys_error_log)
-- 用途: 记录系统运行时的错误、异常、SQL错误等，便于快速排查和统计分析
-- ============================================================================
create table sys_error_log
(
    -- ========== 主键与租户 ==========
    id              bigint(20)   not null comment '主键ID',
    tenant_id       varchar(20)  default '000000' comment '租户ID',

    -- ========== 错误基本信息 ==========
    error_level     varchar(20)  not null comment '严重级别(ERROR/WARN/FATAL)',
    error_type      varchar(255) not null comment '异常类名(ServiceException/SQLException等)',
    error_code      varchar(50)  default null comment '业务错误码',
    error_message   text         not null comment '错误消息',
    error_stack     mediumtext   default null comment '异常堆栈(限制5000字符)',

    -- ========== 请求信息 ==========
      trace_id        varchar(50)  default null comment '链路追踪ID(MDC中的traceId)',
      request_uri     varchar(500) default null comment '请求URI',
      request_pattern varchar(500) default null comment '请求路径模板',
      request_method  varchar(10)  default null comment '请求方法(GET/POST)',
    request_params  text         default null comment '请求参数(JSON格式，已脱敏)',
    request_ip      varchar(50)  default null comment '请求IP',
    user_agent      varchar(500) default null comment 'User-Agent',

    -- ========== 用户信息 ==========
    user_id         bigint(20)   default null comment '操作用户ID',
    user_name       varchar(100) default null comment '操作用户名',
    dept_id         bigint(20)   default null comment '所属部门ID',

    -- ========== SQL错误专项信息 ==========
    sql_statement   text         default null comment '执行的SQL语句',
    sql_params      text         default null comment 'SQL参数(JSON格式)',
    sql_duration    int          default null comment 'SQL执行耗时(ms)',

    -- ========== 业务上下文 ==========
    module_name     varchar(100) default null comment '业务模块(base/mall/iot/crm)',
    business_type   varchar(50)  default null comment '业务类型(查询/新增/修改/删除)',
    business_key    varchar(200) default null comment '业务关键字(订单号/商品ID/设备ID等)',

    -- ========== 客户端信息（前端错误上报） ==========
    client_type     varchar(20)  default null comment '平台类型(PC/H5/MINIAPP/APP)',
    client_version  varchar(50)  default null comment '客户端版本',
    os_type         varchar(20)  default null comment '操作系统(Windows/Mac/Android/iOS)',

    -- ========== 服务器环境信息 ==========
    server_name     varchar(100) default null comment '服务器名称',
    server_ip       varchar(50)  default null comment '服务器IP',
    app_version     varchar(20)  default null comment '应用版本',
    thread_name     varchar(100) default null comment '线程名称',

    -- ========== 处理状态 ==========
    handle_status   char(1)      default '0' comment '处理状态(0未处理 1已处理 2已忽略)',
    handle_by       bigint(20)   default null comment '处理人ID',
    handle_time     datetime     default null comment '处理时间',
    handle_remark   varchar(500) default null comment '处理备注',

    -- ========== 统计信息（去重机制） ==========
    occurrence_count int         default 1 comment '相同错误出现次数',
    first_time      datetime     default null comment '首次出现时间',
    last_time       datetime     default null comment '最后出现时间',

    -- ========== 审计字段（项目规范） ==========
    create_dept     bigint(20)   default null comment '创建部门',
    create_by       bigint(20)   default null comment '创建人',
    create_time     datetime     default current_timestamp comment '创建时间',
    update_by       bigint(20)   default null comment '更新人',
    update_time     datetime     default current_timestamp on update current_timestamp comment '更新时间',
    remark          varchar(500) default null comment '备注',

    primary key (id)
) engine=innodb default charset=utf8mb4 comment='错误日志表';

-- MySQL 数据库索引创建语句

-- 1. sys_social 第三方平台授权表
CREATE INDEX idx_tenant_user ON sys_social (tenant_id, user_id);
CREATE INDEX idx_auth_id ON sys_social (auth_id);
CREATE INDEX idx_source ON sys_social (source);

-- 2. sys_tenant 租户表
CREATE INDEX idx_tenant_id ON sys_tenant (tenant_id);

-- 4. sys_dept 部门表
CREATE INDEX idx_tenant_id ON sys_dept (tenant_id);
CREATE INDEX idx_parent_id ON sys_dept (parent_id);

-- 5. sys_user 用户信息表
CREATE INDEX idx_tenant_id ON sys_user (tenant_id);
CREATE INDEX idx_user_name ON sys_user (user_name);

-- 6. sys_post 岗位信息表
CREATE INDEX idx_tenant_id ON sys_post (tenant_id);
CREATE INDEX idx_dept_id ON sys_post (dept_id);

-- 7. sys_role 角色信息表
CREATE INDEX idx_tenant_id ON sys_role (tenant_id);
CREATE INDEX idx_role_key ON sys_role (role_key);

-- 8. sys_menu 菜单权限表
CREATE INDEX idx_parent_id ON sys_menu (parent_id);
CREATE INDEX idx_menu_type ON sys_menu (menu_type);

-- 9. sys_user_role 用户和角色关联表
CREATE INDEX idx_role_id ON sys_user_role (role_id);

-- 10. sys_role_menu 角色和菜单关联表
CREATE INDEX idx_menu_id ON sys_role_menu (menu_id);

-- 11. sys_role_dept 角色和部门关联表
CREATE INDEX idx_dept_id ON sys_role_dept (dept_id);

-- 12. sys_user_post 用户与岗位关联表
CREATE INDEX idx_post_id ON sys_user_post (post_id);

-- 13. sys_oper_log 操作日志
CREATE INDEX idx_tenant_id ON sys_oper_log (tenant_id);

-- 14. sys_dict_type 字典类型表
CREATE INDEX idx_tenant_id ON sys_dict_type (tenant_id);
CREATE INDEX idx_dict_type ON sys_dict_type (dict_type);

-- 15. sys_dict_data 字典数据表
CREATE INDEX idx_tenant_id ON sys_dict_data (tenant_id);
CREATE INDEX idx_dict_type ON sys_dict_data (dict_type);

-- 16. sys_config 参数配置表
CREATE INDEX idx_tenant_id ON sys_config (tenant_id);
CREATE INDEX idx_config_key ON sys_config (config_key);

-- 17. sys_login_log 登录日志
CREATE INDEX idx_tenant_id ON sys_login_log (tenant_id);
CREATE INDEX idx_user_id ON sys_login_log (user_id);

-- 18. sys_notice 通知公告表
CREATE INDEX idx_tenant_id ON sys_notice (tenant_id);

-- 21. sys_oss OSS对象存储表
CREATE INDEX idx_tenant_id ON sys_oss (tenant_id);
CREATE INDEX idx_directory_id ON sys_oss (directory_id);

-- 23. sys_oss_directory OSS目录
CREATE INDEX idx_tenant_id ON sys_oss_directory (tenant_id);
CREATE INDEX idx_parent_id ON sys_oss_directory (parent_id);

-- 24. sys_error_log 错误日志表
CREATE INDEX idx_tenant_time ON sys_error_log (tenant_id, create_time);
CREATE INDEX idx_severity ON sys_error_log (error_level);
CREATE INDEX idx_exception ON sys_error_log (error_type(100));
CREATE INDEX idx_user_id ON sys_error_log (user_id);
CREATE INDEX idx_trace_id ON sys_error_log (trace_id);
CREATE INDEX idx_module ON sys_error_log (module_name);
CREATE INDEX idx_handle_status ON sys_error_log (handle_status);
CREATE INDEX idx_business_key ON sys_error_log (business_key(100));

