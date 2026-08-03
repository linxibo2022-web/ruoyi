package plus.ruoyi.system.core.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import plus.ruoyi.common.mybatis.core.domain.BaseEntity;
import plus.ruoyi.system.core.domain.SysPost;

import java.util.List;

/**
 * 岗位信息业务对象 sys_post
 *
 * @author Michelle.Chung
 */

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = SysPost.class, reverseConvertGenerate = false)
public class SysPostBo extends BaseEntity {

    /**
     * 岗位ID
     */
    private Long postId;

    /**
     * 部门id（单部门）
     */
    @NotNull(message = "部门id不能为空")
    private Long deptId;

    /**
     * 归属部门id（部门树）
     */
    private Long belongDeptId;

    /**
     * 部门ID列表（Service层设置，用于部门树查询）
     */
    private List<Long> deptIds;

    /**
     * 岗位编码
     */
    @NotBlank(message = "岗位编码不能为空")
    @Size(min = 0, max = 64, message = "岗位编码长度不能超过{max}个字符")
    private String postCode;

    /**
     * 岗位名称
     */
    @NotBlank(message = "岗位名称不能为空")
    @Size(min = 0, max = 50, message = "岗位名称长度不能超过{max}个字符")
    private String postName;

    /**
     * 岗位类别编码
     */
    @Size(min = 0, max = 100, message = "类别编码长度不能超过{max}个字符")
    private String postCategory;

    /**
     * 显示顺序
     */
    @NotNull(message = "显示顺序不能为空")
    private Integer postSort;

    /**
     * 状态
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

}
