const { ethers } = require("ethers");
const fs = require("fs");
const path = require("path");

async function main() {
    console.log("🔐 创建部署钱包...\n");

    // 创建新钱包
    const wallet = ethers.Wallet.createRandom();

    // 准备钱包信息
    const walletInfo = {
        address: wallet.address,
        privateKey: wallet.privateKey,
        mnemonic: wallet.mnemonic.phrase,
        createdAt: new Date().toISOString()
    };

    console.log("✅ 钱包创建成功！\n");
    console.log("=" .repeat(60));
    console.log("📍 钱包地址:", walletInfo.address);
    console.log("🔑 私钥:", walletInfo.privateKey);
    console.log("📝 助记词:", walletInfo.mnemonic);
    console.log("=" .repeat(60));

    // 保存到 wallet-info.json
    const walletInfoPath = path.join(__dirname, "..", "wallet-info.json");
    fs.writeFileSync(
        walletInfoPath,
        JSON.stringify(walletInfo, null, 2),
        "utf8"
    );
    console.log("\n✅ 钱包信息已保存到:", walletInfoPath);

    // 更新 .env 文件
    const envPath = path.join(__dirname, "..", ".env");
    let envContent = fs.readFileSync(envPath, "utf8");

    // 更新 PRIVATE_KEY
    envContent = envContent.replace(
        /PRIVATE_KEY=.*/,
        `PRIVATE_KEY=${wallet.privateKey.slice(2)}`  // 移除 0x 前缀
    );

    // 更新 PLATFORM_FEE_RECEIVER
    envContent = envContent.replace(
        /PLATFORM_FEE_RECEIVER=.*/,
        `PLATFORM_FEE_RECEIVER=${wallet.address}`
    );

    fs.writeFileSync(envPath, envContent, "utf8");
    console.log("✅ .env 文件已更新\n");

    // 安全提示
    console.log("⚠️  重要提示:");
    console.log("1. 请妥善保管 wallet-info.json 文件");
    console.log("2. 不要将私钥和助记词泄露给任何人");
    console.log("3. wallet-info.json 已添加到 .gitignore");
    console.log("4. 请备份助记词到安全的地方\n");

    // 下一步提示
    console.log("📋 下一步:");
    console.log("1. 访问 Monad Testnet 水龙头获取测试 MON");
    console.log("2. 输入地址:", wallet.address);
    console.log("3. 运行配置检查: npx hardhat run scripts/check-config.js");
}

main()
    .then(() => process.exit(0))
    .catch((error) => {
        console.error(error);
        process.exit(1);
    });
