package plus.ruoyi.client;

import com.dtflys.forest.annotation.*;
import com.dtflys.forest.http.ForestResponse;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.system.auth.domain.vo.AuthTokenVo;
import plus.ruoyi.system.core.domain.bo.SysRoleBo;
import plus.ruoyi.system.core.domain.bo.SysUserBo;
import plus.ruoyi.system.core.domain.vo.SysUserInfoVo;
import plus.ruoyi.system.core.domain.vo.SysUserVo;
import plus.ruoyi.common.mybatis.core.page.PageResult;

/**
 * 系统API客户端 - 用于集成测试
 * <p>
 * 使用Forest框架进行HTTP请求,测试真实的REST API接口
 * <p>
 * 注意: 测试前需要先启动应用(默认端口5500)
 *
 * @author 抓蛙师
 */
@BaseRequest(
    baseURL = "${baseUrl}",  // 应用服务器地址
    headers = {
        "Content-Type: application/json"
    }
)
public interface SystemApiClient {

    // ==================== 认证接口 ====================

    /**
     * 用户登录
     *
     * @param loginBody 登录信息(JSON字符串)
     * @return 登录结果,包含token
     */
    @Post("/auth/userLogin")
    ForestResponse<R<AuthTokenVo>> login(@JSONBody String loginBody);

    /**
     * 用户登出
     *
     * @param token 访问令牌
     * @return 登出结果
     */
    @Post("/auth/userLogout")
    ForestResponse<R<Void>> logout(@Header("Authorization") String token);

    // ==================== 用户管理接口 ====================

    /**
     * 查询用户详情
     *
     * @param token  访问令牌
     * @param userId 用户ID
     * @return 用户详情(包含用户基本信息、角色和岗位)
     */
    @Get("/system/user/getUser/{userId}")
    ForestResponse<R<SysUserInfoVo>> getUserById(
        @Header("Authorization") String token,
        @Var("userId") Long userId
    );

    /**
     * 分页查询用户列表
     *
     * @param token     访问令牌
     * @param pageNum   页码
     * @param pageSize  每页数量
     * @param userName  用户名(可选)
     * @return 用户分页列表
     */
    @Get("/system/user/pageUsers")
    ForestResponse<R<PageResult<SysUserVo>>> pageUsers(
        @Header("Authorization") String token,
        @Query("pageNum") Integer pageNum,
        @Query("pageSize") Integer pageSize,
        @Query("userName") String userName
    );

    /**
     * 新增用户
     *
     * @param token 访问令牌
     * @param user  用户信息
     * @return 新增结果,返回用户ID
     */
    @Post("/system/user/addUser")
    ForestResponse<R<Long>> insertUser(
        @Header("Authorization") String token,
        @JSONBody SysUserBo user
    );

    /**
     * 修改用户
     *
     * @param token 访问令牌
     * @param user  用户信息
     * @return 修改结果
     */
    @Put("/system/user/updateUser")
    ForestResponse<R<Void>> updateUser(
        @Header("Authorization") String token,
        @JSONBody SysUserBo user
    );

    /**
     * 删除用户
     *
     * @param token   访问令牌
     * @param userIds 用户ID数组
     * @return 删除结果
     */
    @Delete("/system/user/deleteUsers/{userIds}")
    ForestResponse<R<Void>> deleteUser(
        @Header("Authorization") String token,
        @Var("userIds") Long userIds
    );

    /**
     * 获取当前登录用户信息
     *
     * @param token 访问令牌
     * @return 当前用户信息
     */
    @Get("/system/user/getUserInfo")
    ForestResponse<R<Object>> getUserInfo(
        @Header("Authorization") String token
    );

    /**
     * 重置用户密码
     *
     * @param token 访问令牌
     * @param user  用户信息(只需userId和password)
     * @return 操作结果
     */
    @Put("/system/user/resetUserPwd")
    ForestResponse<R<Void>> resetUserPwd(
        @Header("Authorization") String token,
        @Body SysUserBo user
    );

    /**
     * 修改用户状态
     *
     * @param token 访问令牌
     * @param user  用户信息(只需userId和status)
     * @return 操作结果
     */
    @Put("/system/user/changeUserStatus")
    ForestResponse<R<Void>> changeUserStatus(
        @Header("Authorization") String token,
        @Body SysUserBo user
    );

    /**
     * 获取用户选择框列表
     *
     * @param token 访问令牌
     * @return 用户选择框列表
     */
    @Get("/system/user/getUserOptions")
    ForestResponse<R<Object>> getUserOptions(
        @Header("Authorization") String token
    );

    // ==================== 角色管理接口 ====================

    /**
     * 查询角色详情
     *
     * @param token  访问令牌
     * @param roleId 角色ID
     * @return 角色详情
     */
    @Get("/system/role/getRole/{roleId}")
    ForestResponse<R<Object>> getRoleById(
        @Header("Authorization") String token,
        @Var("roleId") Long roleId
    );

    /**
     * 分页查询角色列表
     *
     * @param token    访问令牌
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 角色分页列表
     */
    @Get("/system/role/pageRoles")
    ForestResponse<R<PageResult<Object>>> pageRoles(
        @Header("Authorization") String token,
        @Query("pageNum") Integer pageNum,
        @Query("pageSize") Integer pageSize
    );

    /**
     * 新增角色
     *
     * @param token 访问令牌
     * @param role  角色信息(JSON)
     * @return 新增结果
     */
    @Post("/system/role/addRole")
    ForestResponse<R<Void>> addRole(
        @Header("Authorization") String token,
        @JSONBody Object role
    );

    /**
     * 修改角色
     *
     * @param token 访问令牌
     * @param role  角色信息(JSON)
     * @return 修改结果
     */
    @Put("/system/role/updateRole")
    ForestResponse<R<Void>> updateRole(
        @Header("Authorization") String token,
        @JSONBody Object role
    );

    /**
     * 删除角色
     *
     * @param token   访问令牌
     * @param roleIds 角色ID数组
     * @return 删除结果
     */
    @Delete("/system/role/deleteRoles/{roleIds}")
    ForestResponse<R<Void>> deleteRoles(
        @Header("Authorization") String token,
        @Var("roleIds") String roleIds
    );

    /**
     * 修改角色状态
     *
     * @param token 访问令牌
     * @param role  角色信息(只需roleId和status)
     * @return 操作结果
     */
    @Put("/system/role/changeRoleStatus")
    ForestResponse<R<Void>> changeRoleStatus(
        @Header("Authorization") String token,
        @Body SysRoleBo role
    );

    /**
     * 获取角色选择框列表
     *
     * @param token 访问令牌
     * @return 角色选择框列表
     */
    @Get("/system/role/getRoleOptions")
    ForestResponse<R<Object>> getRoleOptions(
        @Header("Authorization") String token
    );

    // ==================== 菜单管理接口 ====================

    /**
     * 获取路由信息
     *
     * @param token 访问令牌
     * @return 路由列表
     */
    @Get("/system/menu/getRouters")
    ForestResponse<R<Object>> getRouters(
        @Header("Authorization") String token
    );

    /**
     * 获取菜单列表
     *
     * @param token 访问令牌
     * @return 菜单列表
     */
    @Get("/system/menu/listMenus")
    ForestResponse<R<Object>> listMenus(
        @Header("Authorization") String token
    );

    /**
     * 查询菜单详情
     *
     * @param token  访问令牌
     * @param menuId 菜单ID
     * @return 菜单详情
     */
    @Get("/system/menu/getMenu/{menuId}")
    ForestResponse<R<Object>> getMenuById(
        @Header("Authorization") String token,
        @Var("menuId") Long menuId
    );

    /**
     * 获取菜单下拉树列表
     *
     * @param token 访问令牌
     * @return 菜单树
     */
    @Get("/system/menu/getMenuTreeOptions")
    ForestResponse<R<Object>> getMenuTreeOptions(
        @Header("Authorization") String token
    );

    /**
     * 新增菜单
     *
     * @param token 访问令牌
     * @param menu  菜单信息(JSON)
     * @return 新增结果
     */
    @Post("/system/menu/addMenu")
    ForestResponse<R<Void>> addMenu(
        @Header("Authorization") String token,
        @JSONBody Object menu
    );

    /**
     * 修改菜单
     *
     * @param token 访问令牌
     * @param menu  菜单信息(JSON)
     * @return 修改结果
     */
    @Put("/system/menu/updateMenu")
    ForestResponse<R<Void>> updateMenu(
        @Header("Authorization") String token,
        @JSONBody Object menu
    );

    /**
     * 删除菜单
     *
     * @param token  访问令牌
     * @param menuId 菜单ID
     * @return 删除结果
     */
    @Delete("/system/menu/deleteMenu/{menuId}")
    ForestResponse<R<Void>> deleteMenu(
        @Header("Authorization") String token,
        @Var("menuId") Long menuId
    );

    // ==================== 部门管理接口 ====================

    /**
     * 获取部门列表
     *
     * @param token 访问令牌
     * @return 部门列表
     */
    @Get("/system/dept/listDepts")
    ForestResponse<R<Object>> listDepts(
        @Header("Authorization") String token
    );

    /**
     * 查询部门详情
     *
     * @param token  访问令牌
     * @param deptId 部门ID
     * @return 部门详情
     */
    @Get("/system/dept/getDept/{deptId}")
    ForestResponse<R<Object>> getDeptById(
        @Header("Authorization") String token,
        @Var("deptId") Long deptId
    );

    /**
     * 获取部门树列表
     *
     * @param token 访问令牌
     * @return 部门树
     */
    @Get("/system/dept/getDeptTreeOptions")
    ForestResponse<R<Object>> getDeptTreeOptions(
        @Header("Authorization") String token
    );

    /**
     * 新增部门
     *
     * @param token 访问令牌
     * @param dept  部门信息(JSON)
     * @return 新增结果
     */
    @Post("/system/dept/addDept")
    ForestResponse<R<Void>> addDept(
        @Header("Authorization") String token,
        @JSONBody Object dept
    );

    /**
     * 修改部门
     *
     * @param token 访问令牌
     * @param dept  部门信息(JSON)
     * @return 修改结果
     */
    @Put("/system/dept/updateDept")
    ForestResponse<R<Void>> updateDept(
        @Header("Authorization") String token,
        @JSONBody Object dept
    );

    /**
     * 删除部门
     *
     * @param token  访问令牌
     * @param deptId 部门ID
     * @return 删除结果
     */
    @Delete("/system/dept/deleteDept/{deptId}")
    ForestResponse<R<Void>> deleteDept(
        @Header("Authorization") String token,
        @Var("deptId") Long deptId
    );

    // ==================== 字典管理接口 ====================

    /**
     * 分页查询字典数据列表
     *
     * @param token    访问令牌
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 字典数据分页列表
     */
    @Get("/system/dictData/pageDictDatas")
    ForestResponse<R<PageResult<Object>>> pageDictDatas(
        @Header("Authorization") String token,
        @Query("pageNum") Integer pageNum,
        @Query("pageSize") Integer pageSize
    );

    /**
     * 查询字典数据详情
     *
     * @param token      访问令牌
     * @param dictDataId 字典数据ID
     * @return 字典数据详情
     */
    @Get("/system/dictData/getDictData/{dictDataId}")
    ForestResponse<R<Object>> getDictDataById(
        @Header("Authorization") String token,
        @Var("dictDataId") Long dictDataId
    );

    /**
     * 根据字典类型查询字典数据
     *
     * @param token    访问令牌
     * @param dictType 字典类型
     * @return 字典数据列表
     */
    @Get("/system/dictData/listDictDatasByDictType/{dictType}")
    ForestResponse<R<Object>> listDictDatasByDictType(
        @Header("Authorization") String token,
        @Var("dictType") String dictType
    );

    // ==================== 字典类型接口 ====================

    /**
     * 分页查询字典类型列表
     *
     * @param token    访问令牌
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 字典类型分页列表
     */
    @Get("/system/dictType/pageDictTypes")
    ForestResponse<R<PageResult<Object>>> pageDictTypes(
        @Header("Authorization") String token,
        @Query("pageNum") Integer pageNum,
        @Query("pageSize") Integer pageSize
    );

    /**
     * 查询字典类型详情
     *
     * @param token  访问令牌
     * @param dictId 字典类型ID
     * @return 字典类型详情
     */
    @Get("/system/dictType/getDictType/{dictId}")
    ForestResponse<R<Object>> getDictTypeById(
        @Header("Authorization") String token,
        @Var("dictId") Long dictId
    );

    /**
     * 获取字典类型选项列表
     *
     * @param token 访问令牌
     * @return 字典类型选项列表
     */
    @Get("/system/dictType/getDictTypeOptions")
    ForestResponse<R<Object>> getDictTypeOptions(
        @Header("Authorization") String token
    );

    // ==================== 配置管理接口 ====================

    /**
     * 分页查询参数配置列表
     *
     * @param token    访问令牌
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 配置分页列表
     */
    @Get("/system/config/pageConfigs")
    ForestResponse<R<PageResult<Object>>> pageConfigs(
        @Header("Authorization") String token,
        @Query("pageNum") Integer pageNum,
        @Query("pageSize") Integer pageSize
    );

    /**
     * 查询配置详情
     *
     * @param token    访问令牌
     * @param configId 配置ID
     * @return 配置详情
     */
    @Get("/system/config/getConfig/{configId}")
    ForestResponse<R<Object>> getConfigById(
        @Header("Authorization") String token,
        @Var("configId") Long configId
    );

    /**
     * 根据参数键名查询参数值
     *
     * @param token     访问令牌
     * @param configKey 配置键
     * @return 配置值
     */
    @Get("/system/config/getByConfigKey/{configKey}")
    ForestResponse<R<String>> getConfigByKey(
        @Header("Authorization") String token,
        @Var("configKey") String configKey
    );

    // ==================== 岗位管理接口 ====================

    /**
     * 分页查询岗位列表
     *
     * @param token    访问令牌
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 岗位分页列表
     */
    @Get("/system/post/pagePosts")
    ForestResponse<R<PageResult<Object>>> pagePosts(
        @Header("Authorization") String token,
        @Query("pageNum") Integer pageNum,
        @Query("pageSize") Integer pageSize
    );

    /**
     * 查询岗位详情
     *
     * @param token  访问令牌
     * @param postId 岗位ID
     * @return 岗位详情
     */
    @Get("/system/post/getPost/{postId}")
    ForestResponse<R<Object>> getPostById(
        @Header("Authorization") String token,
        @Var("postId") Long postId
    );

    /**
     * 获取岗位选择框列表
     *
     * @param token 访问令牌
     * @return 岗位列表
     */
    @Get("/system/post/getPostOptions")
    ForestResponse<R<Object>> getPostOptions(
        @Header("Authorization") String token
    );

    // ==================== 通知公告接口 ====================

    /**
     * 分页查询通知公告列表
     *
     * @param token    访问令牌
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 通知公告分页列表
     */
    @Get("/system/notice/pageNotices")
    ForestResponse<R<PageResult<Object>>> pageNotices(
        @Header("Authorization") String token,
        @Query("pageNum") Integer pageNum,
        @Query("pageSize") Integer pageSize
    );

    /**
     * 查询通知公告详情
     *
     * @param token    访问令牌
     * @param noticeId 公告ID
     * @return 公告详情
     */
    @Get("/system/notice/getNotice/{noticeId}")
    ForestResponse<R<Object>> getNoticeById(
        @Header("Authorization") String token,
        @Var("noticeId") Long noticeId
    );

    /**
     * 获取用户通知列表
     *
     * @param token    访问令牌
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 用户通知分页列表
     */
    @Get("/system/notice/pageUserNotices")
    ForestResponse<R<PageResult<Object>>> pageUserNotices(
        @Header("Authorization") String token,
        @Query("pageNum") Integer pageNum,
        @Query("pageSize") Integer pageSize
    );

    /**
     * 获取未读通知数量
     *
     * @param token 访问令牌
     * @return 未读数量
     */
    @Get("/system/notice/getNoticeUnreadCount")
    ForestResponse<R<Long>> getNoticeUnreadCount(
        @Header("Authorization") String token
    );

    /**
     * 标记公告为已读
     *
     * @param token    访问令牌
     * @param noticeId 公告ID
     * @return 操作结果
     */
    @Post("/system/notice/markNoticeAsRead/{noticeId}")
    ForestResponse<R<Void>> markNoticeAsRead(
        @Header("Authorization") String token,
        @Var("noticeId") Long noticeId
    );

    // ==================== 租户管理接口 ====================

    /**
     * 分页查询租户列表
     *
     * @param token    访问令牌
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 租户分页列表
     */
    @Get("/system/tenant/pageTenants")
    ForestResponse<R<PageResult<Object>>> pageTenants(
        @Header("Authorization") String token,
        @Query("pageNum") Integer pageNum,
        @Query("pageSize") Integer pageSize
    );

    /**
     * 查询租户详情
     *
     * @param token 访问令牌
     * @param id    租户主键ID
     * @return 租户详情
     */
    @Get("/system/tenant/getTenant/{id}")
    ForestResponse<R<Object>> getTenantById(
        @Header("Authorization") String token,
        @Var("id") Long id
    );

    /**
     * 分页查询租户套餐列表
     *
     * @param token    访问令牌
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 租户套餐分页列表
     */
    @Get("/system/tenant/pageTenantPackages")
    ForestResponse<R<PageResult<Object>>> pageTenantPackages(
        @Header("Authorization") String token,
        @Query("pageNum") Integer pageNum,
        @Query("pageSize") Integer pageSize
    );

    // ==================== 登录日志接口 ====================

    /**
     * 分页查询登录日志列表
     *
     * @param token    访问令牌
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 登录日志分页列表
     */
    @Get("/monitor/loginLog/pageLoginLogs")
    ForestResponse<R<PageResult<Object>>> pageLoginLogs(
        @Header("Authorization") String token,
        @Query("pageNum") Integer pageNum,
        @Query("pageSize") Integer pageSize
    );

    // ==================== 操作日志接口 ====================

    /**
     * 分页查询操作日志列表
     *
     * @param token    访问令牌
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 操作日志分页列表
     */
    @Get("/monitor/operLog/pageOperLogs")
    ForestResponse<R<PageResult<Object>>> pageOperlogs(
        @Header("Authorization") String token,
        @Query("pageNum") Integer pageNum,
        @Query("pageSize") Integer pageSize
    );

    // ==================== 在线用户接口 ====================

    /**
     * 分页查询在线用户列表
     *
     * @param token    访问令牌
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 在线用户分页列表
     */
    @Get("/monitor/online/pageOnlineUsers")
    ForestResponse<R<PageResult<Object>>> pageOnlineUsers(
        @Header("Authorization") String token,
        @Query("pageNum") Integer pageNum,
        @Query("pageSize") Integer pageSize
    );

    // ==================== 缓存监控接口 ====================

    /**
     * 获取缓存信息
     *
     * @param token 访问令牌
     * @return 缓存信息
     */
    @Get("/monitor/cache/getCacheInfo")
    ForestResponse<R<Object>> getCacheInfo(
        @Header("Authorization") String token
    );
}
