package cn.iocoder.yudao.module.market.convert.order;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.market.controller.admin.order.vo.OrderRespVO;
import cn.iocoder.yudao.module.market.controller.app.order.vo.AppOrderRespVO;
import cn.iocoder.yudao.module.market.dal.dataobject.market.PmMarketDO;
import cn.iocoder.yudao.module.market.dal.dataobject.order.PmOrderDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper
public interface PmOrderConvert {

    PmOrderConvert INSTANCE = Mappers.getMapper(PmOrderConvert.class);

    // ========== App 端 ==========

    AppOrderRespVO convertToApp(PmOrderDO bean);

    List<AppOrderRespVO> convertToAppList(List<PmOrderDO> list);

    default PageResult<AppOrderRespVO> convertToAppPage(PageResult<PmOrderDO> page) {
        return new PageResult<>(convertToAppList(page.getList()), page.getTotal());
    }

    /**
     * 转换订单分页（带市场信息）
     */
    default PageResult<AppOrderRespVO> convertToAppPageWithMarket(PageResult<PmOrderDO> page,
            Map<Long, PmMarketDO> marketMap) {
        List<AppOrderRespVO> list = page.getList().stream().map(order -> {
            AppOrderRespVO vo = convertToApp(order);
            PmMarketDO market = marketMap.get(order.getMarketId());
            if (market != null) {
                vo.setMarketQuestion(market.getQuestion());
            }
            return vo;
        }).collect(Collectors.toList());
        return new PageResult<>(list, page.getTotal());
    }

    // ========== Admin 端 ==========

    OrderRespVO convertAdmin(PmOrderDO bean);

    List<OrderRespVO> convertAdminList(List<PmOrderDO> list);

    default PageResult<OrderRespVO> convertAdminPage(PageResult<PmOrderDO> page) {
        return new PageResult<>(convertAdminList(page.getList()), page.getTotal());
    }

}
