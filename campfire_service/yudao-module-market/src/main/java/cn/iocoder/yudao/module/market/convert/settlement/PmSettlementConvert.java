package cn.iocoder.yudao.module.market.convert.settlement;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.market.controller.admin.settlement.vo.SettlementRespVO;
import cn.iocoder.yudao.module.market.dal.dataobject.settlement.PmSettlementDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface PmSettlementConvert {

    PmSettlementConvert INSTANCE = Mappers.getMapper(PmSettlementConvert.class);

    SettlementRespVO convert(PmSettlementDO bean);

    List<SettlementRespVO> convertList(List<PmSettlementDO> list);

    default PageResult<SettlementRespVO> convertPage(PageResult<PmSettlementDO> page) {
        return new PageResult<>(convertList(page.getList()), page.getTotal());
    }

}
