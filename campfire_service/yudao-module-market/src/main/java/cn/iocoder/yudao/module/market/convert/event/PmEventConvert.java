package cn.iocoder.yudao.module.market.convert.event;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.market.controller.app.event.vo.AppEventRespVO;
import cn.iocoder.yudao.module.market.dal.dataobject.event.PmEventDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface PmEventConvert {

    PmEventConvert INSTANCE = Mappers.getMapper(PmEventConvert.class);

    AppEventRespVO convertToApp(PmEventDO bean);

    List<AppEventRespVO> convertToAppList(List<PmEventDO> list);

    default PageResult<AppEventRespVO> convertToAppPage(PageResult<PmEventDO> page) {
        return new PageResult<>(convertToAppList(page.getList()), page.getTotal());
    }

}
