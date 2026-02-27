package cn.iocoder.yudao.module.social.convert;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.social.controller.admin.vo.CommentRespVO;
import cn.iocoder.yudao.module.social.controller.app.vo.AppCommentRespVO;
import cn.iocoder.yudao.module.social.dal.dataobject.SocialCommentDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 社交评论 Convert
 */
@Mapper
public interface SocialCommentConvert {

    SocialCommentConvert INSTANCE = Mappers.getMapper(SocialCommentConvert.class);

    // ========== App 端 ==========

    AppCommentRespVO convertApp(SocialCommentDO bean);

    List<AppCommentRespVO> convertAppList(List<SocialCommentDO> list);

    default PageResult<AppCommentRespVO> convertAppPage(PageResult<SocialCommentDO> page) {
        return new PageResult<>(convertAppList(page.getList()), page.getTotal());
    }

    // ========== Admin 端 ==========

    CommentRespVO convert(SocialCommentDO bean);

    List<CommentRespVO> convertList(List<SocialCommentDO> list);

    default PageResult<CommentRespVO> convertPage(PageResult<SocialCommentDO> page) {
        return new PageResult<>(convertList(page.getList()), page.getTotal());
    }

}
