// RSA加密
import { SystemConfig } from '@/systemConfig'
/**
 * RSA加密解密工具
 *
 * 参考文档 https://www.npmjs.com/package/jsencrypt
 * 包含以下功能：
 * - 加密: 使用RSA公钥加密文本 (rsaEncrypt)
 * - 解密: 使用RSA私钥解密文本 (rsaDecrypt)
 * - 验证解密: 检查文本是否可以使用指定的私钥解密 (rsaCanDecrypt)
 * - 签名: 使用RSA私钥签名文本 (rsaSign)
 * - 验证签名: 验证RSA签名 (rsaVerify)
 * - 工具函数: 创建加密器实例 (createEncryptor)
 */
import JSEncrypt from 'jsencrypt'

// 从环境变量获取默认密钥
const defaultPublicKey = SystemConfig.security.rsaPublicKey
const defaultPrivateKey = SystemConfig.security.rsaPrivateKey

/**
 * 创建加密器实例
 * @returns JSEncrypt实例
 */
const createEncryptor = (): JSEncrypt => {
  return new JSEncrypt()
}

/**
 * 使用RSA公钥加密文本
 * @param {string} txt - 要加密的文本
 * @param {string} [pubKey] - 可选的自定义公钥，不提供则使用环境变量中的默认公钥
 * @returns {string | null} 加密后的文本，失败返回null
 */
export const rsaEncrypt = (txt: string, pubKey?: string): string | null => {
  try {
    if (!txt) {
      return null
    }

    const encryptor = createEncryptor()
    const keyToUse = pubKey || defaultPublicKey

    encryptor.setPublicKey(keyToUse)
    return encryptor.encrypt(txt)
  } catch (err) {
    console.error('RSA加密失败:', err)
    return null
  }
}

/**
 * 使用RSA私钥解密文本
 * @param {string} txt - 要解密的文本
 * @param {string} [privKey] - 可选的自定义私钥，不提供则使用环境变量中的默认私钥
 * @returns {string | null} 解密后的文本，失败返回null
 */
export const rsaDecrypt = (txt: string, privKey?: string): string | null => {
  try {
    if (!txt) {
      return null
    }

    const encryptor = createEncryptor()
    const keyToUse = privKey || defaultPrivateKey

    encryptor.setPrivateKey(keyToUse)
    return encryptor.decrypt(txt)
  } catch (err) {
    console.error('RSA解密失败:', err)
    return null
  }
}

/**
 * 检查文本是否可以使用指定的私钥解密
 * @param {string} txt - 要检查的加密文本
 * @param {string} [privKey] - 可选的私钥
 * @returns {boolean} 是否可以解密
 */
export const rsaCanDecrypt = (txt: string, privKey?: string): boolean => {
  return rsaDecrypt(txt, privKey) !== null
}

/**
 * 使用RSA私钥签名文本
 * @param {string} txt - 要签名的文本
 * @param {string} [privKey] - 可选的私钥
 * @returns {string | null} 签名结果
 */
export const rsaSign = (txt: string, privKey?: string): string | null => {
  try {
    const encryptor = createEncryptor()
    const keyToUse = privKey || defaultPrivateKey

    encryptor.setPrivateKey(keyToUse)
    // 使用SHA256哈希算法
    return encryptor.sign(txt, CryptoJS.SHA256, 'sha256')
  } catch (err) {
    console.error('RSA签名失败:', err)
    return null
  }
}

/**
 * 验证RSA签名
 * @param {string} txt - 原始文本
 * @param {string} signature - 签名
 * @param {string} [pubKey] - 可选的公钥
 * @returns {boolean} 验证结果
 */
export const rsaVerify = (txt: string, signature: string, pubKey?: string): boolean => {
  try {
    const encryptor = createEncryptor()
    const keyToUse = pubKey || defaultPublicKey

    encryptor.setPublicKey(keyToUse)
    return encryptor.verify(txt, signature, CryptoJS.SHA256)
  } catch (err) {
    console.error('RSA签名验证失败:', err)
    return false
  }
}
