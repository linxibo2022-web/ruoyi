---
name: architecture-design
description: |
  系统架构设计、模块划分、代码重构、技术栈选型。包含本项目四层架构、领域划分、依赖管理、技术栈优先级。

  触发场景：
  - 系统架构设计
  - 新模块划分规划
  - 代码重构策略
  - 依赖关系梳理
  - 四层架构（Controller/Service/DAO/Mapper）设计
  - 领域边界划分
  - 技术栈选型咨询

  触发词：架构设计、模块划分、四层架构、领域划分、重构、解耦、依赖管理、分层设计、系统设计、代码组织、技术栈、技术选型

  注意：如果是具体技术方案对比（如Redis vs本地缓存），请使用 tech-decision。如果是开发具体 CRUD 模块，请使用 crud-development。
---

# 架构设计指南

## 本项目技术栈

### 核心技术架构

| 层级 | 技术栈 | 版本 | 说明 |
|------|--------|------|------|
| **后端框架** | Spring Boot | 3.5.8 | 核心框架 |
| **开发语言** | Java | 21 | LTS 版本 |
| **ORM** | MyBatis-Plus | 3.5.14 | 持久层框架 |
| **安全** | Sa-Token | 1.44.0 | 认证授权 |
| **前端-PC** | Vue 3 + Element Plus | - | 管理后台 |
| **前端-移动** | UniApp + WD UI | - | 跨平台移动端 |
| **数据库** | MySQL 8.0+ | 8.0+ | 主数据库（支持多库） |
| **缓存** | Redis + Redisson | 3.52.0 | 分布式缓存 |
| **文档** | SpringDoc | 2.8.14 | API 文档 |
| **工具库** | Hutool | 5.8.40 | Java 工具集 |
| **对象转换** | Mapstruct-Plus | 1.5.0 | BO/VO/Entity 映射 |

### 扩展技术栈（按优先级）

#### 1️⃣ 高优先级技术（优先选择）

| 技术 | 优先级 | 使用场景 | 说明 |
|------|--------|---------|------|
| **Redis** | ⭐⭐⭐⭐⭐ | 缓存、分布式锁、延迟队列 | 优先选择，覆盖大多数场景 |
| **WebSocket** | ⭐⭐⭐⭐⭐ | 实时推送、在线聊天、消息通知 | 实时通信首选 |
| **Sa-Token** | ⭐⭐⭐⭐⭐ | 权限控制、登录认证、单点登录 | 项目安全核心 |
| **Lock4j** | ⭐⭐⭐⭐⭐ | 分布式锁 | 基于 Redis/Redisson |
| **MyBatis-Plus** | ⭐⭐⭐⭐⭐ | ORM、CRUD | 项目数据访问核心 |
| **Redisson** | ⭐⭐⭐⭐⭐ | 分布式对象、布隆过滤器 | Redis 客户端增强 |
| **Spring Scheduled** | ⭐⭐⭐⭐ | 简单定时任务 | 单机场景足够 |

#### 2️⃣ 中优先级技术（按需使用）

| 技术 | 优先级 | 使用场景 | 说明 |
|------|--------|---------|------|
| **SnailJob** | ⭐⭐⭐ | 分布式定时任务、复杂调度 | 复杂场景用，简单用 `@Scheduled` |
| **RocketMQ** | ⭐⭐⭐ | 高吞吐消息队列、系统解耦 | 只在高并发、事务消息场景使用 |
| **MQTT** | ⭐⭐⭐ | 物联网设备通信 | IoT 场景专用（mica-mqtt） |
| **RocketMQ Delay** | ⭐⭐⭐ | 延迟消息（复杂场景） | 优先用 Redis 延迟队列 |
| **SSE** | ⭐⭐⭐ | 服务端推送 | 单向推送场景 |
| **LangChain4j** | ⭐⭐⭐ | AI 大模型集成 | AI 业务专用 |

#### 3️⃣ 专用技术（特定场景）

| 技术 | 优先级 | 使用场景 | 说明 |
|------|--------|---------|------|
| **WxJava** | ⭐⭐⭐⭐ | 微信生态集成 | 小程序、公众号、支付 |
| **Alipay SDK** | ⭐⭐⭐⭐ | 支付宝支付 | 支付场景 |
| **SMS4j** | ⭐⭐⭐ | 短信发送 | 多平台短信 |
| **AWS SDK** | ⭐⭐⭐ | 对象存储（S3/MinIO/阿里云OSS） | 文件上传 |
| **EasyExcel** | ⭐⭐⭐⭐ | Excel 导入导出 | 替代 POI |
| **Ip2Region** | ⭐⭐⭐ | IP 地址定位 | 离线 IP 库 |
| **JustAuth** | ⭐⭐⭐ | 第三方登录 | OAuth 登录 |
| **P6spy** | ⭐⭐ | SQL 日志打印 | 开发调试 |
| **Forest** | ⭐⭐⭐ | HTTP 客户端 | 声明式 HTTP 调用 |

### 技术选型决策树

```
需要实时通信？
├─ 是 → WebSocket（首选）
└─ 否 → 需要消息队列？
         ├─ 是 → 高吞吐/事务消息？
         │      ├─ 是 → RocketMQ
         │      └─ 否 → Redis Streams / RedissonDelayedQueue
         └─ 否 → 需要定时任务？
                ├─ 是 → 分布式调度？
                │      ├─ 是 → SnailJob
                │      └─ 否 → @Scheduled
                └─ 否 → 需要缓存？
                       └─ 是 → Redis（首选）
```

## 本项目架构

### 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                        客户端                                │
├──────────────────┬──────────────────┬───────────────────────┤
│     PC Web       │     小程序        │         App           │
│   (plus-ui)      │  (plus-uniapp)   │     (plus-app)        │
│  Vue 3 + EP      │  UniApp + WD UI  │   UniApp Native       │
└────────┬─────────┴────────┬─────────┴───────────┬───────────┘
         │                  │                     │
         └──────────────────┼─────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                      API 网关 (可选)                          │
│                   Nginx / Spring Cloud Gateway              │
└─────────────────────────────┬───────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      后端服务                                │
│           ruoyi-admin (Spring Boot 3.5.8)                   │
├─────────────────────────────────────────────────────────────┤
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ ruoyi-system │  │ruoyi-business│  │ruoyi-generator│     │
│  │   系统管理    │  │   业务模块   │  │   代码生成    │      │
│  │   (sys_*)    │  │ (b_/m_/iot_) │  │   (gen_*)     │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
├─────────────────────────────────────────────────────────────┤
│                    ruoyi-common (20+ 模块)                   │
│  mybatis/redis/oss/websocket/pay/langchain4j/mqtt/rocketmq  │
└─────────────────────────────┬───────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      数据与存储层                             │
├──────────┬──────────┬──────────┬──────────┬─────────────────┤
│  MySQL   │  Redis   │   OSS    │ RocketMQ │   MQTT Broker   │
│ (主数据)  │  (缓存)  │ (文件)   │ (可选)   │   (IoT 可选)    │
└──────────┴──────────┴──────────┴──────────┴─────────────────┘
```

### 后端分层架构

```
┌─────────────────────────────────────────────────────────────┐
│                    Controller 层                             │
│              接收请求、参数校验、返回响应                       │
└─────────────────────────────┬───────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     Service 层                               │
│              业务逻辑处理、事务管理                            │
│              ⚠️ 不继承任何基类                                │
└─────────────────────────────┬───────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                       DAO 层                                 │
│              数据访问、查询条件构建                            │
│              ⭐ 本项目独有设计                                │
└─────────────────────────────┬───────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     Mapper 层                                │
│                    MyBatis 映射                              │
└─────────────────────────────────────────────────────────────┘
```

---

## 架构设计原则

### 1. 单一职责

```java
// ✅ 好的设计：每个类只负责一件事
public class OrderService {
    // 只处理订单业务
}

public class PaymentService {
    // 只处理支付业务
}

// ❌ 不好的设计：一个类做太多事
public class OrderService {
    // 订单 + 支付 + 物流 + 通知...
}
```

### 2. 开闭原则

```java
// ✅ 好的设计：对扩展开放，对修改关闭
public interface PaymentStrategy {
    void pay(Order order);
}

public class WechatPayment implements PaymentStrategy { }
public class AlipayPayment implements PaymentStrategy { }
// 新增支付方式只需新增实现类

// ❌ 不好的设计：新增功能需要修改原有代码
public void pay(Order order, String type) {
    if ("wechat".equals(type)) {
        // 微信支付
    } else if ("alipay".equals(type)) {
        // 支付宝支付
    }
    // 新增支付方式需要修改这里
}
```

### 3. 依赖倒置

```java
// ✅ 好的设计：依赖抽象而非具体实现
@Service
public class OrderServiceImpl implements IOrderService {
    private final IPaymentService paymentService;  // 依赖接口
}

// ❌ 不好的设计：直接依赖具体实现
@Service
public class OrderServiceImpl {
    private final WechatPaymentService paymentService;  // 依赖具体类
}
```

---

## 模块划分与表前缀规范

### 按业务领域划分

| 模块名 | 包路径 | 表前缀 | 用途 | 示例表 |
|--------|-------|--------|------|--------|
| **base** | `plus.ruoyi.business.base` | `b_` | 基础业务 | `b_ad`, `b_platform`, `b_config` |
| **mall** | `plus.ruoyi.business.mall` | `m_` | 商城业务 | `m_goods`, `m_order`, `m_cart` |
| **iot** | `plus.ruoyi.business.iot` | `iot_` | 物联网 | `iot_device`, `iot_data` |
| **crm** | `plus.ruoyi.business.crm` | `crm_` | 客户管理 | `crm_customer`, `crm_contact` |
| **system** | `plus.ruoyi.system` | `sys_` | 系统管理 | `sys_user`, `sys_menu`, `sys_role` |
| **generator** | `plus.ruoyi.generator` | `gen_` | 代码生成 | `gen_table`, `gen_table_column` |

**重要规则：**
- ✅ 表前缀必须与模块对应（`b_xxx` 表必须在 `base` 模块）
- ✅ Java 类名不带前缀（`Ad.java` 而非 `BAd.java`）
- ✅ `@TableName("b_ad")` 明确指定表名
- ✅ 所有业务表继承 `TenantEntity`（支持多租户）
- ✅ 主键使用雪花 ID（全局配置，不用 `AUTO_INCREMENT`）

### 模块内部结构（标准四层）

```
mall/                                    # 模块根目录
├── controller/                          # 控制器层
│   └── OrderController.java             # @RestController，接收 HTTP 请求
├── service/                             # 服务层（不继承基类）
│   ├── IOrderService.java               # 接口定义
│   └── impl/
│       └── OrderServiceImpl.java        # 业务逻辑实现
├── dao/                                 # 数据访问层（本项目独有）
│   ├── IOrderDao.java                   # DAO 接口
│   └── impl/
│       └── OrderDaoImpl.java            # buildQueryWrapper() 核心
├── mapper/                              # Mapper 层
│   └── OrderMapper.java                 # 继承 BaseMapper<Order>
└── domain/                              # 领域模型
    ├── Order.java                       # Entity（继承 TenantEntity）
    ├── bo/
    │   └── OrderBo.java                 # 业务对象（参数接收）
    └── vo/
        └── OrderVo.java                 # 视图对象（结果返回）
```

### 表设计规范

#### 建表模板（MySQL）

```sql
-- 表前缀：b_(base) / m_(mall) / iot_(iot) / crm_(crm) / sys_(system)
CREATE TABLE b_xxx (
    -- 主键（雪花 ID，不用 AUTO_INCREMENT）
    id           BIGINT(20)   NOT NULL COMMENT '主键ID',

    -- 多租户字段（必须）
    tenant_id    VARCHAR(20)  DEFAULT '000000' COMMENT '租户ID',

    -- 业务字段
    xxx_name     VARCHAR(100) NOT NULL COMMENT '名称',
    xxx_code     VARCHAR(50)  DEFAULT NULL COMMENT '编码',
    status       CHAR(1)      DEFAULT '1' COMMENT '状态(0停用 1正常)',
    sort_order   INT          DEFAULT 0 COMMENT '排序',

    -- 审计字段（必须，继承自 TenantEntity）
    create_dept  BIGINT(20)   DEFAULT NULL COMMENT '创建部门',
    create_by    BIGINT(20)   DEFAULT NULL COMMENT '创建人',
    create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by    BIGINT(20)   DEFAULT NULL COMMENT '更新人',
    update_time  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 备注与逻辑删除
    remark       VARCHAR(500) DEFAULT NULL COMMENT '备注',
    is_deleted   CHAR(1)      DEFAULT '0' COMMENT '是否删除(0正常 1已删除)',

    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='xxx表';

-- 索引建议
CREATE INDEX idx_tenant_id ON b_xxx(tenant_id);        -- 多租户索引
CREATE INDEX idx_status ON b_xxx(status);              -- 状态索引
CREATE INDEX idx_create_time ON b_xxx(create_time);    -- 时间索引
```

#### 多数据库支持

| 数据库 | SQL 文件位置 | 说明 |
|--------|------------|------|
| MySQL | `script/sql/ry_plus_new.sql` | 主数据库（优先） |
| Oracle | `script/sql/oracle/oracle_ry_plus_new.sql` | Oracle 版本 |
| PostgreSQL | `script/sql/postgres/postgres_ry_plus_new.sql` | PostgreSQL 版本 |
| SQL Server | `script/sql/sqlserver/sqlserver_ry_plus_new.sql` | SQL Server 版本 |

**注意：**
- 新业务表必须同步到所有数据库的对应 SQL 文件
- 使用 `likeCast()` 而非 `like()` 处理非字符串字段（PostgreSQL 兼容）

---

## 实战架构案例

### 案例 1：订单系统架构

**需求：** 电商订单创建、支付、发货、退款

**技术选型：**
```
├── 数据存储
│   ├── MySQL（订单主数据）
│   ├── Redis（库存缓存、分布式锁）
│   └── OSS（发票、物流单图片）
├── 消息通信
│   ├── WebSocket（订单状态实时推送）
│   └── RocketMQ（订单异步处理、削峰填谷）
├── 定时任务
│   ├── @Scheduled（订单超时取消 - 简单）
│   └── SnailJob（对账任务 - 分布式）
└── 支付集成
    ├── WxJava（微信支付）
    └── Alipay SDK（支付宝支付）
```

**表设计：**
```sql
-- 订单主表
CREATE TABLE m_order (
    id BIGINT(20) NOT NULL COMMENT '订单ID',
    tenant_id VARCHAR(20) DEFAULT '000000',
    order_no VARCHAR(32) NOT NULL COMMENT '订单号',
    user_id BIGINT(20) NOT NULL COMMENT '用户ID',
    total_amount DECIMAL(10,2) COMMENT '订单金额',
    status CHAR(1) DEFAULT '0' COMMENT '状态(0待付款 1已付款 2已发货 3已完成 4已取消)',
    -- ... 审计字段
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    INDEX idx_user_id (user_id),
    INDEX idx_status_create_time (status, create_time)
) ENGINE=InnoDB COMMENT='订单表';

-- 订单商品明细表
CREATE TABLE m_order_item (
    id BIGINT(20) NOT NULL,
    order_id BIGINT(20) NOT NULL COMMENT '订单ID',
    goods_id BIGINT(20) NOT NULL COMMENT '商品ID',
    sku_id BIGINT(20) COMMENT 'SKU ID',
    quantity INT NOT NULL COMMENT '数量',
    -- ...
    PRIMARY KEY (id),
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB COMMENT='订单商品表';
```

**模块划分：**
```
ruoyi-mall/
├── order/              # 订单模块（m_order）
├── goods/              # 商品模块（m_goods）
├── payment/            # 支付模块（m_payment）
└── logistics/          # 物流模块（m_logistics）
```

---

### 案例 2：IoT 设备监控系统

**需求：** 设备数据采集、实时监控、告警推送

**技术选型：**
```
├── 设备通信
│   └── MQTT（物联网协议）
├── 数据存储
│   ├── MySQL（设备元数据、告警记录）
│   └── Redis（设备在线状态、实时数据）
├── 实时推送
│   └── WebSocket（告警推送到管理后台）
└── 定时任务
    └── @Scheduled（设备离线检测）
```

**表设计：**
```sql
-- 设备表
CREATE TABLE iot_device (
    id BIGINT(20) NOT NULL,
    device_code VARCHAR(50) NOT NULL COMMENT '设备编码',
    device_name VARCHAR(100) COMMENT '设备名称',
    device_type VARCHAR(20) COMMENT '设备类型',
    online_status CHAR(1) DEFAULT '0' COMMENT '在线状态(0离线 1在线)',
    -- ...
    PRIMARY KEY (id),
    UNIQUE KEY uk_device_code (device_code)
) ENGINE=InnoDB COMMENT='设备表';

-- 设备数据表
CREATE TABLE iot_data (
    id BIGINT(20) NOT NULL,
    device_id BIGINT(20) NOT NULL,
    data_type VARCHAR(20) COMMENT '数据类型',
    data_value VARCHAR(255) COMMENT '数据值',
    collect_time DATETIME COMMENT '采集时间',
    -- ...
    PRIMARY KEY (id),
    INDEX idx_device_time (device_id, collect_time)
) ENGINE=InnoDB COMMENT='设备数据表';
```

---

### 案例 3：AI 智能客服系统

**需求：** AI 对话、知识库、会话记录

**技术选型：**
```
├── AI 能力
│   └── LangChain4j（接入 ChatGPT/DeepSeek）
├── 数据存储
│   ├── MySQL（会话记录、知识库）
│   └── Redis（会话上下文缓存）
├── 实时通信
│   └── WebSocket（流式对话响应）
└── 文件存储
    └── OSS（知识库文档）
```

**表设计：**
```sql
-- 会话表
CREATE TABLE b_chat_session (
    id BIGINT(20) NOT NULL,
    user_id BIGINT(20) COMMENT '用户ID',
    session_id VARCHAR(50) NOT NULL COMMENT '会话ID',
    title VARCHAR(200) COMMENT '会话标题',
    model VARCHAR(50) COMMENT 'AI模型',
    -- ...
    PRIMARY KEY (id),
    INDEX idx_user_session (user_id, session_id)
) ENGINE=InnoDB COMMENT='会话表';

-- 消息表
CREATE TABLE b_chat_message (
    id BIGINT(20) NOT NULL,
    session_id VARCHAR(50) NOT NULL,
    role VARCHAR(20) COMMENT '角色(user/assistant/system)',
    content TEXT COMMENT '消息内容',
    -- ...
    PRIMARY KEY (id),
    INDEX idx_session (session_id)
) ENGINE=InnoDB COMMENT='消息表';
```

---

## 技术选型决策指南

### 场景 1：需要消息队列吗？

| 场景 | 推荐方案 | 理由 |
|------|---------|------|
| 订单创建后发送通知 | ❌ 不需要，同步调用 | 简单场景，直接 Service 调用 |
| 订单支付成功后更新库存 | ✅ Redis Streams | 解耦，但数据量不大 |
| 秒杀活动削峰填谷 | ✅ RocketMQ | 高并发、需要持久化 |
| 用户注册送积分 | ❌ 不需要，事务内处理 | 强一致性需求 |

### 场景 2：定时任务如何选择？

| 场景 | 推荐方案 | 配置示例 |
|------|---------|---------|
| 订单超时自动取消（单机） | `@Scheduled` | `@Scheduled(cron = "0 */5 * * * ?")` |
| 每日数据汇总（分布式） | SnailJob | Web 界面配置 |
| 实时监控设备状态 | `@Scheduled` | `@Scheduled(fixedRate = 10000)` |
| 复杂业务流程调度 | SnailJob | 支持工作流、失败重试 |

### 场景 3：实时通信方案

| 场景 | 推荐方案 | 说明 |
|------|---------|------|
| 订单状态推送到用户 | WebSocket | 双向通信 |
| 系统通知推送 | SSE | 单向推送 |
| IoT 设备通信 | MQTT | 低功耗、物联网协议 |
| 聊天室功能 | WebSocket | 实时聊天 |

---

## 常见架构模式对比

| 架构模式 | 适用场景 | 优点 | 缺点 | 本项目采用 |
|---------|---------|------|------|-----------|
| **分层架构** | 中小型项目、业务清晰 | 简单、易维护 | 可能过度设计 | ✅ 是（四层架构） |
| **DDD** | 复杂业务领域 | 领域驱动、高内聚 | 学习成本高 | ❌ 否 |
| **微服务** | 大型分布式系统 | 独立部署、扩展灵活 | 运维复杂 | ⚠️ 可选（根据规模） |
| **CQRS** | 读写分离场景 | 性能优化 | 数据同步复杂 | ❌ 否 |

### 本项目推荐架构路径

```
阶段 1：单体应用（当前）
├── 四层架构：Controller → Service → DAO → Mapper
├── 多租户支持
├── 模块化设计（base/mall/iot/crm）
└── 适合：0-10万用户

阶段 2：垂直拆分（可选）
├── 按模块拆分微服务（订单服务、商品服务等）
├── 引入 Spring Cloud Gateway
├── 服务间 Feign 调用
└── 适合：10万-100万用户

阶段 3：水平扩展（高并发）
├── 数据库分库分表（ShardingSphere）
├── Redis 集群
├── RocketMQ 集群
└── 适合：100万+ 用户
```

---

## 设计模板

### 系统设计模板

```markdown
# [系统名称] 架构设计

## 1. 背景和目标
- 业务背景
- 设计目标
- 非功能需求

## 2. 系统架构
[架构图]

## 3. 模块设计
| 模块 | 职责 | 依赖 |
|------|------|------|

## 4. 数据设计
- 数据库设计
- 缓存设计

## 5. 接口设计
- 内部接口
- 外部接口

## 6. 安全设计
- 认证授权
- 数据安全

## 7. 部署架构
[部署图]

## 8. 风险评估
| 风险 | 概率 | 影响 | 应对措施 |
```

### 模块设计模板

```markdown
# [模块名称] 设计

## 1. 功能概述
[模块要实现的功能]

## 2. 类设计
```
XxxController
    └── IXxxService
        └── XxxServiceImpl
            └── IXxxDao
                └── XxxDaoImpl
                    └── XxxMapper
```

## 3. 数据模型
- Entity
- BO
- VO

## 4. 接口设计
| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|

## 5. 业务流程
[流程图]
```

---

## 重构建议

### 识别需要重构的信号

1. **代码重复**: 多处相同/相似代码
2. **过长方法**: 方法超过 50 行
3. **过大的类**: 类超过 500 行
4. **过多参数**: 方法参数超过 5 个
5. **复杂条件**: 嵌套 if-else 超过 3 层
6. **难以测试**: 依赖过多、难以 Mock

### 重构策略

1. **提取方法**: 将重复代码提取为方法
2. **提取类**: 将相关功能提取为新类
3. **引入接口**: 解耦具体实现
4. **使用设计模式**: 策略、工厂等
5. **小步重构**: 每次只改一点，保证可用

---

## 架构设计快速检查清单

### ✅ 新模块设计检查

- [ ] **包路径正确**：`plus.ruoyi.business.{模块名}`
- [ ] **表前缀匹配**：`b_` (base) / `m_` (mall) / `iot_` / `crm_`
- [ ] **表设计规范**：
  - [ ] 主键使用雪花 ID（不用 `AUTO_INCREMENT`）
  - [ ] 包含 `tenant_id` 字段
  - [ ] 包含审计字段（`create_by`, `create_time`, `update_by`, `update_time`）
  - [ ] 包含逻辑删除字段 `is_deleted`
  - [ ] 添加必要索引（`tenant_id`, `status`, `create_time`）
- [ ] **四层架构完整**：
  - [ ] Controller（`@RestController`，路径包含实体名）
  - [ ] Service（不继承基类，接口 + 实现）
  - [ ] DAO（`buildQueryWrapper()` 方法）
  - [ ] Mapper（继承 `BaseMapper<Entity>`）
- [ ] **对象转换**：使用 `MapstructUtils.convert()`
- [ ] **异常处理**：使用 `ServiceException.of("错误信息")`
- [ ] **权限注解**：`@SaCheckPermission("{模块}:{实体}:{操作}")`

### ✅ 技术选型检查

- [ ] **缓存需求**：优先选择 Redis
- [ ] **实时通信**：优先选择 WebSocket
- [ ] **定时任务**：简单场景用 `@Scheduled`，复杂场景用 SnailJob
- [ ] **消息队列**：优先 Redis Streams，高并发用 RocketMQ
- [ ] **支付集成**：WxJava（微信）、Alipay SDK（支付宝）
- [ ] **文件上传**：OSS（S3/MinIO/阿里云）
- [ ] **AI 集成**：LangChain4j
- [ ] **IoT 通信**：MQTT（mica-mqtt）

### ✅ 数据库设计检查

- [ ] **多数据库兼容**：SQL 写入对应的 4 个数据库文件
- [ ] **索引优化**：
  - [ ] 多租户查询必加 `tenant_id` 索引
  - [ ] 状态查询加 `status` 索引
  - [ ] 时间范围查询加 `create_time` 索引
  - [ ] 联合查询加组合索引
- [ ] **字段类型**：
  - [ ] 主键：`BIGINT(20)`
  - [ ] 状态：`CHAR(1)`
  - [ ] 金额：`DECIMAL(10,2)`
  - [ ] 时间：`DATETIME`
- [ ] **跨库兼容**：非 String 字段搜索用 `likeCast()`

### ✅ 前端开发检查

#### PC 端 (plus-ui)

- [ ] **先读参考代码**：`plus-ui/src/views/business/base/ad/ad.vue`
- [ ] **使用封装组件**：`AFormInput`, `AFormSelect`, `AModal` 等
- [ ] **API 调用**：`const [err, data] = await pageXxxs(params)`
- [ ] **禁止原生组件**：不使用 `el-input`, `el-dialog` 等

#### 移动端 (plus-uniapp)

- [ ] **先读参考代码**：
  - [ ] `src/components/tabbar/Home.vue`（列表+分页）
  - [ ] `src/pages/auth/login.vue`（表单）
- [ ] **使用 WD UI**：`wd-form`, `wd-input`, `wd-paging` 等
- [ ] **导入方式**：`import { useToast } from '@/wd'`
- [ ] **API 调用**：`const [err, data] = await api()`
- [ ] **样式单位**：使用 `rpx`

---

## 常见问题 FAQ

### Q1: 什么时候需要使用 RocketMQ？

**A:** 只在以下场景使用：
- ✅ 高并发削峰填谷（秒杀、抢购）
- ✅ 事务消息（分布式事务）
- ✅ 顺序消息（订单状态流转）
- ❌ **不要用于**：简单的异步任务（用 Redis 或同步调用）

### Q2: Service 层为什么不继承 ServiceImpl？

**A:** 本项目设计理念：
- Service 层专注业务逻辑
- DAO 层封装数据访问（`buildQueryWrapper()`）
- 避免 Service 层直接操作 MyBatis-Plus API
- 更清晰的分层边界

### Q3: 表前缀与模块如何对应？

**A:** 严格对应规则：
| 模块 | 表前缀 | 错误示例 |
|------|--------|---------|
| base | `b_` | ❌ `m_ad` 在 base 模块 |
| mall | `m_` | ❌ `b_order` 在 mall 模块 |
| iot | `iot_` | ❌ `device` 缺少前缀 |

### Q4: 什么时候用 SnailJob 而非 @Scheduled？

**A:** 选择标准：
- **@Scheduled**：单机、简单定时任务（订单超时取消）
- **SnailJob**：分布式、需要失败重试、工作流编排（对账、报表）

### Q5: 为什么用 likeCast() 而非 like()？

**A:** 多数据库兼容：
```java
// ❌ PostgreSQL 报错
lqw.like(Xxx::getId, searchValue)  // Long 类型

// ✅ 自动类型转换
lqw.likeCast(Xxx::getId, searchValue)  // 兼容所有数据库
```

### Q6: PC 端和移动端可以共用后端接口吗？

**A:** 是的，完全共用：
- 同一套后端 API
- PC 端：`plus-ui` → `ruoyi-admin`
- 移动端：`plus-uniapp` → `ruoyi-admin`
- 权限控制通过 `Sa-Token` 统一管理

### Q7: 如何选择合适的缓存策略？

**A:** 按场景选择：
| 场景 | 方案 | 示例 |
|------|------|------|
| 热点数据 | Redis String | 用户信息、配置 |
| 排行榜 | Redis ZSet | 商品销量排行 |
| 分布式锁 | Redisson Lock | 库存扣减 |
| 延迟队列 | RedissonDelayedQueue | 订单超时取消 |
| 布隆过滤器 | Redisson BloomFilter | 防缓存穿透 |

### Q8: 新增模块需要创建哪些文件？

**A:** 最小集合（以 `Ad` 为例）：
```
ruoyi-business/src/main/java/plus/ruoyi/business/base/
├── controller/AdController.java
├── service/IAdService.java
├── service/impl/AdServiceImpl.java
├── dao/IAdDao.java
├── dao/impl/AdDaoImpl.java
├── mapper/AdMapper.java
└── domain/
    ├── Ad.java (Entity)
    ├── bo/AdBo.java
    └── vo/AdVo.java

script/sql/ry_plus_new.sql        # MySQL 建表语句
```

---

## 🔗 关联技能边界

本技能专注于**系统级架构与模块划分**。遇到以下场景请改用其他技能：

| 场景 | 应使用技能 | 判断关键词 |
|------|-----------|-----------|
| 具体技术方案二选一（如 Redis vs Caffeine） | `tech-decision` | "用哪个好"、"优缺点对比" |
| 完全不知道怎么设计、需要多方案探索 | `brainstorm` | "有什么办法"、"帮我想想" |
| 具体业务模块 CRUD 开发 | `crud-development` | "新建 XX 模块"、"Entity/Service/DAO" |
| 性能瓶颈排查（架构已定、要优化） | `performance-doctor` | "响应慢"、"SQL 慢" |

**三阶段辨别**：
1. **brainstorm** = 方案探索阶段（不知道做什么）
2. **architecture-design** = 架构落地阶段（已知方向、设计分层）
3. **tech-decision** = 选型决策阶段（具体选 A 还是 B）
