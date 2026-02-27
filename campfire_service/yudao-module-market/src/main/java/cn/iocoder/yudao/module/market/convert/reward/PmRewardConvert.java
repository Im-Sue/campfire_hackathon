package cn.iocoder.yudao.module.market.convert.reward;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.market.controller.admin.reward.vo.RewardRespVO;
import cn.iocoder.yudao.module.market.controller.app.reward.vo.AppRewardRespVO;
import cn.iocoder.yudao.module.market.dal.dataobject.reward.PmRewardDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface PmRewardConvert {

    PmRewardConvert INSTANCE = Mappers.getMapper(PmRewardConvert.class);

    // ========== App 端 ==========

    AppRewardRespVO convertToApp(PmRewardDO bean);

    List<AppRewardRespVO> convertToAppList(List<PmRewardDO> list);

    default PageResult<AppRewardRespVO> convertToAppPage(PageResult<PmRewardDO> page) {
        return new PageResult<>(convertToAppList(page.getList()), page.getTotal());
    }

    // ========== Admin 端 ==========

    RewardRespVO convertAdmin(PmRewardDO bean);

    List<RewardRespVO> convertAdminList(List<PmRewardDO> list);

    default PageResult<RewardRespVO> convertAdminPage(PageResult<PmRewardDO> page) {
        return new PageResult<>(convertAdminList(page.getList()), page.getTotal());
    }

}
