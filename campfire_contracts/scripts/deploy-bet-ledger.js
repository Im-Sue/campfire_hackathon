const { ethers } = require("hardhat");
const fs = require("fs");
const path = require("path");
require("dotenv").config();

async function main() {
    console.log("🚀 开始部署 BetLedger 合约到 Monad Testnet...\n");

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

    // 部署合约（BetLedger 无构造参数）
    console.log("⏳ 正在部署 BetLedger 合约...");
    const BetLedger = await ethers.getContractFactory("BetLedger");
    const betLedger = await BetLedger.deploy();

    console.log("⏳ 等待合约部署确认...");
    await betLedger.waitForDeployment();

    const contractAddress = await betLedger.getAddress();
    console.log("✅ BetLedger 合约部署成功!");
    console.log("📍 合约地址:", contractAddress);

    // 获取部署交易信息
    const deployTx = betLedger.deploymentTransaction();
    if (deployTx) {
        console.log("📝 部署交易哈希:", deployTx.hash);
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
    const owner = await betLedger.owner();
    const batchCounter = await betLedger.batchCounter();
    console.log("   Owner:", owner);
    console.log("   BatchCounter:", batchCounter.toString());

    if (owner !== deployer.address) {
        console.error("❌ 错误: Owner 地址不匹配");
        process.exit(1);
    }
    console.log("✅ 合约初始化验证通过");

    // 保存部署信息
    const deploymentInfo = {
        contract: "BetLedger",
        network: "monad-testnet",
        chainId: 10143,
        contractAddress: contractAddress,
        deployerAddress: deployer.address,
        deploymentTxHash: deployTx ? deployTx.hash : null,
        deployedAt: new Date().toISOString()
    };

    const deploymentsDir = path.join(__dirname, "..", "deployments");
    if (!fs.existsSync(deploymentsDir)) {
        fs.mkdirSync(deploymentsDir, { recursive: true });
    }

    const date = new Date().toISOString().split('T')[0];
    const deploymentPath = path.join(deploymentsDir, `bet-ledger-${date}.json`);
    fs.writeFileSync(deploymentPath, JSON.stringify(deploymentInfo, null, 2), "utf8");
    console.log("\n✅ 部署信息已保存到:", deploymentPath);

    // 部署总结
    console.log("\n" + "=".repeat(60));
    console.log("🎉 BetLedger 部署完成!");
    console.log("=".repeat(60));
    console.log("📍 合约地址:", contractAddress);
    console.log("🔗 区块浏览器:", `https://testnet.monadexplorer.com/address/${contractAddress}`);
    console.log("\n📋 下一步:");
    console.log("1. 在 pm_config 中更新 chain.contract_address =", contractAddress);
    console.log("2. 执行 DDL: V012__bet_chain_ledger.sql");
    console.log("3. 重启后端服务");
    console.log("=".repeat(60));
}

main()
    .then(() => process.exit(0))
    .catch((error) => {
        console.error("\n❌ 部署失败:");
        console.error(error);
        process.exit(1);
    });
