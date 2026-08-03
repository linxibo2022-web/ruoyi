package plus.ruoyi.business.mall.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import plus.ruoyi.business.mall.service.IOrderService;
import plus.ruoyi.common.core.dict.DictOrderStatus;
import plus.ruoyi.common.core.dict.DictPaymentMethod;
import plus.ruoyi.common.pay.event.PaySuccessEvent;

/**
 * 订单支付事件监听器
 * <p>
 * 只有当支付模块启用时才加载此监听器
 *
 * @author 抓蛙师
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "module", name = "pay-enabled", havingValue = "true", matchIfMissing = true)
public class OrderPaymentEventListener {

    private final IOrderService orderService;

    @EventListener
    @Transactional(rollbackFor = Exception.class)
    public void handlePaymentSuccess(PaySuccessEvent event) {
        try {
            log.info("处理订单支付成功事件: 订单号={}, 支付方式={}",
                event.getOutTradeNo(), event.getPaymentMethod());

            // 更新订单状态为已支付,并保存支付方式
            // 返回 false 表示订单已非待支付(条码支付同步事件+异步回调的重复触发),幂等跳过后续逻辑
            boolean transitioned = orderService.updateOrderByOutTradeNo(
                event.getOutTradeNo(),
                DictOrderStatus.PAID.getValue(),
                event.getTransactionId(),
                event.getPaymentMethod()  // 保存支付方式
            );

            if (!transitioned) {
                log.info("订单非待支付或已处理,幂等跳过支付后续逻辑: {}", event.getOutTradeNo());
                return;
            }

            // 根据不同支付方式执行不同的后续逻辑
            handlePostPaymentLogic(event);

            log.info("订单支付事件处理完成: {}", event.getOutTradeNo());

        } catch (Exception e) {
            log.error("处理订单支付事件失败: {}, 错误: {}", event.getOutTradeNo(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 根据支付方式执行不同的后续逻辑
     */
    private void handlePostPaymentLogic(PaySuccessEvent event) {
        try {
            DictPaymentMethod paymentMethod = DictPaymentMethod.getByValue(event.getPaymentMethod());

            switch (paymentMethod) {
                case WECHAT:
                case ALIPAY:
                case UNIONPAY:
                    // 在线支付的后续逻辑
                    handleOnlinePaymentSuccess(event);
                    break;
                case BALANCE:
                    // 余额支付的后续逻辑
                    handleBalancePaymentSuccess(event);
                    break;
                case POINTS:
                    // 积分支付的后续逻辑
                    handlePointsPaymentSuccess(event);
                    break;
                default:
                    log.warn("未知的支付方式: {}", event.getPaymentMethod());
            }
        } catch (Exception e) {
            log.error("处理支付后续逻辑失败: {}, 错误: {}", event.getOutTradeNo(), e.getMessage());
            // 不抛出异常，避免影响主流程
        }
    }

    /**
     * 处理在线支付成功后的逻辑
     */
    private void handleOnlinePaymentSuccess(PaySuccessEvent event) {
        // 1. 减库存
        // 2. 发送支付成功通知
        // 3. 记录积分
        // 4. 触发发货流程
        log.info("处理在线支付成功后续逻辑: {}", event.getOutTradeNo());

        try {
            // TODO: 实现具体的在线支付后续逻辑
            // 1. 调用库存服务减库存
            // 2. 发送支付成功短信/邮件通知
            // 3. 计算并赠送积分
            // 4. 创建发货任务

        } catch (Exception e) {
            log.error("在线支付后续逻辑处理失败: {}, 错误: {}", event.getOutTradeNo(), e.getMessage());
        }
    }

    /**
     * 处理余额支付成功后的逻辑
     */
    private void handleBalancePaymentSuccess(PaySuccessEvent event) {
        // 1. 记录余额变动日志
        // 2. 发送余额支付通知
        log.info("处理余额支付成功后续逻辑: {}", event.getOutTradeNo());

        try {
            // TODO: 实现具体的余额支付后续逻辑
            // 1. 记录用户余额变动日志
            // 2. 发送余额支付成功通知
            // 3. 统计余额支付相关数据

        } catch (Exception e) {
            log.error("余额支付后续逻辑处理失败: {}, 错误: {}", event.getOutTradeNo(), e.getMessage());
        }
    }

    /**
     * 处理积分支付成功后的逻辑
     */
    private void handlePointsPaymentSuccess(PaySuccessEvent event) {
        // 1. 记录积分变动日志
        // 2. 发送积分支付通知
        log.info("处理积分支付成功后续逻辑: {}", event.getOutTradeNo());

        try {
            // TODO: 实现具体的积分支付后续逻辑
            // 1. 记录用户积分变动日志
            // 2. 发送积分支付成功通知
            // 3. 统计积分支付相关数据

        } catch (Exception e) {
            log.error("积分支付后续逻辑处理失败: {}, 错误: {}", event.getOutTradeNo(), e.getMessage());
        }
    }
}
