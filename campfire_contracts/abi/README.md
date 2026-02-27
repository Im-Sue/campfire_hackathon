# TreasurePool Contract ABI

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

```typescript
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
```

### 监听事件

```typescript
// 监听 TicketPurchased 事件
contract.on("TicketPurchased", (poolId, user, ticketIndex) => {
    console.log(`用户 ${user} 购买了奖池 ${poolId} 的票号 ${ticketIndex}`);
});

// 监听 DrawCompleted 事件
contract.on("DrawCompleted", (poolId, winners, prizePerWinner) => {
    console.log(`奖池 ${poolId} 开奖完成`);
    console.log("中奖者:", winners);
    console.log("每人奖金:", ethers.formatEther(prizePerWinner), "MON");
});
```

## 后端使用（Java + web3j）

### 生成 Java 包装类

```bash
# 安装 web3j CLI
# https://docs.web3j.io/4.8.7/command_line_tools/

# 生成 Java 包装类
web3j generate solidity \
    -a abi/TreasurePool.json \
    -o src/main/java \
    -p cn.iocoder.yudao.module.treasure.contract
```

### 使用 Java 包装类

```java
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
```

## 合约地址

- **Monad Testnet**: `0x0D8A1Fd375b4D75f5301dDCAc018Feb899a150bF`
- **区块浏览器**: https://testnet.monad.xyz/address/0x0D8A1Fd375b4D75f5301dDCAc018Feb899a150bF

## 更新说明

当合约重新部署时，需要更新：
1. `TreasurePool.ts` 中的 `TREASURE_POOL_ADDRESS`
2. 本 README 中的合约地址
3. 前端和后端代码中的合约地址配置
