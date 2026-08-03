package plus.ruoyi.common.encrypt.core.encryptor;

import plus.ruoyi.common.encrypt.core.EncryptContext;
import plus.ruoyi.common.encrypt.core.IEncryptor;

/**
 * 抽象加密器基类
 *
 * 为所有加密器提供公共的初始化逻辑，子类继承此类并实现具体的加密算法
 *
 * @author 老马
 * @version 4.6.0
 */
public abstract class AbstractEncryptor implements IEncryptor {

    /**
     * 构造函数
     *
     * 在子类实例化时进行用户配置校验与配置注入
     *
     * @param context 加密上下文，包含算法、密钥等配置信息
     */
    public AbstractEncryptor(EncryptContext context) {
        // 用户配置校验与配置注入
        // 具体校验逻辑由子类在构造函数中实现
    }
}
