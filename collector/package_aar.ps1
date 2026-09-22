#Requires -Version 5.1
<#
.SYNOPSIS
  一键打包 DeviceCollector SDK（collector-release.aar）

.DESCRIPTION
  - 本脚本位于 collector/ 目录
  - 自动探测并设置 JAVA_HOME（若未配置）
  - 在仓库根目录执行 :collector:assembleRelease
  - 将 AAR 复制到 collector/ 目录，文件名带时间戳：
    collector-release-yyyyMMdd-HHmmss.aar

.EXAMPLE
  cd collector
  .\package_aar.ps1
  .\package_aar.ps1 -SkipClean
#>
param(
    [switch]$SkipClean
)

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
# 脚本在 collector/ 内，仓库根目录为其上一级
$Root = Split-Path -Parent $ScriptDir
Set-Location $Root

function Test-JavaHome([string]$path) {
    return $path -and (Test-Path (Join-Path $path "bin\java.exe"))
}

function Resolve-JavaHome {
    if (Test-JavaHome $env:JAVA_HOME) {
        return $env:JAVA_HOME
    }

    # 通配版本号：jdk-17.0.18 / jdk-21.x 等
    $candidates = @(
        "C:\Program Files\Java\latest",
        "C:\Program Files\Java\jdk-17*",
        "C:\Program Files\Java\jdk-21*",
        "C:\Program Files\Java\jdk-*",
        "C:\Program Files\Android\Android Studio\jbr",
        "C:\Program Files\Android\Android Studio\jre",
        "$env:LOCALAPPDATA\Programs\Android\Android Studio\jbr",
        "C:\Program Files\Eclipse Adoptium\jdk-17*",
        "C:\Program Files\Eclipse Adoptium\jdk-21*",
        "C:\Program Files\Microsoft\jdk-17*",
        "C:\Program Files\Microsoft\jdk-21*",
        "$env:USERPROFILE\.jdks\*"
    )

    foreach ($pattern in $candidates) {
        $hits = @(Get-Item -Path $pattern -ErrorAction SilentlyContinue |
            Where-Object { Test-JavaHome $_.FullName } |
            Sort-Object FullName -Descending)
        if ($hits.Count -gt 0) { return $hits[0].FullName }
    }

    # PATH 里的 java.exe：跳过 Oracle javapath shim，优先找真实 JDK
    $javaCmd = Get-Command java -ErrorAction SilentlyContinue
    if ($javaCmd) {
        $binDir = Split-Path $javaCmd.Source
        $maybeHome = Split-Path $binDir
        if (Test-JavaHome $maybeHome) { return $maybeHome }

        # javapath\java.exe 是 shim，从同机 Java 安装目录再扫一遍
        $javaRoot = "C:\Program Files\Java"
        if (Test-Path $javaRoot) {
            $jdk = Get-ChildItem $javaRoot -Directory -ErrorAction SilentlyContinue |
                Where-Object { $_.Name -like "jdk-*" -and (Test-JavaHome $_.FullName) } |
                Sort-Object Name -Descending |
                Select-Object -First 1
            if ($jdk) { return $jdk.FullName }
        }
    }

    throw "未找到可用 JDK。请安装 JDK 17+ 或设置 JAVA_HOME 后重试。当前可手动: `$env:JAVA_HOME='C:\Program Files\Java\jdk-17.0.18'"
}

Write-Host "==> 仓库根目录: $Root" -ForegroundColor Cyan
Write-Host "==> 解析 JAVA_HOME ..." -ForegroundColor Cyan
$env:JAVA_HOME = Resolve-JavaHome
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
Write-Host "    JAVA_HOME = $env:JAVA_HOME"
& "$env:JAVA_HOME\bin\java.exe" -version

$gradlew = Join-Path $Root "gradlew.bat"
if (-not (Test-Path $gradlew)) {
    throw "找不到 gradlew.bat：$gradlew"
}

if (-not $SkipClean) {
    Write-Host "==> gradlew clean ..." -ForegroundColor Cyan
    & $gradlew clean --no-daemon
    if ($LASTEXITCODE -ne 0) { throw "clean 失败，exit=$LASTEXITCODE" }
}

Write-Host "==> gradlew :collector:assembleRelease ..." -ForegroundColor Cyan
& $gradlew :collector:assembleRelease --no-daemon
if ($LASTEXITCODE -ne 0) { throw "assembleRelease 失败，exit=$LASTEXITCODE" }

$aarSrc = Join-Path $ScriptDir "build\outputs\aar\collector-release.aar"
if (-not (Test-Path $aarSrc)) {
    throw "未找到 AAR：$aarSrc"
}

$stamp = Get-Date -Format "yyyyMMdd-HHmmss"
$aarName = "collector-release-$stamp.aar"
$aarDst = Join-Path $ScriptDir $aarName
Copy-Item -Force $aarSrc $aarDst

$info = Get-Item $aarDst
Write-Host ""
Write-Host "==> 打包成功" -ForegroundColor Green
Write-Host ("    文件: {0}" -f $info.FullName)
Write-Host ("    大小: {0:N0} bytes ({1:N1} KB)" -f $info.Length, ($info.Length / 1KB))
Write-Host ("    时间: {0}" -f $info.LastWriteTime)
Write-Host ""
Write-Host "接入说明见: collector\README.md" -ForegroundColor Yellow
