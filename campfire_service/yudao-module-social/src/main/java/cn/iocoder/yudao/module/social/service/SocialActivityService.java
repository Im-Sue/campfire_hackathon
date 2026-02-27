package cn.iocoder.yudao.module.social.service;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.social.controller.app.vo.AppActivityRespVO;

/**
 * 社交互动记录 Service 接口
 */
public interface SocialActivityService {

    /**
     * 异步创建互动记录
     *
     * @param type         互动类型
     * @param actorUserId  发起者用户 ID
     * @param targetUserId 接收者用户 ID
     * @param targetId     目标 ID（帖子/评论 ID）
     */
    void createActivityAsync(Integer type, Long actorUserId, Long targetUserId, Long targetId);

    /**
     * 获取用户相关的互动记录（增强版，包含用户信息和内容摘要）
     *
     * @param userId    用户 ID
     * @param pageParam 分页参数
     * @return 互动记录列表（增强版 VO）
     */
    PageResult<AppActivityRespVO> getActivityList(Long userId, PageParam pageParam);

}
