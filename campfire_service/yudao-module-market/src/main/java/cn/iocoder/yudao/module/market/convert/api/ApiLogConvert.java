package cn.iocoder.yudao.module.market.convert.api;

import cn.iocoder.yudao.module.market.controller.admin.api.vo.ApiLogRespVO;
import cn.iocoder.yudao.module.market.dal.dataobject.api.PmApiLogDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * API 日志 Convert
 */
@Mapper
public interface ApiLogConvert {

    ApiLogConvert INSTANCE = Mappers.getMapper(ApiLogConvert.class);

    ApiLogRespVO convert(PmApiLogDO bean);

    List<ApiLogRespVO> convertList(List<PmApiLogDO> list);

}
