package cn.iocoder.yudao.module.social.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.social.controller.admin.vo.PostAuditReqVO;
import cn.iocoder.yudao.module.social.controller.admin.vo.PostPageReqVO;
import cn.iocoder.yudao.module.social.controller.app.vo.AppPostCreateReqVO;
import cn.iocoder.yudao.module.social.controller.app.vo.AppPostPageReqVO;
import cn.iocoder.yudao.module.social.dal.dataobject.SocialPostDO;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 社交帖子 Service 接口
 */
public interface SocialPostService {

    /**
     * 创建帖子
     *
     * @param userId 用户 ID
     * @param reqVO  创建请求
     * @return 帖子对象
     */
    SocialPostDO createPost(Long userId, @Valid AppPostCreateReqVO reqVO);

    /**
     * 获取帖子
     *
     * @param id 帖子 ID
     * @return 帖子
     */
    SocialPostDO getPost(Long id);

    /**
     * 获取帖子（校验存在）
     *
     * @param id 帖子 ID
     * @return 帖子
     */
    SocialPostDO validatePostExists(Long id);

    /**
     * 分页查询帖子 (用户端)
     *
     * @param reqVO 查询条件
     * @return 分页结果
     */
    PageResult<SocialPostDO> getPostPage(AppPostPageReqVO reqVO);

    /**
     * 分页查询帖子 (管理端)
     *
     * @param reqVO 查询条件
     * @return 分页结果
     */
    PageResult<SocialPostDO> getPostPage(PostPageReqVO reqVO);

    /**
     * 删除帖子 (用户端 - 仅自己)
     *
     * @param userId 用户 ID
     * @param postId 帖子 ID
     */
    void deletePost(Long userId, Long postId);

    /**
     * 删除帖子 (管理端)
     *
     * @param postId 帖子 ID
     */
    void deletePostByAdmin(Long postId);

    /**
     * 批量删除帖子 (管理端)
     *
     * @param ids 帖子 ID 列表
     */
    void batchDeletePostByAdmin(List<Long> ids);

    /**
     * 审核帖子
     *
     * @param reqVO 审核请求
     */
    void auditPost(@Valid PostAuditReqVO reqVO);

    /**
     * 点赞帖子
     *
     * @param userId 用户 ID
     * @param postId 帖子 ID
     */
    void likePost(Long userId, Long postId);

    /**
     * 取消点赞帖子
     *
     * @param userId 用户 ID
     * @param postId 帖子 ID
     */
    void unlikePost(Long userId, Long postId);

    /**
     * 检查用户是否已点赞帖子
     *
     * @param userId 用户 ID
     * @param postId 帖子 ID
     * @return 是否已点赞
     */
    boolean hasLikedPost(Long userId, Long postId);

    /**
     * 增加评论数
     *
     * @param postId 帖子 ID
     * @param delta  增量 (正数增加，负数减少)
     */
    void updateCommentCount(Long postId, int delta);

    /**
     * 分页查询热门帖子（按热度排序）
     *
     * @param reqVO 分页参数
     * @return 分页结果
     */
    PageResult<SocialPostDO> getHotPostPage(AppPostPageReqVO reqVO);

    /**
     * 获取用户帖子数量
     *
     * @param userId 用户 ID
     * @return 帖子数量
     */
    Integer getPostCountByUserId(Long userId);

}
