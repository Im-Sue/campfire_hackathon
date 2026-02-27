package cn.iocoder.yudao.module.market.convert.comment;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.market.controller.admin.comment.vo.EventCommentRespVO;
import cn.iocoder.yudao.module.market.controller.app.comment.vo.AppEventCommentRespVO;
import cn.iocoder.yudao.module.market.dal.dataobject.comment.PmEventCommentDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 事件评论 Convert
 */
@Mapper
public interface EventCommentConvert {

    EventCommentConvert INSTANCE = Mappers.getMapper(EventCommentConvert.class);

    AppEventCommentRespVO convert(PmEventCommentDO bean);

    List<AppEventCommentRespVO> convertList(List<PmEventCommentDO> list);

    PageResult<AppEventCommentRespVO> convertPage(PageResult<PmEventCommentDO> page);

    EventCommentRespVO convertAdmin(PmEventCommentDO bean);

    List<EventCommentRespVO> convertAdminList(List<PmEventCommentDO> list);

    PageResult<EventCommentRespVO> convertAdminPage(PageResult<PmEventCommentDO> page);

}
