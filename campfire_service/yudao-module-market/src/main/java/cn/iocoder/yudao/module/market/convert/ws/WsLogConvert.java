package cn.iocoder.yudao.module.market.convert.ws;

import cn.iocoder.yudao.module.market.controller.admin.ws.vo.WsLogRespVO;
import cn.iocoder.yudao.module.market.dal.dataobject.ws.PmWsLogDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * WS 日志 Convert
 */
@Mapper
public interface WsLogConvert {

    WsLogConvert INSTANCE = Mappers.getMapper(WsLogConvert.class);

    WsLogRespVO convert(PmWsLogDO bean);

    List<WsLogRespVO> convertList(List<PmWsLogDO> list);

}
