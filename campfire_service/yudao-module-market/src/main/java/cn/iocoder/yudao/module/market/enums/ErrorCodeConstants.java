package cn.iocoder.yudao.module.market.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Market 模块错误码枚举
 * 
 * market 模块使用 1-011-000-000 段
 */
public interface ErrorCodeConstants {

        // ========== Event 相关 1-011-001-000 ==========
        cn.iocoder.yudao.framework.common.exception.ErrorCode EVENT_NOT_EXISTS = new cn.iocoder.yudao.framework.common.exception.ErrorCode(
                        1_011_001_000, "事件不存在");
        cn.iocoder.yudao.framework.common.exception.ErrorCode EVENT_ALREADY_PUBLISHED = new cn.iocoder.yudao.framework.common.exception.ErrorCode(
                        1_011_001_001, "事件已上架");
        cn.iocoder.yudao.framework.common.exception.ErrorCode EVENT_NOT_PUBLISHED = new cn.iocoder.yudao.framework.common.exception.ErrorCode(
                        1_011_001_002, "事件未上架");

        // ========== Market 相关 1-011-002-000 ==========
        cn.iocoder.yudao.framework.common.exception.ErrorCode MARKET_NOT_EXISTS = new cn.iocoder.yudao.framework.common.exception.ErrorCode(
                        1_011_002_000, "市场不存在");
        cn.iocoder.yudao.framework.common.exception.ErrorCode MARKET_NOT_TRADING = new cn.iocoder.yudao.framework.common.exception.ErrorCode(
                        1_011_002_001, "市场未开放交易");
        cn.iocoder.yudao.framework.common.exception.ErrorCode MARKET_SUSPENDED = new cn.iocoder.yudao.framework.common.exception.ErrorCode(
                        1_011_002_002, "市场已封盘");
        cn.iocoder.yudao.framework.common.exception.ErrorCode MARKET_SETTLED = new cn.iocoder.yudao.framework.common.exception.ErrorCode(
                        1_011_002_003, "市场已结算");
        cn.iocoder.yudao.framework.common.exception.ErrorCode MARKET_PRICE_NOT_AVAILABLE = new cn.iocoder.yudao.framework.common.exception.ErrorCode(
                        1_011_002_004, "无法获取市场价格");

        // ========== Order 相关 1-011-003-000 ==========
        cn.iocoder.yudao.framework.common.exception.ErrorCode ORDER_NOT_EXISTS = new cn.iocoder.yudao.framework.common.exception.ErrorCode(
                        1_011_003_000, "订单不存在");
        cn.iocoder.yudao.framework.common.exception.ErrorCode ORDER_CANNOT_CANCEL = new cn.iocoder.yudao.framework.common.exception.ErrorCode(
                        1_011_003_001, "订单无法取消");
        cn.iocoder.yudao.framework.common.exception.ErrorCode ORDER_AMOUNT_TOO_SMALL = new cn.iocoder.yudao.framework.common.exception.ErrorCode(
                        1_011_003_002, "下单金额过小");
        cn.iocoder.yudao.framework.common.exception.ErrorCode ORDER_AMOUNT_TOO_LARGE = new cn.iocoder.yudao.framework.common.exception.ErrorCode(
                        1_011_003_003, "下单金额过大");
        cn.iocoder.yudao.framework.common.exception.ErrorCode ORDER_SLIPPAGE_EXCEEDED = new cn.iocoder.yudao.framework.common.exception.ErrorCode(
                        1_011_003_004, "价格滑点超出容忍范围");
        cn.iocoder.yudao.framework.common.exception.ErrorCode ORDER_INSUFFICIENT_POINTS = new cn.iocoder.yudao.framework.common.exception.ErrorCode(
                        1_011_003_005, "积分余额不足");
        cn.iocoder.yudao.framework.common.exception.ErrorCode ORDER_EXPOSURE_EXCEEDED = new cn.iocoder.yudao.framework.common.exception.ErrorCode(
                        1_011_003_006, "市场敞口已达上限");
        cn.iocoder.yudao.framework.common.exception.ErrorCode ORDER_AMOUNT_REQUIRED = new cn.iocoder.yudao.framework.common.exception.ErrorCode(
                        1_011_003_007, "市价单必须指定花费积分");
        cn.iocoder.yudao.framework.common.exception.ErrorCode ORDER_QUANTITY_REQUIRED = new cn.iocoder.yudao.framework.common.exception.ErrorCode(
                        1_011_003_008, "限价单必须指定份数");

        // ========== Position 相关 1-011-004-000 ==========
        cn.iocoder.yudao.framework.common.exception.ErrorCode POSITION_NOT_EXISTS = new cn.iocoder.yudao.framework.common.exception.ErrorCode(
                        1_011_004_000, "持仓不存在");
        cn.iocoder.yudao.framework.common.exception.ErrorCode POSITION_INSUFFICIENT = new cn.iocoder.yudao.framework.common.exception.ErrorCode(
                        1_011_004_001, "持仓不足");

        // ========== Settlement 相关 1-011-005-000 ==========
        cn.iocoder.yudao.framework.common.exception.ErrorCode SETTLEMENT_NOT_EXISTS = new cn.iocoder.yudao.framework.common.exception.ErrorCode(
                        1_011_005_000, "结算记录不存在");
        cn.iocoder.yudao.framework.common.exception.ErrorCode SETTLEMENT_ALREADY_CONFIRMED = new cn.iocoder.yudao.framework.common.exception.ErrorCode(
                        1_011_005_001, "结算已确认");

        // ========== Reward 相关 1-011-006-000 ==========
        cn.iocoder.yudao.framework.common.exception.ErrorCode REWARD_NOT_EXISTS = new cn.iocoder.yudao.framework.common.exception.ErrorCode(
                        1_011_006_000, "奖励记录不存在");
        cn.iocoder.yudao.framework.common.exception.ErrorCode REWARD_ALREADY_CLAIMED = new cn.iocoder.yudao.framework.common.exception.ErrorCode(
                        1_011_006_001, "奖励已领取");
        cn.iocoder.yudao.framework.common.exception.ErrorCode REWARD_CANNOT_CLAIM = new cn.iocoder.yudao.framework.common.exception.ErrorCode(
                        1_011_006_002, "奖励无法领取");

        // ========== Polymarket 同步相关 1-011-007-000 ==========
        cn.iocoder.yudao.framework.common.exception.ErrorCode EVENT_ALREADY_IMPORTED = new cn.iocoder.yudao.framework.common.exception.ErrorCode(
                        1_011_007_000, "事件已导入，请勿重复导入");
        cn.iocoder.yudao.framework.common.exception.ErrorCode POLYMARKET_EVENT_NOT_FOUND = new cn.iocoder.yudao.framework.common.exception.ErrorCode(
                        1_011_007_001, "Polymarket 事件不存在或获取失败");
        cn.iocoder.yudao.framework.common.exception.ErrorCode SCALAR_MARKET_NOT_SUPPORTED = new cn.iocoder.yudao.framework.common.exception.ErrorCode(
                        1_011_007_002, "Scalar 类型市场暂不支持");

        // ========== 事件评论相关 1-011-008-000 ==========
        cn.iocoder.yudao.framework.common.exception.ErrorCode EVENT_COMMENT_NOT_EXISTS = new cn.iocoder.yudao.framework.common.exception.ErrorCode(
                        1_011_008_000, "评论不存在");
        cn.iocoder.yudao.framework.common.exception.ErrorCode EVENT_COMMENT_DELETED = new cn.iocoder.yudao.framework.common.exception.ErrorCode(
                        1_011_008_001, "评论已被删除");
        cn.iocoder.yudao.framework.common.exception.ErrorCode EVENT_COMMENT_CONTENT_EMPTY = new cn.iocoder.yudao.framework.common.exception.ErrorCode(
                        1_011_008_002, "评论内容不能为空");
        cn.iocoder.yudao.framework.common.exception.ErrorCode EVENT_COMMENT_CONTENT_EXCEED_LIMIT = new cn.iocoder.yudao.framework.common.exception.ErrorCode(
                        1_011_008_003, "评论内容超过限制");
        cn.iocoder.yudao.framework.common.exception.ErrorCode EVENT_COMMENT_NOT_YOURS = new cn.iocoder.yudao.framework.common.exception.ErrorCode(
                        1_011_008_004, "只能删除自己的评论");

}
