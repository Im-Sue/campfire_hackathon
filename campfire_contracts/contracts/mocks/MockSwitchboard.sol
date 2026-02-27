// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

/**
 * @title MockSwitchboard
 * @notice Mock Switchboard VRF contract for testing
 */
contract MockSwitchboard {
    uint256 private requestCounter;

    /**
     * @notice Mock requestRandomness function
     * @return requestId The unique identifier for this randomness request
     */
    function requestRandomness() external payable returns (bytes32) {
        requestCounter++;
        return keccak256(abi.encodePacked(block.timestamp, msg.sender, requestCounter));
    }

    /**
     * @notice Receive function to accept ETH
     */
    receive() external payable {}
}
