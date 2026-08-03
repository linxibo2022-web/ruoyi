/**
 * 加密工具类
 *
 * 包含以下功能：
 * - 随机生成: 生成随机字符串和AES密钥 (generateRandomString, generateAesKey)
 * - Base64处理: Base64编码与解码 (encodeBase64, decodeBase64)
 * - AES加密/解密: 使用AES密钥加密和解密数据 (encryptWithAes, decryptWithAes)
 * - 高级加解密: 自动密钥生成的加密和支持JSON解析的解密 (encryptWithAutoKey, decryptWithParsing)
 * - 哈希计算: 计算数据的哈希值，用于唯一标识 (computeSha256Hash, computeMd5Hash, generateImageHash, generateFileHash)
 */
import CryptoJS from 'crypto-js'

/**
 * 随机生成32位的字符串
 * @returns {string} 随机字符串
 */
export const generateRandomString = (): string => {
  const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
  let result = ''
  const charactersLength = characters.length
  for (let i = 0; i < 32; i++) {
    result += characters.charAt(Math.floor(Math.random() * charactersLength))
  }
  return result
}

/**
 * 随机生成AES密钥
 * @returns {CryptoJS.lib.WordArray} AES密钥
 */
export const generateAesKey = (): CryptoJS.lib.WordArray => {
  return CryptoJS.enc.Utf8.parse(generateRandomString())
}

/**
 * Base64编码
 * @param {CryptoJS.lib.WordArray} data - 要编码的数据
 * @returns {string} Base64编码后的字符串
 */
export const encodeBase64 = (data: CryptoJS.lib.WordArray): string => {
  return CryptoJS.enc.Base64.stringify(data)
}

/**
 * Base64解码
 * @param {string} str - Base64编码的字符串
 * @returns {CryptoJS.lib.WordArray} 解码后的数据
 */
export const decodeBase64 = (str: string): CryptoJS.lib.WordArray => {
  return CryptoJS.enc.Base64.parse(str)
}

/**
 * 使用AES密钥加密数据
 * @param {string} message - 要加密的消息
 * @param {CryptoJS.lib.WordArray} aesKey - AES密钥
 * @returns {string} 加密后的字符串
 */
export const encryptWithAes = (message: string, aesKey: CryptoJS.lib.WordArray): string => {
  const encrypted = CryptoJS.AES.encrypt(message, aesKey, {
    mode: CryptoJS.mode.ECB,
    padding: CryptoJS.pad.Pkcs7
  })
  return encrypted.toString()
}

/**
 * 使用AES密钥解密数据
 * @param {string} message - 加密的消息
 * @param {CryptoJS.lib.WordArray} aesKey - AES密钥
 * @returns {string} 解密后的字符串
 */
export const decryptWithAes = (message: string, aesKey: CryptoJS.lib.WordArray): string => {
  const decrypted = CryptoJS.AES.decrypt(message, aesKey, {
    mode: CryptoJS.mode.ECB,
    padding: CryptoJS.pad.Pkcs7
  })
  return decrypted.toString(CryptoJS.enc.Utf8)
}

/**
 * 一步完成加密过程 - 生成密钥、加密数据并返回结果
 * @param {string | object} data - 要加密的数据
 * @returns {{ encryptedData: string, key: CryptoJS.lib.WordArray }} 加密后的数据和使用的密钥
 */
export const encryptWithAutoKey = (data: string | object): { encryptedData: string; key: CryptoJS.lib.WordArray } => {
  const key = generateAesKey()
  const message = typeof data === 'object' ? JSON.stringify(data) : data
  const encryptedData = encryptWithAes(message, key)

  return {
    encryptedData,
    key
  }
}

/**
 * 使用提供的密钥解密数据
 * @param {string} encryptedData - 加密的数据
 * @param {CryptoJS.lib.WordArray} key - 解密密钥
 * @param {boolean} parseJson - 是否将结果解析为JSON对象
 * @returns {string | object} 解密后的数据
 */
export const decryptWithParsing = (encryptedData: string, key: CryptoJS.lib.WordArray, parseJson: boolean = false): string | object => {
  const decryptedStr = decryptWithAes(encryptedData, key)

  if (parseJson) {
    try {
      return JSON.parse(decryptedStr)
    } catch (e) {
      console.error('Failed to parse decrypted data as JSON:', e)
      return decryptedStr
    }
  }

  return decryptedStr
}

/**
 * 本地存储加密（用于"记住密码"等场景）
 * 使用固定密钥对敏感数据进行 AES 加密后再存储到本地
 * @param {string} plainText - 要加密的明文
 * @returns {string} 加密后的密文
 */
export const encryptLocal = (plainText: string): string => {
  const key = CryptoJS.enc.Utf8.parse(import.meta.env.VITE_APP_RSA_PRIVATE_KEY.substring(0, 32))
  return encryptWithAes(plainText, key)
}

/**
 * 本地存储解密（用于"记住密码"等场景）
 * @param {string} cipherText - 要解密的密文
 * @returns {string} 解密后的明文，解密失败返回空字符串
 */
export const decryptLocal = (cipherText: string): string => {
  try {
    const key = CryptoJS.enc.Utf8.parse(import.meta.env.VITE_APP_RSA_PRIVATE_KEY.substring(0, 32))
    return decryptWithAes(cipherText, key)
  } catch {
    return ''
  }
}

/**
 * 计算字符串的SHA-256哈希值
 * @param {string} data - 要计算哈希的数据
 * @returns {string} 哈希值（十六进制表示）
 */
export const computeSha256Hash = (data: string): string => {
  return CryptoJS.SHA256(data).toString(CryptoJS.enc.Hex)
}

/**
 * 计算字符串的MD5哈希值
 * @param {string} data - 要计算哈希的数据
 * @returns {string} 哈希值（十六进制表示）
 */
export const computeMd5Hash = (data: string): string => {
  return CryptoJS.MD5(data).toString(CryptoJS.enc.Hex)
}

/**
 * 生成图片的唯一哈希标识
 * @param {string} base64Data - 图片的Base64编码数据
 * @returns {string} 图片的唯一哈希值
 */
export const generateImageHash = (base64Data: string): string => {
  return computeSha256Hash(base64Data)
}

/**
 * 异步生成文件的哈希标识
 * @param {Blob | File | ArrayBuffer} file - 文件或数据流
 * @returns {Promise<string>} 文件的哈希值
 */
export const generateFileHash = async (file: Blob | File | ArrayBuffer): Promise<string> => {
  // 将文件转换为ArrayBuffer
  let arrayBuffer: ArrayBuffer

  if (file instanceof Blob || file instanceof File) {
    arrayBuffer = await file.arrayBuffer()
  } else {
    arrayBuffer = file
  }

  // 将ArrayBuffer转换为字符串
  const uint8Array = new Uint8Array(arrayBuffer)
  let binaryString = ''
  for (let i = 0; i < uint8Array.length; i++) {
    binaryString += String.fromCharCode(uint8Array[i])
  }

  // 计算哈希值
  return computeSha256Hash(binaryString)
}
