package plus.ruoyi.common.core.utils;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * 授权模块完整性校验（RSA 签名验证版）
 *
 * <p>用于检测 ruoyi-common-license 模块是否完整，防止被删除或篡改。</p>
 * <p>使用 RSA 数字签名保护校验值，攻击者无法在没有私钥的情况下伪造签名。</p>
 *
 * <h3>使用方式</h3>
 * <p>在项目中任意位置调用即可（首次校验后会缓存结果，重复调用无性能开销）：</p>
 * <pre>{@code
 * ClientLicenseModuleValidator.check();
 * }</pre>
 *
 * <h3>建议调用位置（至少选一个）</h3>
 * <ul>
 *   <li>应用启动阶段（ApplicationRunner 或 @PostConstruct）</li>
 *   <li>登录接口</li>
 *   <li>其他关键业务入口</li>
 * </ul>
 *
 * <h3>放置位置</h3>
 * <p>复制本文件到：ruoyi-common/ruoyi-common-core/src/main/java/plus/ruoyi/common/core/utils/</p>
 *
 * <h3>更新校验值</h3>
 * <p>每次 ruoyi-common-license 发版后，运行开发版 LicenseModuleValidator 的 main 方法，
 * 将输出的【客户版代码】中的 _PK、EXPECTED_CHECKSUMS、_SIG 三个常量替换到本文件。</p>
 *
 * @author 抓蛙师
 */
public class ClientLicenseModuleValidator {

    private static volatile boolean passed = false;

    // ============================== ⬇⬇⬇ 由开发版 main 方法生成，请勿手动修改 ⬇⬇⬇ ==============================

    /**
     * RSA 公钥（Base64）
     * 用于验证校验值签名，确保校验值未被篡改
     */
    private static final String _PK = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA0/H2WKnfX7Jp/cto9qAOwicYvvOkQSGHUwUYOX7Yys/beN1jnipt7LeZ4Ju7U/1djXw05FnkiiHCIcwcu2eGIs/gsqC4YZo6xRjeBvcWaxl4zS+x6KrB4VKFQcSo1faf2RvEYDUCW4xxwwO1NGXMwwfaYA5fuKYZuSSq2ukoIZJJXcvkC3yYv0Sj6yxm/0Rz8XrIj0jdhD06w9GQd9YfXdgK6GNWLuviHDOHrELSw2BGlg2R85Df6CBMNO93FJWb1uBv7nGSiZspHeLEwyrhrnCcHw9SEmtHeshk/T+3egDtCiVE4NuEaUfo3OwvjyTAnpdxV7kejJJVsC6iTH0ZhQIDAQAB";

    /**
     * 校验值（明文存储，由 RSA 签名保护）
     * 格式: {资源路径, MD5}
     *
     * ⚠️ 篡改此处的值会导致签名验证失败
     */
    private static final String[][] EXPECTED_CHECKSUMS = {
            {"plus/ruoyi/common/license/initializer/LicenseVerifyInitializer.class", "1b4318b05be0605f39828f8fd21ef796"},
            {"plus/ruoyi/common/license/utils/OfflineLicenseValidator.class", "292de0fca5d07e1df15e7ecdca9ca864"},
            {"plus/ruoyi/common/license/utils/DeviceUtils.class", "2ecc3a1ca898d37819f0a616f6820400"},
            {"plus/ruoyi/common/license/utils/ConstantDecryptor.class", "ab32b83366e3aa9e440c38a723492fee"},
            {"plus/ruoyi/common/license/utils/LicenseEncryptUtils.class", "493cbc152cf0d8cac47539f5b9640da3"},
            {"plus/ruoyi/common/license/utils/StringDecryptor.class", "343c520801b2391e916ff3e7a8390fb5"},
            {"plus/ruoyi/common/license/config/LicenseAutoConfiguration.class", "ff76669910ac918ce52565a14ee99fc7"},
    };

    /**
     * RSA 数字签名（Base64）
     * 签名内容为所有 "资源路径:MD5" 用 "|" 连接的字符串
     */
    private static final String _SIG = "mIPVZdnxCakjV1yoVt8Cwtyn10w/4G6WLUyiE3E+FyXrqPqtl82NiZEYLRqMouBcAai6ZC6+u7iFzaeRzA640OT710KT0hGshjTwosaY9LUncF6wiV5tzAqgrzJiZ4BQ+ZElkA7eA7UkrQRbF7goVZmOEDL7vwKsvQFcFdfcRU3e08znxTE7CQd4dqv5ZT85sqgyRneGyZJywPwOGEm+22KWmrS34EN7jc5qczAn4vDY+Zfq8ThcNbBgPrzO0Qusu6oRwU6Vv/WXTAD+RNL4Q/eQCG69YevftNlQSBiGqvVcob8w4RH5EYKWrHnUy2eciws16zAahWMEpPvlDhIpPQ==";

    // ============================== ⬆⬆⬆ 由开发版 main 方法生成，请勿手动修改 ⬆⬆⬆ ==============================

    private static final String[] REQUIRED_CLASSES = {
            "plus.ruoyi.common.license.initializer.LicenseVerifyInitializer",
            "plus.ruoyi.common.license.utils.OfflineLicenseValidator",
            "plus.ruoyi.common.license.utils.DeviceUtils",
            "plus.ruoyi.common.license.utils.ConstantDecryptor",
            "plus.ruoyi.common.license.utils.LicenseEncryptUtils",
            "plus.ruoyi.common.license.utils.StringDecryptor",
            "plus.ruoyi.common.license.config.LicenseAutoConfiguration"
    };

    private static final String[][] REQUIRED_METHODS = {
            {"plus.ruoyi.common.license.initializer.LicenseVerifyInitializer", "run"},
            {"plus.ruoyi.common.license.utils.OfflineLicenseValidator", "verify"},
            {"plus.ruoyi.common.license.utils.OfflineLicenseValidator", "generate"},
            {"plus.ruoyi.common.license.utils.OfflineLicenseValidator", "validateFormat"},
            {"plus.ruoyi.common.license.utils.DeviceUtils", "generateDeviceFingerprint"},
            {"plus.ruoyi.common.license.utils.ConstantDecryptor", "decryptLicenseVerifyUrl"},
            {"plus.ruoyi.common.license.utils.LicenseEncryptUtils", "generateAesKey"},
            {"plus.ruoyi.common.license.utils.LicenseEncryptUtils", "encryptByAes"},
    };

    /**
     * 校验授权模块完整性（首次校验后缓存结果）
     */
    public static void check() {
        if (passed) {
            return;
        }
        synchronized (ClientLicenseModuleValidator.class) {
            if (passed) {
                return;
            }
            doCheck();
            passed = true;
        }
    }

    private static void doCheck() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = ClientLicenseModuleValidator.class.getClassLoader();
        }

        // ========== 第一层：类存在性检测 ==========
        for (String className : REQUIRED_CLASSES) {
            try {
                Class.forName(className, false, cl);
            } catch (ClassNotFoundException e) {
                onFailure("E01");
            }
        }

        // ========== 第二层：方法签名检测 ==========
        for (String[] entry : REQUIRED_METHODS) {
            try {
                Class<?> clazz = Class.forName(entry[0], false, cl);
                boolean found = false;
                for (Method m : clazz.getDeclaredMethods()) {
                    if (m.getName().equals(entry[1])) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    onFailure("E02");
                }
            } catch (ClassNotFoundException e) {
                onFailure("E01");
            }
        }

        // ========== 第三层：RSA 签名 + MD5 校验 ==========
        if (EXPECTED_CHECKSUMS.length > 0 && !_PK.isEmpty() && !_SIG.isEmpty()) {

            // 3a. 验证 RSA 签名（防篡改）
            StringBuilder signData = new StringBuilder();
            for (int i = 0; i < EXPECTED_CHECKSUMS.length; i++) {
                if (i > 0) signData.append("|");
                signData.append(EXPECTED_CHECKSUMS[i][0]).append(":").append(EXPECTED_CHECKSUMS[i][1]);
            }

            try {
                byte[] pubKeyBytes = Base64.getDecoder().decode(_PK);
                X509EncodedKeySpec keySpec = new X509EncodedKeySpec(pubKeyBytes);
                PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(keySpec);

                Signature sig = Signature.getInstance("SHA256withRSA");
                sig.initVerify(publicKey);
                sig.update(signData.toString().getBytes("UTF-8"));

                byte[] sigBytes = Base64.getDecoder().decode(_SIG);
                if (!sig.verify(sigBytes)) {
                    onFailure("E06");
                }
            } catch (Exception e) {
                onFailure("E06");
            }

            // 3b. 校验 class 字节码 MD5
            for (String[] entry : EXPECTED_CHECKSUMS) {
                String resourcePath = entry[0];
                String expectedMd5 = entry[1];
                try (InputStream is = cl.getResourceAsStream(resourcePath)) {
                    if (is == null) {
                        onFailure("E03");
                    }
                    if (!expectedMd5.equalsIgnoreCase(computeMd5(is))) {
                        onFailure("E04");
                    }
                } catch (Exception e) {
                    onFailure("E05");
                }
            }
        }
    }

    private static String computeMd5(InputStream is) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] buf = new byte[4096];
        int len;
        while ((len = is.read(buf)) != -1) {
            md.update(buf, 0, len);
        }
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder(32);
        for (byte b : digest) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    private static void onFailure(String code) {
        System.err.println();
        System.err.println("  ============================================");
        System.err.println("  FATAL: Framework module integrity check failed");
        System.err.println("  Code : " + code);
        System.err.println("  ============================================");
        System.err.println("  The license module has been removed or modified.");
        System.err.println("  Application cannot start without valid license module.");
        System.err.println();
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        System.exit(1);
    }
}
