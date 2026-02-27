package cn.iocoder.yudao.module.market.dal.mysql.chain;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.market.dal.dataobject.chain.PmChainBatchDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 链上批次记录 Mapper
 */
@Mapper
public interface PmChainBatchMapper extends BaseMapperX<PmChainBatchDO> {

}
