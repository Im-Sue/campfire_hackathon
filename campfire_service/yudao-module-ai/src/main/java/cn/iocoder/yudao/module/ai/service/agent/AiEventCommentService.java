package cn.iocoder.yudao.module.ai.service.agent;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiEventCommentPageReqVO;
import cn.iocoder.yudao.module.ai.controller.app.agent.vo.AppAiEventCommentRespVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiEventCommentDO;

import java.util.List;

/**
 * AI Agent 事件评论 Service 接口
 *
 * @author campfire
 */
public interface AiEventCommentService {

    /**
     * 为事件生成所有 Agent 评论
     *
     * @param eventId 事件 ID
     */
    void generateCommentForEvent(Long eventId);

    /**
     * 重新生成评论
     *
     * @param eventId 事件 ID
     * @param agentId Agent ID（为空则重新生成全部）
     */
    void regenerateComment(Long eventId, Long agentId);

    /**
     * 获取事件的所有 Agent 评论（C端展示用）
     *
     * @param eventId 事件 ID
     * @return 评论列表（含 Agent 信息）
     */
    List<AppAiEventCommentRespVO> getCommentsByEventId(Long eventId);

    /**
     * 删除评论
     *
     * @param id 评论 ID
     */
    void deleteComment(Long id);

    /**
     * 分页查询评论
     *
     * @param reqVO 查询参数
     * @return 分页结果
     */
    PageResult<AiEventCommentDO> getCommentPage(AiEventCommentPageReqVO reqVO);
}
