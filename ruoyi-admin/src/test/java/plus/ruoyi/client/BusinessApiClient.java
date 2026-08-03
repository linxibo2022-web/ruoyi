package plus.ruoyi.client;

import com.dtflys.forest.annotation.*;
import com.dtflys.forest.http.ForestResponse;
import plus.ruoyi.common.core.domain.R;
import plus.ruoyi.common.mybatis.core.page.PageResult;

import java.util.List;

/**
 * 业务模块API客户端 - 用于集成测试
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
public interface BusinessApiClient {

    // ==================== 首页接口 ====================

    /**
     * 根据appid获取租户标识
     *
     * @param appid 小程序或应用的唯一标识
     * @return 租户编号
     */
    @Get("/app/home/getTenantIdByAppid")
    ForestResponse<R<String>> getTenantIdByAppid(@Query("appid") String appid);

    /**
     * 查询广告列表
     *
     * @param position 广告位置(可选)
     * @return 广告列表
     */
    @Get("/app/home/listAds")
    ForestResponse<R<List<Object>>> listAds(@Query("position") String position);

    /**
     * 分页查询商品
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 商品分页数据
     */
    @Get("/app/home/pageGoods")
    ForestResponse<R<PageResult<Object>>> pageGoods(
        @Query("pageNum") Integer pageNum,
        @Query("pageSize") Integer pageSize
    );

    // ==================== AI聊天接口 ====================

    /**
     * 同步对话
     *
     * @param token   访问令牌
     * @param request 对话请求参数(JSON)
     * @return 对话回复
     */
    @Post("/common/ai/chat/syncChat")
    ForestResponse<R<Object>> syncChat(
        @Header("Authorization") String token,
        @JSONBody Object request
    );

    /**
     * 创建会话
     *
     * @param token 访问令牌
     * @return 会话标识
     */
    @Post("/common/ai/chat/createSession")
    ForestResponse<R<String>> createSession(@Header("Authorization") String token);

    /**
     * 清除会话
     *
     * @param token     访问令牌
     * @param sessionId 会话标识
     * @return 操作结果
     */
    @Delete("/common/ai/chat/deleteSession/{sessionId}")
    ForestResponse<R<Void>> deleteSession(
        @Header("Authorization") String token,
        @Var("sessionId") String sessionId
    );

    /**
     * 查询会话历史
     *
     * @param token     访问令牌
     * @param sessionId 会话标识
     * @return 消息列表
     */
    @Get("/common/ai/chat/getSessionMessages/{sessionId}")
    ForestResponse<R<List<Object>>> getSessionMessages(
        @Header("Authorization") String token,
        @Var("sessionId") String sessionId
    );

    /**
     * 快速测试AI对话
     *
     * @param token   访问令牌
     * @param message 测试问题
     * @return 对话回复
     */
    @Get("/common/ai/chat/testChat")
    ForestResponse<R<Object>> testChat(
        @Header("Authorization") String token,
        @Query("message") String message
    );

    // ==================== 统计接口 ====================

    /**
     * 获取首页统计数据
     *
     * @param token 访问令牌
     * @return 首页统计视图
     */
    @Get("/common/base/statistics/getHomeStatistics")
    ForestResponse<R<Object>> getHomeStatistics(@Header("Authorization") String token);

    // ==================== 系统功能配置接口 ====================

    /**
     * 获取系统功能开关
     *
     * @return 功能配置
     */
    @Get("/common/system/features")
    ForestResponse<R<Object>> getSystemFeatures();
}
