const { expect } = require("chai");
const { ethers } = require("hardhat");

describe("TreasurePool - Basic Functionality", function () {
    let treasurePool;
    let owner;
    let user1;
    let user2;
    let user3;
    let platformFeeReceiver;
    let mockSwitchboard;

    const PLATFORM_FEE_RATE = 500; // 5%
    const POOL_PRICE = ethers.parseEther("1"); // 1 ETH
    const TOTAL_SHARES = 10;
    const WINNER_COUNT = 3;
    const DURATION = 3600; // 1 hour

    beforeEach(async function () {
        [owner, user1, user2, user3, platformFeeReceiver, mockSwitchboard] = await ethers.getSigners();

        // Deploy TreasurePool contract
        const TreasurePool = await ethers.getContractFactory("TreasurePool");
        treasurePool = await TreasurePool.deploy(
            mockSwitchboard.address,
            PLATFORM_FEE_RATE,
            platformFeeReceiver.address,
            0 // initialPoolCounter
        );
    });

    describe("createPool", function () {
        it("应该成功创建奖池", async function () {
            const tx = await treasurePool.createPool(
                POOL_PRICE,
                TOTAL_SHARES,
                DURATION,
                WINNER_COUNT
            );

            const receipt = await tx.wait();
            const event = receipt.logs.find(log => {
                try {
                    return treasurePool.interface.parseLog(log).name === "PoolCreated";
                } catch {
                    return false;
                }
            });

            expect(event).to.not.be.undefined;

            const poolId = 1;
            const pool = await treasurePool.getPool(poolId);

            expect(pool.id).to.equal(poolId);
            expect(pool.price).to.equal(POOL_PRICE);
            expect(pool.totalShares).to.equal(TOTAL_SHARES);
            expect(pool.soldShares).to.equal(0);
            expect(pool.winnerCount).to.equal(WINNER_COUNT);
            expect(pool.status).to.equal(0); // Active
        });

        it("应该拒绝 price = 0", async function () {
            await expect(
                treasurePool.createPool(0, TOTAL_SHARES, DURATION, WINNER_COUNT)
            ).to.be.revertedWith("Price must be greater than 0");
        });

        it("应该拒绝 totalShares = 0", async function () {
            await expect(
                treasurePool.createPool(POOL_PRICE, 0, DURATION, WINNER_COUNT)
            ).to.be.revertedWith("Total shares must be greater than 0");
        });

        it("应该拒绝 duration = 0", async function () {
            await expect(
                treasurePool.createPool(POOL_PRICE, TOTAL_SHARES, 0, WINNER_COUNT)
            ).to.be.revertedWith("Duration must be greater than 0");
        });

        it("应该拒绝 winnerCount = 0", async function () {
            await expect(
                treasurePool.createPool(POOL_PRICE, TOTAL_SHARES, DURATION, 0)
            ).to.be.revertedWith("Winner count must be greater than 0");
        });

        it("应该拒绝 winnerCount > 10", async function () {
            await expect(
                treasurePool.createPool(POOL_PRICE, TOTAL_SHARES, DURATION, 11)
            ).to.be.revertedWith("Winner count cannot exceed 10");
        });

        it("应该拒绝 winnerCount > totalShares", async function () {
            await expect(
                treasurePool.createPool(POOL_PRICE, 5, DURATION, 10)
            ).to.be.revertedWith("Winner count cannot exceed total shares");
        });

        it("应该拒绝非 owner 调用", async function () {
            await expect(
                treasurePool.connect(user1).createPool(
                    POOL_PRICE,
                    TOTAL_SHARES,
                    DURATION,
                    WINNER_COUNT
                )
            ).to.be.revertedWith("Not owner");
        });
    });

    describe("joinPool", function () {
        let poolId;

        beforeEach(async function () {
            await treasurePool.createPool(
                POOL_PRICE,
                TOTAL_SHARES,
                DURATION,
                WINNER_COUNT
            );
            poolId = 1;
        });

        it("应该成功参与夺宝", async function () {
            const tx = await treasurePool.connect(user1).joinPool(poolId, {
                value: POOL_PRICE
            });

            const receipt = await tx.wait();
            const event = receipt.logs.find(log => {
                try {
                    return treasurePool.interface.parseLog(log).name === "TicketPurchased";
                } catch {
                    return false;
                }
            });

            expect(event).to.not.be.undefined;

            const pool = await treasurePool.getPool(poolId);
            expect(pool.soldShares).to.equal(1);

            const hasParticipated = await treasurePool.hasParticipated(poolId, user1.address);
            expect(hasParticipated).to.be.true;

            const ticketIndex = await treasurePool.getUserTicket(user1.address, poolId);
            expect(ticketIndex).to.equal(1);
        });

        it("应该拒绝重复参与", async function () {
            await treasurePool.connect(user1).joinPool(poolId, {
                value: POOL_PRICE
            });

            await expect(
                treasurePool.connect(user1).joinPool(poolId, {
                    value: POOL_PRICE
                })
            ).to.be.revertedWith("Already participated");
        });

        it("应该拒绝支付金额不正确", async function () {
            await expect(
                treasurePool.connect(user1).joinPool(poolId, {
                    value: ethers.parseEther("0.5")
                })
            ).to.be.revertedWith("Incorrect payment amount");
        });

        it("应该拒绝奖池已售罄", async function () {
            // 创建一个只有 2 份的奖池
            await treasurePool.createPool(
                POOL_PRICE,
                2,
                DURATION,
                1
            );
            const smallPoolId = 2;

            // 两个用户购买
            await treasurePool.connect(user1).joinPool(smallPoolId, {
                value: POOL_PRICE
            });
            await treasurePool.connect(user2).joinPool(smallPoolId, {
                value: POOL_PRICE
            });

            // 第三个用户尝试购买
            await expect(
                treasurePool.connect(user3).joinPool(smallPoolId, {
                    value: POOL_PRICE
                })
            ).to.be.revertedWith("Pool is sold out");
        });

        it("应该拒绝截止时间后参与", async function () {
            // 先推进时间到奖池截止之后
            await ethers.provider.send("evm_increaseTime", [DURATION + 1]);
            await ethers.provider.send("evm_mine");

            // 截止后参与应被拒绝
            await expect(
                treasurePool.connect(user1).joinPool(poolId, {
                    value: POOL_PRICE
                })
            ).to.be.revertedWith("Pool has ended");
        });

        it("应该正确记录多个用户参与", async function () {
            await treasurePool.connect(user1).joinPool(poolId, {
                value: POOL_PRICE
            });
            await treasurePool.connect(user2).joinPool(poolId, {
                value: POOL_PRICE
            });
            await treasurePool.connect(user3).joinPool(poolId, {
                value: POOL_PRICE
            });

            const pool = await treasurePool.getPool(poolId);
            expect(pool.soldShares).to.equal(3);

            const ticket1 = await treasurePool.getUserTicket(user1.address, poolId);
            const ticket2 = await treasurePool.getUserTicket(user2.address, poolId);
            const ticket3 = await treasurePool.getUserTicket(user3.address, poolId);

            expect(ticket1).to.equal(1);
            expect(ticket2).to.equal(2);
            expect(ticket3).to.equal(3);
        });
    });

    describe("getDisplayCode", function () {
        it("应该生成正确的展示码", async function () {
            const poolId = 1;
            const index = 1;

            const displayCode = await treasurePool.getDisplayCode(poolId, index);

            // 验证返回的是 bytes8
            expect(displayCode).to.have.lengthOf(18); // "0x" + 16 hex chars

            // 验证相同输入生成相同输出
            const displayCode2 = await treasurePool.getDisplayCode(poolId, index);
            expect(displayCode).to.equal(displayCode2);

            // 验证不同输入生成不同输出
            const displayCode3 = await treasurePool.getDisplayCode(poolId, 2);
            expect(displayCode).to.not.equal(displayCode3);
        });

        it("应该为不同 poolId 生成不同展示码", async function () {
            const code1 = await treasurePool.getDisplayCode(1, 1);
            const code2 = await treasurePool.getDisplayCode(2, 1);

            expect(code1).to.not.equal(code2);
        });

        it("应该为不同 index 生成不同展示码", async function () {
            const code1 = await treasurePool.getDisplayCode(1, 1);
            const code2 = await treasurePool.getDisplayCode(1, 2);

            expect(code1).to.not.equal(code2);
        });
    });
});
