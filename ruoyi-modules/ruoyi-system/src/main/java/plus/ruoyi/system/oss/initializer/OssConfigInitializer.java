package plus.ruoyi.system.oss.initializer;

import plus.ruoyi.common.core.service.OssConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 初始化OSS配置
 *
 * @author Lion Li
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class OssConfigInitializer implements ApplicationRunner {

    private final OssConfigService ossConfigService;

    @Override
    public void run(ApplicationArguments args) {
        ossConfigService.initOssConfig();
        log.info("初始化OSS配置成功");
    }

}
