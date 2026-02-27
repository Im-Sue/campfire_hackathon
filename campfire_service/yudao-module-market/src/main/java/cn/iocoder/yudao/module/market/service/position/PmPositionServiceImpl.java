package cn.iocoder.yudao.module.market.service.position;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.market.dal.dataobject.position.PmPositionDO;
import cn.iocoder.yudao.module.market.dal.mysql.position.PmPositionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.market.enums.ErrorCodeConstants.*;

/**
 * 用户持仓 Service 实现类
 */
@Service
@Validated
@Slf4j
public class PmPositionServiceImpl implements PmPositionService {

    @Resource
    private PmPositionMapper pmPositionMapper;

    @Override
    public PmPositionDO getPosition(Long id) {
        return pmPositionMapper.selectById(id);
    }

    @Override
    public PmPositionDO getPosition(Long userId, Long marketId, String outcome) {
        return pmPositionMapper.selectByUserMarketOutcome(userId, marketId, outcome);
    }

    @Override
    public List<PmPositionDO> getPositionsByUserId(Long userId) {
        return pmPositionMapper.selectByUserId(userId);
    }

    @Override
    public PageResult<PmPositionDO> getPositionPageByUserId(Long userId, PageParam pageParam) {
        return pmPositionMapper.selectPageByUserId(userId, pageParam);
    }

    @Override
    public List<PmPositionDO> getPositionsByMarketId(Long marketId) {
        return pmPositionMapper.selectByMarketId(marketId);
    }

    @Override
    public void addPosition(Long userId, String walletAddress, Long marketId, String outcome,
            BigDecimal quantity, BigDecimal price, Long cost) {
        // 查找现有持仓
        PmPositionDO position = pmPositionMapper.selectByUserMarketOutcome(userId, marketId, outcome);

        if (position == null) {
            // 新建持仓
            position = PmPositionDO.builder()
                    .userId(userId)
                    .walletAddress(walletAddress)
                    .marketId(marketId)
                    .outcome(outcome)
                    .quantity(quantity)
                    .avgPrice(price)
                    .totalCost(cost)
                    .realizedPnl(0L)
                    .build();
            pmPositionMapper.insert(position);
            log.info("[addPosition][新建持仓 userId={}, marketId={}, outcome={}, quantity={}]",
                    userId, marketId, outcome, quantity);
        } else {
            // 更新持仓（计算加权平均价）
            BigDecimal newQuantity = position.getQuantity().add(quantity);
            BigDecimal totalValue = position.getAvgPrice().multiply(position.getQuantity())
                    .add(price.multiply(quantity));
            BigDecimal newAvgPrice = totalValue.divide(newQuantity, 4, RoundingMode.HALF_UP);

            position.setQuantity(newQuantity);
            position.setAvgPrice(newAvgPrice);
            position.setTotalCost(position.getTotalCost() + cost);
            pmPositionMapper.updateById(position);

            log.info("[addPosition][更新持仓 positionId={}, newQuantity={}, newAvgPrice={}]",
                    position.getId(), newQuantity, newAvgPrice);
        }
    }

    @Override
    public Long reducePosition(Long userId, Long marketId, String outcome, BigDecimal quantity, BigDecimal price) {
        PmPositionDO position = pmPositionMapper.selectByUserMarketOutcome(userId, marketId, outcome);
        if (position == null) {
            throw exception(POSITION_NOT_EXISTS);
        }
        if (position.getQuantity().compareTo(quantity) < 0) {
            throw exception(POSITION_INSUFFICIENT);
        }

        // 计算实现盈亏
        // 盈亏 = (卖出价 - 持仓均价) × 份数 × 100
        BigDecimal priceDiff = price.subtract(position.getAvgPrice());
        Long pnl = priceDiff.multiply(quantity)
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();

        // 更新持仓
        BigDecimal newQuantity = position.getQuantity().subtract(quantity);
        if (newQuantity.compareTo(BigDecimal.ZERO) == 0) {
            // 清空持仓时保留记录，只是份数为 0
            position.setQuantity(BigDecimal.ZERO);
        } else {
            position.setQuantity(newQuantity);
        }
        position.setRealizedPnl(position.getRealizedPnl() + pnl);
        pmPositionMapper.updateById(position);

        log.info("[reducePosition][减少持仓 positionId={}, quantity={}, pnl={}]",
                position.getId(), quantity, pnl);

        return pnl;
    }

    @Override
    public void clearPosition(Long positionId) {
        PmPositionDO position = pmPositionMapper.selectById(positionId);
        if (position == null) {
            throw exception(POSITION_NOT_EXISTS);
        }

        position.setQuantity(BigDecimal.ZERO);
        pmPositionMapper.updateById(position);

        log.info("[clearPosition][清空持仓 positionId={}]", positionId);
    }

    @Override
    public void markAsSettled(Long positionId) {
        PmPositionDO position = pmPositionMapper.selectById(positionId);
        if (position == null) {
            throw exception(POSITION_NOT_EXISTS);
        }

        position.setSettled(true);
        pmPositionMapper.updateById(position);

        log.info("[markAsSettled][标记持仓已结算 positionId={}]", positionId);
    }

    @Override
    public BigDecimal getNetExposure(Long marketId) {
        List<PmPositionDO> positions = pmPositionMapper.selectByMarketId(marketId);

        BigDecimal yesTotal = BigDecimal.ZERO;
        BigDecimal noTotal = BigDecimal.ZERO;

        for (PmPositionDO position : positions) {
            if ("Yes".equalsIgnoreCase(position.getOutcome())) {
                yesTotal = yesTotal.add(position.getQuantity());
            } else if ("No".equalsIgnoreCase(position.getOutcome())) {
                noTotal = noTotal.add(position.getQuantity());
            }
        }

        return yesTotal.subtract(noTotal).abs();
    }

}
