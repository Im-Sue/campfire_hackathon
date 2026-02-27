package cn.iocoder.yudao.module.point.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 钱包用户 DO（只读，用于排行榜查询用户头像）
 * 注意：这是一个只读 DO，仅用于查询 wallet_user 表中的用户基本信息
 */
@TableName("wallet_user")
@Data
public class WalletUserReadOnlyDO {

    /**
     * 用户 ID
     */
    @TableId
    private Long id;

    /**
     * 钱包地址
     */
    private String walletAddress;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatar;

}
