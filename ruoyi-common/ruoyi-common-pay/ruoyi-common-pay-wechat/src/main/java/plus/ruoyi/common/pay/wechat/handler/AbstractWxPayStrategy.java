package plus.ruoyi.common.pay.wechat.handler;

import com.github.binarywang.wxpay.service.WxPayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.config.properties.AppProperties;
import plus.ruoyi.common.core.dict.DictPaymentMethod;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.common.pay.core.strategy.PayVersionStrategy;
import plus.ruoyi.common.pay.domain.response.NotifyResponse;
import plus.ruoyi.common.pay.domain.response.PayResponse;
import plus.ruoyi.common.pay.domain.response.RefundResponse;
import plus.ruoyi.common.pay.event.PaySuccessEvent;
import plus.ruoyi.common.pay.exception.WxPayException;
import plus.ruoyi.common.pay.utils.PayNotifyUrlBuilder;
import plus.ruoyi.common.pay.wechat.adapter.WxPayNotifyResultAdapter;
import plus.ruoyi.common.pay.wechat.registry.WxPayServiceRegistry;

/**
 * 微信支付策略抽象基类
 *
 * 提取v2和v3的公共逻辑,避免代码重复
 *
 * @author 抓蛙师
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractWxPayStrategy implements PayVersionStrategy {

    protected final WxPayServiceRegistry registry;
    protected final AppProperties appProperties;

    /**
     * 获取 WxPayService (统一处理异常)
     *
     * @param appid 应用ID
     * @return WxPayService实例
     * @throws WxPayException 服务不存在时抛出
     */
    protected WxPayService getWxPayService(String appid) {
        WxPayService wxPayService = registry.getService(appid);
        if (wxPayService == null) {
            throw new WxPayException("未找到WxPayService: appid=" + appid);
        }
        return wxPayService;
    }

    /**
     * 构建微信支付回调地址
     *
     * 微信支付要求回调地址必须是完整的URL (http:// 或 https://)
     * 从配置中读取 app.base-api 并拼接回调路径
     *
     * @param appid 应用ID
     * @return 完整的回调URL
     */
    protected String buildNotifyUrl(String appid) {
        String baseApi = appProperties.getBaseApi();
        String notifyUrl = PayNotifyUrlBuilder.buildWechatNotifyUrl(baseApi, appid);
        log.debug("微信支付回调地址: {}", notifyUrl);
        return notifyUrl;
    }

    /**
     * 发布支付成功事件 (统一处理v2和v3)
     *
     * 通过适配器模式统一v2和v3的数据获取,解决:
     * 1. 字段名不一致 (mchId vs mchid)
     * 2. 字段路径不一致 (openid vs payer.openid)
     * 3. 金额单位统一 (都转换为元)
     * 4. 空值安全保护
     *
     * @param adapter 回调结果适配器
     */
    protected void publishPaySuccessEvent(WxPayNotifyResultAdapter adapter) {
        try {
            PaySuccessEvent event = PaySuccessEvent.builder()
                .source(this)
                .outTradeNo(adapter.getOutTradeNo())
                .transactionId(adapter.getTransactionId())
                .totalFee(adapter.getTotalFeeInYuan().toString()) // 统一使用元
                .mchId(adapter.getMchId())
                .appId(adapter.getAppId())
                .openId(adapter.getOpenId()) // 带空值保护
                .paymentMethod(DictPaymentMethod.WECHAT.getValue())
                .attach(adapter.getAttach())
                .currency(adapter.getCurrency())
                .build();

            SpringUtils.context().publishEvent(event);

            log.debug("支付成功事件已发布: outTradeNo={}, amount={}元",
                adapter.getOutTradeNo(), adapter.getTotalFeeInYuan());

        } catch (Exception e) {
            log.error("发布支付成功事件失败: outTradeNo={}, error={}",
                adapter.getOutTradeNo(), e.getMessage(), e);
            // ⚠️ 事件发布失败不影响回调确认,只记录日志
        }
    }

    // ==================== 统一异常处理方法 ====================

    /**
     * 统一处理支付异常
     *
     * @param e WxPayException
     * @param outTradeNo 商户订单号
     * @param operation 操作描述 (如 "v2支付", "v3支付")
     * @return PayResponse 失败响应
     */
    protected PayResponse handlePayException(com.github.binarywang.wxpay.exception.WxPayException e,
                                              String outTradeNo,
                                              String operation) {
        log.error("微信{}失败: outTradeNo={}, errCode={}, errMsg={}",
            operation, outTradeNo, e.getErrCode(), e.getErrCodeDes(), e);
        return PayResponse.fail(e.getErrCode(), e.getErrCodeDes());
    }

    /**
     * 统一处理支付通用异常
     *
     * @param e Exception
     * @param outTradeNo 商户订单号
     * @param operation 操作描述
     * @return PayResponse 失败响应
     */
    protected PayResponse handlePayGeneralException(Exception e,
                                                     String outTradeNo,
                                                     String operation) {
        log.error("微信{}异常: outTradeNo={}, error={}",
            operation, outTradeNo, e.getMessage(), e);
        return PayResponse.fail("支付失败: " + e.getMessage());
    }

    /**
     * 统一处理退款异常
     *
     * @param e WxPayException
     * @param outRefundNo 商户退款单号
     * @param outTradeNo 商户订单号
     * @param operation 操作描述
     * @return RefundResponse 失败响应
     */
    protected RefundResponse handleRefundException(
        com.github.binarywang.wxpay.exception.WxPayException e,
        String outRefundNo,
        String outTradeNo,
        String operation) {

        log.error("微信{}失败: outRefundNo={}, errCode={}, errMsg={}",
            operation, outRefundNo, e.getErrCode(), e.getErrCodeDes(), e);

        return RefundResponse.builder()
            .success(false)
            .errorCode(e.getErrCode())
            .message(e.getErrCodeDes())
            .outRefundNo(outRefundNo)
            .outTradeNo(outTradeNo)
            .build();
    }

    /**
     * 统一处理退款通用异常
     *
     * @param e Exception
     * @param outRefundNo 商户退款单号
     * @param operation 操作描述
     * @return RefundResponse 失败响应
     */
    protected RefundResponse handleRefundGeneralException(
        Exception e,
        String outRefundNo,
        String operation) {

        log.error("微信{}异常: outRefundNo={}, error={}",
            operation, outRefundNo, e.getMessage(), e);

        return RefundResponse.builder()
            .success(false)
            .message("退款失败: " + e.getMessage())
            .outRefundNo(outRefundNo)
            .build();
    }

    /**
     * 统一处理回调异常
     *
     * @param e WxPayException
     * @param operation 操作描述
     * @return NotifyResponse 失败响应
     */
    protected NotifyResponse handleNotifyException(
        com.github.binarywang.wxpay.exception.WxPayException e,
        String operation) {

        log.error("微信{}回调验签失败: error={}", operation, e.getMessage(), e);
        return NotifyResponse.fail("验签失败");
    }

    /**
     * 统一处理回调通用异常
     *
     * @param e Exception
     * @param operation 操作描述
     * @return NotifyResponse 失败响应
     */
    protected NotifyResponse handleNotifyGeneralException(
        Exception e,
        String operation) {

        log.error("处理微信{}回调异常: error={}", operation, e.getMessage(), e);
        return NotifyResponse.fail("处理失败");
    }
}
