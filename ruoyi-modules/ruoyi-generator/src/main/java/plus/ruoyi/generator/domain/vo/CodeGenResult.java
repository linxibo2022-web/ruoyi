package plus.ruoyi.generator.domain.vo;

import lombok.Data;

/**
 * 代码生成结果VO
 * <p>包含代码生成和菜单导入的详细信息</p>
 *
 * @author Lion Li
 */
@Data
public class CodeGenResult {

    /**
     * 生成是否成功
     */
    private Boolean success;

    /**
     * 生成的文件数量
     */
    private Integer fileCount;

    /**
     * 覆盖的文件数量
     */
    private Integer overwriteCount;

    /**
     * 菜单导入状态
     * <ul>
     *   <li>null - 未开启自动导入</li>
     *   <li>"菜单导入成功" - 成功导入</li>
     *   <li>"菜单已存在，跳过导入" - 已存在，跳过</li>
     *   <li>"菜单导入失败: xxx" - 导入失败</li>
     *   <li>"生成方式为zip下载，跳过菜单导入" - zip方式</li>
     *   <li>"生成路径非当前项目，跳过菜单导入" - 其他项目</li>
     * </ul>
     */
    private String menuImportResult;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 创建成功结果
     *
     * @param fileCount        文件数量
     * @param overwriteCount   覆盖数量
     * @param menuImportResult 菜单导入结果
     * @return 结果对象
     */
    public static CodeGenResult success(Integer fileCount, Integer overwriteCount, String menuImportResult) {
        CodeGenResult result = new CodeGenResult();
        result.setSuccess(true);
        result.setFileCount(fileCount);
        result.setOverwriteCount(overwriteCount);
        result.setMenuImportResult(menuImportResult);
        return result;
    }

    /**
     * 创建失败结果
     *
     * @param errorMessage 错误信息
     * @return 结果对象
     */
    public static CodeGenResult failure(String errorMessage) {
        CodeGenResult result = new CodeGenResult();
        result.setSuccess(false);
        result.setErrorMessage(errorMessage);
        return result;
    }
}
