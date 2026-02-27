package cn.iocoder.yudao.module.point.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.point.dal.dataobject.PointAccountDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 积分账户 Mapper
 */
@Mapper
public interface PointAccountMapper extends BaseMapperX<PointAccountDO> {

    /**
     * 根据用户 ID 查询积分账户
     *
     * @param userId 用户ID
     * @return 积分账户
     */
    default PointAccountDO selectByUserId(Long userId) {
        return selectOne(PointAccountDO::getUserId, userId);
    }

    /**
     * 原子扣减积分（防止超扣）
     *
     * @param userId 用户ID
     * @param amount 扣减金额
     * @return 影响行数（0表示余额不足）
     */
    @Update("UPDATE point_account " +
            "SET available_points = available_points - #{amount}, " +
            "    total_spent = total_spent + #{amount}, " +
            "    update_time = NOW() " +
            "WHERE user_id = #{userId} " +
            "  AND available_points >= #{amount} " +
            "  AND deleted = 0")
    int atomicDeduct(@Param("userId") Long userId, @Param("amount") Long amount);

    /**
     * 原子增加积分
     *
     * @param userId 用户ID
     * @param amount 增加金额
     * @return 影响行数
     */
    @Update("UPDATE point_account " +
            "SET available_points = available_points + #{amount}, " +
            "    total_earned = total_earned + #{amount}, " +
            "    update_time = NOW() " +
            "WHERE user_id = #{userId} " +
            "  AND deleted = 0")
    int atomicAdd(@Param("userId") Long userId, @Param("amount") Long amount);

    /**
     * 查询积分排行榜（按可用积分降序）
     *
     * @param limit 限制返回数量
     * @return 积分账户列表
     */
    @Select("SELECT * FROM point_account " +
            "WHERE deleted = 0 " +
            "ORDER BY available_points DESC " +
            "LIMIT #{limit}")
    List<PointAccountDO> selectTopByAvailablePoints(@Param("limit") Integer limit);

}
