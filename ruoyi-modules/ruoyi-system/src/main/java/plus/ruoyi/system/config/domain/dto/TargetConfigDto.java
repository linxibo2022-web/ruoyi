package plus.ruoyi.system.config.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 推送配置DTO
 *
 * @author Lion Li
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TargetConfigDto {

    /**
     * 推送类型：all-全员 dept-部门 role-角色 user-指定用户
     */
    private String type;

    /**
     * 部门ID列表
     */
    private List<Long> ids;

    /**
     * 部门/角色/用户名称列表
     */
    private List<String> names;

    public static TargetConfigDto ofAll() {
        return new TargetConfigDto("all", null, null);
    }

    public static TargetConfigDto of(String type, List<Long> ids, List<String> names) {
        return new TargetConfigDto(type, ids, names);
    }
}
