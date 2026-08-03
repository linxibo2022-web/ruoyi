package plus.ruoyi.common.pay.wechat.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.dict.DictPaymentMethod;
import plus.ruoyi.common.core.utils.StringUtils;
import plus.ruoyi.common.pay.config.PayConfig;
import plus.ruoyi.common.pay.config.PayConfigManager;
import plus.ruoyi.common.pay.domain.request.NotifyRequest;
import plus.ruoyi.common.pay.domain.request.PayRequest;
import plus.ruoyi.common.pay.domain.request.RefundRequest;
import plus.ruoyi.common.pay.domain.response.NotifyResponse;
import plus.ruoyi.common.pay.domain.response.PayResponse;
import plus.ruoyi.common.pay.domain.response.RefundResponse;
import plus.ruoyi.common.pay.core.handler.PayHandler;
import plus.ruoyi.common.pay.wechat.strategy.PayVersionSelector;
import plus.ruoyi.common.pay.exception.PayException;
import plus.ruoyi.common.pay.wechat.enums.WxPayVersion;
import plus.ruoyi.common.pay.wechat.handler.v2.WxPayV2Strategy;
import plus.ruoyi.common.pay.wechat.handler.v3.WxPayV3Strategy;
import plus.ruoyi.common.satoken.utils.LoginHelper;

/**
 * 微信支付处理器 - 统一入口
 *
 * 功能:
 * 1. 智能选择v2/v3策略
 * 2. 统一处理支付/退款/查询/回调
 * 3. 自动配置切换
 *
 * 注意: 此类不需要 @Component 注解,已在 PayAutoConfiguration 中通过 @Bean 注册
 *
 * @author 抓蛙师
 */
@Slf4j
@RequiredArgsConstructor
public class WxPayHandler implements PayHandler {

    private final PayConfigManager configManager;
    private final PayVersionSelector versionSelector;
    private final WxPayV2Strategy v2Strategy;
    private final WxPayV3Strategy v3Strategy;

    @Override
    public DictPaymentMethod getPaymentMethod() {
        return DictPaymentMethod.WECHAT;
    }

    @Override
    public PayResponse pay(PayRequest request) {
        try {
            log.info("微信支付请求: outTradeNo={}, tradeType={}",
                request.getOutTradeNo(), request.getTradeType());

            // 1. 智能获取配置
            PayConfig config = selectAndSwitchConfig(request.getAppid());

            // 2. 预处理请求参数
            preprocessPayRequest(request, config);

            // 3. 选择版本策略
            WxPayVersion version = versionSelector.selectWxPayVersion(config);

            // 4. 委托给对应策略执行
            PayResponse response;
            if (version == WxPayVersion.V3) {
                log.info("使用微信v3支付: appid={}", config.getAppid());
                response = v3Strategy.executePay(request, config);
            } else {
                log.info("使用微信v2支付: appid={}", config.getAppid());
                response = v2Strategy.executePay(request, config);
            }

            log.info("微信支付完成: outTradeNo={}, success={}",
                request.getOutTradeNo(), response.isSuccess());

            return response;

        } catch (Exception e) {
            log.error("微信支付失败: outTradeNo={}, error={}",
                request.getOutTradeNo(), e.getMessage(), e);

            return PayResponse.fail("微信支付失败: " + e.getMessage());
        }
    }

    @Override
    public RefundResponse refund(RefundRequest request) {
        try {
            log.info("微信退款请求: outRefundNo={}, outTradeNo={}",
                request.getOutRefundNo(), request.getOutTradeNo());

            // 1. 获取配置
            PayConfig config = selectAndSwitchConfig(request.getAppid());

            // 2. 选择版本策略
            WxPayVersion version = versionSelector.selectWxPayVersion(config);

            // 3. 执行退款
            RefundResponse response;
            if (version == WxPayVersion.V3) {
                log.info("使用微信v3退款: appid={}", config.getAppid());
                response = v3Strategy.executeRefund(request, config);
            } else {
                log.info("使用微信v2退款: appid={}", config.getAppid());
                response = v2Strategy.executeRefund(request, config);
            }

            log.info("微信退款完成: outRefundNo={}, success={}",
                request.getOutRefundNo(), response.isSuccess());

            return response;

        } catch (Exception e) {
            log.error("微信退款失败: outRefundNo={}, error={}",
                request.getOutRefundNo(), e.getMessage(), e);

            return RefundResponse.builder()
                .success(false)
                .message("微信退款失败: " + e.getMessage())
                .outRefundNo(request.getOutRefundNo())
                .build();
        }
    }

    @Override
    public PayResponse queryPayment(String outTradeNo, String appid) {
        try {
            log.info("查询微信支付: outTradeNo={}, appid={}", outTradeNo, appid);

            // 1. 获取配置
            PayConfig config = getConfigByAppid(appid);

            // 2. 选择版本策略
            WxPayVersion version = versionSelector.selectWxPayVersion(config);

            // 3. 执行查询
            if (version == WxPayVersion.V3) {
                return v3Strategy.queryPayment(outTradeNo, config);
            } else {
                return v2Strategy.queryPayment(outTradeNo, config);
            }

        } catch (Exception e) {
            log.error("查询微信支付失败: outTradeNo={}, error={}",
                outTradeNo, e.getMessage(), e);

            return PayResponse.fail("查询失败: " + e.getMessage());
        }
    }

    @Override
    public RefundResponse queryRefund(String outRefundNo, String appid) {
        try {
            log.info("查询微信退款: outRefundNo={}, appid={}", outRefundNo, appid);

            // 1. 获取配置
            PayConfig config = getConfigByAppid(appid);

            // 2. 选择版本策略
            WxPayVersion version = versionSelector.selectWxPayVersion(config);

            // 3. 执行查询
            if (version == WxPayVersion.V3) {
                return v3Strategy.queryRefund(outRefundNo, config);
            } else {
                return v2Strategy.queryRefund(outRefundNo, config);
            }

        } catch (Exception e) {
            log.error("查询微信退款失败: outRefundNo={}, error={}",
                outRefundNo, e.getMessage(), e);

            return RefundResponse.builder()
                .success(false)
                .message("查询失败: " + e.getMessage())
                .outRefundNo(outRefundNo)
                .build();
        }
    }

    @Override
    public NotifyResponse handleNotify(NotifyRequest request) {
        try {
            // 注意: NotifyRequest.mchId 实际存储的是 appid (由回调URL路径传入)
            // 回调URL格式: /payment/notify/wechat/{appid}
            String appid = request.getMchId();
            log.info("处理微信支付回调: appid={}", appid);

            // 1. 判断回调版本(v2为XML,v3为JSON)
            boolean isV3 = request.hasJsonData();

            // 2. 根据 appid 获取配置
            PayConfig config = getConfigByAppid(appid);

            // 3. 委托给对应版本处理
            if (isV3) {
                log.info("处理微信v3回调: appid={}, mchId={}", config.getAppid(), config.getMchId());
                return v3Strategy.handleNotify(request, config);
            } else {
                log.info("处理微信v2回调: appid={}, mchId={}", config.getAppid(), config.getMchId());
                return v2Strategy.handleNotify(request, config);
            }

        } catch (Exception e) {
            log.error("处理微信回调失败: error={}", e.getMessage(), e);
            return NotifyResponse.fail("处理失败");
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 智能获取配置并切换
     */
    private PayConfig selectAndSwitchConfig(String appid) {
        // 1. 智能获取appid
        // 优先级: 请求参数 > 登录用户appid > 默认配置
        if (StringUtils.isBlank(appid)) {
            appid = LoginHelper.getAppid();
        }

        // 2. 如果仍然为空，尝试获取默认微信支付配置
        if (StringUtils.isBlank(appid)) {
            return getDefaultWxPayConfig();
        }

        // 3. 根据appid获取配置
        return getConfigByAppid(appid);
    }

    /**
     * 根据appid获取配置
     */
    private PayConfig getConfigByAppid(String appid) {
        try {
            return configManager.getConfigByAppid(appid);
        } catch (Exception e) {
            throw new PayException("未找到微信支付配置: appid=" + appid, e);
        }
    }

    /**
     * 根据商户号获取配置
     */
    private PayConfig getConfigByMchId(String mchId) {
        try {
            return configManager.getConfigByMchId(mchId);
        } catch (Exception e) {
            throw new PayException("未找到微信支付配置: mchId=" + mchId, e);
        }
    }

    /**
     * 获取默认微信支付配置
     * 当请求中未指定appid时，自动选择第一个可用的微信支付配置
     *
     * @return 默认微信支付配置
     * @throws PayException 当前租户没有任何微信支付配置时抛出
     */
    private PayConfig getDefaultWxPayConfig() {
        try {
            log.info("未指定appid，尝试获取默认微信支付配置");
            return configManager.getDefaultWxPayConfig();
        } catch (Exception e) {
            throw new PayException("当前租户没有可用的微信支付配置，请先配置微信支付参数", e);
        }
    }

    /**
     * 预处理支付请求参数
     */
    private void preprocessPayRequest(PayRequest request, PayConfig config) {
        // 1. 设置appid
        if (StringUtils.isBlank(request.getAppid())) {
            request.setAppid(config.getAppid());
        }

        // 2. 设置mchId
        if (StringUtils.isBlank(request.getMchId())) {
            request.setMchId(config.getMchId());
        }

        // 3. 设置默认交易类型
        if (StringUtils.isBlank(request.getTradeType())) {
            request.setTradeType("JSAPI"); // 默认JSAPI
        }

        // 4. JSAPI支付需要openid，智能获取当前用户openid
        if ("JSAPI".equalsIgnoreCase(request.getTradeType())) {
            String openid = StringUtils.defaultIfBlank(LoginHelper.getOpenid(), request.getOpenId());
            if (StringUtils.isBlank(openid)) {
                throw new PayException("微信JSAPI支付需要提供openid");
            }
            request.setOpenId(openid);
            log.debug("JSAPI支付openid设置: {}", openid);
        }
    }
}
