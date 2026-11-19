# 基线结果汇总

## 1. 概览

| Model | N | Total | Coverage | Invalid | Fail(窗/敏/政) |
|---|---|---|---|---|---|
| gpt-5.6-sol | 460 | 503 | 91.5% | 3 | 39/3/1 |
| gpt-6-sol | 457 | 503 | 90.9% | 13 | 42/4/0 |

## 2. 难度与长度（成功口径 / 全量口径）

| Model | Overall | Easy | Hard | Short | Medium | Long |
|---|---|---|---|---|---|---|
| gpt-5.6-sol | 60.4 / 55.3 | 65.7 / 60.9 | 57.1 / 51.8 | 61.7 / 61.7 | 57.1 / 56.3 | 67.6 / 42.6 |
| gpt-6-sol | 65.6 / 59.6 | 74.4 / 68.2 | 60.1 / 54.3 | 65.0 / 65.0 | 65.3 / 64.7 | 68.8 / 40.7 |

## 3. 六大类领域（成功口径 / 全量口径）

| Model | SD-QA | MD-QA | LIL | Dialogue | Code | Structured |
|---|---|---|---|---|---|---|
| gpt-5.6-sol | 57.0 / 56.0 | 60.5 / 57.6 | 62.9 / 54.3 | 51.3 / 51.3 | 71.0 / 44.0 | 75.9 / 66.7 |
| gpt-6-sol | 62.5 / 60.0 | 57.9 / 56.0 | 75.4 / 64.2 | 74.4 / 74.4 | 71.0 / 44.0 | 75.9 / 66.7 |

> 口径：成功口径 ACC 分母为成功评测样本；全量口径分母为数据集全量，失败/Invalid 记 0。
> Fail 格式为 超窗/敏感词/政策拦截。
> 缩写：SD-QA=Single-Document QA，MD-QA=Multi-Document QA，LIL=Long In-context Learning，
> Dialogue=Long-dialogue History Understanding，Code=Code Repository Understanding，Structured=Long Structured Data Understanding。
