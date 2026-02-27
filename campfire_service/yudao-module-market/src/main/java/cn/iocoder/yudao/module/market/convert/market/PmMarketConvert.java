package cn.iocoder.yudao.module.market.convert.market;

import cn.iocoder.yudao.module.market.controller.app.event.vo.AppMarketSimpleVO;
import cn.iocoder.yudao.module.market.dal.dataobject.market.PmMarketDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface PmMarketConvert {

    PmMarketConvert INSTANCE = Mappers.getMapper(PmMarketConvert.class);

    AppMarketSimpleVO convertToAppSimple(PmMarketDO bean);

    List<AppMarketSimpleVO> convertToAppSimpleList(List<PmMarketDO> list);

}
