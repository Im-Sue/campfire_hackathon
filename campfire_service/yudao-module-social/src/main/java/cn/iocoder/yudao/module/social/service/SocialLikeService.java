package cn.iocoder.yudao.module.social.service;

/**
 * 社交点赞 Service 接口
 */
public interface SocialLikeService {

    /**
     * 点赞（Toggle 模式：已点赞则取消，未点赞则添加）
     *
     * @param userId     用户 ID
     * @param targetType 目标类型 (1-帖子 2-评论)
     * @param targetId   目标 ID
     * @return true=执行了点赞，false=执行了取消点赞
     */
    boolean like(Long userId, Integer targetType, Long targetId);

    /**
     * 取消点赞
     *
     * @param userId     用户 ID
     * @param targetType 目标类型
     * @param targetId   目标 ID
     * @return true=成功删除了点赞记录，false=没有点赞记录可删除
     */
    boolean unlike(Long userId, Integer targetType, Long targetId);

    /**
     * 检查是否已点赞
     *
     * @param userId     用户 ID
     * @param targetType 目标类型
     * @param targetId   目标 ID
     * @return 是否已点赞
     */
    boolean hasLiked(Long userId, Integer targetType, Long targetId);

}
