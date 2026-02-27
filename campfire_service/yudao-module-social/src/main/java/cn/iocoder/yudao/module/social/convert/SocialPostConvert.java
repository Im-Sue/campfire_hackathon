package cn.iocoder.yudao.module.social.convert;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.social.controller.admin.vo.PostRespVO;
import cn.iocoder.yudao.module.social.controller.app.vo.AppPostRespVO;
import cn.iocoder.yudao.module.social.dal.dataobject.SocialPostDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 社交帖子 Convert
 */
@Mapper
public interface SocialPostConvert {

    SocialPostConvert INSTANCE = Mappers.getMapper(SocialPostConvert.class);

    // ========== App 端 ==========

    AppPostRespVO convertApp(SocialPostDO bean);

    List<AppPostRespVO> convertAppList(List<SocialPostDO> list);

    default PageResult<AppPostRespVO> convertAppPage(PageResult<SocialPostDO> page) {
        return new PageResult<>(convertAppList(page.getList()), page.getTotal());
    }

    // ========== Admin 端 ==========

    PostRespVO convert(SocialPostDO bean);

    List<PostRespVO> convertList(List<SocialPostDO> list);

    default PageResult<PostRespVO> convertPage(PageResult<SocialPostDO> page) {
        return new PageResult<>(convertList(page.getList()), page.getTotal());
    }

}
