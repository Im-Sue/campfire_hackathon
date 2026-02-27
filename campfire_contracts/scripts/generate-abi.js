const fs = require("fs");
const path = require("path");

async function main() {
    console.log("📦 生成合约 ABI 和类型定义...\n");

    // 读取编译产物
    const artifactPath = path.join(
        __dirname,
        "..",
        "artifacts",
        "contracts",
        "treasure",
        "TreasurePool.sol",
        "TreasurePool.json"
    );

    if (!fs.existsSync(artifactPath)) {
        console.error("❌ 错误: 找不到编译产物，请先编译合约");
        console.error("   运行: npx hardhat compile");
        process.exit(1);
    }

    const artifact = JSON.parse(fs.readFileSync(artifactPath, "utf8"));

    // 创建 abi 目录
    const abiDir = path.join(__dirname, "..", "abi");
    if (!fs.existsSync(abiDir)) {
        fs.mkdirSync(abiDir, { recursive: true });
    }

    // 1. 提取并保存 ABI JSON
    console.log("1️⃣ 提取 ABI JSON...");
    const abiPath = path.join(abiDir, "TreasurePool.json");
    fs.writeFileSync(
        abiPath,
        JSON.stringify(artifact.abi, null, 2),
        "utf8"
    );
    console.log("   ✅ ABI 已保存到:", abiPath);

    // 2. 生成 TypeScript 类型定义
    console.log("\n2️⃣ 生成 TypeScript 类型定义...");
    const tsTypes = generateTypeScriptTypes(artifact.abi);
    const tsPath = path.join(abiDir, "TreasurePool.ts");
    fs.writeFileSync(tsPath, tsTypes, "utf8");
    console.log("   ✅ TypeScript 类型已保存到:", tsPath);

    // 3. 保存完整的 artifact（包含 bytecode）
    console.log("\n3️⃣ 保存完整 artifact...");
    const fullArtifactPath = path.join(abiDir, "TreasurePool.artifact.json");
    fs.writeFileSync(
        fullArtifactPath,
        JSON.stringify(artifact, null, 2),
        "utf8"
    );
    console.log("   ✅ 完整 artifact 已保存到:", fullArtifactPath);

    // 4. 生成使用说明
    console.log("\n4️⃣ 生成使用说明...");
    const readmePath = path.join(abiDir, "README.md");
    const readmeContent = generateReadme();
    fs.writeFileSync(readmePath, readmeContent, "utf8");
    console.log("   ✅ 使用说明已保存到:", readmePath);

    console.log("\n" + "=".repeat(60));
    console.log("✅ ABI 和类型定义生成完成!");
    console.log("=".repeat(60));
    console.log("\n📁 生成的文件:");
    console.log("   - TreasurePool.json (ABI)");
    console.log("   - TreasurePool.ts (TypeScript 类型)");
    console.log("   - TreasurePool.artifact.json (完整 artifact)");
    console.log("   - README.md (使用说明)");
    console.log("\n📋 下一步:");
    console.log("   - 前端: 复制 TreasurePool.json 和 TreasurePool.ts 到前端项目");
    console.log("   - 后端: 使用 web3j 生成 Java 包装类");
    console.log("     命令: web3j generate solidity -a abi/TreasurePool.json -o [输出目录] -p [包名]");
}

function generateTypeScriptTypes(abi) {
    return `// Auto-generated TypeScript types for TreasurePool contract
// Generated at: ${new Date().toISOString()}

export const TreasurePoolABI = ${JSON.stringify(abi, null, 2)} as const;

// Contract address (update after deployment)
export const TREASURE_POOL_ADDRESS = "${process.env.TREASURE_POOL_ADDRESS || "0x0D8A1Fd375b4D75f5301dDCAc018Feb899a150bF"}";

// Pool status enum
export enum PoolStatus {
    Active = 0,
    Locked = 1,
    Drawing = 2,
    Settled = 3
}

// Pool structure
export interface Pool {
    id: bigint;
    price: bigint;
    totalShares: bigint;
    soldShares: bigint;
    winnerCount: bigint;
    endTime: bigint;
    status: PoolStatus;
    randomnessRequestId: string;
    prizePerWinner: bigint;
    winners: string[];
}

// Event types
export interface PoolCreatedEvent {
    poolId: bigint;
    price: bigint;
    totalShares: bigint;
    winnerCount: bigint;
    endTime: bigint;
}

export interface TicketPurchasedEvent {
    poolId: bigint;
    user: string;
    ticketIndex: bigint;
}

export interface DrawStartedEvent {
    poolId: bigint;
    requestId: string;
}

export interface DrawCompletedEvent {
    poolId: bigint;
    winners: string[];
    prizePerWinner: bigint;
}

export interface PrizeClaimedEvent {
    poolId: bigint;
    winner: string;
    amount: bigint;
}

// Contract function types
export interface TreasurePoolContract {
    // Read functions
    getPool(poolId: bigint): Promise<Pool>;
    getUserTicket(user: string, poolId: bigint): Promise<bigint>;
    isWinner(user: string, poolId: bigint): Promise<boolean>;
    getDisplayCode(poolId: bigint, index: bigint): Promise<string>;
    owner(): Promise<string>;
    platformFeeRate(): Promise<bigint>;
    platformFeeReceiver(): Promise<string>;
    switchboardVRF(): Promise<string>;

    // Write functions (owner only)
    createPool(
        price: bigint,
        totalShares: bigint,
        duration: bigint,
        winnerCount: bigint
    ): Promise<void>;

    executeDraw(poolId: bigint, vrfFee: bigint): Promise<void>;

    setPlatformFeeRate(newRate: bigint): Promise<void>;
    setPlatformFeeReceiver(newReceiver: string): Promise<void>;
    withdrawPlatformFee(): Promise<void>;
    transferOwnership(newOwner: string): Promise<void>;

    // Write functions (user)
    joinPool(poolId: bigint, value: bigint): Promise<void>;
    claimPrize(poolId: bigint): Promise<void>;
}

// Helper function to format wei to ether
export function formatMON(wei: bigint): string {
    return (Number(wei) / 1e18).toFixed(4);
}

// Helper function to parse ether to wei
export function parseMON(ether: string): bigint {
    return BigInt(Math.floor(parseFloat(ether) * 1e18));
}
`;
}

function generateReadme() {
    return `# TreasurePool Contract ABI

本目录包含 TreasurePool 智能合约的 ABI 和类型定义。

## 文件说明

### TreasurePool.json
纯 ABI JSON 文件，包含合约的所有函数和事件定义。

**用途**:
- 前端: 使用 ethers.js 或 web3.js 与合约交互
- 后端: 使用 web3j 生成 Java 包装类

### TreasurePool.ts
TypeScript 类型定义文件，包含：
- ABI 常量导出
- 合约地址常量
- Pool 结构体类型
- 事件类型定义
- 合约函数接口
- 辅助函数（格式化 MON）

**用途**: 前端 TypeScript 项目

### TreasurePool.artifact.json
完整的 Hardhat 编译产物，包含：
- ABI
- Bytecode
- 部署信息
- 编译器版本等

**用途**: 需要部署合约或获取 bytecode 时使用

## 前端使用示例

### 使用 ethers.js v6

\`\`\`typescript
import { ethers } from "ethers";
import { TreasurePoolABI, TREASURE_POOL_ADDRESS } from "./abi/TreasurePool";

// 连接到合约
const provider = new ethers.JsonRpcProvider("https://testnet-rpc.monad.xyz");
const contract = new ethers.Contract(
    TREASURE_POOL_ADDRESS,
    TreasurePoolABI,
    provider
);

// 读取奖池信息
const pool = await contract.getPool(1);
console.log("奖池价格:", ethers.formatEther(pool.price), "MON");

// 用户参与（需要 signer）
const signer = await provider.getSigner();
const contractWithSigner = contract.connect(signer);
await contractWithSigner.joinPool(1, { value: pool.price });
\`\`\`

### 监听事件

\`\`\`typescript
// 监听 TicketPurchased 事件
contract.on("TicketPurchased", (poolId, user, ticketIndex) => {
    console.log(\`用户 \${user} 购买了奖池 \${poolId} 的票号 \${ticketIndex}\`);
});

// 监听 DrawCompleted 事件
contract.on("DrawCompleted", (poolId, winners, prizePerWinner) => {
    console.log(\`奖池 \${poolId} 开奖完成\`);
    console.log("中奖者:", winners);
    console.log("每人奖金:", ethers.formatEther(prizePerWinner), "MON");
});
\`\`\`

## 后端使用（Java + web3j）

### 生成 Java 包装类

\`\`\`bash
# 安装 web3j CLI
# https://docs.web3j.io/4.8.7/command_line_tools/

# 生成 Java 包装类
web3j generate solidity \\
    -a abi/TreasurePool.json \\
    -o src/main/java \\
    -p cn.iocoder.yudao.module.treasure.contract
\`\`\`

### 使用 Java 包装类

\`\`\`java
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;
import cn.iocoder.yudao.module.treasure.contract.TreasurePool;

// 连接到合约
Web3j web3j = Web3j.build(new HttpService("https://testnet-rpc.monad.xyz"));
TreasurePool contract = TreasurePool.load(
    "0x0D8A1Fd375b4D75f5301dDCAc018Feb899a150bF",
    web3j,
    credentials,
    new DefaultGasProvider()
);

// 读取奖池信息
TreasurePool.Pool pool = contract.getPool(BigInteger.ONE).send();
System.out.println("奖池价格: " + pool.price);

// 监听事件
contract.ticketPurchasedEventFlowable(
    DefaultBlockParameterName.LATEST,
    DefaultBlockParameterName.LATEST
).subscribe(event -> {
    System.out.println("用户购买票号: " + event.user);
});
\`\`\`

## 合约地址

- **Monad Testnet**: \`${process.env.TREASURE_POOL_ADDRESS || "0x0D8A1Fd375b4D75f5301dDCAc018Feb899a150bF"}\`
- **区块浏览器**: https://testnet.monad.xyz/address/${process.env.TREASURE_POOL_ADDRESS || "0x0D8A1Fd375b4D75f5301dDCAc018Feb899a150bF"}

## 更新说明

当合约重新部署时，需要更新：
1. \`TreasurePool.ts\` 中的 \`TREASURE_POOL_ADDRESS\`
2. 本 README 中的合约地址
3. 前端和后端代码中的合约地址配置
`;
}

main()
    .then(() => process.exit(0))
    .catch((error) => {
        console.error(error);
        process.exit(1);
    });
