package cn.iocoder.yudao.module.market.controller.admin.comment;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.market.controller.admin.comment.vo.EventCommentPageReqVO;
import cn.iocoder.yudao.module.market.controller.admin.comment.vo.EventCommentRespVO;
import cn.iocoder.yudao.module.market.convert.comment.EventCommentConvert;
import cn.iocoder.yudao.module.market.dal.dataobject.comment.PmEventCommentDO;
import cn.iocoder.yudao.module.market.dal.dataobject.event.PmEventDO;
import cn.iocoder.yudao.module.market.service.comment.EventCommentService;
import cn.iocoder.yudao.module.market.service.event.PmEventService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 事件评论")
@RestController
@RequestMapping("/market/comment")
@Validated
public class AdminEventCommentController {

    @Resource
    private EventCommentService commentService;

    @Resource
    private PmEventService eventService;

    @Resource
    private AdminUserApi adminUserApi;

    @GetMapping("/page")
    @Operation(summary = "获取评论分页列表")
    @PreAuthorize("@ss.hasPermission('market:event-comment:query')")
    public CommonResult<PageResult<EventCommentRespVO>> getCommentPage(@Valid EventCommentPageReqVO reqVO) {
        PageResult<PmEventCommentDO> pageResult = commentService.getCommentPage(reqVO);
        PageResult<EventCommentRespVO> result = EventCommentConvert.INSTANCE.convertAdminPage(pageResult);

        // 填充事件标题和用户信息
        fillEventAndUserInfo(result.getList());

        return success(result);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除评论")
    @Parameter(name = "id", description = "评论 ID", required = true, example = "1001")
    @PreAuthorize("@ss.hasPermission('market:event-comment:delete')")
    public CommonResult<Boolean> deleteComment(@RequestParam("id") Long id) {
        commentService.deleteCommentByAdmin(id);
        return success(true);
    }

    /**
     * 填充事件标题和用户信息
     */
    private void fillEventAndUserInfo(List<EventCommentRespVO> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        // 收集事件 ID 和用户 ID
        Set<Long> eventIds = new HashSet<>();
        Set<Long> userIds = new HashSet<>();
        for (EventCommentRespVO vo : list) {
            eventIds.add(vo.getEventId());
            userIds.add(vo.getUserId());
            if (vo.getReplyUserId() != null) {
                userIds.add(vo.getReplyUserId());
            }
        }

        // 批量查询事件
        Map<Long, PmEventDO> eventMap = new HashMap<>();
        for (Long eventId : eventIds) {
            PmEventDO event = eventService.getEvent(eventId);
            if (event != null) {
                eventMap.put(eventId, event);
            }
        }

        // 批量查询用户
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);

        // 填充信息
        for (EventCommentRespVO vo : list) {
            // 事件标题
            PmEventDO event = eventMap.get(vo.getEventId());
            if (event != null) {
                vo.setEventTitle(event.getTitle());
            }

            // 评论者昵称
            AdminUserRespDTO user = userMap.get(vo.getUserId());
            if (user != null) {
                vo.setUserNickname(user.getNickname());
            }

            // 被回复用户昵称
            if (vo.getReplyUserId() != null) {
                AdminUserRespDTO replyUser = userMap.get(vo.getReplyUserId());
                if (replyUser != null) {
                    vo.setReplyUserNickname(replyUser.getNickname());
                }
            }
        }
    }

}
