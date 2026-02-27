package cn.iocoder.yudao.module.treasure.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.treasure.dal.dataobject.TreasureConfigDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 夺宝模块配置 Mapper
 *
 * @author Sue
 */
@Mapper
public interface TreasureConfigMapper extends BaseMapperX<TreasureConfigDO> {

    /**
     * 根据配置键查询
     */
    default TreasureConfigDO selectByConfigKey(String configKey) {
        return selectOne(new LambdaQueryWrapperX<TreasureConfigDO>()
                .eq(TreasureConfigDO::getConfigKey, configKey));
    }

    /**
     * 查询所有配置
     */
    default List<TreasureConfigDO> selectAllConfigs() {
        return selectList(new LambdaQueryWrapperX<TreasureConfigDO>()
                .orderByAsc(TreasureConfigDO::getConfigKey));
    }
}
