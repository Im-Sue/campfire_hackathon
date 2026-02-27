// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

/**
 * @title BetLedger
 * @author Sue
 * @notice 预测市场下注链上记账合约 - 批量存储下注哈希
 * @dev 仅 owner 可写入，任何人可查询验证
 */
contract BetLedger {
    // ============ 状态变量 ============

    /// @notice 合约所有者
    address public owner;

    /// @notice 批次计数器
    uint256 public batchCounter;

    /// @notice 下注哈希记录: betHash => 是否已记录
    mapping(bytes32 => bool) public recordedBets;

    // ============ 事件 ============

    /**
     * @notice 批量记账事件
     * @param batchId 批次ID
     * @param betHashes 下注哈希数组
     * @param timestamp 记录时间戳
     */
    event BetsRecorded(
        uint256 indexed batchId,
        bytes32[] betHashes,
        uint256 timestamp
    );

    /**
     * @notice 所有权转移事件
     */
    event OwnershipTransferred(
        address indexed previousOwner,
        address indexed newOwner
    );

    // ============ 修饰符 ============

    modifier onlyOwner() {
        require(msg.sender == owner, "Not owner");
        _;
    }

    // ============ 构造函数 ============

    constructor() {
        owner = msg.sender;
        emit OwnershipTransferred(address(0), msg.sender);
    }

    // ============ 核心功能 ============

    /**
     * @notice 批量记录下注哈希
     * @param betHashes 下注哈希数组
     * @dev betHash = keccak256(abi.encodePacked(orderId, walletAddress, marketId, outcome, amount, filledAt))
     */
    function recordBets(bytes32[] calldata betHashes) external onlyOwner {
        require(betHashes.length > 0, "Empty batch");
        require(betHashes.length <= 100, "Batch too large");

        batchCounter++;
        uint256 currentBatchId = batchCounter;

        for (uint256 i = 0; i < betHashes.length; i++) {
            require(!recordedBets[betHashes[i]], "Duplicate bet");
            recordedBets[betHashes[i]] = true;
        }

        emit BetsRecorded(currentBatchId, betHashes, block.timestamp);
    }

    // ============ 查询功能 ============

    /**
     * @notice 查询下注是否已上链
     * @param betHash 下注哈希
     * @return 是否已记录
     */
    function isBetRecorded(bytes32 betHash) external view returns (bool) {
        return recordedBets[betHash];
    }

    /**
     * @notice 批量查询下注是否已上链
     * @param betHashes 下注哈希数组
     * @return results 每个哈希的记录状态
     */
    function areBetsRecorded(bytes32[] calldata betHashes) external view returns (bool[] memory) {
        bool[] memory results = new bool[](betHashes.length);
        for (uint256 i = 0; i < betHashes.length; i++) {
            results[i] = recordedBets[betHashes[i]];
        }
        return results;
    }

    // ============ 管理功能 ============

    /**
     * @notice 转移所有权
     * @param newOwner 新所有者地址
     */
    function transferOwnership(address newOwner) external onlyOwner {
        require(newOwner != address(0), "Invalid new owner");
        address oldOwner = owner;
        owner = newOwner;
        emit OwnershipTransferred(oldOwner, newOwner);
    }
}
