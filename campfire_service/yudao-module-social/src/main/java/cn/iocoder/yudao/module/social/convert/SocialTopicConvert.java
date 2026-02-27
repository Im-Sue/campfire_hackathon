package cn.iocoder.yudao.module.social.convert;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.social.controller.admin.vo.TopicCreateReqVO;
import cn.iocoder.yudao.module.social.controller.admin.vo.TopicRespVO;
import cn.iocoder.yudao.module.social.controller.admin.vo.TopicUpdateReqVO;
import cn.iocoder.yudao.module.social.controller.app.vo.AppTopicRespVO;
import cn.iocoder.yudao.module.social.dal.dataobject.SocialTopicDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 话题转换器
 */
@Mapper
public interface SocialTopicConvert {

    SocialTopicConvert INSTANCE = Mappers.getMapper(SocialTopicConvert.class);

    // ========== C端转换 ==========

    AppTopicRespVO convert(SocialTopicDO bean);

    List<AppTopicRespVO> convertList(List<SocialTopicDO> list);

    // ========== 管理端转换 ==========

    SocialTopicDO convert(TopicCreateReqVO bean);

    SocialTopicDO convert(TopicUpdateReqVO bean);

    TopicRespVO convertToAdmin(SocialTopicDO bean);

    PageResult<TopicRespVO> convertPage(PageResult<SocialTopicDO> page);

}
