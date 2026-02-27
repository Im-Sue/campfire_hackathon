package cn.iocoder.yudao.module.point.dal.mysql;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.point.controller.admin.vo.PointTransactionPageReqVO;
import cn.iocoder.yudao.module.point.dal.dataobject.PointTransactionDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 积分流水 Mapper
 */
@Mapper
public interface PointTransactionMapper extends BaseMapperX<PointTransactionDO> {

    /**
     * 根据业务类型和业务ID查询流水
     *
     * @param bizType 业务类型
     * @param bizId   业务ID
     * @param type    流水类型
     * @return 流水记录
     */
    default PointTransactionDO selectByBiz(String bizType, Long bizId, Integer type) {
        return selectOne(new LambdaQueryWrapperX<PointTransactionDO>()
                .eq(PointTransactionDO::getBizType, bizType)
                .eq(PointTransactionDO::getBizId, bizId)
                .eq(PointTransactionDO::getType, type));
    }

    /**
     * 分页查询用户积分流水
     *
     * @param userId 用户ID
     * @param reqVO  分页查询条件
     * @return 分页结果
     */
    default PageResult<PointTransactionDO> selectPageByUserId(Long userId, PointTransactionPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PointTransactionDO>()
                .eq(PointTransactionDO::getUserId, userId)
                .eqIfPresent(PointTransactionDO::getType, reqVO.getType())
                .eqIfPresent(PointTransactionDO::getBizType, reqVO.getBizType())
                .betweenIfPresent(PointTransactionDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(PointTransactionDO::getId));
    }

    /**
     * 管理端分页查询积分流水
     *
     * @param reqVO 分页查询条件
     * @return 分页结果
     */
    default PageResult<PointTransactionDO> selectPage(PointTransactionPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PointTransactionDO>()
                .eqIfPresent(PointTransactionDO::getUserId, reqVO.getUserId())
                .likeIfPresent(PointTransactionDO::getWalletAddress, reqVO.getWalletAddress())
                .eqIfPresent(PointTransactionDO::getType, reqVO.getType())
                .eqIfPresent(PointTransactionDO::getBizType, reqVO.getBizType())
                .betweenIfPresent(PointTransactionDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(PointTransactionDO::getId));
    }

}
