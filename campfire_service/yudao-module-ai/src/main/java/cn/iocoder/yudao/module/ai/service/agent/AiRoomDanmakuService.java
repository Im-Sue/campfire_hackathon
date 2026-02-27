package cn.iocoder.yudao.module.ai.service.agent;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiDanmakuPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiRoomDanmakuDO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 竞赛弹幕 Service 接口
 *
 * @author campfire
 */
public interface AiRoomDanmakuService {

    /**
     * 发送弹幕
     *
     * @param roomId 房间ID
     * @param userId 用户ID
     * @param walletAddress 钱包地址
     * @param content 内容
     * @param color 颜色
     * @return 弹幕ID
     */
    Long sendDanmaku(Long roomId, Long userId, String walletAddress, String content, String color);

    /**
     * 获取弹幕列表（轮询）
     *
     * @param roomId 房间ID
     * @param afterTime 增量时间点
     * @return 弹幕列表
     */
    List<AiRoomDanmakuDO> getDanmakuList(Long roomId, LocalDateTime afterTime);

    /**
     * 删除弹幕
     *
     * @param id 弹幕ID
     */
    void deleteDanmaku(Long id);

    /**
     * 获得弹幕分页
     *
     * @param pageReqVO 分页查询
     * @return 弹幕分页
     */
    PageResult<AiRoomDanmakuDO> getDanmakuPage(AiDanmakuPageReqVO pageReqVO);

}
