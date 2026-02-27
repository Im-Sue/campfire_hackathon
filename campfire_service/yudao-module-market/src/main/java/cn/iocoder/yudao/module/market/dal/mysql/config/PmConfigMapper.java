package cn.iocoder.yudao.module.market.dal.mysql.config;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.market.dal.dataobject.config.PmConfigDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预测市场配置 Mapper
 */
@Mapper
public interface PmConfigMapper extends BaseMapperX<PmConfigDO> {

    default PmConfigDO selectByKey(String configKey) {
        return selectOne(PmConfigDO::getConfigKey, configKey);
    }

}
