
# 支付集成指南

## 支持的支付方式

| 支付方式 | 枚举值 | 模块 | 支持的交易类型 |
|---------|-------|------|--------------|
| 微信支付 | `WECHAT` | pay-wechat | JSAPI、NATIVE、APP、H5 |
| 支付宝 | `ALIPAY` | pay-alipay | WAP、PAGE、APP |
| 银联 | `UNIONPAY` | pay-unionpay | WAP、WEB |
| 余额支付 | `BALANCE` | pay-balance | 内部扣款 |

---

## 核心接口：PayService

```java
import plus.ruoyi.common.pay.service.PayService;
import plus.ruoyi.common.core.dict.DictPaymentMethod;
import plus.ruoyi.common.pay.domain.request.PayRequest;
import plus.ruoyi.common.pay.domain.request.RefundRequest;
import plus.ruoyi.common.pay.domain.response.PayResponse;
import plus.ruoyi.common.pay.domain.response.RefundResponse;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {

    private final PayService payService;

    // 发起支付
    public PayResponse createPayment(Order order, String openId) {
        PayRequest request = PayRequest.createWxJsapiRequest(
            appid,
            mchId,
            "商品描述",
            order.getOutTradeNo(),
            order.getTotalAmount(),
            openId,
            clientIp
        );
        return payService.pay(DictPaymentMethod.WECHAT, request);
    }

    // 申请退款
    public RefundResponse createRefund(Order order, BigDecimal refundAmount) {
        RefundRequest request = RefundRequest.createWxRefundRequest(
            appid,
            mchId,
            order.getOutTradeNo(),
            generateRefundNo(),
            order.getTotalAmount(),
            refundAmount,
            "退款原因"
        );
        return payService.refund(DictPaymentMethod.WECHAT, request);
    }

    // 查询支付状态
    public PayResponse queryPayment(String outTradeNo) {
        return payService.queryPayment(DictPaymentMethod.WECHAT, outTradeNo, appid);
    }
}
```

---

## 支付请求创建

### 微信支付

```java
// 1. JSAPI 支付（小程序、公众号）
PayRequest request = PayRequest.createWxJsapiRequest(
    appid,        // 微信appid
    mchId,        // 商户号
    "商品描述",    // 商品描述
    outTradeNo,   // 商户订单号
    totalFee,     // 金额（元）
    openId,       // 用户openid（必填）
    clientIp      // 客户端IP
);

// 2. NATIVE 支付（扫码支付）
PayRequest request = PayRequest.createWxNativeRequest(
    appid, mchId, "商品描述", outTradeNo, totalFee, clientIp
);

// 3. APP 支付
PayRequest request = PayRequest.createWxAppRequest(
    appid, mchId, "商品描述", outTradeNo, totalFee, clientIp
);

// 4. H5 支付（手机浏览器）
PayRequest request = PayRequest.createWxH5Request(
    appid, mchId, "商品描述", outTradeNo, totalFee, clientIp
);

// 发起支付
PayResponse response = payService.pay(DictPaymentMethod.WECHAT, request);
```

### 支付宝

```java
// 1. 手机网站支付（WAP）
PayRequest request = PayRequest.createAlipayWapRequest(
    appid,        // 支付宝appid
    "商品描述",
    outTradeNo,
    totalFee,
    returnUrl     // 同步返回地址
);

// 2. 电脑网站支付（PAGE）
PayRequest request = PayRequest.createAlipayPageRequest(
    appid, "商品描述", outTradeNo, totalFee, returnUrl
);

// 3. APP 支付
PayRequest request = PayRequest.createAlipayAppRequest(
    appid, "商品描述", outTradeNo, totalFee
);

// 发起支付
PayResponse response = payService.pay(DictPaymentMethod.ALIPAY, request);
```

### 余额支付

```java
PayRequest request = PayRequest.createBalanceRequest(
    outTradeNo,   // 订单号
    totalFee,     // 金额
    "商品描述",
    clientIp
);

PayResponse response = payService.pay(DictPaymentMethod.BALANCE, request);
```

---

## 支付响应处理

### PayResponse 字段

| 字段 | 类型 | 说明 |
|------|------|------|
| `success` | boolean | 是否成功 |
| `message` | String | 响应消息 |
| `outTradeNo` | String | 商户订单号 |
| `transactionId` | String | 第三方交易号 |
| `tradeState` | String | 支付状态 |
| `prepayId` | String | 预支付ID（微信JSAPI） |
| `payInfo` | Map | 前端调起支付参数 |
| `codeUrl` | String | 二维码链接（NATIVE） |
| `qrCodeBase64` | String | 二维码Base64 |
| `payUrl` | String | 支付跳转链接（H5） |
| `payForm` | String | 支付表单（支付宝PC） |

### 支付状态（tradeState）

| 状态 | 说明 |
|------|------|
| `SUCCESS` | 支付成功 |
| `WAIT_PAY` | 等待支付 |
| `CLOSED` | 已关闭 |
| `REVOKED` | 已撤销 |
| `USERPAYING` | 用户支付中 |
| `PAYERROR` | 支付失败 |

### 响应处理示例

```java
PayResponse response = payService.pay(DictPaymentMethod.WECHAT, request);

if (!response.isSuccess()) {
    throw ServiceException.of("支付失败: " + response.getMessage());
}

// 根据支付类型返回不同数据给前端
switch (request.getTradeType()) {
    case "JSAPI":
        // 返回 payInfo 给前端调起支付
        return R.ok(response.getPayInfo());

    case "NATIVE":
        // 返回二维码给前端展示
        return R.ok(Map.of(
            "codeUrl", response.getCodeUrl(),
            "qrCode", response.getQrCodeBase64()
        ));

    case "MWEB":
        // 返回 H5 跳转链接
        return R.ok(Map.of("payUrl", response.getPayUrl()));

    default:
        return R.ok(response);
}
```

---

## 支付回调处理

### 监听支付成功事件

```java
import plus.ruoyi.common.pay.event.PaySuccessEvent;
import org.springframework.context.event.EventListener;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final IOrderService orderService;

    /**
     * 监听支付成功事件
     */
    @EventListener
    public void onPaymentSuccess(PaySuccessEvent event) {
        log.info("收到支付成功事件: {}", event.getSummary());

        // 获取关键信息
        String outTradeNo = event.getOutTradeNo();
        String transactionId = event.getTransactionId();
        String totalFee = event.getTotalFee();  // 单位：分
        String paymentMethod = event.getPaymentMethod();

        // 更新订单状态
        orderService.paySuccess(outTradeNo, transactionId);
    }
}
```

### PaySuccessEvent 字段

| 字段 | 类型 | 说明 |
|------|------|------|
| `outTradeNo` | String | 商户订单号 |
| `transactionId` | String | 第三方交易号 |
| `totalFee` | String | 支付金额（分） |
| `paymentMethod` | String | 支付方式 |
| `appId` | String | 应用ID |
| `openId` | String | 用户标识 |
| `payTime` | String | 支付时间 |
| `attach` | String | 附加数据 |

### 事件辅助方法

```java
// 获取金额（元）
String amountYuan = event.getTotalAmountYuan();

// 获取金额（分）
Long amountFen = event.getTotalAmountFen();

// 判断支付方式
if (event.isWechatPay()) { ... }
if (event.isAlipay()) { ... }
if (event.isUnionpay()) { ... }
if (event.isBalance()) { ... }
```

---

## 退款操作

### 创建退款请求

```java
// 微信退款
RefundRequest request = RefundRequest.createWxRefundRequest(
    appid,
    mchId,
    outTradeNo,     // 原订单号
    outRefundNo,    // 退款单号
    totalFee,       // 订单总金额
    refundFee,      // 退款金额
    "退款原因"
);

// 支付宝退款
RefundRequest request = RefundRequest.createAlipayRefundRequest(
    appid,
    outTradeNo,
    outRefundNo,
    refundFee,
    "退款原因"
);

// 余额退款
RefundRequest request = RefundRequest.createBalanceRefundRequest(
    outTradeNo,
    outRefundNo,
    refundFee,
    "退款原因"
);

// 发起退款
RefundResponse response = payService.refund(DictPaymentMethod.WECHAT, request);
```

### RefundResponse 字段

| 字段 | 类型 | 说明 |
|------|------|------|
| `success` | boolean | 是否成功 |
| `outRefundNo` | String | 商户退款单号 |
| `refundId` | String | 第三方退款单号 |
| `refundFee` | BigDecimal | 退款金额 |
| `refundStatus` | String | 退款状态 |

---

## 移动端支付对接

### 小程序端（plus-uniapp）

```typescript
// composables/usePayment.ts 已封装好支付逻辑

// 使用示例
const { pay, loading } = usePayment()

const handlePay = async () => {
  const result = await pay({
    orderId: order.id,
    paymentMethod: 'wechat',
    openId: userStore.openId
  })

  if (result.success) {
    // 支付成功，跳转结果页
    uni.redirectTo({ url: '/pages/order/result' })
  }
}
```

### 前端 API 定义

```typescript
// api/pay/payApi.ts

// 创建支付
export const createPayment = (data: PaymentBo): Result<PaymentVo> => {
  return http.post('/app/pay/create', data)
}

// 查询支付状态
export const queryPayment = (outTradeNo: string): Result<PaymentVo> => {
  return http.get(`/app/pay/query/${outTradeNo}`)
}
```

---

## 支付配置

### 数据库配置表

支付配置存储在 `b_payment` 表中，通过 `PayConfigManager` 管理：

```java
import plus.ruoyi.common.pay.config.PayConfigManager;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl {

    private final PayConfigManager payConfigManager;

    public PayConfig getConfig(String appid) {
        return payConfigManager.getConfig(appid);
    }
}
```

### 配置字段说明

| 字段 | 说明 |
|------|------|
| `appid` | 应用ID |
| `mchId` | 商户号 |
| `apiKey` | API密钥 |
| `apiV3Key` | API V3密钥（微信V3） |
| `certPath` | 证书路径 |
| `privateKeyPath` | 私钥路径 |
| `serialNo` | 证书序列号 |
| `notifyUrl` | 回调地址 |

---

## 最佳实践

### 1. 订单号生成

```java
// 使用有意义的前缀 + 时间戳 + 随机数
public String generateOutTradeNo(String prefix) {
    return prefix +
           DateUtils.format(new Date(), "yyyyMMddHHmmss") +
           RandomUtil.randomNumbers(6);
}

// 示例：ORDER20241201120000123456
```

### 2. 幂等性处理

```java
@Transactional
public void paySuccess(String outTradeNo, String transactionId) {
    Order order = orderDao.getByOutTradeNo(outTradeNo);

    // 幂等检查：已支付则直接返回
    if (OrderStatus.PAID.equals(order.getStatus())) {
        log.info("订单已支付，跳过处理: {}", outTradeNo);
        return;
    }

    // 更新订单状态
    order.setStatus(OrderStatus.PAID);
    order.setTransactionId(transactionId);
    order.setPayTime(new Date());
    orderDao.updateById(order);
}
```

### 3. 异常处理

```java
try {
    PayResponse response = payService.pay(paymentMethod, request);
    if (!response.isSuccess()) {
        throw ServiceException.of("支付失败: " + response.getMessage());
    }
    return response;
} catch (PayException e) {
    log.error("支付异常: {}", e.getMessage(), e);
    throw ServiceException.of("支付服务暂不可用，请稍后重试");
}
```

### 4. 回调验签

回调验签由框架自动处理，业务只需监听 `PaySuccessEvent` 事件即可。

---

## 常见问题

### 1. 微信支付签名失败

- 检查 API 密钥是否正确
- 检查证书是否过期
- V2 和 V3 版本密钥不同

### 2. 回调收不到

- 检查 notifyUrl 是否可公网访问
- 检查防火墙配置
- 查看支付平台的回调日志

### 3. 金额单位问题

- 微信支付金额单位：**分**
- 支付宝金额单位：**元**
- PayRequest.totalFee 统一使用：**元**（框架自动转换）
