const { ethers } = require("hardhat");
const fs = require("fs");
const path = require("path");
require("dotenv").config();

async function main() {
    console.log("🚀 开始部署 TreasurePool 合约到 Monad Testnet...\n");

    // 获取部署者账户
    const [deployer] = await ethers.getSigners();
    console.log("📍 部署地址:", deployer.address);

    // 检查余额
    const balance = await ethers.provider.getBalance(deployer.address);
    console.log("💰 账户余额:", ethers.formatEther(balance), "MON\n");

    if (balance === 0n) {
        console.error("❌ 错误: 账户余额为 0，无法部署合约");
        process.exit(1);
    }

    // 获取配置参数
    const switchboardVRF = process.env.SWITCHBOARD_VRF_ADDRESS || "0x36825bf3Fbdf5a29E2d5148bfe7Dcf7B5639e320";
    const platformFeeRate = process.env.PLATFORM_FEE_RATE || "500"; // 5%
    const platformFeeReceiver = process.env.PLATFORM_FEE_RECEIVER || deployer.address;

    console.log("📋 部署参数:");
    console.log("   Switchboard VRF:", switchboardVRF);
    console.log("   平台手续费率:", platformFeeRate, "(", parseInt(platformFeeRate) / 100, "%)");
    console.log("   手续费接收地址:", platformFeeReceiver);

    // 读取旧合约的 poolCounter（用于延续 poolId）
    let initialPoolCounter = 0;
    const oldContractAddress = process.env.TREASURE_POOL_ADDRESS;
    if (oldContractAddress && oldContractAddress !== "" && oldContractAddress !== "0x") {
        try {
            const oldABI = ["function poolCounter() external view returns (uint256)"];
            const oldContract = new ethers.Contract(oldContractAddress, oldABI, deployer);
            const oldCounter = await oldContract.poolCounter();
            initialPoolCounter = Number(oldCounter);
            console.log("   旧合约地址:", oldContractAddress);
            console.log("   旧合约 poolCounter:", initialPoolCounter);
            console.log("   → 新合约将从 poolId =", initialPoolCounter + 1, "开始");
        } catch (err) {
            console.warn("   ⚠️ 无法读取旧合约 poolCounter，从 0 开始:", err.message);
        }
    } else {
        console.log("   无旧合约，poolCounter 从 0 开始");
    }
    console.log();

    // 部署合约
    console.log("⏳ 正在部署 TreasurePool 合约...");
    const TreasurePool = await ethers.getContractFactory("TreasurePool");
    const treasurePool = await TreasurePool.deploy(
        switchboardVRF,
        parseInt(platformFeeRate),
        platformFeeReceiver,
        initialPoolCounter
    );

    console.log("⏳ 等待合约部署确认...");
    await treasurePool.waitForDeployment();

    const contractAddress = await treasurePool.getAddress();
    console.log("✅ TreasurePool 合约部署成功!");
    console.log("📍 合约地址:", contractAddress);

    // 获取部署交易信息
    const deployTx = treasurePool.deploymentTransaction();
    if (deployTx) {
        console.log("📝 部署交易哈希:", deployTx.hash);
        console.log("⛽ Gas 使用:", deployTx.gasLimit.toString());
    }

    // 验证合约部署
    console.log("\n🔍 验证合约部署...");
    const code = await ethers.provider.getCode(contractAddress);
    if (code === "0x") {
        console.error("❌ 错误: 合约代码为空，部署可能失败");
        process.exit(1);
    }
    console.log("✅ 合约代码已部署");

    // 验证合约初始化
    console.log("\n🔍 验证合约初始化...");
    const owner = await treasurePool.owner();
    const vrfAddress = await treasurePool.switchboardVRF();
    const feeRate = await treasurePool.platformFeeRate();
    const feeReceiver = await treasurePool.platformFeeReceiver();

    console.log("   Owner:", owner);
    console.log("   Switchboard VRF:", vrfAddress);
    console.log("   平台手续费率:", feeRate.toString());
    console.log("   手续费接收地址:", feeReceiver);

    if (owner !== deployer.address) {
        console.error("❌ 错误: Owner 地址不匹配");
        process.exit(1);
    }
    console.log("✅ 合约初始化验证通过");

    // 保存部署信息
    const deploymentInfo = {
        network: "monad-testnet",
        chainId: 10143,
        contractAddress: contractAddress,
        deployerAddress: deployer.address,
        switchboardVRF: switchboardVRF,
        platformFeeRate: platformFeeRate,
        platformFeeReceiver: platformFeeReceiver,
        deploymentTxHash: deployTx ? deployTx.hash : null,
        deployedAt: new Date().toISOString(),
        blockNumber: deployTx ? deployTx.blockNumber : null
    };

    // 创建 deployments 文件夹（如果不存在）
    const deploymentsDir = path.join(__dirname, "..", "deployments");
    if (!fs.existsSync(deploymentsDir)) {
        fs.mkdirSync(deploymentsDir, { recursive: true });
    }

    // 使用日期命名部署文件
    const date = new Date().toISOString().split('T')[0]; // YYYY-MM-DD
    const deploymentPath = path.join(deploymentsDir, `monad-testnet-${date}.json`);

    fs.writeFileSync(
        deploymentPath,
        JSON.stringify(deploymentInfo, null, 2),
        "utf8"
    );
    console.log("\n✅ 部署信息已保存到:", deploymentPath);

    // 更新 .env 文件
    console.log("\n⏳ 更新 .env 文件...");
    const envPath = path.join(__dirname, "..", ".env");
    let envContent = fs.readFileSync(envPath, "utf8");

    envContent = envContent.replace(
        /TREASURE_POOL_ADDRESS=.*/,
        `TREASURE_POOL_ADDRESS=${contractAddress}`
    );

    fs.writeFileSync(envPath, envContent, "utf8");
    console.log("✅ .env 文件已更新");

    // 部署总结
    console.log("\n" + "=".repeat(60));
    console.log("🎉 部署完成!");
    console.log("=".repeat(60));
    console.log("📍 合约地址:", contractAddress);
    console.log("🔗 区块浏览器:", `https://testnet.monad.xyz/address/${contractAddress}`);
    console.log("\n📋 下一步:");
    console.log("1. 在区块浏览器中验证合约部署");
    console.log("2. 测试合约基本功能");
    console.log("3. 开始后端开发集成");
    console.log("=".repeat(60));
}

main()
    .then(() => process.exit(0))
    .catch((error) => {
        console.error("\n❌ 部署失败:");
        console.error(error);
        process.exit(1);
    });
