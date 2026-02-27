package cn.iocoder.yudao.module.ai.service.agent;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiInteractionPageReqVO;
import cn.iocoder.yudao.module.ai.controller.app.agent.vo.AppInteractionStatsRespVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiMessageInteractionDO;
import cn.iocoder.yudao.module.ai.dal.mysql.agent.AiMessageInteractionMapper;
import cn.iocoder.yudao.module.ai.enums.agent.AiInteractionTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.INTERACTION_COMMENT_TOO_LONG;

/**
 * AI 消息互动 Service 实现类
 *
 * @author campfire
 */
@Service
@Validated
public class AiMessageInteractionServiceImpl implements AiMessageInteractionService {

    @Resource
    private AiMessageInteractionMapper interactionMapper;

    @Override
    public boolean addFlower(Integer targetType, Long targetId, Long userId, String walletAddress) {
        return addInteraction(targetType, targetId, userId, walletAddress, AiInteractionTypeEnum.FLOWER.getType(), null);
    }

    @Override
    public boolean addEgg(Integer targetType, Long targetId, Long userId, String walletAddress) {
        return addInteraction(targetType, targetId, userId, walletAddress, AiInteractionTypeEnum.EGG.getType(), null);
    }

    @Override
    public Long addComment(Integer targetType, Long targetId, Long userId, String walletAddress, String content) {
        // 校验评论内容
        if (content == null || content.length() > 30) {
            throw exception(INTERACTION_COMMENT_TOO_LONG);
        }
        
        AiMessageInteractionDO interaction = AiMessageInteractionDO.builder()
                .targetType(targetType)
                .targetId(targetId)
                .userId(userId)
                .walletAddress(walletAddress)
                .interactionType(AiInteractionTypeEnum.COMMENT.getType())
                .content(content)
                .status(1)
                .build();
        interactionMapper.insert(interaction);
        return interaction.getId();
    }

    private boolean addInteraction(Integer targetType, Long targetId, Long userId, String walletAddress, Integer interactionType, String content) {
        // 1. 检查是否已存在 (仅针对鲜花和鸡蛋)
        if (interactionType != AiInteractionTypeEnum.COMMENT.getType()) {
            AiMessageInteractionDO existing = interactionMapper.selectByUserAndTarget(
                    userId, targetType, targetId, interactionType);
            if (existing != null) {
                return true; // 幂等：已送过直接返回成功
            }
        }

        // 2. 插入记录
        try {
            AiMessageInteractionDO interaction = AiMessageInteractionDO.builder()
                    .targetType(targetType)
                    .targetId(targetId)
                    .userId(userId)
                    .walletAddress(walletAddress)
                    .interactionType(interactionType)
                    .content(content)
                    .status(1)
                    .build();
            interactionMapper.insert(interaction);
            return true;
        } catch (DuplicateKeyException e) {
            return true; // 并发情况下也返回成功
        }
    }

    @Override
    public AppInteractionStatsRespVO getStats(Integer targetType, Long targetId, Long userId) {
        List<Map<String, Object>> counts = interactionMapper.countByTargetGroupByType(targetType, targetId);
        
        AppInteractionStatsRespVO resp = new AppInteractionStatsRespVO();
        resp.setTargetType(targetType);
        resp.setTargetId(targetId);
        resp.setFlowerCount(0);
        resp.setEggCount(0);
        resp.setCommentCount(0);
        resp.setHasFlower(false);
        resp.setHasEgg(false);

        for (Map<String, Object> map : counts) {
            Integer type = ((Number) map.get("type")).intValue();
            Integer count = ((Number) map.get("count")).intValue();
            if (AiInteractionTypeEnum.FLOWER.getType().equals(type)) {
                resp.setFlowerCount(count);
            } else if (AiInteractionTypeEnum.EGG.getType().equals(type)) {
                resp.setEggCount(count);
            } else if (AiInteractionTypeEnum.COMMENT.getType().equals(type)) {
                resp.setCommentCount(count);
            }
        }

        if (userId != null) {
            resp.setHasFlower(interactionMapper.existsByUserAndType(userId, targetType, targetId, AiInteractionTypeEnum.FLOWER.getType()) > 0);
            resp.setHasEgg(interactionMapper.existsByUserAndType(userId, targetType, targetId, AiInteractionTypeEnum.EGG.getType()) > 0);
        }

        return resp;
    }

    @Override
    public List<AiMessageInteractionDO> getComments(Integer targetType, Long targetId) {
        return interactionMapper.selectCommentList(targetType, targetId);
    }

    @Override
    public void deleteInteraction(Long id) {
        interactionMapper.deleteById(id);
    }

    @Override
    public PageResult<AiMessageInteractionDO> getInteractionPage(AiInteractionPageReqVO pageReqVO) {
        return interactionMapper.selectPage(pageReqVO, new LambdaQueryWrapperX<AiMessageInteractionDO>()
                .eqIfPresent(AiMessageInteractionDO::getTargetType, pageReqVO.getTargetType())
                .eqIfPresent(AiMessageInteractionDO::getTargetId, pageReqVO.getTargetId())
                .eqIfPresent(AiMessageInteractionDO::getUserId, pageReqVO.getUserId())
                .eqIfPresent(AiMessageInteractionDO::getInteractionType, pageReqVO.getInteractionType())
                .orderByDesc(AiMessageInteractionDO::getId));
    }
}
