const { ethers } = require("hardhat");
const fs = require("fs");
const path = require("path");
require("dotenv").config();

async function main() {
    console.log("🚀 开始部署 MockVRF 合约到 Monad Testnet...\n");

    const [deployer] = await ethers.getSigners();
    console.log("📍 部署地址:", deployer.address);

    const balance = await ethers.provider.getBalance(deployer.address);
    console.log("💰 账户余额:", ethers.formatEther(balance), "MON\n");

    // ===== Step 1: 部署 MockVRF =====
    console.log("⏳ 正在部署 MockVRF 合约...");
    const MockVRF = await ethers.getContractFactory("MockVRF");
    const mockVRF = await MockVRF.deploy();
    await mockVRF.waitForDeployment();
    const mockVRFAddress = await mockVRF.getAddress();
    console.log("✅ MockVRF 部署成功:", mockVRFAddress);

    // 验证
    const mockOwner = await mockVRF.owner();
    console.log("   Owner:", mockOwner);

    // ===== Step 2: 更新 TreasurePool 的 VRF 地址 =====
    const treasurePoolAddress = process.env.TREASURE_POOL_ADDRESS;
    if (!treasurePoolAddress) {
        console.error("❌ 错误: 未设置 TREASURE_POOL_ADDRESS 环境变量");
        process.exit(1);
    }

    console.log("\n⏳ 正在更新 TreasurePool 的 VRF 地址...");
    console.log("   TreasurePool:", treasurePoolAddress);
    console.log("   新 VRF 地址:", mockVRFAddress);

    const treasurePoolABI = [
        "function setSwitchboardVRF(address newVRF) external",
        "function switchboardVRF() external view returns (address)",
        "function owner() external view returns (address)"
    ];
    const treasurePool = new ethers.Contract(treasurePoolAddress, treasurePoolABI, deployer);

    const currentVRF = await treasurePool.switchboardVRF();
    console.log("   当前 VRF 地址:", currentVRF);

    const tx = await treasurePool.setSwitchboardVRF(mockVRFAddress);
    await tx.wait();

    const newVRF = await treasurePool.switchboardVRF();
    console.log("✅ VRF 地址已更新:", newVRF);

    if (newVRF.toLowerCase() !== mockVRFAddress.toLowerCase()) {
        console.error("❌ 错误: VRF 地址更新验证失败");
        process.exit(1);
    }

    // ===== Step 3: 保存部署信息 =====
    const deploymentsDir = path.join(__dirname, "..", "deployments");
    if (!fs.existsSync(deploymentsDir)) {
        fs.mkdirSync(deploymentsDir, { recursive: true });
    }

    const deploymentInfo = {
        network: "monad-testnet",
        chainId: 10143,
        mockVRFAddress: mockVRFAddress,
        treasurePoolAddress: treasurePoolAddress,
        previousVRFAddress: currentVRF,
        deployerAddress: deployer.address,
        deployedAt: new Date().toISOString()
    };

    const deploymentPath = path.join(deploymentsDir, `mock-vrf-${new Date().toISOString().split('T')[0]}.json`);
    fs.writeFileSync(deploymentPath, JSON.stringify(deploymentInfo, null, 2), "utf8");
    console.log("\n✅ 部署信息已保存:", deploymentPath);

    // ===== 总结 =====
    console.log("\n" + "=".repeat(60));
    console.log("🎉 MockVRF 部署完成!");
    console.log("=".repeat(60));
    console.log("📍 MockVRF 合约地址:", mockVRFAddress);
    console.log("📍 TreasurePool 地址:", treasurePoolAddress);
    console.log("🔗 VRF 已切换: ", currentVRF, "→", mockVRFAddress);
    console.log("\n📋 下一步:");
    console.log("1. 更新后端 treasure_config 表: contract.mock_vrf_address =", mockVRFAddress);
    console.log("2. 重启后端服务");
    console.log("3. 等待定时任务触发开奖测试");
    console.log("=".repeat(60));
}

main()
    .then(() => process.exit(0))
    .catch((error) => {
        console.error("\n❌ 部署失败:");
        console.error(error);
        process.exit(1);
    });
