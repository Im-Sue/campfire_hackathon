const { ethers } = require("ethers");

const RPC_URL = "https://testnet-rpc.monad.xyz";
const CONTRACT_ADDRESS = "0x0D8A1Fd375b4D75f5301dDCAc018Feb899a150bF";
const POOL_ID = 23;

const ABI = [
    "function getPool(uint256 poolId) external view returns (tuple(uint256 id, uint256 price, uint256 totalShares, uint256 soldShares, uint256 winnerCount, uint256 endTime, uint8 status, bytes32 randomnessRequestId, uint256 prizePerWinner, uint256 initialPrize, address[] winners))"
];

const STATUS_MAP = {
    0: "Active",
    1: "Locked",
    2: "Drawing",
    3: "Settled"
};

async function main() {
    const provider = new ethers.JsonRpcProvider(RPC_URL);
    const contract = new ethers.Contract(CONTRACT_ADDRESS, ABI, provider);

    console.log(`Querying pool ${POOL_ID} on ${CONTRACT_ADDRESS}...\n`);

    const pool = await contract.getPool(POOL_ID);

    console.log(`Pool ID:          ${pool.id}`);
    console.log(`Status:           ${pool.status} (${STATUS_MAP[Number(pool.status)] || "Unknown"})`);
    console.log(`Price:            ${ethers.formatEther(pool.price)} MON`);
    console.log(`Total Shares:     ${pool.totalShares}`);
    console.log(`Sold Shares:      ${pool.soldShares}`);
    console.log(`Winner Count:     ${pool.winnerCount}`);
    console.log(`End Time:         ${new Date(Number(pool.endTime) * 1000).toISOString()}`);
    console.log(`RequestId:        ${pool.randomnessRequestId}`);
    console.log(`Prize Per Winner: ${ethers.formatEther(pool.prizePerWinner)} MON`);
    console.log(`Initial Prize:    ${ethers.formatEther(pool.initialPrize)} MON`);
    console.log(`Winners:          ${pool.winners.length > 0 ? pool.winners.join(", ") : "(none)"}`);
}

main().catch(console.error);
