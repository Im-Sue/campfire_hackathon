package cn.iocoder.yudao.module.ai.service.agent;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiDanmakuPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiRoomDanmakuDO;
import cn.iocoder.yudao.module.ai.dal.mysql.agent.AiRoomDanmakuMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 竞赛弹幕 Service 实现类
 *
 * @author campfire
 */
@Service
@Validated
public class AiRoomDanmakuServiceImpl implements AiRoomDanmakuService {

    @Resource
    private AiRoomDanmakuMapper danmakuMapper;

    @Override
    public Long sendDanmaku(Long roomId, Long userId, String walletAddress, String content, String color) {
        AiRoomDanmakuDO danmaku = AiRoomDanmakuDO.builder()
                .roomId(roomId)
                .userId(userId)
                .walletAddress(walletAddress)
                .content(content)
                .color(color)
                .status(1) // 默认正常
                .build();
        danmakuMapper.insert(danmaku);
        return danmaku.getId();
    }

    @Override
    public List<AiRoomDanmakuDO> getDanmakuList(Long roomId, LocalDateTime afterTime) {
        return danmakuMapper.selectListByRoomIdAndAfterTime(roomId, afterTime);
    }

    @Override
    public void deleteDanmaku(Long id) {
        danmakuMapper.deleteById(id);
    }

    @Override
    public PageResult<AiRoomDanmakuDO> getDanmakuPage(AiDanmakuPageReqVO pageReqVO) {
        return danmakuMapper.selectPage(pageReqVO, new LambdaQueryWrapperX<AiRoomDanmakuDO>()
                .eqIfPresent(AiRoomDanmakuDO::getRoomId, pageReqVO.getRoomId())
                .eqIfPresent(AiRoomDanmakuDO::getUserId, pageReqVO.getUserId())
                .likeIfPresent(AiRoomDanmakuDO::getContent, pageReqVO.getContent())
                .orderByDesc(AiRoomDanmakuDO::getId));
    }
}
