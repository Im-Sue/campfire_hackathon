# KMarket Service 启动脚本 (Windows PowerShell)
# 用法: .\start.ps1 [profile]
# 示例: .\start.ps1          → 使用 local 环境
#       .\start.ps1 dev      → 使用 dev 环境

param(
    [string]$Profile = "local"
)

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ScriptDir

Write-Host "==============================" -ForegroundColor Cyan
Write-Host " KMarket Service Launcher" -ForegroundColor Cyan
Write-Host "==============================" -ForegroundColor Cyan

# 加载 .env 文件
$envFile = Join-Path $ScriptDir ".env"
if (Test-Path $envFile) {
    Write-Host "[OK] Loading .env ..." -ForegroundColor Green
    $loaded = 0
    Get-Content $envFile | ForEach-Object {
        $line = $_.Trim()
        if ($line -and -not $line.StartsWith('#') -and $line.Contains('=')) {
            $parts = $line -split '=', 2
            $key = $parts[0].Trim()
            $val = $parts[1].Trim()
            [System.Environment]::SetEnvironmentVariable($key, $val)
            $loaded++
        }
    }
    Write-Host "[OK] Loaded $loaded env variables" -ForegroundColor Green
} else {
    Write-Host "[WARN] .env not found, using defaults from YAML" -ForegroundColor Yellow
    Write-Host "       Run: cp .env.example .env" -ForegroundColor Yellow
}

# 验证关键环境变量
$privateKey = [System.Environment]::GetEnvironmentVariable("TREASURE_PRIVATE_KEY")
if ($privateKey) {
    $masked = $privateKey.Substring(0, 6) + "..." + $privateKey.Substring($privateKey.Length - 4)
    Write-Host "[OK] TREASURE_PRIVATE_KEY: $masked" -ForegroundColor Green
} else {
    Write-Host "[WARN] TREASURE_PRIVATE_KEY not set" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Profile : $Profile" -ForegroundColor White
Write-Host "Command : mvn spring-boot:run -pl yudao-server" -ForegroundColor White
Write-Host "==============================" -ForegroundColor Cyan
Write-Host ""

mvn spring-boot:run "-pl" yudao-server "-Dspring-boot.run.profiles=$Profile"
