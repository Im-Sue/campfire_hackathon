const { expect } = require("chai");
const { ethers } = require("hardhat");

describe("TreasurePool - Claim Prize", function () {
    let treasurePool;
    let mockSwitchboard;
    let owner;
    let user1;
    let user2;
    let user3;
    let user4;
    let user5;
    let platformFeeReceiver;

    const PLATFORM_FEE_RATE = 500; // 5%
    const POOL_PRICE = ethers.parseEther("1"); // 1 ETH
    const TOTAL_SHARES = 10;
    const WINNER_COUNT = 3;
    const DURATION = 3600; // 1 hour
    const VRF_FEE = ethers.parseEther("0.01"); // 0.01 ETH for VRF

    beforeEach(async function () {
        [owner, user1, user2, user3, user4, user5, platformFeeReceiver] = await ethers.getSigners();

        // Deploy MockSwitchboard contract
        const MockSwitchboard = await ethers.getContractFactory("MockSwitchboard");
        mockSwitchboard = await MockSwitchboard.deploy();

        // Deploy TreasurePool contract
        const TreasurePool = await ethers.getContractFactory("TreasurePool");
        treasurePool = await TreasurePool.deploy(
            await mockSwitchboard.getAddress(),
            PLATFORM_FEE_RATE,
            platformFeeReceiver.address,
            0 // initialPoolCounter
        );

        // Create a pool
        await treasurePool.createPool(
            POOL_PRICE,
            TOTAL_SHARES,
            DURATION,
            WINNER_COUNT
        );

        const poolId = 1;

        // 5个用户参与
        await treasurePool.connect(user1).joinPool(poolId, {
            value: POOL_PRICE
        });
        await treasurePool.connect(user2).joinPool(poolId, {
            value: POOL_PRICE
        });
        await treasurePool.connect(user3).joinPool(poolId, {
            value: POOL_PRICE
        });
        await treasurePool.connect(user4).joinPool(poolId, {
            value: POOL_PRICE
        });
        await treasurePool.connect(user5).joinPool(poolId, {
            value: POOL_PRICE
        });

        // 增加时间到到期后
        await ethers.provider.send("evm_increaseTime", [DURATION + 1]);
        await ethers.provider.send("evm_mine");

        // 触发开奖
        const tx = await treasurePool.executeDraw(poolId, { value: VRF_FEE });
        const receipt = await tx.wait();

        // 获取 requestId
        const event = receipt.logs.find(log => {
            try {
                return treasurePool.interface.parseLog(log).name === "DrawStarted";
            } catch {
                return false;
            }
        });

        const parsedEvent = treasurePool.interface.parseLog(event);
        const requestId = parsedEvent.args.requestId;

        // 使用 impersonateAccount 来模拟 Switchboard 合约调用
        await ethers.provider.send("hardhat_impersonateAccount", [await mockSwitchboard.getAddress()]);
        const switchboardImpersonated = await ethers.getSigner(await mockSwitchboard.getAddress());

        // 给 impersonated account 一些 ETH 用于 gas
        await owner.sendTransaction({
            to: await mockSwitchboard.getAddress(),
            value: ethers.parseEther("1")
        });

        // 模拟 VRF 回调
        const randomness = BigInt(ethers.hexlify(ethers.randomBytes(32)));
        await treasurePool.connect(switchboardImpersonated).fulfillRandomness(
            requestId,
            randomness
        );

        await ethers.provider.send("hardhat_stopImpersonatingAccount", [await mockSwitchboard.getAddress()]);
    });

    describe("claimPrize", function () {
        it("应该拒绝奖池未结算", async function () {
            // 创建一个新奖池但不开奖
            await treasurePool.createPool(
                POOL_PRICE,
                TOTAL_SHARES,
                DURATION,
                WINNER_COUNT
            );
            const newPoolId = 2;

            await treasurePool.connect(user1).joinPool(newPoolId, {
                value: POOL_PRICE
            });

            // 尝试领奖
            await expect(
                treasurePool.connect(user1).claimPrize(newPoolId)
            ).to.be.revertedWith("Pool is not settled");
        });

        it("应该拒绝非中奖者领奖", async function () {
            const poolId = 1;

            // 获取中奖者列表
            const pool = await treasurePool.getPool(poolId);
            const winners = pool.winners;

            // 找一个非中奖者
            let nonWinner;
            for (const user of [user1, user2, user3, user4, user5]) {
                if (!winners.includes(user.address)) {
                    nonWinner = user;
                    break;
                }
            }

            // 非中奖者尝试领奖
            if (nonWinner) {
                await expect(
                    treasurePool.connect(nonWinner).claimPrize(poolId)
                ).to.be.revertedWith("Not a winner");
            }
        });

        it("应该成功领取奖金", async function () {
            const poolId = 1;

            // 获取中奖者列表
            const pool = await treasurePool.getPool(poolId);
            const winners = pool.winners;
            const prizePerWinner = pool.prizePerWinner;

            // 选择第一个中奖者
            const winnerAddress = winners[0];
            const winner = await ethers.getSigner(winnerAddress);

            // 记录领奖前的余额
            const balanceBefore = await ethers.provider.getBalance(winnerAddress);

            // 领奖
            const tx = await treasurePool.connect(winner).claimPrize(poolId);
            const receipt = await tx.wait();

            // 计算 gas 费用
            const gasUsed = receipt.gasUsed * receipt.gasPrice;

            // 记录领奖后的余额
            const balanceAfter = await ethers.provider.getBalance(winnerAddress);

            // 验证余额变化
            expect(balanceAfter - balanceBefore + gasUsed).to.equal(prizePerWinner);

            // 验证 PrizeClaimed 事件
            const event = receipt.logs.find(log => {
                try {
                    return treasurePool.interface.parseLog(log).name === "PrizeClaimed";
                } catch {
                    return false;
                }
            });

            expect(event).to.not.be.undefined;

            const parsedEvent = treasurePool.interface.parseLog(event);
            expect(parsedEvent.args.poolId).to.equal(poolId);
            expect(parsedEvent.args.winner).to.equal(winnerAddress);
            expect(parsedEvent.args.amount).to.equal(prizePerWinner);

            // 验证 hasClaimed 状态
            const hasClaimed = await treasurePool.hasClaimed(poolId, winnerAddress);
            expect(hasClaimed).to.be.true;
        });

        it("应该拒绝重复领奖", async function () {
            const poolId = 1;

            // 获取中奖者列表
            const pool = await treasurePool.getPool(poolId);
            const winners = pool.winners;

            // 选择第一个中奖者
            const winnerAddress = winners[0];
            const winner = await ethers.getSigner(winnerAddress);

            // 第一次领奖
            await treasurePool.connect(winner).claimPrize(poolId);

            // 尝试第二次领奖
            await expect(
                treasurePool.connect(winner).claimPrize(poolId)
            ).to.be.revertedWith("Already claimed");
        });

        it("应该允许所有中奖者领奖", async function () {
            const poolId = 1;

            // 获取中奖者列表
            const pool = await treasurePool.getPool(poolId);
            const winners = pool.winners;
            const prizePerWinner = pool.prizePerWinner;

            // 所有中奖者领奖
            for (const winnerAddress of winners) {
                const winner = await ethers.getSigner(winnerAddress);

                // 记录领奖前的余额
                const balanceBefore = await ethers.provider.getBalance(winnerAddress);

                // 领奖
                const tx = await treasurePool.connect(winner).claimPrize(poolId);
                const receipt = await tx.wait();

                // 计算 gas 费用
                const gasUsed = receipt.gasUsed * receipt.gasPrice;

                // 记录领奖后的余额
                const balanceAfter = await ethers.provider.getBalance(winnerAddress);

                // 验证余额变化
                expect(balanceAfter - balanceBefore + gasUsed).to.equal(prizePerWinner);

                // 验证 hasClaimed 状态
                const hasClaimed = await treasurePool.hasClaimed(poolId, winnerAddress);
                expect(hasClaimed).to.be.true;
            }
        });

        it("应该正确计算合约余额", async function () {
            const poolId = 1;

            // 获取中奖者列表
            const pool = await treasurePool.getPool(poolId);
            const winners = pool.winners;
            const prizePerWinner = pool.prizePerWinner;

            // 记录领奖前的合约余额
            const contractBalanceBefore = await ethers.provider.getBalance(await treasurePool.getAddress());

            // 第一个中奖者领奖
            const winnerAddress = winners[0];
            const winner = await ethers.getSigner(winnerAddress);
            await treasurePool.connect(winner).claimPrize(poolId);

            // 记录领奖后的合约余额
            const contractBalanceAfter = await ethers.provider.getBalance(await treasurePool.getAddress());

            // 验证合约余额减少了奖金金额
            expect(contractBalanceBefore - contractBalanceAfter).to.equal(prizePerWinner);
        });

        it("应该在领奖后减少未领取奖金累计值", async function () {
            const poolId = 1;

            // 获取中奖者列表
            const pool = await treasurePool.getPool(poolId);
            const winners = pool.winners;
            const prizePerWinner = pool.prizePerWinner;

            // 领奖前累计未领取奖金
            const beforeUnclaimed = await treasurePool.totalUnclaimedPrize();

            // 首个中奖者领奖
            const winnerAddress = winners[0];
            const winner = await ethers.getSigner(winnerAddress);
            await treasurePool.connect(winner).claimPrize(poolId);

            // 领奖后累计未领取奖金应减少一个人奖金
            const afterUnclaimed = await treasurePool.totalUnclaimedPrize();
            expect(beforeUnclaimed - afterUnclaimed).to.equal(prizePerWinner);
        });
    });

    describe("查询函数", function () {
        it("应该正确查询用户是否中奖", async function () {
            const poolId = 1;

            // 获取中奖者列表
            const pool = await treasurePool.getPool(poolId);
            const winners = pool.winners;

            // 验证中奖者
            for (const winnerAddress of winners) {
                const isWinner = await treasurePool.isWinner(winnerAddress, poolId);
                expect(isWinner).to.be.true;
            }

            // 验证非中奖者
            for (const user of [user1, user2, user3, user4, user5]) {
                if (!winners.includes(user.address)) {
                    const isWinner = await treasurePool.isWinner(user.address, poolId);
                    expect(isWinner).to.be.false;
                }
            }
        });

        it("应该正确查询用户票号", async function () {
            const poolId = 1;

            // 验证每个用户的票号
            const ticket1 = await treasurePool.getUserTicket(user1.address, poolId);
            const ticket2 = await treasurePool.getUserTicket(user2.address, poolId);
            const ticket3 = await treasurePool.getUserTicket(user3.address, poolId);
            const ticket4 = await treasurePool.getUserTicket(user4.address, poolId);
            const ticket5 = await treasurePool.getUserTicket(user5.address, poolId);

            expect(ticket1).to.equal(1);
            expect(ticket2).to.equal(2);
            expect(ticket3).to.equal(3);
            expect(ticket4).to.equal(4);
            expect(ticket5).to.equal(5);
        });
    });
});
