package cn.iocoder.yudao.module.ai.dal.mysql.agent;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiMessageInteractionDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * AI 消息互动 Mapper
 *
 * @author campfire
 */
@Mapper
public interface AiMessageInteractionMapper extends BaseMapperX<AiMessageInteractionDO> {

    default AiMessageInteractionDO selectByUserAndTarget(Long userId, Integer targetType, Long targetId, Integer interactionType) {
        return selectOne(new LambdaQueryWrapperX<AiMessageInteractionDO>()
                .eq(AiMessageInteractionDO::getUserId, userId)
                .eq(AiMessageInteractionDO::getTargetType, targetType)
                .eq(AiMessageInteractionDO::getTargetId, targetId)
                .eq(AiMessageInteractionDO::getInteractionType, interactionType)
                .eq(AiMessageInteractionDO::getDeleted, 0)); // 确保未删除
    }

    default Long existsByUserAndType(Long userId, Integer targetType, Long targetId, Integer interactionType) {
        return selectCount(new LambdaQueryWrapperX<AiMessageInteractionDO>()
                .eq(AiMessageInteractionDO::getUserId, userId)
                .eq(AiMessageInteractionDO::getTargetType, targetType)
                .eq(AiMessageInteractionDO::getTargetId, targetId)
                .eq(AiMessageInteractionDO::getInteractionType, interactionType));
    }

    @Select("SELECT interaction_type as type, COUNT(*) as count " +
            "FROM ai_message_interaction " +
            "WHERE target_type = #{targetType} AND target_id = #{targetId} AND status = 1 AND deleted = 0 " +
            "GROUP BY interaction_type")
    List<Map<String, Object>> countByTargetGroupByType(Integer targetType, Long targetId);

    default List<AiMessageInteractionDO> selectCommentList(Integer targetType, Long targetId) {
        return selectList(new LambdaQueryWrapperX<AiMessageInteractionDO>()
                .eq(AiMessageInteractionDO::getTargetType, targetType)
                .eq(AiMessageInteractionDO::getTargetId, targetId)
                .eq(AiMessageInteractionDO::getInteractionType, 3) // 3=评论
                .eq(AiMessageInteractionDO::getStatus, 1) // 仅查询正常状态
                .orderByDesc(AiMessageInteractionDO::getCreateTime));
    }
}
