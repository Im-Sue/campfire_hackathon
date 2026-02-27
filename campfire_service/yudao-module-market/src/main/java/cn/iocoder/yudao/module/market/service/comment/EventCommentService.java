package cn.iocoder.yudao.module.market.service.comment;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.market.controller.admin.comment.vo.EventCommentPageReqVO;
import cn.iocoder.yudao.module.market.controller.app.comment.vo.AppEventCommentCreateReqVO;
import cn.iocoder.yudao.module.market.controller.app.comment.vo.AppEventCommentDeleteRespVO;
import cn.iocoder.yudao.module.market.controller.app.comment.vo.AppEventCommentPageReqVO;
import cn.iocoder.yudao.module.market.controller.app.comment.vo.AppEventCommentReplyPageReqVO;
import cn.iocoder.yudao.module.market.dal.dataobject.comment.PmEventCommentDO;

import java.util.List;
import java.util.Set;

/**
 * 事件评论 Service 接口
 */
public interface EventCommentService {

    // ========== 评论 CRUD ==========

    /**
     * 创建评论
     *
     * @param userId 用户 ID
     * @param reqVO  创建请求
     * @return 评论对象
     */
    PmEventCommentDO createComment(Long userId, AppEventCommentCreateReqVO reqVO);

    /**
     * 删除评论（用户删除自己的）
     *
     * @param userId    用户 ID
     * @param commentId 评论 ID
     * @return 删除后的评论数量信息
     */
    AppEventCommentDeleteRespVO deleteComment(Long userId, Long commentId);

    /**
     * 删除评论（管理员删除）
     *
     * @param commentId 评论 ID
     */
    void deleteCommentByAdmin(Long commentId);

    /**
     * 获取评论
     *
     * @param id 评论 ID
     * @return 评论对象
     */
    PmEventCommentDO getComment(Long id);

    /**
     * 校验评论存在
     *
     * @param id 评论 ID
     * @return 评论对象
     */
    PmEventCommentDO validateCommentExists(Long id);

    // ========== 查询 ==========

    /**
     * 获取一级评论列表（分页）
     *
     * @param reqVO 查询请求
     * @return 评论列表
     */
    PageResult<PmEventCommentDO> getCommentPage(AppEventCommentPageReqVO reqVO);

    /**
     * 获取回复列表（分页）
     *
     * @param reqVO 查询请求
     * @return 回复列表
     */
    PageResult<PmEventCommentDO> getReplyPage(AppEventCommentReplyPageReqVO reqVO);

    /**
     * 管理端获取评论列表（分页）
     *
     * @param reqVO 查询请求
     * @return 评论列表
     */
    PageResult<PmEventCommentDO> getCommentPage(EventCommentPageReqVO reqVO);

    // ========== 点赞 ==========

    /**
     * 点赞评论（Toggle 模式）
     *
     * @param userId    用户 ID
     * @param commentId 评论 ID
     * @return true=点赞成功, false=取消点赞
     */
    boolean likeComment(Long userId, Long commentId);

    /**
     * 检查是否已点赞
     *
     * @param userId    用户 ID
     * @param commentId 评论 ID
     * @return 是否已点赞
     */
    boolean hasLiked(Long userId, Long commentId);

    /**
     * 批量检查是否已点赞
     *
     * @param userId     用户 ID
     * @param commentIds 评论 ID 列表
     * @return 已点赞的评论 ID 集合
     */
    Set<Long> getLikedCommentIds(Long userId, List<Long> commentIds);

}
