package cn.iocoder.yudao.module.point.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.point.controller.admin.vo.PointTransactionPageReqVO;
import cn.iocoder.yudao.module.point.dal.dataobject.PointAccountDO;
import cn.iocoder.yudao.module.point.dal.dataobject.PointTransactionDO;

import java.util.Map;

/**
 * 积分 Service 接口
 */
public interface PointService {

        // ========== 积分账户相关 ==========

        /**
         * 获取用户积分账户
         *
         * @param userId 用户ID
         * @return 积分账户
         */
        PointAccountDO getAccount(Long userId);

        /**
         * 获取或创建用户积分账户
         *
         * @param userId        用户ID
         * @param walletAddress 钱包地址（冗余存储）
         * @return 积分账户
         */
        PointAccountDO getOrCreateAccount(Long userId, String walletAddress);

        /**
         * 获取或创建用户积分账户（兼容旧调用，walletAddress 为空）
         *
         * @param userId 用户ID
         * @return 积分账户
         */
        PointAccountDO getOrCreateAccount(Long userId);

        /**
         * 获取用户可用积分
         *
         * @param userId 用户ID
         * @return 可用积分
         */
        Long getAvailablePoints(Long userId);

        // ========== 积分操作相关 ==========

        /**
         * 增加积分
         *
         * @param userId        用户ID
         * @param walletAddress 钱包地址（冗余存储）
         * @param amount        增加金额
         * @param type          流水类型
         * @param bizType       业务类型
         * @param bizId         业务ID
         * @param remark        备注
         * @param extension     扩展信息
         */
        void addPoints(Long userId, String walletAddress, Long amount, Integer type, String bizType, Long bizId,
                        String remark, Map<String, Object> extension);

        /**
         * 扣减积分
         *
         * @param userId        用户ID
         * @param walletAddress 钱包地址（冗余存储）
         * @param amount        扣减金额
         * @param type          流水类型
         * @param bizType       业务类型
         * @param bizId         业务ID
         * @param remark        备注
         * @param extension     扩展信息
         */
        void deductPoints(Long userId, String walletAddress, Long amount, Integer type, String bizType, Long bizId,
                        String remark, Map<String, Object> extension);

        /**
         * 尝试扣减积分（不抛异常）
         *
         * @param userId        用户ID
         * @param walletAddress 钱包地址（冗余存储）
         * @param amount        扣减金额
         * @param type          流水类型
         * @param bizType       业务类型
         * @param bizId         业务ID
         * @param remark        备注
         * @param extension     扩展信息
         * @return 是否扣减成功
         */
        boolean tryDeductPoints(Long userId, String walletAddress, Long amount, Integer type, String bizType,
                        Long bizId,
                        String remark, Map<String, Object> extension);

        /**
         * 管理员调整积分
         *
         * @param userId        用户ID
         * @param walletAddress 钱包地址（冗余存储）
         * @param amount        调整金额（正数增加，负数减少）
         * @param remark        备注
         */
        void adjustPoints(Long userId, String walletAddress, Long amount, String remark);

        // ========== 积分流水相关 ==========

        /**
         * 分页查询用户积分流水
         *
         * @param userId 用户ID
         * @param reqVO  查询条件
         * @return 分页结果
         */
        PageResult<PointTransactionDO> getTransactionPage(Long userId, PointTransactionPageReqVO reqVO);

        /**
         * 管理端分页查询积分流水
         *
         * @param reqVO 查询条件
         * @return 分页结果
         */
        PageResult<PointTransactionDO> getTransactionPage(PointTransactionPageReqVO reqVO);

        // ========== 积分排行榜相关 ==========

        /**
         * 获取积分排行榜
         *
         * @param limit 返回数量
         * @return 排行榜列表
         */
        java.util.List<PointAccountDO> getPointRanking(Integer limit);

}
