package cn.iocoder.yudao.module.market.convert.position;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.market.controller.app.position.vo.AppPositionRespVO;
import cn.iocoder.yudao.module.market.dal.dataobject.market.PmMarketDO;
import cn.iocoder.yudao.module.market.dal.dataobject.position.PmPositionDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper
public interface PmPositionConvert {

    PmPositionConvert INSTANCE = Mappers.getMapper(PmPositionConvert.class);

    AppPositionRespVO convertToApp(PmPositionDO bean);

    List<AppPositionRespVO> convertToAppList(List<PmPositionDO> list);

    default PageResult<AppPositionRespVO> convertToAppPage(PageResult<PmPositionDO> page) {
        return new PageResult<>(convertToAppList(page.getList()), page.getTotal());
    }

    /**
     * 转换持仓列表（带市场信息）
     */
    default List<AppPositionRespVO> convertToAppListWithMarket(List<PmPositionDO> list,
            Map<Long, PmMarketDO> marketMap) {
        return list.stream().map(position -> {
            AppPositionRespVO vo = convertToApp(position);
            PmMarketDO market = marketMap.get(position.getMarketId());
            if (market != null) {
                vo.setMarketQuestion(market.getQuestion());
                vo.setEventId(market.getEventId());
            }
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 转换持仓分页（带市场信息）
     */
    default PageResult<AppPositionRespVO> convertToAppPageWithMarket(PageResult<PmPositionDO> page,
            Map<Long, PmMarketDO> marketMap) {
        return new PageResult<>(convertToAppListWithMarket(page.getList(), marketMap), page.getTotal());
    }

}
