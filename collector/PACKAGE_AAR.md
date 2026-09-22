# package_aar.ps1 使用说明

一键打包 DeviceCollector SDK，生成带时间戳的 Release AAR。

## 环境要求

- Windows + PowerShell 5.1+
- JDK 17+（脚本会自动探测；也可手动设置 `JAVA_HOME`）
- 已安装 Android SDK / NDK（与本工程 `local.properties`、`collector/build.gradle.kts` 一致）
- 仓库根目录存在 `gradlew.bat`

## 基本用法

1. 打开 PowerShell
2. 进入 `collector` 目录
3. 执行脚本

```powershell
cd D:\Project\kanxue_collect_data\collector
.\package_aar.ps1
```

脚本会：

1. 解析并设置 `JAVA_HOME`
2. 切到仓库根目录
3. 执行 `gradlew clean`（默认）
4. 执行 `gradlew :collector:assembleRelease`
5. 把产物复制到 **当前 `collector` 目录**

## 产物位置与命名

| 项 | 说明 |
|----|------|
| 目录 | `D:\Project\kanxue_collect_data\collector\` |
| 文件名 | `collector-release-yyyyMMdd-HHmmss.aar` |
| 示例 | `collector-release-20260812-113045.aar` |

时间戳为本地时间：`年月日-时分秒`（中间用 `-` 分隔）。

Gradle 原始产物仍在：

`collector\build\outputs\aar\collector-release.aar`

脚本只是再复制一份带时间戳的文件到 `collector\` 根下，方便分发。

## 可选参数

跳过 `clean`（增量打包更快）：

```powershell
.\package_aar.ps1 -SkipClean
```

## 执行策略被拦截时

若提示无法运行脚本：

```powershell
Set-ExecutionPolicy -Scope Process Bypass
.\package_aar.ps1
```

`Process` 作用域只影响当前窗口，关闭后失效。

## JAVA_HOME 找不到时

脚本会尝试常见安装路径（含 `C:\Program Files\Java\jdk-17*` 等）。若仍失败，先手动指定：

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17.0.18"
.\package_aar.ps1
```

把路径换成你本机实际 JDK 目录（目录下应有 `bin\java.exe`）。

## 成功输出示例

```
==> 仓库根目录: D:\Project\kanxue_collect_data
==> 解析 JAVA_HOME ...
    JAVA_HOME = C:\Program Files\Java\jdk-17.0.18
==> gradlew clean ...
==> gradlew :collector:assembleRelease ...
==> 打包成功
    文件: D:\Project\kanxue_collect_data\collector\collector-release-20260812113045.aar
    大小: ...
```

## 接入宿主 App

把生成的 `collector-release-*.aar` 拷到宿主工程（如 `app/libs/`），依赖与调用方式见同目录：

[README.md](./README.md)
