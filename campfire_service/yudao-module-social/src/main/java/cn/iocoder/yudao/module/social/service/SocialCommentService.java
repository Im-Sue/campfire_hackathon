package cn.iocoder.yudao.module.social.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.social.controller.admin.vo.CommentPageReqVO;
import cn.iocoder.yudao.module.social.controller.app.vo.AppCommentCreateReqVO;
import cn.iocoder.yudao.module.social.controller.app.vo.AppCommentDeleteRespVO;
import cn.iocoder.yudao.module.social.controller.app.vo.AppCommentPageReqVO;
import cn.iocoder.yudao.module.social.controller.app.vo.AppReplyPageReqVO;
import cn.iocoder.yudao.module.social.dal.dataobject.SocialCommentDO;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 社交评论 Service 接口
 */
public interface SocialCommentService {

    /**
     * 创建评论
     *
     * @param userId 用户 ID
     * @param reqVO  创建请求
     * @return 评论对象
     */
    SocialCommentDO createComment(Long userId, @Valid AppCommentCreateReqVO reqVO);

    /**
     * 获取评论
     *
     * @param id 评论 ID
     * @return 评论
     */
    SocialCommentDO getComment(Long id);

    /**
     * 获取评论（校验存在）
     *
     * @param id 评论 ID
     * @return 评论
     */
    SocialCommentDO validateCommentExists(Long id);

    /**
     * 分页查询帖子一级评论 (用户端)
     *
     * @param reqVO 查询条件
     * @return 分页结果
     */
    PageResult<SocialCommentDO> getCommentPage(AppCommentPageReqVO reqVO);

    /**
     * 分页查询评论的回复 (用户端)
     *
     * @param reqVO 查询条件
     * @return 分页结果
     */
    PageResult<SocialCommentDO> getReplyPage(AppReplyPageReqVO reqVO);

    /**
     * 分页查询评论 (管理端)
     *
     * @param reqVO 查询条件
     * @return 分页结果
     */
    PageResult<SocialCommentDO> getCommentPage(CommentPageReqVO reqVO);

    /**
     * 获取帖子的评论列表
     *
     * @param postId 帖子 ID
     * @return 评论列表
     */
    List<SocialCommentDO> getCommentListByPostId(Long postId);

    /**
     * 删除评论 (用户端 - 仅自己)
     *
     * @param userId    用户 ID
     * @param commentId 评论 ID
     * @return 删除后的评论数量信息
     */
    AppCommentDeleteRespVO deleteComment(Long userId, Long commentId);

    /**
     * 删除评论 (管理端)
     *
     * @param commentId 评论 ID
     */
    void deleteCommentByAdmin(Long commentId);

    /**
     * 批量删除评论 (管理端)
     *
     * @param ids 评论 ID 列表
     */
    void batchDeleteCommentByAdmin(List<Long> ids);

    /**
     * 点赞评论
     *
     * @param userId    用户 ID
     * @param commentId 评论 ID
     */
    void likeComment(Long userId, Long commentId);

    /**
     * 取消点赞评论
     *
     * @param userId    用户 ID
     * @param commentId 评论 ID
     */
    void unlikeComment(Long userId, Long commentId);

    /**
     * 检查用户是否已点赞评论
     *
     * @param userId    用户 ID
     * @param commentId 评论 ID
     * @return 是否已点赞
     */
    boolean hasLikedComment(Long userId, Long commentId);

}
