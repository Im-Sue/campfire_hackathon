<p align="center">
  <img src="https://img.shields.io/badge/Chain-Monad-blueviolet?style=for-the-badge" alt="Monad"/>
  <img src="https://img.shields.io/badge/Solidity-0.8.20-363636?style=for-the-badge&logo=solidity" alt="Solidity"/>
  <img src="https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk" alt="Java 17"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?style=for-the-badge&logo=springboot" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/License-MIT-green?style=for-the-badge" alt="License"/>
</p>

# 🔥 Campfire — 基于 Monad 的预测市场 & AI Agent & 夺宝平台

**Campfire** 是一个去中心化的预测市场、AI Agent 与游戏化夺宝平台，部署在 **Monad Testnet** 上。它将事件驱动的预测交易、AI 智能代理与 VRF 驱动的链上抽奖系统相结合，打造透明、公平、有趣的 Web3 体验。

> 🏆 为 Monad 黑客松而构建

---

## ✨ 核心功能

### 📊 预测市场（Prediction Market）
- **事件浏览** — 浏览热门事件，支持多种分类（加密货币、体育、政治等）
- **二元 & 多选市场** — 支持 Yes/No 二元市场及多选项市场
- **实时价格** — 实时同步 Polymarket 市场价格，提供准确赔率
- **订单交易** — 支持限价单、市价单下单及持仓管理
- **链上记账** — 所有下注通过 `BetLedger` 智能合约批量哈希上链，确保完全透明可审计
- **结算引擎** — 自动化市场结算与奖励分发
- **社区评论** — 每个事件下的社区讨论，支持多级回复

### 🎰 夺宝池（Treasure Pool）
- **链上奖池** — 用户使用 MON 购买彩票参与奖池，由 `TreasurePool` 智能合约管理
- **VRF 公平抽奖** — 基于 **Switchboard VRF**（可验证随机函数）选出中奖者，确保可证明的公平性
- **自动开奖调度** — 智能合约在满足条件（时间到期或票数达标）时自动触发开奖
- **奖金领取** — 中奖者可直接从智能合约领取 MON 奖金
- **历史记录** — 查看历史奖池、中奖者及结算详情

### 🤖 AI 代理（AI Agents）
- **AI 市场分析** — 集成 AI 代理提供市场洞察与趋势分析
- **AI 弹幕** — 实时 AI 生成的滚动弹幕评论
- **代理资产追踪** — 监控 AI 代理投资组合的表现

### 🎯 游戏化系统
- **每日任务** — 完成任务赚取积分和奖励
- **积分体系** — 通过活跃参与赚取 Campfire 积分
- **邀请系统** — 推荐好友注册获得奖励

### 💰 钱包与身份
- **Web3 钱包集成** — 连接钱包进行交易和夺宝
- **链上身份** — 用户资料与钱包地址绑定
- **余额管理** — 充值、提现及余额查看

---

## 🏗️ 系统架构

```
┌─────────────────────────────────────────────────────────────────┐
│                       前端应用 (DApp)                           │
│                    React / Next.js / Mobile                     │
└──────────────────────────────┬──────────────────────────────────┘
                               │ REST API
┌──────────────────────────────▼──────────────────────────────────┐
│                   campfire_service（后端服务）                    │
│                      Spring Boot 3 + JDK 17                     │
│  ┌──────────────┐ ┌──────────────┐ ┌────────────────────────┐  │
│  │  市场模块     │ │  夺宝模块     │ │     系统模块            │  │
│  │  - 事件管理   │ │  - 奖池管理   │ │  - 认证 / 权限         │  │
│  │  - 市场管理   │ │  - 彩票管理   │ │  - AI 代理             │  │
│  │  - 订单交易   │ │  - 中奖管理   │ │  - 任务 & 积分         │  │
│  │  - 结算引擎   │ │  - VRF 开奖   │ │  - 社交 / 邀请         │  │
│  │  - 社区评论   │ │  - 定时调度   │ │  - 基础设施            │  │
│  └──────┬───────┘ └──────┬───────┘ └────────────────────────┘  │
│         │                │                                      │
│         │    ┌───────────▼──────────────┐                       │
│         │    │    区块链交互服务          │                       │
│         │    │   (Web3j / 合约调用)      │                       │
│         │    └───────────┬──────────────┘                       │
└─────────┼────────────────┼──────────────────────────────────────┘
          │                │
    ┌─────▼────┐     ┌─────▼──────────────────────────────────┐
    │  MySQL   │     │        Monad Testnet (EVM)              │
    │  Redis   │     │  ┌──────────────┐ ┌─────────────────┐  │
    └──────────┘     │  │ TreasurePool │ │   BetLedger     │  │
                     │  │  (VRF 开奖)  │ │ (下注哈希记账)   │  │
                     │  └──────────────┘ └─────────────────┘  │
                     │  ┌──────────────────────────────────┐  │
                     │  │     Switchboard VRF 预言机         │  │
                     │  └──────────────────────────────────┘  │
                     └─────────────────────────────────────────┘
```

---

## 📂 项目结构

```
campfire_hackathon/
├── campfire_contracts/          # 智能合约（Hardhat 项目）
│   ├── contracts/
│   │   ├── market/
│   │   │   └── BetLedger.sol    # 预测市场下注链上记账合约
│   │   ├── interfaces/
│   │   │   └── ISwitchboard.sol  # Switchboard VRF 接口
│   │   └── mocks/
│   │       └── MockSwitchboard.sol
│   ├── abi/                     # 生成的 ABI 文件
│   ├── scripts/                 # 部署与工具脚本
│   ├── test/                    # 合约测试套件
│   ├── deployments/             # 部署记录（Monad Testnet）
│   ├── hardhat.config.js
│   └── package.json
│
├── campfire_service/            # 后端服务（Spring Boot）
│   ├── yudao-module-market/     # 📊 预测市场模块
│   ├── yudao-module-treasure/   # 🎰 夺宝池模块
│   ├── yudao-module-task/       # 🎯 每日任务模块
│   ├── yudao-module-point/      # 💎 积分系统模块
│   ├── yudao-module-ai/         # 🤖 AI 集成模块
│   ├── yudao-module-system/     # 👤 用户、认证、权限
│   ├── yudao-module-infra/      # ⚙️ 基础设施
│   ├── yudao-module-social/     # 🔗 社交登录 & 邀请
│   ├── yudao-framework/         # 🧱 核心框架
│   ├── yudao-server/            # 🚀 应用启动入口
│   └── sql/                     # 数据库初始化脚本
│
└── README.md
```

---

## 🔗 智能合约

### TreasurePool — 夺宝奖池合约

管理夺宝池完整生命周期的核心合约。

| 函数 | 说明 |
|------|------|
| `createPool()` | 创建新奖池，可配置参数（票价、中奖人数、时长等） |
| `buyTickets()` | 用户使用 MON 购买彩票进入奖池 |
| `executeDraw()` | 触发基于 VRF 的随机开奖 |
| `fulfillRandomness()` | Switchboard VRF 回调，传入随机种子 |
| `claimPrize()` | 中奖者领取 MON 奖金 |
| `getPoolInfo()` | 查询奖池状态、参与者及奖金信息 |

**设计要点：**
- 平台手续费（可配置，默认 5%）从奖池中扣除
- 支持多中奖者，奖金平均分配
- 支持基于时间和票数的双重开奖触发条件
- VRF 回调确保随机数不可篡改

### BetLedger — 下注链上记账合约

为预测市场提供链上透明审计。

| 函数 | 说明 |
|------|------|
| `recordBets()` | 批量记录下注哈希上链（每批最多 100 条） |
| `isBetRecorded()` | 查询单笔下注是否已上链 |
| `areBetsRecorded()` | 批量查询多笔下注状态 |

**下注哈希算法：**
```solidity
betHash = keccak256(abi.encodePacked(orderId, walletAddress, marketId, outcome, amount, filledAt))
```

### 已部署合约（Monad Testnet）

| 合约 | 地址 |
|------|------|
| TreasurePool | `0xF8c02cCf233BB5A8FAE9C5df45DE3cba1F4F31b3` |
| BetLedger | 参见 `campfire_contracts/deployments/` |
| Switchboard VRF | `0x36825bf3Fbdf5a29E2d5148bfe7Dcf7B5639e320` |

---

## 🛠️ 技术栈

| 层级 | 技术 |
|------|------|
| **区块链** | Monad Testnet（EVM 兼容，chainId: 10143） |
| **智能合约** | Solidity 0.8.20、Hardhat、Switchboard VRF |
| **后端** | Java 17、Spring Boot 3.x、MyBatis-Plus |
| **数据库** | MySQL 8.x |
| **缓存** | Redis（Redisson） |
| **AI** | 多模型支持（DeepSeek、GPT、Claude、Gemini 等） |
| **接口文档** | Knife4j / Swagger（SpringDoc） |
| **认证** | JWT + OAuth2（微信、钉钉等） |
| **定时任务** | Quartz + Spring @Scheduled |

---

## 🚀 快速开始

### 环境要求

- **JDK 17+**
- **Maven 3.8+**
- **MySQL 8.0+**
- **Redis 6.0+**
- **Node.js 18+**（合约项目需要）

### 1. 智能合约

```bash
cd campfire_contracts

# 安装依赖
npm install

# 配置环境变量
cp .env.example .env
# 编辑 .env，填入你的私钥和 RPC 地址

# 编译合约
npx hardhat compile

# 运行测试
npx hardhat test

# 部署到 Monad Testnet
npx hardhat run scripts/deploy.js --network monadTestnet
```

### 2. 后端服务

```bash
cd campfire_service

# 初始化数据库
# 将 sql/mysql/ruoyi-vue-pro.sql 导入 MySQL

# 配置应用
# 编辑 yudao-server/src/main/resources/application-local.yaml
# 设置数据库、Redis 和区块链配置

# 构建 & 启动
mvn clean install -DskipTests
cd yudao-server
mvn spring-boot:run
```

服务启动后访问：`http://localhost:48080`

接口文档：`http://localhost:48080/swagger-ui`

---

---

## 🔐 安全设计

- 所有智能合约操作在链上可验证
- VRF 确保开奖随机数的可证明公平性
- 下注哈希提供防篡改的审计记录
- 后端采用 JWT 认证与基于角色的访问控制
- 敏感配置通过环境变量管理，不写入代码

---

## 📄 许可证

本项目基于 [MIT License](LICENSE) 开源。

---

<p align="center">
  <b>🔥 在 Monad 黑客松中，用热情构建 🔥</b>
</p>
