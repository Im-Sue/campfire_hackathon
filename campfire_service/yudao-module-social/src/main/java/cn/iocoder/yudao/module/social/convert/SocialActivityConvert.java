package cn.iocoder.yudao.module.social.convert;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.social.controller.app.vo.AppActivityRespVO;
import cn.iocoder.yudao.module.social.dal.dataobject.SocialActivityDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 互动记录转换器
 */
@Mapper
public interface SocialActivityConvert {

    SocialActivityConvert INSTANCE = Mappers.getMapper(SocialActivityConvert.class);

    AppActivityRespVO convert(SocialActivityDO bean);

    List<AppActivityRespVO> convertList(List<SocialActivityDO> list);

    default PageResult<AppActivityRespVO> convertPage(PageResult<SocialActivityDO> page) {
        return new PageResult<>(convertList(page.getList()), page.getTotal());
    }

}
