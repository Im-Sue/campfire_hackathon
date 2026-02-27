package cn.iocoder.yudao.module.point.convert;

import cn.iocoder.yudao.module.point.controller.admin.vo.PointAccountRespVO;
import cn.iocoder.yudao.module.point.controller.admin.vo.PointTransactionRespVO;
import cn.iocoder.yudao.module.point.controller.app.vo.AppPointAccountRespVO;
import cn.iocoder.yudao.module.point.controller.app.vo.AppPointTransactionRespVO;
import cn.iocoder.yudao.module.point.dal.dataobject.PointAccountDO;
import cn.iocoder.yudao.module.point.dal.dataobject.PointTransactionDO;
import cn.iocoder.yudao.module.point.enums.PointTransactionTypeEnum;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 积分 Convert
 */
@Mapper
public interface PointConvert {

    PointConvert INSTANCE = Mappers.getMapper(PointConvert.class);

    // ========== 积分账户 ==========

    PointAccountRespVO convertToAccountResp(PointAccountDO account);

    AppPointAccountRespVO convertToAppAccountResp(PointAccountDO account);

    // ========== 积分流水 ==========

    @Mapping(target = "typeName", ignore = true)
    PointTransactionRespVO convertToTransactionResp(PointTransactionDO transaction);

    @Mapping(target = "typeName", ignore = true)
    AppPointTransactionRespVO convertToAppTransactionResp(PointTransactionDO transaction);

    /**
     * 转换并填充类型名称（管理端）
     */
    default PointTransactionRespVO convertWithTypeName(PointTransactionDO transaction) {
        if (transaction == null) {
            return null;
        }
        PointTransactionRespVO vo = new PointTransactionRespVO();
        vo.setId(transaction.getId());
        vo.setUserId(transaction.getUserId());
        vo.setType(transaction.getType());
        vo.setAmount(transaction.getAmount());
        vo.setBeforeBalance(transaction.getBeforeBalance());
        vo.setAfterBalance(transaction.getAfterBalance());
        vo.setBizType(transaction.getBizType());
        vo.setBizId(transaction.getBizId());
        vo.setExtension(transaction.getExtension());
        vo.setRemark(transaction.getRemark());
        vo.setCreateTime(transaction.getCreateTime());
        
        if (transaction.getType() != null) {
            PointTransactionTypeEnum typeEnum = PointTransactionTypeEnum.getByType(transaction.getType());
            if (typeEnum != null) {
                vo.setTypeName(typeEnum.getName());
            }
        }
        return vo;
    }

    /**
     * 转换并填充类型名称（C端）
     */
    default AppPointTransactionRespVO convertToAppWithTypeName(PointTransactionDO transaction) {
        if (transaction == null) {
            return null;
        }
        AppPointTransactionRespVO vo = new AppPointTransactionRespVO();
        vo.setId(transaction.getId());
        vo.setType(transaction.getType());
        vo.setAmount(transaction.getAmount());
        vo.setAfterBalance(transaction.getAfterBalance());
        vo.setBizType(transaction.getBizType());
        vo.setRemark(transaction.getRemark());
        vo.setCreateTime(transaction.getCreateTime());
        
        if (transaction.getType() != null) {
            PointTransactionTypeEnum typeEnum = PointTransactionTypeEnum.getByType(transaction.getType());
            if (typeEnum != null) {
                vo.setTypeName(typeEnum.getName());
            }
        }
        return vo;
    }

}

