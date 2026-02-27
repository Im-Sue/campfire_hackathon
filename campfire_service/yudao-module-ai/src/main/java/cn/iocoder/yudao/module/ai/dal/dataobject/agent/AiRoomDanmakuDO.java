package cn.iocoder.yudao.module.ai.dal.dataobject.agent;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * AI 竞赛弹幕 DO
 *
 * @author campfire
 */
@TableName("ai_room_danmaku")
@KeySequence("ai_room_danmaku_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRoomDanmakuDO extends BaseDO {

    /**
     * 弹幕ID
     */
    @TableId
    private Long id;

    /**
     * 讨论房间ID
     */
    private Long roomId;

    /**
     * 钱包用户ID
     */
    private Long userId;

    /**
     * 钱包地址
     */
    private String walletAddress;

    /**
     * 弹幕内容
     */
    private String content;

    /**
     * 弹幕颜色
     */
    private String color;

    /**
     * 状态 0-删除 1-正常
     */
    private Integer status;

}
