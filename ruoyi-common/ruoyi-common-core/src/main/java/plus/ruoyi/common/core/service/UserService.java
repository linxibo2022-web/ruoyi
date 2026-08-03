package plus.ruoyi.common.core.service;

import plus.ruoyi.common.core.domain.dto.UserDTO;

import java.util.List;
import java.util.Map;

/**
 * 通用 用户服务
 *
 * @author Lion Li
 */
public interface UserService {

    /**
     * 通过用户ID查询用户账户
     *
     * @param userId 用户ID
     * @return 用户账户
     */
    String getUserNameById(Long userId);

    /**
     * 通过用户ID查询用户账户
     *
     * @param userId 用户ID
     * @return 用户名称
     */
    String getNickNameById(Long userId);

    /**
     * 通过用户ID列表查询用户昵称列表
     *
     * @param userIds 用户ID 多个用逗号隔开
     * @return 用户名称
     */
    String getNickNamesByIds(String userIds);

    /**
     * 通过用户ID查询用户头像
     *
     * @param userId 用户ID
     * @return 用户头像
     */
    String getAvatarById(Long userId);

    /**
     * 通过用户ID查询用户头像
     *
     * @param userIds 用户ID
     * @return 用户头像
     */
    String getAvatarsByIds(String userIds);


    /**
     * 通过用户ID查询用户手机号
     *
     * @param userId 用户id
     * @return 用户手机号
     */
    String getPhoneById(Long userId);

    /**
     * 通过用户ID查询用户邮箱
     *
     * @param userId 用户id
     * @return 用户邮箱
     */
    String getEmailById(Long userId);

    /**
     * 通过用户ID查询用户列表
     *
     * @param userIds 用户ids
     * @return 用户列表
     */
    List<UserDTO> listUsersByIds(List<Long> userIds);

    /**
     * 通过角色ID查询用户ID
     *
     * @param roleIds 角色ids
     * @return 用户ids
     */
    List<Long> listUserIdsByRoleIds(List<Long> roleIds);

    /**
     * 通过角色ID查询用户
     *
     * @param roleIds 角色ids
     * @return 用户
     */
    List<UserDTO> listUsersByRoleIds(List<Long> roleIds);

    /**
     * 通过部门ID查询用户
     *
     * @param deptIds 部门ids
     * @return 用户
     */
    List<UserDTO> listUsersByDeptIds(List<Long> deptIds);

    /**
     * 通过岗位ID查询用户
     *
     * @param postIds 岗位ids
     * @return 用户
     */
    List<UserDTO> listUsersByPostIds(List<Long> postIds);

    /**
     * 根据用户 ID 列表查询用户名称映射关系
     *
     * @param userIds 用户 ID 列表
     * @return Map，其中 key 为用户 ID，value 为对应的用户名称
     */
    Map<Long, String> mapUserNames(List<Long> userIds);

}
