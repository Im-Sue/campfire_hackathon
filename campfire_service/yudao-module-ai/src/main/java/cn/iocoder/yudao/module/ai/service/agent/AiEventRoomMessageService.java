package cn.iocoder.yudao.module.ai.service.agent;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiEventRoomMessagePageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiEventRoomMessageRespVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiEventRoomMessageDO;

import java.util.List;

/**
 * AI 房间消息 Service 接口
 *
 * @author campfire
 */
public interface AiEventRoomMessageService {

    /**
     * 创建消息
     *
     * @param message 消息
     * @return 消息 ID
     */
    Long createMessage(AiEventRoomMessageDO message);

    /**
     * 获取消息分页
     *
     * @param pageReqVO 分页查询
     * @return 消息分页
     */
    PageResult<AiEventRoomMessageDO> getMessagePage(AiEventRoomMessagePageReqVO pageReqVO);

    /**
     * 获取房间某轮次的消息列表
     *
     * @param roomId 房间 ID
     * @param round  轮次
     * @return 消息列表
     */
    List<AiEventRoomMessageDO> getMessagesByRoomAndRound(Long roomId, Integer round);

    /**
     * 获取房间的所有消息
     *
     * @param roomId 房间 ID
     * @return 消息列表
     */
    List<AiEventRoomMessageDO> getMessagesByRoomId(Long roomId);

    /**
     * 获取房间的所有消息（包含Agent信息）
     *
     * @param roomId 房间 ID
     * @return 消息响应列表
     */
    List<AiEventRoomMessageRespVO> getMessagesByRoomIdWithDetails(Long roomId);

    /**
     * 获取房间指定消息ID之后的消息 (轮询用)
     *
     * @param roomId         房间 ID
     * @param afterMessageId 上次获取的最后消息ID
     * @return 消息列表
     */
    List<AiEventRoomMessageDO> getMessagesAfter(Long roomId, Long afterMessageId);

    /**
     * 获取房间指定消息ID之后的消息（包含Agent信息）
     *
     * @param roomId         房间 ID
     * @param afterMessageId 上次获取的最后消息ID
     * @return 消息响应列表
     */
    List<AiEventRoomMessageRespVO> getMessagesAfterWithDetails(Long roomId, Long afterMessageId);

    /**
     * 获取全局 AI Agent 时间线（不区分房间）
     *
     * @param reqVO 查询参数
     * @return 分页结果
     */
    cn.iocoder.yudao.framework.common.pojo.PageResult<cn.iocoder.yudao.module.ai.controller.app.agent.vo.AppAgentTimelineRespVO> getGlobalTimeline(
            cn.iocoder.yudao.module.ai.controller.app.agent.vo.AppAgentTimelineReqVO reqVO);

}
