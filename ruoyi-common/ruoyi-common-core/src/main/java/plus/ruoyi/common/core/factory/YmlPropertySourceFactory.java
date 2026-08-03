package plus.ruoyi.common.core.factory;

import plus.ruoyi.common.core.utils.StringUtils;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.core.io.support.EncodedResource;

import java.io.IOException;

/**
 * yml 配置源工厂
 *
 * 用于支持 Spring 加载 YAML 格式的配置文件
 * 扩展默认的属性源工厂以处理 .yml 和 .yaml 文件
 *
 * @author Lion Li
 */
public class YmlPropertySourceFactory extends DefaultPropertySourceFactory {

    /**
     * 创建属性源
     *
     * @param name 属性源名称
     * @param resource 编码资源
     * @return 属性源对象
     * @throws IOException IO异常
     */
    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        // 获取资源文件名
        String sourceName = resource.getResource().getFilename();

        // 判断是否为 YAML 格式文件
        if (StringUtils.isNotBlank(sourceName) && StringUtils.endsWithAny(sourceName, ".yml", ".yaml")) {
            // 创建 YAML 属性工厂
            YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
            factory.setResources(resource.getResource());
            factory.afterPropertiesSet();

            // 返回 YAML 属性源
            return new PropertiesPropertySource(sourceName, factory.getObject());
        }

        // 非 YAML 文件使用默认处理方式
        return super.createPropertySource(name, resource);
    }

}
