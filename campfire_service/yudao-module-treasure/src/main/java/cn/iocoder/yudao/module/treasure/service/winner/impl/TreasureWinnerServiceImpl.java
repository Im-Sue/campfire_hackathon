package cn.iocoder.yudao.module.treasure.service.winner.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.treasure.config.TreasureProperties;
import cn.iocoder.yudao.module.treasure.controller.admin.winner.vo.TreasureWinnerPageReqVO;
import cn.iocoder.yudao.module.treasure.dal.dataobject.TreasureWinnerDO;
import cn.iocoder.yudao.module.treasure.dal.mysql.TreasureWinnerMapper;
import cn.iocoder.yudao.module.treasure.service.winner.TreasureWinnerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

/**
 * 中奖记录管理 Service 实现类
 *
 * @author Sue
 */
@Slf4j
@Service
public class TreasureWinnerServiceImpl implements TreasureWinnerService {

    @Resource
    private TreasureWinnerMapper winnerMapper;

    @Resource
    private TreasureProperties treasureProperties;

    @Override
    public PageResult<TreasureWinnerDO> getWinnerPage(TreasureWinnerPageReqVO pageReqVO) {
        return winnerMapper.selectPage(pageReqVO);
    }

    @Override
    public TreasureWinnerDO getWinner(Long id) {
        return winnerMapper.selectById(id);
    }

    @Override
    public PageResult<TreasureWinnerDO> getPoolWinnerPage(Long poolId, TreasureWinnerPageReqVO pageReqVO) {
        pageReqVO.setPoolId(poolId);
        return winnerMapper.selectPage(pageReqVO);
    }

    @Override
    public PageResult<TreasureWinnerDO> getUserWinnerPage(String userAddress, TreasureWinnerPageReqVO pageReqVO) {
        pageReqVO.setWinnerAddress(userAddress);
        return winnerMapper.selectPage(pageReqVO);
    }
}
