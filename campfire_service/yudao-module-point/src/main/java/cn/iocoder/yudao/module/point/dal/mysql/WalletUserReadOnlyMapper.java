package cn.iocoder.yudao.module.point.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.point.dal.dataobject.WalletUserReadOnlyDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 钱包用户 Mapper（只读，用于排行榜查询用户头像）
 */
@Mapper
public interface WalletUserReadOnlyMapper extends BaseMapperX<WalletUserReadOnlyDO> {

    /**
     * 批量查询用户
     *
     * @param ids 用户 ID 集合
     * @return 用户列表
     */
    default List<WalletUserReadOnlyDO> selectListByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(WalletUserReadOnlyDO::getId, ids);
    }

}
