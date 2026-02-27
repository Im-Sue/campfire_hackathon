// Auto-generated TypeScript types for TreasurePool contract
// Generated at: 2026-02-09T12:54:37.734Z

export const TreasurePoolABI = [
  {
    "inputs": [
      {
        "internalType": "address",
        "name": "_switchboardVRF",
        "type": "address"
      },
      {
        "internalType": "uint256",
        "name": "_platformFeeRate",
        "type": "uint256"
      },
      {
        "internalType": "address",
        "name": "_platformFeeReceiver",
        "type": "address"
      }
    ],
    "stateMutability": "nonpayable",
    "type": "constructor"
  },
  {
    "anonymous": false,
    "inputs": [
      {
        "indexed": true,
        "internalType": "uint256",
        "name": "poolId",
        "type": "uint256"
      },
      {
        "indexed": false,
        "internalType": "address[]",
        "name": "winners",
        "type": "address[]"
      },
      {
        "indexed": false,
        "internalType": "uint256",
        "name": "prizePerWinner",
        "type": "uint256"
      }
    ],
    "name": "DrawCompleted",
    "type": "event"
  },
  {
    "anonymous": false,
    "inputs": [
      {
        "indexed": true,
        "internalType": "uint256",
        "name": "poolId",
        "type": "uint256"
      },
      {
        "indexed": false,
        "internalType": "bytes32",
        "name": "requestId",
        "type": "bytes32"
      }
    ],
    "name": "DrawStarted",
    "type": "event"
  },
  {
    "anonymous": false,
    "inputs": [
      {
        "indexed": true,
        "internalType": "uint256",
        "name": "poolId",
        "type": "uint256"
      },
      {
        "indexed": false,
        "internalType": "uint256",
        "name": "price",
        "type": "uint256"
      },
      {
        "indexed": false,
        "internalType": "uint256",
        "name": "totalShares",
        "type": "uint256"
      },
      {
        "indexed": false,
        "internalType": "uint256",
        "name": "winnerCount",
        "type": "uint256"
      },
      {
        "indexed": false,
        "internalType": "uint256",
        "name": "endTime",
        "type": "uint256"
      }
    ],
    "name": "PoolCreated",
    "type": "event"
  },
  {
    "anonymous": false,
    "inputs": [
      {
        "indexed": true,
        "internalType": "uint256",
        "name": "poolId",
        "type": "uint256"
      },
      {
        "indexed": true,
        "internalType": "address",
        "name": "winner",
        "type": "address"
      },
      {
        "indexed": false,
        "internalType": "uint256",
        "name": "amount",
        "type": "uint256"
      }
    ],
    "name": "PrizeClaimed",
    "type": "event"
  },
  {
    "anonymous": false,
    "inputs": [
      {
        "indexed": true,
        "internalType": "uint256",
        "name": "poolId",
        "type": "uint256"
      },
      {
        "indexed": true,
        "internalType": "address",
        "name": "buyer",
        "type": "address"
      },
      {
        "indexed": false,
        "internalType": "uint256",
        "name": "ticketIndex",
        "type": "uint256"
      }
    ],
    "name": "TicketPurchased",
    "type": "event"
  },
  {
    "inputs": [
      {
        "internalType": "uint256",
        "name": "poolId",
        "type": "uint256"
      }
    ],
    "name": "claimPrize",
    "outputs": [],
    "stateMutability": "nonpayable",
    "type": "function"
  },
  {
    "inputs": [
      {
        "internalType": "uint256",
        "name": "price",
        "type": "uint256"
      },
      {
        "internalType": "uint256",
        "name": "totalShares",
        "type": "uint256"
      },
      {
        "internalType": "uint256",
        "name": "duration",
        "type": "uint256"
      },
      {
        "internalType": "uint256",
        "name": "winnerCount",
        "type": "uint256"
      }
    ],
    "name": "createPool",
    "outputs": [
      {
        "internalType": "uint256",
        "name": "",
        "type": "uint256"
      }
    ],
    "stateMutability": "nonpayable",
    "type": "function"
  },
  {
    "inputs": [
      {
        "internalType": "uint256",
        "name": "poolId",
        "type": "uint256"
      }
    ],
    "name": "executeDraw",
    "outputs": [],
    "stateMutability": "payable",
    "type": "function"
  },
  {
    "inputs": [
      {
        "internalType": "bytes32",
        "name": "requestId",
        "type": "bytes32"
      },
      {
        "internalType": "uint256",
        "name": "randomness",
        "type": "uint256"
      }
    ],
    "name": "fulfillRandomness",
    "outputs": [],
    "stateMutability": "nonpayable",
    "type": "function"
  },
  {
    "inputs": [
      {
        "internalType": "uint256",
        "name": "poolId",
        "type": "uint256"
      },
      {
        "internalType": "uint256",
        "name": "index",
        "type": "uint256"
      }
    ],
    "name": "getDisplayCode",
    "outputs": [
      {
        "internalType": "bytes8",
        "name": "",
        "type": "bytes8"
      }
    ],
    "stateMutability": "pure",
    "type": "function"
  },
  {
    "inputs": [
      {
        "internalType": "uint256",
        "name": "poolId",
        "type": "uint256"
      }
    ],
    "name": "getPool",
    "outputs": [
      {
        "components": [
          {
            "internalType": "uint256",
            "name": "id",
            "type": "uint256"
          },
          {
            "internalType": "uint256",
            "name": "price",
            "type": "uint256"
          },
          {
            "internalType": "uint256",
            "name": "totalShares",
            "type": "uint256"
          },
          {
            "internalType": "uint256",
            "name": "soldShares",
            "type": "uint256"
          },
          {
            "internalType": "uint256",
            "name": "winnerCount",
            "type": "uint256"
          },
          {
            "internalType": "uint256",
            "name": "endTime",
            "type": "uint256"
          },
          {
            "internalType": "enum TreasurePool.PoolStatus",
            "name": "status",
            "type": "uint8"
          },
          {
            "internalType": "bytes32",
            "name": "randomnessRequestId",
            "type": "bytes32"
          },
          {
            "internalType": "uint256",
            "name": "prizePerWinner",
            "type": "uint256"
          },
          {
            "internalType": "address[]",
            "name": "winners",
            "type": "address[]"
          }
        ],
        "internalType": "struct TreasurePool.Pool",
        "name": "",
        "type": "tuple"
      }
    ],
    "stateMutability": "view",
    "type": "function"
  },
  {
    "inputs": [
      {
        "internalType": "address",
        "name": "user",
        "type": "address"
      },
      {
        "internalType": "uint256",
        "name": "poolId",
        "type": "uint256"
      }
    ],
    "name": "getUserTicket",
    "outputs": [
      {
        "internalType": "uint256",
        "name": "",
        "type": "uint256"
      }
    ],
    "stateMutability": "view",
    "type": "function"
  },
  {
    "inputs": [
      {
        "internalType": "uint256",
        "name": "",
        "type": "uint256"
      },
      {
        "internalType": "address",
        "name": "",
        "type": "address"
      }
    ],
    "name": "hasClaimed",
    "outputs": [
      {
        "internalType": "bool",
        "name": "",
        "type": "bool"
      }
    ],
    "stateMutability": "view",
    "type": "function"
  },
  {
    "inputs": [
      {
        "internalType": "uint256",
        "name": "",
        "type": "uint256"
      },
      {
        "internalType": "address",
        "name": "",
        "type": "address"
      }
    ],
    "name": "hasParticipated",
    "outputs": [
      {
        "internalType": "bool",
        "name": "",
        "type": "bool"
      }
    ],
    "stateMutability": "view",
    "type": "function"
  },
  {
    "inputs": [
      {
        "internalType": "address",
        "name": "user",
        "type": "address"
      },
      {
        "internalType": "uint256",
        "name": "poolId",
        "type": "uint256"
      }
    ],
    "name": "isWinner",
    "outputs": [
      {
        "internalType": "bool",
        "name": "",
        "type": "bool"
      }
    ],
    "stateMutability": "view",
    "type": "function"
  },
  {
    "inputs": [
      {
        "internalType": "uint256",
        "name": "poolId",
        "type": "uint256"
      }
    ],
    "name": "joinPool",
    "outputs": [],
    "stateMutability": "payable",
    "type": "function"
  },
  {
    "inputs": [],
    "name": "owner",
    "outputs": [
      {
        "internalType": "address",
        "name": "",
        "type": "address"
      }
    ],
    "stateMutability": "view",
    "type": "function"
  },
  {
    "inputs": [],
    "name": "platformFeeRate",
    "outputs": [
      {
        "internalType": "uint256",
        "name": "",
        "type": "uint256"
      }
    ],
    "stateMutability": "view",
    "type": "function"
  },
  {
    "inputs": [],
    "name": "platformFeeReceiver",
    "outputs": [
      {
        "internalType": "address",
        "name": "",
        "type": "address"
      }
    ],
    "stateMutability": "view",
    "type": "function"
  },
  {
    "inputs": [],
    "name": "poolCounter",
    "outputs": [
      {
        "internalType": "uint256",
        "name": "",
        "type": "uint256"
      }
    ],
    "stateMutability": "view",
    "type": "function"
  },
  {
    "inputs": [
      {
        "internalType": "uint256",
        "name": "",
        "type": "uint256"
      }
    ],
    "name": "pools",
    "outputs": [
      {
        "internalType": "uint256",
        "name": "id",
        "type": "uint256"
      },
      {
        "internalType": "uint256",
        "name": "price",
        "type": "uint256"
      },
      {
        "internalType": "uint256",
        "name": "totalShares",
        "type": "uint256"
      },
      {
        "internalType": "uint256",
        "name": "soldShares",
        "type": "uint256"
      },
      {
        "internalType": "uint256",
        "name": "winnerCount",
        "type": "uint256"
      },
      {
        "internalType": "uint256",
        "name": "endTime",
        "type": "uint256"
      },
      {
        "internalType": "enum TreasurePool.PoolStatus",
        "name": "status",
        "type": "uint8"
      },
      {
        "internalType": "bytes32",
        "name": "randomnessRequestId",
        "type": "bytes32"
      },
      {
        "internalType": "uint256",
        "name": "prizePerWinner",
        "type": "uint256"
      }
    ],
    "stateMutability": "view",
    "type": "function"
  },
  {
    "inputs": [
      {
        "internalType": "bytes32",
        "name": "",
        "type": "bytes32"
      }
    ],
    "name": "requestIdToPoolId",
    "outputs": [
      {
        "internalType": "uint256",
        "name": "",
        "type": "uint256"
      }
    ],
    "stateMutability": "view",
    "type": "function"
  },
  {
    "inputs": [
      {
        "internalType": "uint256",
        "name": "newRate",
        "type": "uint256"
      }
    ],
    "name": "setPlatformFeeRate",
    "outputs": [],
    "stateMutability": "nonpayable",
    "type": "function"
  },
  {
    "inputs": [
      {
        "internalType": "address",
        "name": "newReceiver",
        "type": "address"
      }
    ],
    "name": "setPlatformFeeReceiver",
    "outputs": [],
    "stateMutability": "nonpayable",
    "type": "function"
  },
  {
    "inputs": [],
    "name": "switchboardVRF",
    "outputs": [
      {
        "internalType": "address",
        "name": "",
        "type": "address"
      }
    ],
    "stateMutability": "view",
    "type": "function"
  },
  {
    "inputs": [
      {
        "internalType": "uint256",
        "name": "",
        "type": "uint256"
      },
      {
        "internalType": "uint256",
        "name": "",
        "type": "uint256"
      }
    ],
    "name": "ticketOwners",
    "outputs": [
      {
        "internalType": "address",
        "name": "",
        "type": "address"
      }
    ],
    "stateMutability": "view",
    "type": "function"
  },
  {
    "inputs": [
      {
        "internalType": "address",
        "name": "newOwner",
        "type": "address"
      }
    ],
    "name": "transferOwnership",
    "outputs": [],
    "stateMutability": "nonpayable",
    "type": "function"
  },
  {
    "inputs": [],
    "name": "withdrawPlatformFee",
    "outputs": [],
    "stateMutability": "nonpayable",
    "type": "function"
  }
] as const;

// Contract address (update after deployment)
export const TREASURE_POOL_ADDRESS = "0x0D8A1Fd375b4D75f5301dDCAc018Feb899a150bF";

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
