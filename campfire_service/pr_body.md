## 问题
poolId=23 开奖失败，合约 revert: `Pool is not active`

## 原因
1. 调度器首次调用 `executeDraw` 成功（链上状态 Active→Drawing）
2. 但后续 `resolveRandomness` 失败，Java 层 catch 将数据库状态回滚为 ACTIVE
3. 下次调度再次调用 `executeDraw`，链上已是 Drawing，合约 require Active 导致 revert
4. 无限循环重试

## 修复
### TreasureContractServiceImpl.executeDraw
- 调用合约前先查询链上真实状态
- **链上 Drawing(2)**: 跳过 Step1，直接用链上 requestId 调 resolveRandomness（恢复模式）
- **链上 Settled(3)**: 跳过开奖，返回 ALREADY_SETTLED

### TreasureDrawScheduler
- 失败时从链上同步真实状态，而非无条件回滚为 ACTIVE
- 处理 ALREADY_SETTLED 返回值，同步数据库
