package cn.iocoder.yudao.module.treasure.service.ticket.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.treasure.config.TreasureProperties;
import cn.iocoder.yudao.module.treasure.controller.admin.ticket.vo.TreasureTicketPageReqVO;
import cn.iocoder.yudao.module.treasure.dal.dataobject.TreasureTicketDO;
import cn.iocoder.yudao.module.treasure.dal.mysql.TreasureTicketMapper;
import cn.iocoder.yudao.module.treasure.service.contract.TreasureContractService;
import cn.iocoder.yudao.module.treasure.service.ticket.TreasureTicketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.math.BigInteger;

/**
 * 票号管理 Service 实现类
 *
 * @author Sue
 */
@Slf4j
@Service
public class TreasureTicketServiceImpl implements TreasureTicketService {

    @Resource
    private TreasureTicketMapper ticketMapper;

    @Resource
    private TreasureContractService contractService;

    @Resource
    private TreasureProperties treasureProperties;

    @Resource
    private cn.iocoder.yudao.module.treasure.service.config.TreasureConfigService treasureConfigService;

    @Override
    public PageResult<TreasureTicketDO> getTicketPage(TreasureTicketPageReqVO pageReqVO) {
        return ticketMapper.selectPage(pageReqVO);
    }

    @Override
    public TreasureTicketDO getTicket(Long id) {
        return ticketMapper.selectById(id);
    }

    @Override
    public TreasureTicketDO getUserTicket(String userAddress, Long poolId) {
        String contractAddress = treasureConfigService.getContractAddress();
        Integer chainId = treasureConfigService.getChainId();
        return ticketMapper.selectByUserAndPool(userAddress, poolId, contractAddress, chainId);
    }

    @Override
    public PageResult<TreasureTicketDO> getUserTicketPage(String userAddress, TreasureTicketPageReqVO pageReqVO) {
        pageReqVO.setOwnerAddress(userAddress);
        return ticketMapper.selectPage(pageReqVO);
    }
}
