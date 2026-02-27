package cn.iocoder.yudao.module.social.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * Social 错误码枚举类
 *
 * social 系统，使用 1-021-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== 帖子相关 1-021-001-000 ==========
    ErrorCode POST_NOT_EXISTS = new ErrorCode(1_021_001_000, "帖子不存在");
    ErrorCode POST_DELETED = new ErrorCode(1_021_001_001, "帖子已被删除");
    ErrorCode POST_CONTENT_EMPTY = new ErrorCode(1_021_001_002, "帖子内容不能为空");
    ErrorCode POST_IMAGES_EXCEED_LIMIT = new ErrorCode(1_021_001_003, "帖子图片超过限制");
    ErrorCode POST_NOT_YOURS = new ErrorCode(1_021_001_004, "只能删除自己的帖子");

    // ========== 评论相关 1-021-002-000 ==========
    ErrorCode COMMENT_NOT_EXISTS = new ErrorCode(1_021_002_000, "评论不存在");
    ErrorCode COMMENT_DELETED = new ErrorCode(1_021_002_001, "评论已被删除");
    ErrorCode COMMENT_CONTENT_EMPTY = new ErrorCode(1_021_002_002, "评论内容不能为空");
    ErrorCode COMMENT_CONTENT_EXCEED_LIMIT = new ErrorCode(1_021_002_003, "评论内容超过限制");
    ErrorCode COMMENT_NOT_YOURS = new ErrorCode(1_021_002_004, "只能删除自己的评论");

    // ========== 点赞相关 1-021-003-000 ==========
    ErrorCode LIKE_ALREADY_EXISTS = new ErrorCode(1_021_003_000, "已点赞，请勿重复");
    ErrorCode LIKE_NOT_EXISTS = new ErrorCode(1_021_003_001, "未点赞，无法取消");

    // ========== 关注相关 1-021-004-000 ==========
    ErrorCode FOLLOW_SELF_NOT_ALLOWED = new ErrorCode(1_021_004_000, "不能关注自己");
    ErrorCode FOLLOW_ALREADY_EXISTS = new ErrorCode(1_021_004_001, "已关注该用户");
    ErrorCode FOLLOW_NOT_EXISTS = new ErrorCode(1_021_004_002, "未关注该用户");

    // ========== 话题相关 1-021-005-000 ==========
    ErrorCode TOPIC_NOT_EXISTS = new ErrorCode(1_021_005_000, "话题不存在");
    ErrorCode TOPIC_NAME_EXISTS = new ErrorCode(1_021_005_001, "话题名称已存在");

}
