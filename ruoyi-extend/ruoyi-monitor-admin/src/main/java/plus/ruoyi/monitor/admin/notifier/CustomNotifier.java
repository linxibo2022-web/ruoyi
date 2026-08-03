package plus.ruoyi.monitor.admin.notifier;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.notify.AbstractEventNotifier;
import lombok.extern.slf4j.Slf4j;
import plus.ruoyi.common.core.utils.SpringUtils;
import plus.ruoyi.monitor.admin.event.NotifierEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static de.codecentric.boot.admin.server.domain.values.StatusInfo.*;

/**
 * 自定义事件通知处理器
 * 监听服务实例状态变化事件，并发布内部通知事件
 *
 * @author Lion Li
 */
@Slf4j
@Component
public class CustomNotifier extends AbstractEventNotifier {

    protected CustomNotifier(InstanceRepository repository) {
        super(repository);
    }

    /**
     * 处理实例事件通知
     * 当服务实例状态发生变化时，解析事件信息并发布通知
     *
     * @param event    实例事件
     * @param instance 实例信息
     * @return Mono<Void>
     */
    @Override
    @SuppressWarnings("all")
    protected Mono<Void> doNotify(InstanceEvent event, Instance instance) {
        return Mono.fromRunnable(() -> {
            // 只处理实例状态改变事件
            if (event instanceof InstanceStatusChangedEvent) {
                // 提取事件信息
                String registName = instance.getRegistration().getName();
                // 获取实例ID
                String instanceId = event.getInstance().getValue();
                // 获取实例状态
                String status = ((InstanceStatusChangedEvent) event).getStatusInfo().getStatus();
                // 获取服务URL
                String serviceUrl = instance.getRegistration().getServiceUrl();

                // 将状态码转换为中文描述
                String statusName = getStatusName(status);

                log.info("Instance Status Change: 状态名称【{}】, 注册名称【{}】, 实例ID【{}】, 状态【{}】, 服务URL【{}】",
                    statusName, registName, instanceId, status, serviceUrl);

                // 构建通知事件并发布
                NotifierEvent notifier = new NotifierEvent();
                notifier.setRegisterName(registName);
                notifier.setStatusName(statusName);
                notifier.setInstanceId(instanceId);
                notifier.setStatus(status);
                notifier.setServiceUrl(serviceUrl);

                // 发布事件，触发后续的通知处理
                SpringUtils.context().publishEvent(notifier);
            }
        });
    }

    /**
     * 将状态码转换为中文描述
     *
     * @param status 状态码
     * @return 中文状态描述
     */
    private String getStatusName(String status) {
        return switch (status) {
            case STATUS_UP -> "服务上线";
            case STATUS_OFFLINE -> "服务离线";
            case STATUS_RESTRICTED -> "服务受限";
            case STATUS_OUT_OF_SERVICE -> "停止服务状态";
            case STATUS_DOWN -> "服务下线";
            case STATUS_UNKNOWN -> "服务未知异常";
            default -> "未知状态";
        };
    }
}
