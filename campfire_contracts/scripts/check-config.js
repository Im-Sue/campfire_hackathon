const { ethers } = require("hardhat");
require("dotenv").config();

async function main() {
    console.log("🔍 检查部署配置...\n");

    // 检查私钥
    const privateKey = process.env.PRIVATE_KEY;
    if (!privateKey) {
        console.error("❌ 错误: 未设置 PRIVATE_KEY");
        process.exit(1);
    }

    // 创建钱包
    let wallet;
    try {
        wallet = new ethers.Wallet(privateKey);
        console.log("✅ 私钥格式正确");
        console.log("📍 部署地址:", wallet.address);
    } catch (error) {
        console.error("❌ 错误: 私钥格式无效");
        process.exit(1);
    }

    // 检查平台手续费接收地址
    const feeReceiver = process.env.PLATFORM_FEE_RECEIVER;
    if (!feeReceiver) {
        console.log("⚠️  警告: 未设置 PLATFORM_FEE_RECEIVER，将使用部署地址");
    } else if (!ethers.isAddress(feeReceiver)) {
        console.error("❌ 错误: PLATFORM_FEE_RECEIVER 不是有效的地址");
        process.exit(1);
    } else {
        console.log("✅ 平台手续费接收地址:", feeReceiver);
    }

    // 连接到 Monad Testnet
    console.log("\n🌐 连接到 Monad Testnet...");
    const provider = new ethers.JsonRpcProvider("https://testnet-rpc.monad.xyz");

    try {
        const network = await provider.getNetwork();
        console.log("✅ 网络连接成功");
        console.log("   Chain ID:", network.chainId.toString());
        console.log("   Network Name:", network.name);
    } catch (error) {
        console.error("❌ 错误: 无法连接到 Monad Testnet");
        console.error("   ", error.message);
        process.exit(1);
    }

    // 检查余额
    console.log("\n💰 检查账户余额...");
    try {
        const balance = await provider.getBalance(wallet.address);
        const balanceInMON = ethers.formatEther(balance);
        console.log("   余额:", balanceInMON, "MON");

        if (balance === 0n) {
            console.log("\n⚠️  警告: 账户余额为 0");
            console.log("   请先获取测试代币:");
            console.log("   1. 访问 Monad Testnet 水龙头");
            console.log("   2. 输入地址:", wallet.address);
            console.log("   3. 领取测试 MON");
        } else if (balance < ethers.parseEther("0.1")) {
            console.log("⚠️  警告: 余额较低，建议至少 0.1 MON");
        } else {
            console.log("✅ 余额充足，可以部署合约");
        }
    } catch (error) {
        console.error("❌ 错误: 无法查询余额");
        console.error("   ", error.message);
    }

    // 检查 Switchboard VRF 地址
    console.log("\n🎲 检查 Switchboard VRF 配置...");
    const switchboardAddress = process.env.SWITCHBOARD_VRF_ADDRESS || "0xD3860E2C66cBd5c969Fa7343e6912Eff0416bA33";
    console.log("   Switchboard VRF 地址:", switchboardAddress);

    try {
        const code = await provider.getCode(switchboardAddress);
        if (code === "0x") {
            console.log("⚠️  警告: Switchboard VRF 合约未部署或地址错误");
        } else {
            console.log("✅ Switchboard VRF 合约存在");
        }
    } catch (error) {
        console.error("⚠️  无法验证 Switchboard VRF 合约");
    }

    console.log("\n" + "=".repeat(50));
    console.log("配置检查完成！");
    console.log("=".repeat(50));
}

main()
    .then(() => process.exit(0))
    .catch((error) => {
        console.error(error);
        process.exit(1);
    });
