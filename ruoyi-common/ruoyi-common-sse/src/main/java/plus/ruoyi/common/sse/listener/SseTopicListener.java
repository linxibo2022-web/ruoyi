package plus.ruoyi.common.sse.listener;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.sse.core.SseEmitterManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;

/**
 * SSE主题订阅监听器
 * <p>
 * 在应用启动时初始化Redis主题订阅，监听SSE消息并分发给对应的用户连接
 * 实现集群环境下的消息同步
 *
 * @author Lion Li
 */
@Slf4j
public class SseTopicListener implements ApplicationRunner, Ordered {

    @Autowired
    private SseEmitterManager sseEmitterManager;

    /**
     * 应用启动后初始化SSE消息订阅
     * <p>
     * 订阅Redis SSE主题，根据消息内容决定是定向推送还是广播
     *
     * @param args 应用启动参数
     */
    @Override
    public void run(ApplicationArguments args) {
        sseEmitterManager.subscribeMessage((message) -> {
            log.info("SSE主题订阅收到消息session keys={} message={}", message.getUserIds(), message.getMessage());

            // 根据userIds判断推送方式
            if (CollUtil.isNotEmpty(message.getUserIds())) {
                // 向指定用户推送消息
                message.getUserIds().forEach(userId -> {
                    sseEmitterManager.sendMessage(userId, message.getMessage());
                });
            } else {
                // 向所有用户广播消息
                sseEmitterManager.sendMessage(message.getMessage());
            }
        });
        log.info("初始化SSE主题订阅监听器成功");
    }

    /**
     * 设置执行顺序为最高优先级
     *
     * @return 优先级值，-1表示高优先级
     */
    @Override
    public int getOrder() {
        return -1;
    }
}
