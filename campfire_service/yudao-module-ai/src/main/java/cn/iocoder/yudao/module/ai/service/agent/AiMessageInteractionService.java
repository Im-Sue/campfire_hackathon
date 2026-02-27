package cn.iocoder.yudao.module.ai.service.agent;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiInteractionPageReqVO;
import cn.iocoder.yudao.module.ai.controller.app.agent.vo.AppInteractionStatsRespVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiMessageInteractionDO;

import java.util.List;

/**
 * AI 消息互动 Service 接口
 *
 * @author campfire
 */
public interface AiMessageInteractionService {

    /**
     * 送鲜花 (幂等)
     *
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @param userId 用户ID
     * @param walletAddress 钱包地址
     * @return 是否成功
     */
    boolean addFlower(Integer targetType, Long targetId, Long userId, String walletAddress);

    /**
     * 砸鸡蛋 (幂等)
     *
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @param userId 用户ID
     * @param walletAddress 钱包地址
     * @return 是否成功
     */
    boolean addEgg(Integer targetType, Long targetId, Long userId, String walletAddress);

    /**
     * 发表评论
     *
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @param userId 用户ID
     * @param walletAddress 钱包地址
     * @param content 内容
     * @return 互动ID
     */
    Long addComment(Integer targetType, Long targetId, Long userId, String walletAddress, String content);

    /**
     * 获取互动统计
     *
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @param userId 用户ID (可选，用于判断当前用户是否已送花/蛋)
     * @return 统计数据
     */
    AppInteractionStatsRespVO getStats(Integer targetType, Long targetId, Long userId);

    /**
     * 获取评论列表
     *
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @return 评论列表
     */
    List<AiMessageInteractionDO> getComments(Integer targetType, Long targetId);

    /**
     * 删除互动记录
     *
     * @param id 互动ID
     */
    void deleteInteraction(Long id);

    /**
     * 获得互动分页
     *
     * @param pageReqVO 分页查询
     * @return 互动分页
     */
    PageResult<AiMessageInteractionDO> getInteractionPage(AiInteractionPageReqVO pageReqVO);

}
