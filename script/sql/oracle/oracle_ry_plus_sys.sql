-- 第三方平台授权表
create table sys_social
(
    id                 number(20)        not null,
    user_id            number(20)        not null,
    tenant_id          varchar2(20)      default '000000',
    auth_id            varchar2(255)     not null,
    source             varchar2(255)     not null,
    open_id            varchar2(255)     default null,
    user_name          varchar2(30)      not null,
    nick_name          varchar2(30)      default '',
    email              varchar2(255)     default '',
    avatar             varchar2(500)     default '',
    access_token       varchar2(255)     not null,
    expire_in          number(20)        default null,
    refresh_token      varchar2(255)     default null,
    access_code        varchar2(255)     default null,
    union_id           varchar2(255)     default null,
    scope              varchar2(255)     default null,
    token_type         varchar2(255)     default null,
    id_token           varchar2(2000)    default null,
    mac_algorithm      varchar2(255)     default null,
    mac_key            varchar2(255)     default null,
    code               varchar2(255)     default null,
    oauth_token        varchar2(255)     default null,
    oauth_token_secret varchar2(255)     default null,
    create_dept        number(20),
    create_by          number(20),
    create_time        date,
    update_by          number(20),
    update_time        date,
    is_deleted           char(1)          default '0'
);

alter table sys_social add constraint pk_sys_social primary key (id);

comment on table   sys_social                   is '社会化关系表';
comment on column  sys_social.id                is '主键';
comment on column  sys_social.user_id           is '用户ID';
comment on column  sys_social.tenant_id         is '租户id';
comment on column  sys_social.auth_id           is '平台+平台唯一id';
comment on column  sys_social.source            is '用户来源';
comment on column  sys_social.open_id           is '平台编号唯一id';
comment on column  sys_social.user_name         is '登录账号';
comment on column  sys_social.nick_name         is '用户昵称';
comment on column  sys_social.email             is '用户邮箱';
comment on column  sys_social.avatar            is '头像地址';
comment on column  sys_social.access_token      is '用户的授权令牌';
comment on column  sys_social.expire_in         is '用户的授权令牌的有效期，部分平台可能没有';
comment on column  sys_social.refresh_token     is '刷新令牌，部分平台可能没有';
comment on column  sys_social.access_code       is '平台的授权信息，部分平台可能没有';
comment on column  sys_social.union_id          is '用户的 unionid';
comment on column  sys_social.scope             is '授予的权限，部分平台可能没有';
comment on column  sys_social.token_type        is '个别平台的授权信息，部分平台可能没有';
comment on column  sys_social.id_token          is 'id token，部分平台可能没有';
comment on column  sys_social.mac_algorithm     is '小米平台用户的附带属性，部分平台可能没有';
comment on column  sys_social.mac_key           is '小米平台用户的附带属性，部分平台可能没有';
comment on column  sys_social.code              is '用户的授权code，部分平台可能没有';
comment on column  sys_social.oauth_token       is 'Twitter平台用户的附带属性，部分平台可能没有';
comment on column  sys_social.oauth_token_secret is 'Twitter平台用户的附带属性，部分平台可能没有';
comment on column  sys_social.create_dept       is '创建部门';
comment on column  sys_social.create_by         is '创建者';
comment on column  sys_social.create_time       is '创建时间';
comment on column  sys_social.update_by         is '更新者';
comment on column  sys_social.update_time       is '更新时间';
comment on column  sys_social.is_deleted          is '是否删除';

-- 租户表
create table sys_tenant (
    id                number(20)    not null,
    tenant_id         varchar2(20)  not null,
    contact_user_name varchar2(20)  default '',
    contact_phone     varchar2(20)  default '',
    company_name      varchar2(30)  default '',
    license_number    varchar2(30)  default '',
    address           varchar2(200) default '',
    intro             varchar2(200) default '',
    domain            varchar2(200) default '',
    remark            varchar2(200) default '',
    package_id        number(20)    default null,
    expire_time       date          default null,
    account_count     number(4)     default -1,
    status            char(1)       default '1',
    is_deleted          char(1)       default '0',
    create_dept       number(20)    default null,
    create_by         number(20)    default null,
    create_time       date,
    update_by         number(20)    default null,
    update_time       date
);

alter table sys_tenant add constraint pk_sys_tenant primary key (id);

comment on table   sys_tenant                    is '租户表';
comment on column  sys_tenant.tenant_id          is '租户id';
comment on column  sys_tenant.contact_phone      is '联系电话';
comment on column  sys_tenant.company_name       is '企业名称';
comment on column  sys_tenant.company_name       is '联系人';
comment on column  sys_tenant.license_number     is '统一社会信用代码';
comment on column  sys_tenant.address            is '地址';
comment on column  sys_tenant.intro              is '企业简介';
comment on column  sys_tenant.remark             is '备注';
comment on column  sys_tenant.package_id         is '租户套餐编号';
comment on column  sys_tenant.expire_time        is '过期时间';
comment on column  sys_tenant.account_count      is '用户数量（-1不限制）';
comment on column  sys_tenant.status             is '租户状态';
comment on column  sys_tenant.is_deleted           is '是否删除';
comment on column  sys_tenant.create_dept        is '创建部门';
comment on column  sys_tenant.create_by          is '创建者';
comment on column  sys_tenant.create_time        is '创建时间';
comment on column  sys_tenant.update_by          is '更新者';
comment on column  sys_tenant.update_time        is '更新时间';

-- 初始化-租户表数据

insert into sys_tenant values(1, '000000', '管理组', '15888888888', '若依工作室', null, null, '多租户通用后台管理管理系统', null, null, null, null, -1, '1', '0', 100, 1, sysdate, null, null);


-- 租户套餐表
create table sys_tenant_package (
    package_id              number(20)      not null,
    package_name            varchar2(20)    default '',
    menu_ids                varchar2(3000)  default '',
    remark                  varchar2(200)   default '',
    menu_check_strictly     number(1)       default 1,
    status                  char(1)         default '1',
    is_deleted                char(1)         default '0',
    create_dept             number(20)      default null,
    create_by               number(20)      default null,
    create_time             date,
    update_by               number(20)      default null,
    update_time             date
);

alter table sys_tenant_package add constraint pk_sys_tenant_package primary key (package_id);

comment on table   sys_tenant_package                    is '租户套餐表';
comment on column  sys_tenant_package.package_id         is '租户套餐id';
comment on column  sys_tenant_package.package_name       is '套餐名称';
comment on column  sys_tenant_package.menu_ids           is '关联菜单id';
comment on column  sys_tenant_package.remark             is '备注';
comment on column  sys_tenant_package.status             is '状态';
comment on column  sys_tenant_package.is_deleted           is '是否删除';
comment on column  sys_tenant_package.create_dept        is '创建部门';
comment on column  sys_tenant_package.create_by          is '创建者';
comment on column  sys_tenant_package.create_time        is '创建时间';
comment on column  sys_tenant_package.update_by          is '更新者';
comment on column  sys_tenant_package.update_time        is '更新时间';


-- 1、部门表
create table sys_dept (
  dept_id           number(20)      not null,
  tenant_id         varchar2(20)    default '000000',
  parent_id         number(20)      default 0,
  ancestors         varchar2(500)   default '',
  dept_name         varchar2(30)    default '',
  dept_category     varchar2(100)   default null,
  order_num         number(4)       default 0,
  leader            number(20)     default null,
  phone             varchar2(11)    default null,
  email             varchar2(50)    default null,
  area_code         char(6)         default null,
  status            char(1)         default '1',
  is_deleted          char(1)         default '0',
  create_dept       number(20)      default null,
  create_by         number(20)      default null,
  create_time       date,
  update_by         number(20)      default null,
  update_time       date
);

alter table sys_dept add constraint pk_sys_dept primary key (dept_id);

comment on table  sys_dept              is '部门表';
comment on column sys_dept.dept_id      is '部门id';
comment on column sys_dept.tenant_id    is '租户id';
comment on column sys_dept.parent_id    is '父部门id';
comment on column sys_dept.ancestors    is '祖级列表';
comment on column sys_dept.dept_name    is '部门名称';
comment on column sys_dept.dept_category is '部门类别编码';
comment on column sys_dept.order_num    is '显示顺序';
comment on column sys_dept.leader       is '负责人';
comment on column sys_dept.phone        is '联系电话';
comment on column sys_dept.email        is '邮箱';
comment on column sys_dept.area_code    is '区划代码';
comment on column sys_dept.status       is '部门状态';
comment on column sys_dept.is_deleted     is '是否删除';
comment on column sys_dept.create_dept  is '创建部门';
comment on column sys_dept.create_by    is '创建者';
comment on column sys_dept.create_time  is '创建时间';
comment on column sys_dept.update_by    is '更新者';
comment on column sys_dept.update_time  is '更新时间';

-- 初始化-部门表数据

insert into sys_dept values(100, '000000', 0,   '0',          '若依工作室',   null,0, null, '15888888888', 'xxx@qq.com', null, '1', '0', 100, 1, sysdate, null, null);
insert into sys_dept values(101, '000000', 100, '0,100',      '深圳总公司', null,1, null, '15888888888', 'xxx@qq.com', null, '1', '0', 100, 1, sysdate, null, null);
insert into sys_dept values(102, '000000', 100, '0,100',      '长沙分公司', null,2, null, '15888888888', 'xxx@qq.com', null, '1', '0', 100, 1, sysdate, null, null);
insert into sys_dept values(103, '000000', 101, '0,100,101',  '研发部门',   null,1, 1, '15888888888', 'xxx@qq.com', null, '1', '0', 100, 1, sysdate, null, null);
insert into sys_dept values(104, '000000', 101, '0,100,101',  '市场部门',   null,2, null, '15888888888', 'xxx@qq.com', null, '1', '0', 100, 1, sysdate, null, null);
insert into sys_dept values(105, '000000', 101, '0,100,101',  '测试部门',   null,3, null, '15888888888', 'xxx@qq.com', null, '1', '0', 100, 1, sysdate, null, null);
insert into sys_dept values(106, '000000', 101, '0,100,101',  '财务部门',   null,4, null, '15888888888', 'xxx@qq.com', null, '1', '0', 100, 1, sysdate, null, null);
insert into sys_dept values(107, '000000', 101, '0,100,101',  '运维部门',   null,5, null, '15888888888', 'xxx@qq.com', null, '1', '0', 100, 1, sysdate, null, null);
insert into sys_dept values(108, '000000', 102, '0,100,102',  '市场部门',   null,1, null, '15888888888', 'xxx@qq.com', null, '1', '0', 100, 1, sysdate, null, null);
insert into sys_dept values(109, '000000', 102, '0,100,102',  '财务部门',   null,2, null, '15888888888', 'xxx@qq.com', null, '1', '0', 100, 1, sysdate, null, null);


-- 2、用户信息表
create table sys_user (
  user_id           number(20)      not null,
  tenant_id         varchar2(20)    default '000000',
  dept_id           number(20)      default null,
  user_name         varchar2(40)    default null,
  nick_name         varchar2(40)    default null,
  user_type         varchar2(20)    default 'pc_user',
  email             varchar2(50)    default '',
  phone       varchar2(11)    default '',
  gender            char(1)         default null,
  avatar            varchar2(255)   default '',
  password          varchar2(100)   default '',
  status            char(1)         default '1',
  is_deleted          char(1)         default '0',
  login_ip          varchar2(128)   default '',
  login_date        date,
  create_dept       number(20)      default null,
  create_by         number(20)      default null,
  create_time       date,
  update_by         number(20)      default null,
  update_time       date,
  remark            varchar2(500)   default ''
);

alter table sys_user add constraint pk_sys_user primary key (user_id);

comment on table  sys_user              is '用户信息表';
comment on column sys_user.user_id      is '用户ID';
comment on column sys_user.tenant_id    is '租户id';
comment on column sys_user.dept_id      is '部门ID';
comment on column sys_user.user_name    is '用户账号';
comment on column sys_user.nick_name    is '用户昵称';
comment on column sys_user.user_type    is '用户类型';
comment on column sys_user.email        is '用户邮箱';
comment on column sys_user.phone  is '手机号码';
comment on column sys_user.gender          is '用户性别';
comment on column sys_user.avatar       is '头像路径';
comment on column sys_user.password     is '密码';
comment on column sys_user.status       is '帐号状态';
comment on column sys_user.is_deleted     is '是否删除';
comment on column sys_user.login_ip     is '最后登录IP';
comment on column sys_user.login_date   is '最后登录时间';
comment on column sys_user.create_dept  is '创建部门';
comment on column sys_user.create_by    is '创建者';
comment on column sys_user.create_time  is '创建时间';
comment on column sys_user.update_by    is '更新者';
comment on column sys_user.update_time  is '更新时间';
comment on column sys_user.remark       is '备注';

-- 初始化-用户信息表数据
insert into sys_user values(1, '000000', 100, 'superadmin', '抓蛙师', 'pc_user', '770492966@qq.com', '15888888888', '1', null, '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '1', '0', '127.0.0.1', sysdate, 100, 1, sysdate, null, null, '管理员');
insert into sys_user values(3, '000000', 108, 'test', '本部门及以下 密码666666', 'pc_user', '', '', '0', null, '$2a$10$b8yUzN0C71sbz.PhNOCgJe.Tu1yWC3RNrTyjSQ8p1W0.aaUXUJ.Ne', '1', '0', '127.0.0.1', sysdate, 100, 1, sysdate, null, null, '');
insert into sys_user values(4, '000000', 102, 'test1', '仅本人 密码666666', 'pc_user', '', '', '0', null, '$2a$10$b8yUzN0C71sbz.PhNOCgJe.Tu1yWC3RNrTyjSQ8p1W0.aaUXUJ.Ne', '1', '0', '127.0.0.1', sysdate, 100, 1, sysdate, null, null, '');

-- 3、岗位信息表
create table sys_post (
  post_id           number(20)      not null,
  tenant_id         varchar2(20)    default '000000',
  dept_id           number(20)      not null,
  post_code         varchar2(64)    not null,
  post_category     varchar2(64)    default null,
  post_name         varchar2(50)    not null,
  post_sort         number(4)       not null,
  status            char(1)         default '1',
  create_dept       number(20)      default null,
  create_by         number(20)      default null,
  create_time       date,
  update_by         number(20)      default null,
  update_time       date,
  remark            varchar2(500)
);

alter table sys_post add constraint pk_sys_post primary key (post_id);

comment on table  sys_post              is '岗位信息表';
comment on column sys_post.post_id      is '岗位ID';
comment on column sys_post.tenant_id    is '租户id';
comment on column sys_post.dept_id      is '部门id';
comment on column sys_post.post_code    is '岗位编码';
comment on column sys_post.post_category is '岗位类别编码';
comment on column sys_post.post_name    is '岗位名称';
comment on column sys_post.post_sort    is '显示顺序';
comment on column sys_post.status       is '状态';
comment on column sys_post.create_dept  is '创建部门';
comment on column sys_post.create_by    is '创建者';
comment on column sys_post.create_time  is '创建时间';
comment on column sys_post.update_by    is '更新者';
comment on column sys_post.update_time  is '更新时间';
comment on column sys_post.remark       is '备注';

-- 初始化-岗位信息表数据
insert into sys_post values(1, '000000', 100, 'ceo',  null, '董事长',    1, '1', 100, 1, sysdate, null, null, '');
insert into sys_post values(2, '000000', 100, 'se',   null, '项目经理',  2, '1', 100, 1, sysdate, null, null, '');
insert into sys_post values(3, '000000', 100, 'hr',   null, '人力资源',  3, '1', 100, 1, sysdate, null, null, '');
insert into sys_post values(4, '000000', 100, 'user', null, '普通员工',  4, '1', 100, 1, sysdate, null, null, '');


-- 4、角色信息表
create table sys_role (
  role_id              number(20)      not null,
  tenant_id            varchar2(20)    default '000000',
  role_name            varchar2(30)    not null,
  role_key             varchar2(100)   not null,
  role_sort            number(4)       not null,
  data_scope           char(1)         default '1',
  menu_check_strictly  number(1)       default 1,
  dept_check_strictly  number(1)       default 1,
  status               char(1)         default '1',
  is_deleted             char(1)         default '0',
  create_dept          number(20)      default null,
  create_by            number(20)      default null,
  create_time          date,
  update_by            number(20)      default null,
  update_time          date,
  remark               varchar2(500)   default null
);

alter table sys_role add constraint pk_sys_role primary key (role_id);

comment on table  sys_role                       is '角色信息表';
comment on column sys_role.role_id               is '角色ID';
comment on column sys_role.tenant_id             is '租户id';
comment on column sys_role.role_name             is '角色名称';
comment on column sys_role.role_key              is '角色权限字符串';
comment on column sys_role.role_sort             is '显示顺序';
comment on column sys_role.data_scope            is '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限 5：仅本人数据权限 6：部门及以下或本人数据权限）';
comment on column sys_role.menu_check_strictly   is '菜单树选择项是否关联显示';
comment on column sys_role.dept_check_strictly   is '部门树选择项是否关联显示';
comment on column sys_role.status                is '角色状态';
comment on column sys_role.is_deleted              is '是否删除';
comment on column sys_role.create_dept           is '创建部门';
comment on column sys_role.create_by             is '创建者';
comment on column sys_role.create_time           is '创建时间';
comment on column sys_role.update_by             is '更新者';
comment on column sys_role.update_time           is '更新时间';
comment on column sys_role.remark                is '备注';

-- 初始化-角色信息表数据
insert into sys_role values('1', '000000', '超级管理员',  'superadmin',  1, 1, 1, 1, '1', '0', 100, 1, sysdate, null, null, '超级管理员');
insert into sys_role values('2', '000000', 'PC端普通用户', 'pc_user', 2, 1, 1, 1, '1', '0', 100, 1, sysdate, null, null, null);
insert into sys_role values('3', '000000', '移动端普通用户',      'app_user', 3, 1, 1, 1, '1', '0', 100, 1, sysdate, null, null, null);

-- 5、菜单权限表
create table sys_menu (
  menu_id           number(20)      not null,
  menu_name         varchar2(50)    not null,
  parent_id         number(20)      default 0,
  order_num         number(4)       default 0,
  path              varchar2(200)   default '',
  component         varchar2(255)   default null,
  query_param       varchar2(255)   default null,
  is_external_link  char(1)         default '0',
  is_cache          char(1)         default '1',
  menu_type         char(1)         default '',
  visible           char(1)         default '1',
  status            char(1)         default '1',
  perms             varchar2(100)   default null,
  icon              varchar2(100)   default '#',
  create_dept       number(20)      default null,
  create_by         number(20)      default null,
  create_time       date,
  update_by         number(20)      default null,
  update_time       date ,
  remark            varchar2(500)   default ''
);

alter table sys_menu add constraint pk_sys_menu primary key (menu_id);

comment on table  sys_menu                      is '菜单权限表';
comment on column sys_menu.menu_id              is '菜单ID';
comment on column sys_menu.menu_name            is '菜单名称';
comment on column sys_menu.parent_id            is '父菜单ID';
comment on column sys_menu.order_num            is '显示顺序';
comment on column sys_menu.path                 is '路由地址';
comment on column sys_menu.component            is '组件路径';
comment on column sys_menu.query_param          is '路由参数';
comment on column sys_menu.is_external_link     is '是否为外链';
comment on column sys_menu.is_cache             is '是否缓存';
comment on column sys_menu.menu_type            is '菜单类型';
comment on column sys_menu.visible              is '显示设置';
comment on column sys_menu.status               is '启用状态';
comment on column sys_menu.perms                is '权限标识';
comment on column sys_menu.icon                 is '菜单图标';
comment on column sys_menu.create_dept          is '创建部门';
comment on column sys_menu.create_by            is '创建者';
comment on column sys_menu.create_time          is '创建时间';
comment on column sys_menu.update_by            is '更新者';
comment on column sys_menu.update_time          is '更新时间';
comment on column sys_menu.remark               is '备注';

-- 初始化-菜单信息表数据
-- 一级菜单
insert into sys_menu values('1', '系统管理', '0', '97', 'system',           null, '', '0', '1','M', '1', '1', '', 'setting',   100, 1, sysdate, null, null, '系统管理目录');
insert into sys_menu values('6', '租户管理', '0', '98', 'tenant',           null, '', '0', '1','M', '1', '1', '', 'company',    100, 1, sysdate, null, null, '租户管理目录');
insert into sys_menu values('2', '系统监控', '0', '99', 'monitor',          null, '', '0', '1','M', '1', '1', '', 'monitor',  100, 1, sysdate, null, null, '系统监控目录');
insert into sys_menu values('3', '系统工具', '0', '100', 'tool',             null, '', '0', '1','M', '1', '1', '', 'tool',     100, 1, sysdate, null, null, '系统工具目录');
-- 二级菜单
insert into sys_menu values('100',  '用户管理',     '1',   '1', 'user',             'system/core/user/user',            '', '0', '1','C', '1', '1', 'system:user:view',            'user',          100, 1, sysdate, null, null, '用户管理菜单');
insert into sys_menu values('101',  '角色管理',     '1',   '2', 'role',             'system/core/role/role',            '', '0', '1','C', '1', '1', 'system:role:view',            'role',       100, 1, sysdate, null, null, '角色管理菜单');
insert into sys_menu values('102',  '菜单管理',     '1',   '3', 'menu',             'system/core/menu/menu',            '', '0', '1','C', '1', '1', 'system:menu:view',            'menu',    100, 1, sysdate, null, null, '菜单管理菜单');
insert into sys_menu values('103',  '部门管理',     '1',   '4', 'dept',             'system/core/dept/dept',            '', '0', '1','C', '1', '1', 'system:dept:view',            'department',          100, 1, sysdate, null, null, '部门管理菜单');
insert into sys_menu values('104',  '岗位管理',     '1',   '5', 'post',             'system/core/post/post',            '', '0', '1','C', '1', '1', 'system:post:view',            'post',          100, 1, sysdate, null, null, '岗位管理菜单');
insert into sys_menu values('105',  '字典管理',     '1',   '6', 'dict',             'system/dict/dictType',            '', '0', '1','C', '1', '1', 'system:dict:view',            'dict',          100, 1, sysdate, null, null, '字典管理菜单');
insert into sys_menu values('106',  '参数设置',     '1',   '7', 'config',           'system/config/config',          '', '0', '1','C', '1', '1', 'system:config:view',          'edit',          100, 1, sysdate, null, null, '参数设置菜单');
insert into sys_menu values('107',  '通知公告',     '1',   '9', 'notice',           'system/config/notice',          '', '0', '1','C', '1', '1', 'system:notice:view',          'notification',       100, 1, sysdate, null, null, '通知公告菜单');
insert into sys_menu values('108',  '日志管理',     '1',   '10', 'log',              '',                             '', '0', '1','M', '1', '1', '',                            'log',           100, 1, sysdate, null, null, '日志管理菜单');
insert into sys_menu values('109',  '在线用户',     '2',   '1', 'online',           'system/monitor/online/online',         '', '0', '1','C', '1', '1', 'monitor:online:view',         'online',        100, 1, sysdate, null, null, '在线用户菜单');
insert into sys_menu values('113',  '缓存监控',     '2',   '2', 'cache',            'system/monitor/cache/cache',          '', '0', '1','C', '1', '1', 'monitor:cache:view',          'redis',         100, 1, sysdate, null, null, '缓存监控菜单');
insert into sys_menu values('115',  '代码生成',     '3',   '2', 'gen',              'tool/gen/gen',               '', '0', '1','C', '1', '1', 'tool:gen:view',               'code',          100, 1, sysdate, null, null, '代码生成菜单');
insert into sys_menu values('116',  '页面设计',     '3',   '3', 'pageDesigner',     'tool/pageDesigner/pageDesigner', '', '0', '1','C', '1', '1', 'tool:page:view',              'dashboard',          100, 1, sysdate, null, null, '页面设计菜单');
insert into sys_menu values('121',  '租户管理',     '6',   '1', 'tenant',           'system/tenant/tenant',          '', '0', '1','C', '1', '1', 'system:tenant:view',          'users',          100, 1, sysdate, null, null, '租户管理菜单');
insert into sys_menu values('122',  '租户套餐', '6',   '2', 'tenantPackage',    'system/tenant/tenantPackage',   '', '0', '1','C', '1', '1', 'system:tenantPackage:view',   'combination',          100, 1, sysdate, null, null, '租户套餐菜单');
-- springboot-admin监控
insert into sys_menu values('117',  'admin监控',   '2',    '5', 'admin',            'system/monitor/admin/admin',         '', '0', '1','C', '1', '1', 'monitor:admin:view',          'dashboard',     100, 1, sysdate, null, null, 'Admin监控菜单');
-- oss菜单
insert into sys_menu values('118',  '文件管理',     '1',    '11', 'oss',             'system/oss/oss',            '', '0', '1','C', '1', '1', 'system:oss:view',             'upload',        100, 1, sysdate, null, null, '文件管理菜单');
-- snail-job server控制台
insert into sys_menu values('120',  '任务调度',  '2',    '6', 'snailjob',           'system/monitor/snailjob/snailjob',        '', '0', '1','C', '1', '1', 'monitor:snailjob:view', 'job',           100, 1, sysdate, null, null, 'snailjob控制台菜单');

-- 三级菜单
insert into sys_menu values('500',  '操作日志', '108', '1', 'operLog',    'system/monitor/operLog/operLog',    '', '0', '1','C', '1', '1', 'monitor:operLog:view',    'tool',          100, 1, sysdate, null, null, '操作日志菜单');
insert into sys_menu values('501',  '登录日志', '108', '2', 'loginLog', 'system/monitor/loginLog/loginLog', '', '0', '1','C', '1', '1', 'monitor:loginLog:view', 'login-info',    100, 1, sysdate, null, null, '登录日志菜单');
insert into sys_menu values('1644', '错误日志', '108', '3', 'errorLog', 'system/monitor/errorLog/errorLog', '', '0', '1','C', '1', '1', 'monitor:errorLog:view', 'bug',    100, 1, sysdate, null, null, '错误日志菜单');
-- 用户管理按钮
insert into sys_menu values('1001', '用户查询', '100', '1',  '', '', '', '0', '1','F', '1', '1', 'system:user:query',          '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1002', '用户新增', '100', '2',  '', '', '', '0', '1','F', '1', '1', 'system:user:add',            '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1003', '用户修改', '100', '3',  '', '', '', '0', '1','F', '1', '1', 'system:user:update',           '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1004', '用户删除', '100', '4',  '', '', '', '0', '1','F', '1', '1', 'system:user:delete',         '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1005', '用户导出', '100', '5',  '', '', '', '0', '1','F', '1', '1', 'system:user:export',         '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1006', '用户导入', '100', '6',  '', '', '', '0', '1','F', '1', '1', 'system:user:import',         '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1007', '重置密码', '100', '7',  '', '', '', '0', '1','F', '1', '1', 'system:user:resetPwd',       '#', 100, 1, sysdate, null, null, '');
-- 角色管理按钮
insert into sys_menu values('1008', '角色查询', '101', '1',  '', '', '', '0', '1','F', '1', '1', 'system:role:query',          '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1009', '角色新增', '101', '2',  '', '', '', '0', '1','F', '1', '1', 'system:role:add',            '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1010', '角色修改', '101', '3',  '', '', '', '0', '1','F', '1', '1', 'system:role:update',           '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1011', '角色删除', '101', '4',  '', '', '', '0', '1','F', '1', '1', 'system:role:delete',         '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1012', '角色导出', '101', '5',  '', '', '', '0', '1','F', '1', '1', 'system:role:export',         '#', 100, 1, sysdate, null, null, '');
-- 菜单管理按钮
insert into sys_menu values('1013', '菜单查询', '102', '1',  '', '', '', '0', '1','F', '1', '1', 'system:menu:query',          '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1014', '菜单新增', '102', '2',  '', '', '', '0', '1','F', '1', '1', 'system:menu:add',            '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1015', '菜单修改', '102', '3',  '', '', '', '0', '1','F', '1', '1', 'system:menu:update',           '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1016', '菜单删除', '102', '4',  '', '', '', '0', '1','F', '1', '1', 'system:menu:delete',         '#', 100, 1, sysdate, null, null, '');
-- 部门管理按钮
insert into sys_menu values('1017', '部门查询', '103', '1',  '', '', '', '0', '1','F', '1', '1', 'system:dept:query',          '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1018', '部门新增', '103', '2',  '', '', '', '0', '1','F', '1', '1', 'system:dept:add',            '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1019', '部门修改', '103', '3',  '', '', '', '0', '1','F', '1', '1', 'system:dept:update',           '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1020', '部门删除', '103', '4',  '', '', '', '0', '1','F', '1', '1', 'system:dept:delete',         '#', 100, 1, sysdate, null, null, '');
-- 岗位管理按钮
insert into sys_menu values('1021', '岗位查询', '104', '1',  '', '', '', '0', '1','F', '1', '1', 'system:post:query',          '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1022', '岗位新增', '104', '2',  '', '', '', '0', '1','F', '1', '1', 'system:post:add',            '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1023', '岗位修改', '104', '3',  '', '', '', '0', '1','F', '1', '1', 'system:post:update',           '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1024', '岗位删除', '104', '4',  '', '', '', '0', '1','F', '1', '1', 'system:post:delete',         '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1025', '岗位导出', '104', '5',  '', '', '', '0', '1','F', '1', '1', 'system:post:export',         '#', 100, 1, sysdate, null, null, '');
-- 字典管理按钮
insert into sys_menu values('1026', '字典查询', '105', '1', '#', '', '', '0', '1','F', '1', '1', 'system:dict:query',          '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1027', '字典新增', '105', '2', '#', '', '', '0', '1','F', '1', '1', 'system:dict:add',            '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1028', '字典修改', '105', '3', '#', '', '', '0', '1','F', '1', '1', 'system:dict:update',           '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1029', '字典删除', '105', '4', '#', '', '', '0', '1','F', '1', '1', 'system:dict:delete',         '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1030', '字典导出', '105', '5', '#', '', '', '0', '1','F', '1', '1', 'system:dict:export',         '#', 100, 1, sysdate, null, null, '');
-- 参数设置按钮
insert into sys_menu values('1031', '参数查询', '106', '1', '#', '', '', '0', '1','F', '1', '1', 'system:config:query',        '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1032', '参数新增', '106', '2', '#', '', '', '0', '1','F', '1', '1', 'system:config:add',          '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1033', '参数修改', '106', '3', '#', '', '', '0', '1','F', '1', '1', 'system:config:update',         '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1034', '参数删除', '106', '4', '#', '', '', '0', '1','F', '1', '1', 'system:config:delete',       '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1035', '参数导出', '106', '5', '#', '', '', '0', '1','F', '1', '1', 'system:config:export',       '#', 100, 1, sysdate, null, null, '');
-- 通知公告按钮
insert into sys_menu values('1036', '公告查询', '107', '1', '#', '', '', '0', '1','F', '1', '1', 'system:notice:query',        '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1037', '公告新增', '107', '2', '#', '', '', '0', '1','F', '1', '1', 'system:notice:add',          '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1038', '公告修改', '107', '3', '#', '', '', '0', '1','F', '1', '1', 'system:notice:update',         '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1039', '公告删除', '107', '4', '#', '', '', '0', '1','F', '1', '1', 'system:notice:delete',       '#', 100, 1, sysdate, null, null, '');
-- 操作日志按钮
insert into sys_menu values('1040', '操作查询', '500', '1', '#', '', '', '0', '1','F', '1', '1', 'monitor:operLog:query',      '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1041', '操作删除', '500', '2', '#', '', '', '0', '1','F', '1', '1', 'monitor:operLog:delete',     '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1042', '日志导出', '500', '4', '#', '', '', '0', '1','F', '1', '1', 'monitor:operLog:export',     '#', 100, 1, sysdate, null, null, '');
-- 登录日志按钮
insert into sys_menu values('1043', '登录查询', '501', '1', '#', '', '', '0', '1','F', '1', '1', 'monitor:loginLog:query',   '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1044', '登录删除', '501', '2', '#', '', '', '0', '1','F', '1', '1', 'monitor:loginLog:delete',  '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1045', '日志导出', '501', '3', '#', '', '', '0', '1','F', '1', '1', 'monitor:loginLog:export',  '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1050', '账户解锁', '501', '4', '#', '', '', '0', '1','F', '1', '1', 'monitor:loginLog:unlock',  '#', 100, 1, sysdate, null, null, '');
-- 错误日志按钮
insert into sys_menu values('1645', '错误日志查询', '1644', '1', '#', '', '', '0', '1','F', '1', '1', 'monitor:errorLog:query',  '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1646', '错误日志更新', '1644', '2', '#', '', '', '0', '1','F', '1', '1', 'monitor:errorLog:update', '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1647', '错误日志删除', '1644', '3', '#', '', '', '0', '1','F', '1', '1', 'monitor:errorLog:delete', '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1648', '错误日志导出', '1644', '4', '#', '', '', '0', '1','F', '1', '1', 'monitor:errorLog:export', '#', 100, 1, sysdate, null, null, '');
-- 在线用户按钮
insert into sys_menu values('1046', '在线查询', '109', '1', '#', '', '', '0', '1','F', '1', '1', 'monitor:online:query',       '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1047', '批量强退', '109', '2', '#', '', '', '0', '1','F', '1', '1', 'monitor:online:batchLogout', '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1048', '单条强退', '109', '3', '#', '', '', '0', '1','F', '1', '1', 'monitor:online:forceLogout', '#', 100, 1, sysdate, null, null, '');
-- 代码生成按钮
insert into sys_menu values('1055', '生成查询', '115', '1', '#', '', '', '0', '1','F', '1', '1', 'tool:gen:query',             '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1056', '生成修改', '115', '2', '#', '', '', '0', '1','F', '1', '1', 'tool:gen:update',              '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1057', '生成删除', '115', '3', '#', '', '', '0', '1','F', '1', '1', 'tool:gen:delete',            '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1058', '导入代码', '115', '2', '#', '', '', '0', '1','F', '1', '1', 'tool:gen:import',            '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1059', '预览代码', '115', '4', '#', '', '', '0', '1','F', '1', '1', 'tool:gen:preview',           '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1060', '生成代码', '115', '5', '#', '', '', '0', '1','F', '1', '1', 'tool:gen:code',              '#', 100, 1, sysdate, null, null, '');
-- oss相关按钮
insert into sys_menu values('1600', '文件查询', '118', '1', '#', '', '', '0', '1','F', '1', '1', 'system:oss:query',        '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1601', '文件上传', '118', '2', '#', '', '', '0', '1','F', '1', '1', 'system:oss:upload',       '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1602', '文件下载', '118', '3', '#', '', '', '0', '1','F', '1', '1', 'system:oss:download',     '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1603', '文件删除', '118', '4', '#', '', '', '0', '1','F', '1', '1', 'system:oss:delete',       '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1620', '配置列表', '118', '5', '#', '', '', '0', '1','F', '1', '1', 'system:ossConfig:view',   '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1621', '配置添加', '118', '6', '#', '', '', '0', '1','F', '1', '1', 'system:ossConfig:add',    '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1622', '配置编辑', '118', '7', '#', '', '', '0', '1','F', '1', '1', 'system:ossConfig:update',   '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1623', '配置删除', '118', '8', '#', '', '', '0', '1','F', '1', '1', 'system:ossConfig:delete', '#', 100, 1, sysdate, null, null, '');
-- OSS目录权限按钮
insert into sys_menu values('1630', '目录查询', '118', '9', '#', '', '', '0', '1', 'F', '1', '1', 'system:ossDirectory:query', '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1631', '目录新增', '118', '10', '#', '', '', '0', '1', 'F', '1', '1', 'system:ossDirectory:add', '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1632', '目录修改', '118', '11', '#', '', '', '0', '1', 'F', '1', '1', 'system:ossDirectory:update', '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1633', '目录删除', '118', '12', '#', '', '', '0', '1', 'F', '1', '1', 'system:ossDirectory:delete', '#', 100, 1, sysdate, null, null, '');
-- 租户管理相关按钮
insert into sys_menu values('1606', '租户查询', '121', '1', '#', '', '', '0', '1','F', '1', '1', 'system:tenant:query',   '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1607', '租户新增', '121', '2', '#', '', '', '0', '1','F', '1', '1', 'system:tenant:add',     '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1608', '租户修改', '121', '3', '#', '', '', '0', '1','F', '1', '1', 'system:tenant:update',    '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1609', '租户删除', '121', '4', '#', '', '', '0', '1','F', '1', '1', 'system:tenant:delete',  '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1610', '租户导出', '121', '5', '#', '', '', '0', '1','F', '1', '1', 'system:tenant:export',  '#', 100, 1, sysdate, null, null, '');
-- 租户套餐相关按钮
insert into sys_menu values('1611', '租户套餐查询', '122', '1', '#', '', '', '0', '1','F', '1', '1', 'system:tenantPackage:query',   '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1612', '租户套餐新增', '122', '2', '#', '', '', '0', '1','F', '1', '1', 'system:tenantPackage:add',     '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1613', '租户套餐修改', '122', '3', '#', '', '', '0', '1','F', '1', '1', 'system:tenantPackage:update',    '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1614', '租户套餐删除', '122', '4', '#', '', '', '0', '1','F', '1', '1', 'system:tenantPackage:delete',  '#', 100, 1, sysdate, null, null, '');
insert into sys_menu values('1615', '租户套餐导出', '122', '5', '#', '', '', '0', '1','F', '1', '1', 'system:tenantPackage:export',  '#', 100, 1, sysdate, null, null, '');
INSERT INTO sys_menu VALUES ('1120', '开放平台', '1', '8', 'openApi', '', '', '0', '0', 'M', '1', '1', '', 'key', 103, 0, SYSDATE, NULL, NULL, 'API开放平台管理');
INSERT INTO sys_menu VALUES ('1121', 'API密钥', '1120', '1', 'apiKey', 'system/openApi/apiKey/apiKey', '', '0', '1', 'C', '1', '1', 'system:apiKey:view', 'password', 103, 0, SYSDATE, NULL, NULL, 'API密钥管理');
INSERT INTO sys_menu VALUES ('1122', 'API密钥查询', '1121', '1', '', '', '', '0', '1', 'F', '1', '1', 'system:apiKey:query', '#', 103, 0, SYSDATE, NULL, NULL, '');
INSERT INTO sys_menu VALUES ('1123', 'API密钥新增', '1121', '2', '', '', '', '0', '1', 'F', '1', '1', 'system:apiKey:add', '#', 103, 0, SYSDATE, NULL, NULL, '');
INSERT INTO sys_menu VALUES ('1124', 'API密钥修改', '1121', '3', '', '', '', '0', '1', 'F', '1', '1', 'system:apiKey:update', '#', 103, 0, SYSDATE, NULL, NULL, '');
INSERT INTO sys_menu VALUES ('1125', 'API密钥删除', '1121', '4', '', '', '', '0', '1', 'F', '1', '1', 'system:apiKey:delete', '#', 103, 0, SYSDATE, NULL, NULL, '');
INSERT INTO sys_menu VALUES ('1126', 'API密钥导出', '1121', '5', '', '', '', '0', '1', 'F', '1', '1', 'system:apiKey:export', '#', 103, 0, SYSDATE, NULL, NULL, '');

-- 6、用户和角色关联表  用户N-1角色
create table sys_user_role (
  user_id  number(20)  not null,
  role_id  number(20)  not null
);

alter table sys_user_role add constraint pk_sys_user_role primary key (user_id, role_id);

comment on table  sys_user_role              is '用户和角色关联表';
comment on column sys_user_role.user_id      is '用户ID';
comment on column sys_user_role.role_id      is '角色ID';

-- 初始化-用户和角色关联表数据
insert into sys_user_role values ('1', '1');
insert into sys_user_role values ('3', '3');
insert into sys_user_role values ('4', '3');

-- 7、角色和菜单关联表  角色1-N菜单
create table sys_role_menu (
  role_id  number(20)  not null,
  menu_id  number(20)  not null
);

alter table sys_role_menu add constraint pk_sys_role_menu primary key (role_id, menu_id);

comment on table  sys_role_menu              is '角色和菜单关联表';
comment on column sys_role_menu.role_id      is '角色ID';
comment on column sys_role_menu.menu_id      is '菜单ID';

-- 初始化-角色和菜单关联表数据
insert into sys_role_menu values ('3', '1601');

-- 8、角色和部门关联表  角色1-N部门
create table sys_role_dept (
  role_id  number(20)  not null,
  dept_id  number(20)  not null
);

alter table sys_role_dept add constraint pk_sys_role_dept primary key (role_id, dept_id);

comment on table  sys_role_dept              is '角色和部门关联表';
comment on column sys_role_dept.role_id      is '角色ID';
comment on column sys_role_dept.dept_id      is '部门ID';


-- 9、用户与岗位关联表  用户1-N岗位
create table sys_user_post (
  user_id number(20)  not null,
  post_id number(20)  not null
);

alter table sys_user_post add constraint pk_sys_user_post primary key (user_id, post_id);

comment on table  sys_user_post              is '用户与岗位关联表';
comment on column sys_user_post.user_id      is '用户ID';
comment on column sys_user_post.post_id      is '岗位ID';

-- 初始化-用户与岗位关联表数据
insert into sys_user_post values ('1', '1');

-- 10、操作日志
create table sys_oper_log (
  oper_id           number(20)      not null,
  tenant_id         varchar2(20)    default '000000',
  title             varchar2(50)    default '',
  oper_type         varchar2(8)     default null,
  method            varchar2(100)   default '',
  request_method    varchar2(10)     default '',
  operator_type     varchar2(20)     default '',
  oper_name         varchar2(50)    default '',
  dept_name         varchar2(50)    default '',
  oper_url          varchar2(255)   default '',
  oper_ip           varchar2(128)   default '',
  oper_location     varchar2(255)   default '',
  oper_param        clob            default null,
  json_result       clob            default null,
  status            char(1)         default '1',
  error_msg         clob            default null,
  oper_time         date,
  cost_time         number(20)      default 0
);

alter table sys_oper_log add constraint pk_sys_oper_log primary key (oper_id);
create index idx_sys_oper_log_bt on sys_oper_log (oper_type);
create index idx_sys_oper_log_s on sys_oper_log (status);
create index idx_sys_oper_log_ot on sys_oper_log (oper_time);

comment on table  sys_oper_log                is '操作日志';
comment on column sys_oper_log.oper_id        is '日志主键';
comment on column sys_oper_log.tenant_id      is '租户id';
comment on column sys_oper_log.title          is '模块标题';
comment on column sys_oper_log.oper_type      is '操作类型';
comment on column sys_oper_log.method         is '方法名称';
comment on column sys_oper_log.request_method is '请求方式';
comment on column sys_oper_log.operator_type  is '操作用户类别';
comment on column sys_oper_log.oper_name      is '操作人员';
comment on column sys_oper_log.dept_name      is '部门名称';
comment on column sys_oper_log.oper_url       is '请求URL';
comment on column sys_oper_log.oper_ip        is '主机地址';
comment on column sys_oper_log.oper_location  is '操作地点';
comment on column sys_oper_log.oper_param     is '请求参数';
comment on column sys_oper_log.json_result    is '返回参数';
comment on column sys_oper_log.status         is '操作结果';
comment on column sys_oper_log.error_msg      is '错误消息';
comment on column sys_oper_log.oper_time      is '操作时间';
comment on column sys_oper_log.cost_time      is '消耗时间';


-- 11、字典类型表
create table sys_dict_type (
  dict_id           number(20)      not null,
  tenant_id         varchar2(20)    default '000000',
  dict_name         varchar2(100)   default '',
  dict_type         varchar2(100)   default '',
  is_system         char(1)         default '0',
  status            char(1)         default '1',
  create_dept       number(20)      default null,
  create_by         number(20)      default null,
  create_time       date,
  update_by         number(20)      default null,
  update_time       date,
  remark            varchar2(500)   default null
);

alter table sys_dict_type add constraint pk_sys_dict_type primary key (dict_id);
create unique index sys_dict_type_index1 on sys_dict_type (tenant_id, dict_type);

comment on table  sys_dict_type               is '字典类型表';
comment on column sys_dict_type.dict_id       is '字典主键';
comment on column sys_dict_type.tenant_id     is '租户id';
comment on column sys_dict_type.dict_name     is '字典名称';
comment on column sys_dict_type.dict_type     is '字典类型';
comment on column sys_dict_type.is_system     is '是否系统级';
comment on column sys_dict_type.status        is '状态';
comment on column sys_dict_type.create_dept   is '创建部门';
comment on column sys_dict_type.create_by     is '创建者';
comment on column sys_dict_type.create_time   is '创建时间';
comment on column sys_dict_type.update_by     is '更新者';
comment on column sys_dict_type.update_time   is '更新时间';
comment on column sys_dict_type.remark        is '备注';

-- 字典类型数据
insert into sys_dict_type values(1, '000000', '用户性别', 'sys_user_gender', '1', '1', 100, 1, sysdate, null, null, '用户性别列表');
insert into sys_dict_type values(2, '000000', '数据权限', 'sys_data_scope', '1', '1', 100, 1, sysdate, null, null, '数据权限类型列表');
insert into sys_dict_type values(3, '000000', '启用状态', 'sys_enable_status', '1', '1', 100, 1, sysdate, null, null, '启用状态列表');
insert into sys_dict_type values(4, '000000', '显示设置', 'sys_display_setting', '1', '1', 100, 1, sysdate, null, null, '显示设置列表');
insert into sys_dict_type values(5, '000000', '逻辑标志', 'sys_boolean_flag', '1', '1', 100, 1, sysdate, null, null, '逻辑标志列表');
insert into sys_dict_type values(6, '000000', '操作类型', 'sys_oper_type', '1', '1', 100, 1, sysdate, null, null, '操作类型列表');
insert into sys_dict_type values(7, '000000', '操作结果', 'sys_oper_result', '1', '1', 100, 1, sysdate, null, null, '操作结果列表');
insert into sys_dict_type values(8, '000000', '审核状态', 'sys_audit_status', '1', '1', 100, 1, sysdate, null, null, '审核状态列表');
insert into sys_dict_type values(9, '000000', '文件类型', 'sys_file_type', '1', '1', 100, 1, sysdate, null, null, '文件类型列表');
insert into sys_dict_type values(10, '000000', '消息类型', 'sys_message_type', '1', '1', 100, 1, sysdate, null, null, '消息类型列表');
insert into sys_dict_type values(11, '000000', '通知类型', 'sys_notice_type', '1', '1', 100, 1, sysdate, null, null, '通知类型列表');
insert into sys_dict_type values(12, '000000', '通知状态', 'sys_notice_status', '1', '1', 100, 1, sysdate, null, null, '通知状态列表');
insert into sys_dict_type values(20, '000000', '平台类型', 'sys_platform_type', '1', '1', 100, 1, sysdate, null, null, '平台类型列表');
insert into sys_dict_type values(21, '000000', '支付方式', 'sys_payment_method', '1', '1', 100, 1, sysdate, null, null, '支付方式列表');
insert into sys_dict_type values(22, '000000', '订单状态', 'sys_order_status', '1', '1', 100, 1, sysdate, null, null, '订单状态列表');
insert into sys_dict_type values(23, '000000', '商品规格类型', 'm_goods_spec_type', '0', '1', 100, 1, sysdate, null, null, '商品规格类型(单规格/多规格)');

-- 12、字典数据表
create table sys_dict_data (
  dict_data_id        number(20)      not null,
  tenant_id        varchar2(20)    default '000000',
  dict_sort        number(4)       default 0,
  dict_label       varchar2(100)   default '',
  dict_value       varchar2(100)   default '',
  dict_type        varchar2(100)   default '',
  css_class        varchar2(100)   default null,
  list_class       varchar2(100)   default null,
  is_default       char(1)         default '0',
  status            char(1)         default '1',
  create_dept      number(20)      default null,
  create_by        number(20)      default null,
  create_time      date,
  update_by        number(20)      default null,
  update_time      date,
  remark           varchar2(500)   default null
);

alter table sys_dict_data add constraint pk_sys_dict_data primary key (dict_data_id);

comment on table  sys_dict_data               is '字典数据表';
comment on column sys_dict_data.dict_data_id  is '字典主键';
comment on column sys_dict_data.tenant_id     is '租户id';
comment on column sys_dict_data.dict_sort     is '字典排序';
comment on column sys_dict_data.dict_label    is '字典标签';
comment on column sys_dict_data.dict_value    is '字典键值';
comment on column sys_dict_data.dict_type     is '字典类型';
comment on column sys_dict_data.css_class     is '样式属性（其他样式扩展）';
comment on column sys_dict_data.list_class    is '表格回显样式';
comment on column sys_dict_data.is_default    is '是否默认';
comment on column sys_dict_data.status        is '状态';
comment on column sys_dict_data.create_dept   is '创建部门';
comment on column sys_dict_data.create_by     is '创建者';
comment on column sys_dict_data.create_time   is '创建时间';
comment on column sys_dict_data.update_by     is '更新者';
comment on column sys_dict_data.update_time   is '更新时间';
comment on column sys_dict_data.remark        is '备注';

-- 用户性别
insert into sys_dict_data values(1, '000000', 0, '女', '0', 'sys_user_gender', '', 'success', '1', '1', 100, 1, sysdate, null, null, '性别:女');
insert into sys_dict_data values(2, '000000', 1, '男', '1', 'sys_user_gender', '', 'primary', '0', '1', 100, 1, sysdate, null, null, '性别:男');
insert into sys_dict_data values(3, '000000', 2, '未知', '2', 'sys_user_gender', '', 'info', '0', '1', 100, 1, sysdate, null, null, '性别:未知');

-- 数据权限
insert into sys_dict_data values(4, '000000', 1, '全部数据权限', '1', 'sys_data_scope', '', 'primary', '0', '1', 100, 1, sysdate, null, null, '数据权限:全部数据');
insert into sys_dict_data values(5, '000000', 2, '自定义数据权限', '2', 'sys_data_scope', '', 'info', '0', '1', 100, 1, sysdate, null, null, '数据权限:自定义数据');
insert into sys_dict_data values(6, '000000', 3, '部门数据权限', '3', 'sys_data_scope', '', 'success', '0', '1', 100, 1, sysdate, null, null, '数据权限:本部门数据');
insert into sys_dict_data values(7, '000000', 4, '部门及以下数据权限', '4', 'sys_data_scope', '', 'warning', '0', '1', 100, 1, sysdate, null, null, '数据权限:部门及以下数据');
insert into sys_dict_data values(8, '000000', 5, '仅本人数据权限', '5', 'sys_data_scope', '', 'danger', '0', '1', 100, 1, sysdate, null, null, '数据权限:仅本人数据');
insert into sys_dict_data values(9, '000000', 6, '部门及以下或本人数据', '6', 'sys_data_scope', '', 'info', '0', '1', 100, 1, sysdate, null, null, '数据权限:部门及以下或本人数据');

-- 启用状态
insert into sys_dict_data values(10, '000000', 1, '禁用', '0', 'sys_enable_status', '', 'danger', '0', '1', 100, 1, sysdate, null, null, '启用状态:禁用');
insert into sys_dict_data values(11, '000000', 2, '启用', '1', 'sys_enable_status', '', 'primary', '1', '1', 100, 1, sysdate, null, null, '启用状态:启用');

-- 显示设置
insert into sys_dict_data values(12, '000000', 1, '隐藏', '0', 'sys_display_setting', '', 'danger', '0', '1', 100, 1, sysdate, null, null, '显示设置:隐藏');
insert into sys_dict_data values(13, '000000', 2, '显示', '1', 'sys_display_setting', '', 'primary', '1', '1', 100, 1, sysdate, null, null, '显示设置:显示');

-- 逻辑标志
insert into sys_dict_data values(14, '000000', 1, '否', '0', 'sys_boolean_flag', '', 'danger', '0', '1', 100, 1, sysdate, null, null, '逻辑标志:否');
insert into sys_dict_data values(15, '000000', 2, '是', '1', 'sys_boolean_flag', '', 'primary', '1', '1', 100, 1, sysdate, null, null, '逻辑标志:是');

-- 操作类型
insert into sys_dict_data values(16, '000000', 1, '新增', '1', 'sys_oper_type', '', 'primary', '0', '1', 100, 1, sysdate, null, null, '操作:新增');
insert into sys_dict_data values(17, '000000', 2, '修改', '2', 'sys_oper_type', '', 'info', '0', '1', 100, 1, sysdate, null, null, '操作:修改');
insert into sys_dict_data values(18, '000000', 3, '删除', '3', 'sys_oper_type', '', 'danger', '0', '1', 100, 1, sysdate, null, null, '操作:删除');
insert into sys_dict_data values(19, '000000', 4, '授权', '4', 'sys_oper_type', '', 'success', '0', '1', 100, 1, sysdate, null, null, '操作:授权');
insert into sys_dict_data values(20, '000000', 5, '导出', '5', 'sys_oper_type', '', 'warning', '0', '1', 100, 1, sysdate, null, null, '操作:导出');
insert into sys_dict_data values(21, '000000', 6, '导入', '6', 'sys_oper_type', '', 'warning', '0', '1', 100, 1, sysdate, null, null, '操作:导入');
insert into sys_dict_data values(22, '000000', 7, '强退', '7', 'sys_oper_type', '', 'danger', '0', '1', 100, 1, sysdate, null, null, '操作:强退');
insert into sys_dict_data values(23, '000000', 8, '生成代码', '8', 'sys_oper_type', '', 'success', '0', '1', 100, 1, sysdate, null, null, '操作:生成代码');
insert into sys_dict_data values(24, '000000', 9, '清空数据', '9', 'sys_oper_type', '', 'danger', '0', '1', 100, 1, sysdate, null, null, '操作:清空数据');
insert into sys_dict_data values(25, '000000', 99, '其他', '99', 'sys_oper_type', '', 'info', '0', '1', 100, 1, sysdate, null, null, '操作:其他');

-- 操作结果
insert into sys_dict_data values(26, '000000', 1, '成功', '1', 'sys_oper_result', '', 'success', '0', '1', 100, 1, sysdate, null, null, '操作结果:成功');
insert into sys_dict_data values(27, '000000', 2, '失败', '0', 'sys_oper_result', '', 'danger', '0', '1', 100, 1, sysdate, null, null, '操作结果:失败');

-- 审核状态
insert into sys_dict_data values(28, '000000', 0, '待审核', '0', 'sys_audit_status', '', 'info', '1', '1', 100, 1, sysdate, null, null, '审核状态:待审核');
insert into sys_dict_data values(29, '000000', 1, '通过', '1', 'sys_audit_status', '', 'success', '0', '1', 100, 1, sysdate, null, null, '审核状态:通过');
insert into sys_dict_data values(30, '000000', 2, '驳回', '2', 'sys_audit_status', '', 'warning', '0', '1', 100, 1, sysdate, null, null, '审核状态:驳回');
insert into sys_dict_data values(31, '000000', 3, '拒绝', '3', 'sys_audit_status', '', 'danger', '0', '1', 100, 1, sysdate, null, null, '审核状态:拒绝');

-- 文件类型
insert into sys_dict_data values(32, '000000', 0, '图片', 'image', 'sys_file_type', '', 'primary', '0', '1', 100, 1, sysdate, null, null, '文件类型:图片');
insert into sys_dict_data values(33, '000000', 1, '文档', 'document', 'sys_file_type', '', 'success', '0', '1', 100, 1, sysdate, null, null, '文件类型:文档');
insert into sys_dict_data values(34, '000000', 2, '视频', 'video', 'sys_file_type', '', 'info', '0', '1', 100, 1, sysdate, null, null, '文件类型:视频');
insert into sys_dict_data values(35, '000000', 3, '音频', 'audio', 'sys_file_type', '', 'warning', '0', '1', 100, 1, sysdate, null, null, '文件类型:音频');
insert into sys_dict_data values(36, '000000', 4, '压缩包', 'archive', 'sys_file_type', '', 'danger', '0', '1', 100, 1, sysdate, null, null, '文件类型:压缩包');
insert into sys_dict_data values(37, '000000', 5, '其他', 'other', 'sys_file_type', '', 'info', '0', '1', 100, 1, sysdate, null, null, '文件类型:其他');

-- 消息类型
insert into sys_dict_data values(38, '000000', 0, '系统通知', 'system', 'sys_message_type', '', 'danger', '0', '1', 100, 1, sysdate, null, null, '消息类型:系统通知');
insert into sys_dict_data values(39, '000000', 1, '活动通知', 'activity', 'sys_message_type', '', 'success', '0', '1', 100, 1, sysdate, null, null, '消息类型:活动通知');
insert into sys_dict_data values(40, '000000', 2, '审核通知', 'audit', 'sys_message_type', '', 'info', '0', '1', 100, 1, sysdate, null, null, '消息类型:审核通知');
insert into sys_dict_data values(41, '000000', 3, '账户通知', 'account', 'sys_message_type', '', 'warning', '0', '1', 100, 1, sysdate, null, null, '消息类型:账户通知');
insert into sys_dict_data values(42, '000000', 4, '私信', 'private', 'sys_message_type', '', 'primary', '0', '1', 100, 1, sysdate, null, null, '消息类型:私信');

-- 通知类型
insert into sys_dict_data values(43, '000000', 1, '通知', '1', 'sys_notice_type', '', 'warning', '1', '1', 100, 1, sysdate, null, null, '通知类型:通知');
insert into sys_dict_data values(44, '000000', 2, '公告', '2', 'sys_notice_type', '', 'success', '0', '1', 100, 1, sysdate, null, null, '通知类型:公告');

-- 通知状态
insert into sys_dict_data values(45, '000000', 1, '立即发送', '1', 'sys_notice_status', '', 'success', '1', '1', 100, 1, sysdate, null, null, '通知状态:立即发送');
insert into sys_dict_data values(46, '000000', 2, '保存为草稿', '0', 'sys_notice_status', '', 'danger', '0', '1', 100, 1, sysdate, null, null, '通知状态:保存为草稿');

-- ==================== 移动端相关字典数据 ====================

-- 平台类型
insert into sys_dict_data values(100, '000000', 0, '微信小程序', 'mp-weixin', 'sys_platform_type', '', 'primary', '0', '1', 100, 1, sysdate, null, null, '平台:微信小程序');
insert into sys_dict_data values(101, '000000', 10, '微信公众号', 'mp-official-account', 'sys_platform_type', '', 'success', '0', '1', 100, 1, sysdate, null, null, '平台:微信公众号');
insert into sys_dict_data values(102, '000000', 20, 'QQ小程序', 'mp-qq', 'sys_platform_type', '', 'info', '0', '1', 100, 1, sysdate, null, null, '平台:QQ小程序');
insert into sys_dict_data values(103, '000000', 30, '支付宝小程序', 'mp-alipay', 'sys_platform_type', '', 'warning', '0', '1', 100, 1, sysdate, null, null, '平台:支付宝小程序');
insert into sys_dict_data values(104, '000000', 40, '京东小程序', 'mp-jd', 'sys_platform_type', '', 'danger', '0', '1', 100, 1, sysdate, null, null, '平台:京东小程序');
insert into sys_dict_data values(105, '000000', 50, '快手小程序', 'mp-kuaishou', 'sys_platform_type', '', 'info', '0', '1', 100, 1, sysdate, null, null, '平台:快手小程序');
insert into sys_dict_data values(106, '000000', 60, '飞书小程序', 'mp-lark', 'sys_platform_type', '', 'primary', '0', '1', 100, 1, sysdate, null, null, '平台:飞书小程序');
insert into sys_dict_data values(107, '000000', 70, '百度小程序', 'mp-baidu', 'sys_platform_type', '', 'success', '0', '1', 100, 1, sysdate, null, null, '平台:百度小程序');
insert into sys_dict_data values(108, '000000', 80, '头条小程序', 'mp-toutiao', 'sys_platform_type', '', 'warning', '0', '1', 100, 1, sysdate, null, null, '平台:头条/抖音小程序');
insert into sys_dict_data values(109, '000000', 90, '小红书小程序', 'mp-xhs', 'sys_platform_type', '', 'danger', '0', '1', 100, 1, sysdate, null, null, '平台:小红书小程序');

-- 支付方式
insert into sys_dict_data values(110, '000000', 0, '微信支付', 'wechat', 'sys_payment_method', '', 'success', '0', '1', 100, 1, sysdate, null, null, '支付方式:微信支付');
insert into sys_dict_data values(111, '000000', 1, '支付宝', 'alipay', 'sys_payment_method', '', 'primary', '0', '1', 100, 1, sysdate, null, null, '支付方式:支付宝');
insert into sys_dict_data values(112, '000000', 2, '银联', 'unionpay', 'sys_payment_method', '', 'danger', '0', '1', 100, 1, sysdate, null, null, '支付方式:银联');
insert into sys_dict_data values(113, '000000', 3, '余额支付', 'balance', 'sys_payment_method', '', 'info', '0', '1', 100, 1, sysdate, null, null, '支付方式:余额支付');
insert into sys_dict_data values(114, '000000', 4, '积分抵扣', 'points', 'sys_payment_method', '', 'warning', '0', '1', 100, 1, sysdate, null, null, '支付方式:积分抵扣');

-- 订单状态
insert into sys_dict_data values(120, '000000', 0, '待支付', 'pending', 'sys_order_status', '', 'info', '0', '1', 100, 1, sysdate, null, null, '订单状态:待支付');
insert into sys_dict_data values(121, '000000', 1, '已支付', 'paid', 'sys_order_status', '', 'primary', '0', '1', 100, 1, sysdate, null, null, '订单状态:已支付');
insert into sys_dict_data values(122, '000000', 2, '已发货', 'delivered', 'sys_order_status', '', 'warning', '0', '1', 100, 1, sysdate, null, null, '订单状态:已发货');
insert into sys_dict_data values(123, '000000', 3, '已完成', 'completed', 'sys_order_status', '', 'success', '0', '1', 100, 1, sysdate, null, null, '订单状态:已完成');
insert into sys_dict_data values(124, '000000', 4, '已取消', 'cancelled', 'sys_order_status', '', 'info', '0', '1', 100, 1, sysdate, null, null, '订单状态:已取消');
insert into sys_dict_data values(125, '000000', 5, '已退款', 'refunded', 'sys_order_status', '', 'danger', '0', '1', 100, 1, sysdate, null, null, '订单状态:已退款');
insert into sys_dict_data values(130, '000000', 1, '单规格', '0', 'm_goods_spec_type', '', 'success', '0', '1', 100, 1, sysdate, null, null, '单一规格商品');
insert into sys_dict_data values(131, '000000', 2, '多规格', '1', 'm_goods_spec_type', '', 'primary', '0', '1', 100, 1, sysdate, null, null, '多规格商品');

-- 13、参数配置表

create table sys_config (
  config_id         number(20)     not null,
  tenant_id         varchar2(20)   default '000000',
  config_name       varchar2(100)  default '',
  config_key        varchar2(100)  default '',
  config_value      varchar2(500)  default '',
  config_type       char(1)        default '0',
  create_dept       number(20)     default null,
  create_by         number(20)     default null,
  create_time       date,
  update_by         number(20)     default null,
  update_time       date,
  remark            varchar2(500)  default null
);

alter table sys_config add constraint pk_sys_config primary key (config_id);

comment on table  sys_config               is '参数配置表';
comment on column sys_config.config_id     is '参数主键';
comment on column sys_config.tenant_id     is '租户id';
comment on column sys_config.config_name   is '参数名称';
comment on column sys_config.config_key    is '参数键名';
comment on column sys_config.config_value  is '参数键值';
comment on column sys_config.config_type   is '系统内置（1是 0否';
comment on column sys_config.create_dept   is '创建部门';
comment on column sys_config.create_by     is '创建者';
comment on column sys_config.create_time   is '创建时间';
comment on column sys_config.update_by     is '更新者';
comment on column sys_config.update_time   is '更新时间';
comment on column sys_config.remark        is '备注';

insert into sys_config values(1, '000000', '登录验证-验证码开关', 'system.security.captcha-enabled', 'true', '1', 200, 1, sysdate, null, null, '登录时是否开启验证码功能（true开启，false关闭）');
insert into sys_config values(2, '000000', '用户管理-初始密码', 'system.user.initial-password', '123456', '1', 100, 1, sysdate, null, null, '新用户创建时的默认初始密码');
insert into sys_config values(3, '000000', '账号自助-注册功能开关', 'system.account.register-enabled', 'false', '1', 300, 1, sysdate, null, null, '是否允许用户自助注册账号（true开启，false关闭）');
insert into sys_config values(4, '000000', 'OSS功能-预览列表开关', 'system.oss.preview-enabled', 'true', '1', 300, 1, sysdate, null, null, '是否开启OSS资源预览列表功能（true开启，false关闭）');
insert into sys_config values(5, '000000', '社交登录-自动注册开关', 'system.social.auto-register-enabled', 'false', '1', 300, 1, sysdate, null, null, '是否允许社交登录时自动注册账号（true开启false关闭）');

CREATE TABLE sys_api_key
(
    id                 NUMBER(20)    NOT NULL,
    tenant_id          VARCHAR2(20)  DEFAULT '000000',
    app_name           VARCHAR2(100) NOT NULL,
    app_key            VARCHAR2(64)  NOT NULL,
    app_secret         VARCHAR2(128) NOT NULL,
    user_id            NUMBER(20)    DEFAULT NULL,
    expire_time        DATE          DEFAULT NULL,
    status             CHAR(1)       DEFAULT '1',
    white_ips          VARCHAR2(500) DEFAULT NULL,
    call_count         NUMBER(20)    DEFAULT 0,
    last_call_time     DATE          DEFAULT NULL,
    create_dept        NUMBER(20)    DEFAULT NULL,
    create_by          NUMBER(20)    DEFAULT NULL,
    create_time        DATE,
    update_by          NUMBER(20)    DEFAULT NULL,
    update_time        DATE,
    remark             VARCHAR2(500) DEFAULT NULL,
    CONSTRAINT pk_sys_api_key PRIMARY KEY (id),
    CONSTRAINT uk_sys_api_key_app_key UNIQUE (app_key)
);

CREATE INDEX idx_sys_api_key_user_id ON sys_api_key(user_id);
CREATE INDEX idx_sys_api_key_status ON sys_api_key(status);

COMMENT ON TABLE sys_api_key IS 'API密钥管理表';
COMMENT ON COLUMN sys_api_key.id IS 'API密钥ID';
COMMENT ON COLUMN sys_api_key.tenant_id IS '租户id';
COMMENT ON COLUMN sys_api_key.app_name IS '应用名称';
COMMENT ON COLUMN sys_api_key.app_key IS 'AppKey(公开)';
COMMENT ON COLUMN sys_api_key.app_secret IS 'AppSecret';
COMMENT ON COLUMN sys_api_key.user_id IS '关联用户ID';
COMMENT ON COLUMN sys_api_key.expire_time IS '过期时间';
COMMENT ON COLUMN sys_api_key.status IS '状态(0停用 1正常)';
COMMENT ON COLUMN sys_api_key.white_ips IS 'IP白名单,逗号分隔';
COMMENT ON COLUMN sys_api_key.call_count IS '调用次数';
COMMENT ON COLUMN sys_api_key.last_call_time IS '最后调用时间';
COMMENT ON COLUMN sys_api_key.create_dept IS '创建部门';
COMMENT ON COLUMN sys_api_key.create_by IS '创建者';
COMMENT ON COLUMN sys_api_key.create_time IS '创建时间';
COMMENT ON COLUMN sys_api_key.update_by IS '更新者';
COMMENT ON COLUMN sys_api_key.update_time IS '更新时间';
COMMENT ON COLUMN sys_api_key.remark IS '备注';

-- 14、登录日志
create table sys_login_log (
  info_id         number(20)     not null,
  tenant_id       varchar2(20)   default '000000',
  user_id         number(20),
  user_name       varchar2(50)   default '',
  device_type     varchar2(32)   default '',
  ipaddr          varchar2(128)  default '',
  login_location  varchar2(255)  default '',
  browser         varchar2(50)   default '',
  os              varchar2(50)   default '',
  status          char(1)        default '1',
  msg             varchar2(255)  default '',
  login_time      date
);

alter table sys_login_log add constraint pk_sys_login_log primary key (info_id);
create index idx_sys_login_log_s on sys_login_log (status);
create index idx_sys_login_log_lt on sys_login_log (login_time);

comment on table  sys_login_log                is '登录日志';
comment on column sys_login_log.info_id        is '访问ID';
comment on column sys_login_log.tenant_id      is '租户id';
comment on column sys_login_log.user_id        is '用户id';
comment on column sys_login_log.user_name      is '登录账号';
comment on column sys_login_log.device_type    is '设备类型';
comment on column sys_login_log.ipaddr         is '登录IP地址';
comment on column sys_login_log.login_location is '登录地点';
comment on column sys_login_log.browser        is '浏览器类型';
comment on column sys_login_log.os             is '操作系统';
comment on column sys_login_log.status         is '登录状态';
comment on column sys_login_log.msg            is '提示消息';
comment on column sys_login_log.login_time     is '访问时间';


-- 17、通知公告表
CREATE TABLE sys_notice (
  notice_id         NUMBER(20)      NOT NULL,
  tenant_id         VARCHAR2(20)    DEFAULT '000000',
  notice_type       CHAR(1)         NOT NULL,
  target_config     CLOB            DEFAULT NULL,
  target_user_ids   CLOB            DEFAULT NULL,
  read_user_ids     CLOB            DEFAULT NULL,
  notice_title      VARCHAR2(50)    NOT NULL,
  notice_content    CLOB            DEFAULT NULL,
  status            CHAR(1)         DEFAULT '1',
  create_dept       NUMBER(20)      DEFAULT NULL,
  create_by         NUMBER(20)      DEFAULT NULL,
  create_time       DATE,
  update_by         NUMBER(20)      DEFAULT NULL,
  update_time       DATE,
  remark            VARCHAR2(255)   DEFAULT NULL
);

ALTER TABLE sys_notice ADD CONSTRAINT pk_sys_notice PRIMARY KEY (notice_id);

COMMENT ON TABLE  sys_notice                   IS '通知公告表';
COMMENT ON COLUMN sys_notice.notice_id         IS '公告ID';
COMMENT ON COLUMN sys_notice.tenant_id         IS '租户id';
COMMENT ON COLUMN sys_notice.notice_type       IS '公告类型（1通知 2公告）';
COMMENT ON COLUMN sys_notice.target_config     IS '推送配置JSON';
COMMENT ON COLUMN sys_notice.target_user_ids   IS '目标用户ID列表';
COMMENT ON COLUMN sys_notice.read_user_ids     IS '已读用户ID列表';
COMMENT ON COLUMN sys_notice.notice_title      IS '公告标题';
COMMENT ON COLUMN sys_notice.notice_content    IS '公告内容';
COMMENT ON COLUMN sys_notice.status            IS '公告状态';
COMMENT ON COLUMN sys_notice.create_dept       IS '创建部门';
COMMENT ON COLUMN sys_notice.create_by         IS '创建者';
COMMENT ON COLUMN sys_notice.create_time       IS '创建时间';
COMMENT ON COLUMN sys_notice.update_by         IS '更新者';
COMMENT ON COLUMN sys_notice.update_time       IS '更新时间';
COMMENT ON COLUMN sys_notice.remark            IS '备注';

-- 初始化-公告信息表数据
INSERT INTO sys_notice VALUES('1', '000000', '2', '{"type": "all"}', 1, NULL, '温馨提醒：2018-07-01 新版本发布啦', '新版本内容', '1', 100, 1, SYSDATE, NULL, NULL, '管理员');
INSERT INTO sys_notice VALUES('2', '000000', '1', '{"type": "all"}', 1, NULL, '维护通知：2018-07-01 系统凌晨维护', '维护内容', '1', 100, 1, SYSDATE, NULL, NULL, '管理员');

-- 18、代码生成业务表
create table sys_gen_table (
  table_id          number(20)       not null,
  data_name         varchar2(200)    default '',
  table_name        varchar2(200)    default '',
  table_comment     varchar2(500)    default '',
  sub_table_name    varchar2(64)     default null,
  sub_table_fk_name varchar2(64)     default null,
  class_name        varchar2(100)    default '',
  tpl_category      varchar2(200)    default 'crud',
  package_name      varchar2(100),
  module_name       varchar2(30),
  business_name     varchar2(30),
  function_name     varchar2(50),
  function_author   varchar2(50),
  gen_type          char(1)          default '0',
  gen_path          varchar2(200)    default '/',
  options           varchar2(1000),
  create_dept       number(20)       default null,
  create_by         number(20)       default null,
  create_time       date,
  update_by         number(20)       default null,
  update_time       date,
  remark            varchar2(500)    default null
);

alter table sys_gen_table add constraint pk_gen_table primary key (table_id);

comment on table  sys_gen_table                   is '代码生成业务表';
comment on column sys_gen_table.table_id          is '编号';
comment on column sys_gen_table.data_name         is '数据源名称';
comment on column sys_gen_table.table_name        is '表名称';
comment on column sys_gen_table.table_comment     is '表描述';
comment on column sys_gen_table.sub_table_name    is '关联子表的表名';
comment on column sys_gen_table.sub_table_fk_name is '子表关联的外键名';
comment on column sys_gen_table.class_name        is '实体类名称';
comment on column sys_gen_table.tpl_category      is '使用的模板（crud单表操作 tree树表操作）';
comment on column sys_gen_table.package_name      is '生成包路径';
comment on column sys_gen_table.module_name       is '生成模块名';
comment on column sys_gen_table.business_name     is '生成业务名';
comment on column sys_gen_table.function_name     is '生成功能名';
comment on column sys_gen_table.function_author   is '生成功能作者';
comment on column sys_gen_table.gen_type          is '生成代码方式（0zip压缩包 1自定义路径）';
comment on column sys_gen_table.gen_path          is '生成路径（不填默认项目路径）';
comment on column sys_gen_table.options           is '其它生成选项';
comment on column sys_gen_table.create_dept       is '创建部门';
comment on column sys_gen_table.create_by         is '创建者';
comment on column sys_gen_table.create_time       is '创建时间';
comment on column sys_gen_table.update_by         is '更新者';
comment on column sys_gen_table.update_time       is '更新时间';
comment on column sys_gen_table.remark            is '备注';


-- 19、代码生成业务表字段
create table sys_gen_table_column (
  column_id         number(20)      not null,
  table_id          number(20),
  column_name       varchar2(200),
  column_comment    varchar2(500),
  column_label      varchar2(200),
  column_type       varchar2(100),
  java_type         varchar2(500),
  java_field        varchar2(200),
  is_pk             char(1),
  is_increment      char(1),
  is_required       char(1),
  is_insert         char(1),
  is_edit           char(1),
  is_list           char(1),
  is_query          char(1),
  query_type        varchar2(200)    default 'EQ',
  html_type         varchar2(200),
  dict_type         varchar2(200)    default '',
  column_default    varchar2(100)    default '',
  sort              number(4),
  create_dept       number(20)       default null,
  create_by         number(20)       default null,
  create_time       date ,
  update_by         number(20)       default null,
  update_time       date
);

alter table sys_gen_table_column add constraint pk_sys_gen_table_column primary key (column_id);

comment on table  sys_gen_table_column                is '代码生成业务表字段';
comment on column sys_gen_table_column.column_id      is '编号';
comment on column sys_gen_table_column.table_id       is '归属表编号';
comment on column sys_gen_table_column.column_name    is '列名称';
comment on column sys_gen_table_column.column_comment is '列描述';
comment on column sys_gen_table_column.column_label   is '字段标签（用于显示）';
comment on column sys_gen_table_column.column_type    is '列类型';
comment on column sys_gen_table_column.java_type      is 'JAVA类型';
comment on column sys_gen_table_column.java_field     is 'JAVA字段名';
comment on column sys_gen_table_column.is_pk          is '是否主键（1是）';
comment on column sys_gen_table_column.is_increment   is '是否自增（1是）';
comment on column sys_gen_table_column.is_required    is '是否必填（1是）';
comment on column sys_gen_table_column.is_insert      is '是否为插入字段（1是）';
comment on column sys_gen_table_column.is_edit        is '是否编辑字段（1是）';
comment on column sys_gen_table_column.is_list        is '是否列表字段（1是）';
comment on column sys_gen_table_column.is_query       is '是否查询字段（1是）';
comment on column sys_gen_table_column.query_type     is '查询方式（等于、不等于、大于、小于、范围）';
comment on column sys_gen_table_column.html_type      is '显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）';
comment on column sys_gen_table_column.dict_type      is '字典类型';
comment on column sys_gen_table_column.column_default is '默认值';
comment on column sys_gen_table_column.sort           is '排序';
comment on column sys_gen_table_column.create_dept    is '创建部门';
comment on column sys_gen_table_column.create_by      is '创建者';
comment on column sys_gen_table_column.create_time    is '创建时间';
comment on column sys_gen_table_column.update_by      is '更新者';
comment on column sys_gen_table_column.update_time    is '更新时间';


-- OSS对象存储表
create table sys_oss (
  oss_id          number(20)     not null,
  tenant_id       varchar2(20)   default '000000',
  directory_id    number(20)     null,
  file_name       varchar2(255)  not null,
  original_name   varchar2(255)  not null,
  file_suffix     varchar2(10)   not null,
  file_size       number(20)     default null,
  url             varchar2(500)  not null,
  service         varchar2(20)   default 'minio' not null,
  ext1            varchar2(500)  default '',
  create_dept     number(20)     default null,
  create_by       number(20)     default null,
  create_time     date,
  update_by       number(20)     default null,
  update_time     date
);

alter table sys_oss add constraint pk_sys_oss primary key (oss_id);

comment on table sys_oss                    is 'OSS对象存储表';
comment on column sys_oss.oss_id            is '对象存储主键';
comment on column sys_oss.tenant_id         is '租户编码';
comment on column sys_oss.directory_id      is '所属目录ID';
comment on column sys_oss.file_name         is '文件名';
comment on column sys_oss.original_name     is '原名';
comment on column sys_oss.file_suffix       is '文件后缀名';
comment on column sys_oss.file_size         is '文件大小(字节)';
comment on column sys_oss.url               is 'URL地址';
comment on column sys_oss.service           is '服务商';
comment on column sys_oss.ext1              is '扩展字段';
comment on column sys_oss.create_dept       is '创建部门';
comment on column sys_oss.create_time       is '创建时间';
comment on column sys_oss.create_by         is '上传者';
comment on column sys_oss.update_time       is '更新时间';
comment on column sys_oss.update_by         is '更新者';


-- OSS对象存储动态配置表
create table sys_oss_config (
  oss_config_id   number(20)     not null,
  tenant_id       varchar2(20)   default '000000',
  config_key      varchar2(20)   not null,
  access_key      varchar2(255)  default '',
  secret_key      varchar2(255)  default '',
  bucket_name     varchar2(255)  default '',
  prefix          varchar2(255)  default '',
  endpoint        varchar2(255)  default '',
  domain          varchar2(255)  default '',
  is_https        char(1)        default '0',
  region          varchar2(255)  default '',
  access_policy   char(1)        default '1' not null,
  status          char(1)        default '1',
  ext1            varchar2(255)  default '',
  remark          varchar2(500)  default null,
  create_dept     number(20)     default null,
  create_by       number(20)     default null,
  create_time     date,
  update_by       number(20)     default null,
  update_time     date
);

alter table sys_oss_config add constraint pk_sys_oss_config primary key (oss_config_id);

comment on table sys_oss_config                 is '对象存储配置表';
comment on column sys_oss_config.oss_config_id  is '主键';
comment on column sys_oss_config.tenant_id      is '租户编码';
comment on column sys_oss_config.config_key     is '配置key';
comment on column sys_oss_config.access_key     is 'accessKey';
comment on column sys_oss_config.secret_key     is '秘钥';
comment on column sys_oss_config.bucket_name    is '桶名称';
comment on column sys_oss_config.prefix         is '前缀';
comment on column sys_oss_config.endpoint       is '访问站点';
comment on column sys_oss_config.domain         is '自定义域名';
comment on column sys_oss_config.is_https       is '是否https（1=是,0=否）';
comment on column sys_oss_config.region         is '域';
comment on column sys_oss_config.access_policy  is '桶权限类型(0=private 1=public 2=custom)';
comment on column sys_oss_config.status         is '启用状态';
comment on column sys_oss_config.ext1           is '扩展字段';
comment on column sys_oss_config.remark         is '备注';
comment on column sys_oss_config.create_dept    is '创建部门';
comment on column sys_oss_config.create_by      is '创建者';
comment on column sys_oss_config.create_time    is '创建时间';
comment on column sys_oss_config.update_by      is '更新者';
comment on column sys_oss_config.update_time    is '更新时间';

insert into sys_oss_config values (1, '000000', 'minio',  'ruoyi',            'ruoyi123',        'ruoyi',             'ryplus_demo', '127.0.0.1:9000',                '','0', '',            '1', '0', '', NULL, 100, 1, sysdate, 1, sysdate);
insert into sys_oss_config values (2, '000000', 'qiniu',  'XXXXXXXXXXXXXXX',  'XXXXXXXXXXXXXXX', 'ruoyi',             'ryplus_demo', 's3-cn-north-1.qiniucs.com',     '','0', '',            '1', '0', '', NULL, 100, 1, sysdate, 1, sysdate);
insert into sys_oss_config values (3, '000000', 'aliyun', 'XXXXXXXXXXXXXXX',  'XXXXXXXXXXXXXXX', 'ruoyi',             'ryplus_demo', 'oss-cn-beijing.aliyuncs.com',   '','0', '',            '1', '0', '', NULL, 100, 1, sysdate, 1, sysdate);
insert into sys_oss_config values (4, '000000', 'qcloud', 'XXXXXXXXXXXXXXX',  'XXXXXXXXXXXXXXX', 'ruoyi-1250000000',  'ryplus_demo', 'cos.ap-beijing.myqcloud.com',   '','0', 'ap-beijing',  '1', '0', '', NULL, 100, 1, sysdate, 1, sysdate);
insert into sys_oss_config values (5, '000000', 'image',  'ruoyi',            'ruoyi123',        'ruoyi',             'ryplus_demo', '127.0.0.1:9000',           '','0', '',            '1', '0', '', NULL, 100, 1, sysdate, 1, sysdate);
insert into sys_oss_config values (6, '000000', 'local',  'local',            'local',        'local',             'ryplus_demo', '127.0.0.1',           '','0', '',            '1', '1', '', '支持配置自定义域名和是否https，自定义域名不要加前缀，自定义域名如果做了反向代理，需要带上反向代理路径,如: xxx.ruoyikj.top/xxx', 100, 1, sysdate, 1, sysdate);

-- OSS目录表

CREATE TABLE sys_oss_directory (
   directory_id    number(20)     NOT NULL,
   tenant_id       varchar2(20)   DEFAULT '000000',
   parent_id       number(20)     DEFAULT 0,
   ancestors       varchar2(500)  DEFAULT '',
   directory_name  varchar2(255)  NOT NULL,
   directory_path  varchar2(500)  DEFAULT '',
   order_num       number(10)     DEFAULT 0,
   status          char(1)        DEFAULT '1',
   is_default      char(1)        DEFAULT '0',
   create_dept     number(20)     DEFAULT NULL,
   create_by       number(20)     DEFAULT NULL,
   create_time     date,
   update_by       number(20)     DEFAULT NULL,
   update_time     date,
                                   remark          varchar2(500)  DEFAULT NULL
);

ALTER TABLE sys_oss_directory ADD CONSTRAINT pk_sys_oss_directory PRIMARY KEY (directory_id);

COMMENT ON TABLE sys_oss_directory                    IS 'OSS目录';
COMMENT ON COLUMN sys_oss_directory.directory_id      IS '目录ID';
COMMENT ON COLUMN sys_oss_directory.tenant_id         IS '租户id';
COMMENT ON COLUMN sys_oss_directory.parent_id         IS '父目录ID';
COMMENT ON COLUMN sys_oss_directory.ancestors         IS '祖级列表';
COMMENT ON COLUMN sys_oss_directory.directory_name    IS '目录名称';
COMMENT ON COLUMN sys_oss_directory.directory_path    IS '目录路径';
COMMENT ON COLUMN sys_oss_directory.order_num         IS '显示顺序';
COMMENT ON COLUMN sys_oss_directory.status            IS '目录状态';
COMMENT ON COLUMN sys_oss_directory.is_default        IS '是否默认目录';
COMMENT ON COLUMN sys_oss_directory.create_dept       IS '创建部门';
COMMENT ON COLUMN sys_oss_directory.create_by         IS '创建者';
COMMENT ON COLUMN sys_oss_directory.create_time       IS '创建时间';
COMMENT ON COLUMN sys_oss_directory.update_by         IS '更新者';
COMMENT ON COLUMN sys_oss_directory.update_time       IS '更新时间';
COMMENT ON COLUMN sys_oss_directory.remark            IS '备注';

-- 模板表
create table a_temp (
    id            number(20)      not null,
    tenant_id     varchar2(20)    default '000000',
    status        char(1)         default '1',
    create_dept   number(20)      default null,
    create_by     number(20)      default null,
    create_time   date,
    update_by     number(20)      default null,
    update_time   date,
    remark        varchar2(255)   default null,
    is_deleted    char(1)         default '0'
);

alter table a_temp add constraint pk_a_temp primary key (id);

comment on table  a_temp               is '模板';
comment on column a_temp.id            is 'id';
comment on column a_temp.tenant_id     is '租户id';
comment on column a_temp.status        is '状态';
comment on column a_temp.create_dept   is '创建部门';
comment on column a_temp.create_by     is '创建人';
comment on column a_temp.create_time   is '创建时间';
comment on column a_temp.update_by     is '修改人';
comment on column a_temp.update_time   is '更新时间';
comment on column a_temp.remark        is '备注';
comment on column a_temp.is_deleted    is '是否删除';

-- 钩子 ，用于session连接之后 自动设置默认的date类型格式化 简化时间查询
-- 如需设置其它配置 可在此钩子内任意增加处理语句
-- 例如： SELECT * FROM sys_user WHERE create_time BETWEEN '2022-03-01 00:00:00' AND '2022-04-01 00:00:00'
create or replace trigger login_trg
after logon on SCHEMA
begin
execute immediate 'alter session set nls_date_format=''YYYY-MM-DD HH24:MI:SS''';
end;

-- 1. sys_social 第三方平台授权表
CREATE INDEX idx_social_tenant_user ON sys_social (tenant_id, user_id);
CREATE INDEX idx_social_auth_id ON sys_social (auth_id);
CREATE INDEX idx_social_source ON sys_social (source);

-- 2. sys_tenant 租户表
CREATE INDEX idx_tenant_tenant_id ON sys_tenant (tenant_id);

-- 4. sys_dept 部门表
CREATE INDEX idx_dept_tenant_id ON sys_dept (tenant_id);
CREATE INDEX idx_dept_parent_id ON sys_dept (parent_id);

-- 5. sys_user 用户信息表
CREATE INDEX idx_user_tenant_id ON sys_user (tenant_id);
CREATE INDEX idx_user_user_name ON sys_user (user_name);

-- 6. sys_post 岗位信息表
CREATE INDEX idx_post_tenant_id ON sys_post (tenant_id);
CREATE INDEX idx_post_dept_id ON sys_post (dept_id);

-- 7. sys_role 角色信息表
CREATE INDEX idx_role_tenant_id ON sys_role (tenant_id);
CREATE INDEX idx_role_role_key ON sys_role (role_key);

-- 8. sys_menu 菜单权限表
CREATE INDEX idx_menu_parent_id ON sys_menu (parent_id);
CREATE INDEX idx_menu_menu_type ON sys_menu (menu_type);

-- 9. sys_user_role 用户和角色关联表
CREATE INDEX idx_user_role_role_id ON sys_user_role (role_id);

-- 10. sys_role_menu 角色和菜单关联表
CREATE INDEX idx_role_menu_menu_id ON sys_role_menu (menu_id);

-- 11. sys_role_dept 角色和部门关联表
CREATE INDEX idx_role_dept_dept_id ON sys_role_dept (dept_id);

-- 12. sys_user_post 用户与岗位关联表
CREATE INDEX idx_user_post_post_id ON sys_user_post (post_id);

-- 13. sys_oper_log 操作日志
CREATE INDEX idx_oper_log_tenant_id ON sys_oper_log (tenant_id);

-- 14. sys_dict_type 字典类型表
CREATE INDEX idx_dict_type_tenant_id ON sys_dict_type (tenant_id);
CREATE INDEX idx_dict_type_dict_type ON sys_dict_type (dict_type);

-- 15. sys_dict_data 字典数据表
CREATE INDEX idx_dict_data_tenant_id ON sys_dict_data (tenant_id);
CREATE INDEX idx_dict_data_dict_type ON sys_dict_data (dict_type);

-- 16. sys_config 参数配置表
CREATE INDEX idx_config_tenant_id ON sys_config (tenant_id);
CREATE INDEX idx_config_config_key ON sys_config (config_key);

-- 17. sys_login_log 登录日志
CREATE INDEX idx_login_log_tenant_id ON sys_login_log (tenant_id);
CREATE INDEX idx_login_log_user_id ON sys_login_log (user_id);

-- 18. sys_notice 通知公告表
CREATE INDEX idx_notice_tenant_id ON sys_notice (tenant_id);

-- 21. sys_oss OSS对象存储表
CREATE INDEX idx_oss_tenant_id ON sys_oss (tenant_id);
CREATE INDEX idx_oss_directory_id ON sys_oss (directory_id);

-- 23. sys_oss_directory OSS目录
CREATE INDEX idx_oss_dir_tenant_id ON sys_oss_directory (tenant_id);
CREATE INDEX idx_oss_dir_parent_id ON sys_oss_directory (parent_id);

-- ============================================================================
-- 错误日志表 (sys_error_log)
-- 用途: 记录系统运行时的错误、异常、SQL错误等，便于快速排查和统计分析
-- ============================================================================
create table sys_error_log
(
    -- ========== 主键与租户 ==========
    id              number(20)      not null,
    tenant_id       varchar2(20)    default '000000',

    -- ========== 错误基本信息 ==========
    error_level     varchar2(20)    not null,
    error_type      varchar2(255)   not null,
    error_code      varchar2(50)    default null,
    error_message   clob            not null,
    error_stack     clob            default null,

    -- ========== 请求信息 ==========
    trace_id        varchar2(50)    default null,
    request_uri     varchar2(500)   default null,
    request_pattern varchar2(500)   default null,
    request_method  varchar2(10)    default null,
    request_params  clob            default null,
    request_ip      varchar2(50)    default null,
    user_agent      varchar2(500)   default null,

    -- ========== 用户信息 ==========
    user_id         number(20)      default null,
    user_name       varchar2(100)   default null,
    dept_id         number(20)      default null,

    -- ========== SQL错误专项信息 ==========
    sql_statement   clob            default null,
    sql_params      clob            default null,
    sql_duration    number(10)      default null,

    -- ========== 业务上下文 ==========
    module_name     varchar2(100)   default null,
    business_type   varchar2(50)    default null,
    business_key    varchar2(200)   default null,

    -- ========== 客户端信息（前端错误上报） ==========
    client_type     varchar2(20)    default null,
    client_version  varchar2(50)    default null,
    os_type         varchar2(20)    default null,

    -- ========== 服务器环境信息 ==========
    server_name     varchar2(100)   default null,
    server_ip       varchar2(50)    default null,
    app_version     varchar2(20)    default null,
    thread_name     varchar2(100)   default null,

    -- ========== 处理状态 ==========
    handle_status   char(1)         default '0',
    handle_by       number(20)      default null,
    handle_time     date            default null,
    handle_remark   varchar2(500)   default null,

    -- ========== 统计信息（去重机制） ==========
    occurrence_count number(10)     default 1,
    first_time      date            default null,
    last_time       date            default null,

    -- ========== 审计字段（项目规范） ==========
    create_dept     number(20)      default null,
    create_by       number(20)      default null,
    create_time     date            default sysdate,
    update_by       number(20)      default null,
    update_time     date            default sysdate,
    remark          varchar2(500)   default null
);

alter table sys_error_log add constraint pk_sys_error_log primary key (id);

comment on table  sys_error_log                      is '错误日志表';
comment on column sys_error_log.id                   is '主键ID';
comment on column sys_error_log.tenant_id            is '租户ID';
comment on column sys_error_log.error_level          is '严重级别(ERROR/WARN/FATAL)';
comment on column sys_error_log.error_type           is '异常类名(ServiceException/SQLException等)';
comment on column sys_error_log.error_code           is '业务错误码';
comment on column sys_error_log.error_message        is '错误消息';
comment on column sys_error_log.error_stack          is '异常堆栈(限制5000字符)';
comment on column sys_error_log.trace_id             is '链路追踪ID(MDC中的traceId)';
comment on column sys_error_log.request_uri          is '请求URI';
comment on column sys_error_log.request_pattern      is '请求路径模板';
comment on column sys_error_log.request_method       is '请求方法(GET/POST)';
comment on column sys_error_log.request_params       is '请求参数(JSON格式，已脱敏)';
comment on column sys_error_log.request_ip           is '请求IP';
comment on column sys_error_log.user_agent           is 'User-Agent';
comment on column sys_error_log.user_id              is '操作用户ID';
comment on column sys_error_log.user_name            is '操作用户名';
comment on column sys_error_log.dept_id              is '所属部门ID';
comment on column sys_error_log.sql_statement        is '执行的SQL语句';
comment on column sys_error_log.sql_params           is 'SQL参数(JSON格式)';
comment on column sys_error_log.sql_duration         is 'SQL执行耗时(ms)';
comment on column sys_error_log.module_name          is '业务模块(base/mall/iot/crm)';
comment on column sys_error_log.business_type        is '业务类型(查询/新增/修改/删除)';
comment on column sys_error_log.business_key         is '业务关键字(订单号/商品ID/设备ID等)';
comment on column sys_error_log.client_type          is '平台类型(PC/H5/MINIAPP/APP)';
comment on column sys_error_log.client_version       is '客户端版本';
comment on column sys_error_log.os_type              is '操作系统(Windows/Mac/Android/iOS)';
comment on column sys_error_log.server_name          is '服务器名称';
comment on column sys_error_log.server_ip            is '服务器IP';
comment on column sys_error_log.app_version          is '应用版本';
comment on column sys_error_log.thread_name          is '线程名称';
comment on column sys_error_log.handle_status        is '处理状态(0未处理 1已处理 2已忽略)';
comment on column sys_error_log.handle_by            is '处理人ID';
comment on column sys_error_log.handle_time          is '处理时间';
comment on column sys_error_log.handle_remark        is '处理备注';
comment on column sys_error_log.occurrence_count     is '相同错误出现次数';
comment on column sys_error_log.first_time           is '首次出现时间';
comment on column sys_error_log.last_time            is '最后出现时间';
comment on column sys_error_log.create_dept          is '创建部门';
comment on column sys_error_log.create_by            is '创建人';
comment on column sys_error_log.create_time          is '创建时间';
comment on column sys_error_log.update_by            is '更新人';
comment on column sys_error_log.update_time          is '更新时间';
comment on column sys_error_log.remark               is '备注';

-- 24. sys_error_log 错误日志表
CREATE INDEX idx_error_log_tenant_time ON sys_error_log (tenant_id, create_time);
CREATE INDEX idx_error_log_severity ON sys_error_log (error_level);
CREATE INDEX idx_error_log_exception ON sys_error_log (error_type);
CREATE INDEX idx_error_log_user_id ON sys_error_log (user_id);
CREATE INDEX idx_error_log_trace_id ON sys_error_log (trace_id);
CREATE INDEX idx_error_log_module ON sys_error_log (module_name);
CREATE INDEX idx_error_log_handle_status ON sys_error_log (handle_status);
CREATE INDEX idx_error_log_business_key ON sys_error_log (business_key);

