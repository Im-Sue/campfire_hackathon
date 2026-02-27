package cn.iocoder.yudao.module.ai.dal.dataobject.agent;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * AI Agent DO
 *
 * @author campfire
 */
@TableName("ai_agent")
@KeySequence("ai_agent_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAgentDO extends BaseDO {

    /**
     * Agent ID
     */
    @TableId
    private Long id;

    /**
     * Agent名称
     */
    private String name;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 关联的钱包用户ID
     * 关联 wallet_user.id
     */
    private Long walletUserId;

    /**
     * 关联的角色ID
     * 关联 ai_chat_role.id
     */
    private Long roleId;

    /**
     * Agent简介
     */
    private String description;

    /**
     * 性格描述 (如: 激进/保守/理性)
     */
    private String personality;

    /**
     * 风险偏好 1保守 2中性 3激进
     */
    private Integer riskLevel;

    /**
     * 最小下注金额
     */
    private Long minBetAmount;

    /**
     * 最大下注金额
     */
    private Long maxBetAmount;

    /**
     * 单次下注最大比例 (0-1)
     */
    private BigDecimal maxBetRatio;

    /**
     * 参与事件数
     */
    private Integer totalEvents;

    /**
     * 获胜次数
     */
    private Integer winCount;

    /**
     * 累计盈亏
     */
    private Long totalProfit;

    /**
     * 创建者类型 1系统创建 2用户创建
     */
    private Integer creatorType;

    /**
     * 创建者ID (用户创建时填写)
     */
    private Long creatorId;

    /**
     * 状态
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

}
