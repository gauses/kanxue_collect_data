# thermal.json 格式约定（采集 app ↔ Thermal HAL 接口）

- **文件位置**：`/dev/.mxdroid/mxd/thermal.json`
- **写入方**：采集 app（覆盖写整个文件即可，HAL 自动检测 mtime 变化并重载，无需通知）
- **读取方**：`android.hardware.thermal-service.mxdroid`（每 2s 检测一次文件变化）
- **文件权限**：建议 `0666`，或保证 HAL 进程（`system` 用户）可读

## 完整示例

```json
{
  "sensors": [
    {"type": "CPU",     "name": "cpu0-gold-usr",  "temp": 39.2},
    {"type": "CPU",     "name": "cpu1-gold-usr",  "temp": 40.8},
    {"type": "CPU",     "name": "cpu0-silver-usr","temp": 38.5},
    {"type": "GPU",     "name": "gpu0-usr",       "temp": 36.5},
    {"type": "BATTERY", "name": "battery",        "temp": 33.8},
    {"type": "SKIN",    "name": "xo-therm-adc",   "temp": 36.0},
    {"type": "NPU",     "name": "compute-hvx-lowf","temp": 37.7},
    {"type": "SOC",     "name": "lmh-dcvs-00",    "temp": 75.0}
  ]
}
```

## 结构

```
{
  "sensors": [   // 必填，数组；每个元素是一个传感器条目（对象）
    {
      "type": <字符串或数字>,   // 必填：AIDL TemperatureType
      "name": <字符串>,        // 可选：原始传感器名，仅日志用，HAL 不解释
      "temp": <数字或字符串>    // 必填：温度值，单位摄氏度
    },
    ...
  ]
}
```

## 字段说明

### `type`（必填）

AIDL `android.hardware.thermal.TemperatureType` 的枚举名，**大小写不敏感**
（`CPU`/`cpu`/`Cpu` 均可），也接受数字索引（`0`=`CPU`，依此类推）。合法值：

| type | 含义 |
|---|---|
| `CPU` | 处理器（gold/silver 核均可归此类） |
| `GPU` | 图形处理器 |
| `BATTERY` | 电池 |
| `SKIN` | 外壳/板温（framework 热余量 headroom 依赖它，**建议务必提供**） |
| `USB_PORT` | USB 口 |
| `POWER_AMPLIFIER` | 射频功放 |
| `BCL_VOLTAGE` / `BCL_CURRENT` / `BCL_PERCENTAGE` | 电池充电限流虚拟传感器 |
| `NPU` / `TPU` | 神经网络单元 |
| `DISPLAY` | 显示相关 |
| `MODEM` | 基带 |
| `SOC` | 片上系统整体 |

未知/缺失的 `type` → 该条目被忽略。

### `name`（可选）

采集到的原始传感器名（如 `cpu0-gold-usr`、`pm8998_tz`）。**HAL 不解析它**——
不同设备的传感器名千差万别，HAL 只认 `type`。它仅用于日志，便于排查采集覆盖情况。

### `temp`（必填）

摄氏度数值。两种写法都接受：

- 数字：`39.2`
- 字符串：`"39.2"` 或 `"39.2°C"`（带单位也能解析）

## 处理规则（app 需要注意的约定）

1. **同 `type` 多条目取平均**作为该类型的基准温度（如 4 个 `CPU` 条目 → CPU
   基准 = 4 者平均值）。
2. **值域过滤**：`temp` 必须在 `(0, 150]` 区间才被采纳，否则整条丢弃。用于排除：
   - 热敏电阻断路/短路读数：`-40`、`-274`；
   - 非温度字段：`0.0`（如 soc 百分比误读）；电压/电流值（`vbat`=4.0、
     `ibat-high`=2.3 这类**不应标成温度类型**，应不采集或标对应 BCL 类型）。
3. **类型缺数据**：某 `type` 没有任何有效条目 → 该类型保持禁用（`getTemperatures`
   不返回它，framework 也拿不到）。**建议 app 至少保证 `CPU`、`GPU`、`BATTERY`、
   `SKIN` 有数据**（SKIN 缺失会导致 framework 热余量恒为 NaN）。
4. **整体兜底**：文件缺失、JSON 损坏、`sensors` 为空数组、或所有条目都被过滤 →
   HAL 回退内置默认基准（CPU 40°C / GPU 38°C / BATTERY 34°C / SKIN 36°C 等），
   保证服务始终可用。
5. **更新机制**：app 直接覆盖写整个文件即可；HAL 每 2s 检测文件 mtime，变了就
   自动重载，无需通知 HAL。
6. **波动**：HAL 在基准值上叠加 ±1°C 的正弦波动模拟温度变化，app 无需自己加波动。

## 反例（会怎样被处理）

```json
{"type": "CPU", "name": "cpu0", "temp": -40}     → 丢弃（超出值域）
{"type": "BCL_VOLTAGE", "name": "vbat", "temp": 4.0}  → 采纳为 BCL_VOLTAGE=4.0（值域内；BCL 为虚拟刻度，无阈值，无害）
{"type": "XXX", "name": "sensor1", "temp": 35}   → 丢弃（未知 type）
{"name": "sensor1", "temp": 35}                  → 丢弃（缺 type）
```
