package cn.iocoder.yudao.module.ai.service.agent;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiEventRoomCreateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiEventRoomPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiEventRoomParticipantRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiEventRoomRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.agent.vo.RoomMarketRespVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiEventRoomDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiEventRoomParticipantDO;
import cn.iocoder.yudao.module.ai.service.agent.dto.AgentParticipantInfo;
import jakarta.validation.Valid;

import java.util.List;

/**
 * AI 事件房间 Service 接口
 *
 * @author campfire
 */
public interface AiEventRoomService {

    /**
     * 创建房间
     *
     * @param createReqVO 创建信息
     * @param participantInfos Agent参与者信息(包含余额)
     * @return 房间 ID
     */
    Long createRoom(@Valid AiEventRoomCreateReqVO createReqVO, List<AgentParticipantInfo> participantInfos);

    /**
     * 启动房间
     *
     * @param roomId 房间 ID
     */
    void startRoom(Long roomId);

    /**
     * 暂停房间
     *
     * @param roomId 房间 ID
     */
    void pauseRoom(Long roomId);

    /**
     * 停止房间
     *
     * @param roomId 房间 ID
     */
    void stopRoom(Long roomId);

    /**
     * 获取房间
     *
     * @param id 房间 ID
     * @return 房间
     */
    AiEventRoomDO getRoom(Long id);

    /**
     * 根据事件ID获取房间
     *
     * @param eventId 事件 ID
     * @return 房间
     */
    AiEventRoomDO getRoomByEventId(Long eventId);

    /**
     * 获取房间分页
     *
     * @param pageReqVO 分页查询
     * @return 房间分页
     */
    PageResult<AiEventRoomDO> getRoomPage(AiEventRoomPageReqVO pageReqVO);

    /**
     * 获取房间分页（包含完整详情）
     *
     * @param pageReqVO 分页查询
     * @return 房间分页（含详情）
     */
    PageResult<AiEventRoomRespVO> getRoomPageWithDetails(AiEventRoomPageReqVO pageReqVO);

    /**
     * 获取房间参与者列表
     *
     * @param roomId 房间 ID
     * @return 参与者列表
     */
    List<AiEventRoomParticipantDO> getRoomParticipants(Long roomId);

    /**
     * 获取房间参与者列表（包含完整信息）
     *
     * @param roomId 房间 ID
     * @return 参与者响应列表
     */
    List<AiEventRoomParticipantRespVO> getRoomParticipantsWithDetails(Long roomId);

    /**
     * 获取房间详情（包含扩展信息）
     *
     * @param id 房间 ID
     * @return 房间响应VO
     */
    AiEventRoomRespVO getRoomWithDetails(Long id);

    /**
     * 获取房间关联的市场价格
     *
     * @param roomId 房间 ID
     * @return 市场价格列表
     */
    List<RoomMarketRespVO> getRoomMarkets(Long roomId);

    /**
     * 更新当前轮次
     *
     * @param roomId 房间 ID
     * @param round  轮次
     */
    void updateCurrentRound(Long roomId, Integer round);

    /**
     * 服务重启后恢复运行中的房间
     */
    void recoverRoomsOnStartup();

}
