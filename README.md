# LongBench v2 基线评测

基于 LongBench v2 长上下文选择题数据集，通过 OpenAI 兼容网关调用大模型，完成基线跑批、答案抽取、双口径汇总与结果分析。

## 目录结构

```
rsi-flow-paper/
├── data/                        # 数据集（不入库，需自行下载，见下）
├── notes/
│   └── dataset-profile.md       # 数据集画像、实验结果与模型能力分析报告
├── results/
│   ├── *_slim.jsonl             # 精简版成功结果（去掉超长上下文，可入库）
│   ├── *_failed.jsonl           # 失败记录（超窗 / 敏感词 / 政策拦截）
│   ├── result_summary.md        # 双口径汇总表（Markdown）
│   └── result_summary.txt       # 原始制表输出
├── repo/                        # 外部参考仓库（不入库）
├── scripts/
│   ├── config.example.json      # 配置模板（复制为 config.json 后填写）
│   ├── test_connection.py       # 最小连通测试
│   ├── explore_stats.py         # 数据集分布统计
│   ├── explore_sample.py        # 样本预览
│   ├── run_baseline.py          # 基线跑批主脚本（断点续跑、失败落盘、超窗预判）
│   ├── summarize.py             # 汇总统计（成功口径）
│   ├── make_summary_md.py       # 生成双口径 Markdown 汇总表
│   └── make_slim_results.py     # 原始结果精简为可提交版本
├── requirements.txt
└── .gitignore
```

## 环境依赖

- Python 3.10+（开发环境 3.14）
- 安装依赖：

```
pip install -r requirements.txt
```

## 配置

1. 复制配置模板：

```
copy scripts\config.example.json scripts\config.json
```

2. 在 `config.json` 中填写网关地址与模型名；API Key 不写进文件，通过环境变量传入：

```
$env:THINGO_API_KEY="sk-xxxx"
```

| 字段 | 说明 |
|---|---|
| `base_url` | OpenAI 兼容网关地址 |
| `api_key_env` | 读取 Key 的环境变量名 |
| `model` | 模型名 |
| `temperature` | 采样温度 |
| `max_tokens` | 输出上限 |
| `timeout_seconds` | 单次请求超时（秒） |
| `max_retry` | 失败重试次数 |
| `max_context_words` | 发送前预判跳过的上下文词数阈值 |

## 运行步骤

在 `scripts/` 目录下执行：

```
# 1. 连通测试
python test_connection.py

# 2. 数据集分布统计（可选）
python explore_stats.py
python explore_sample.py --domain "Academic" --n 3

# 3. 基线跑批（--limit N 试跑前 N 条；--model 覆盖配置模型）
python run_baseline.py --limit 5
python run_baseline.py

# 4. 汇总出表
python summarize.py
python make_summary_md.py

# 5. 生成精简版结果（提交仓库前使用）
python make_slim_results.py gpt-5.6-sol
```

## 数据集

- 名称：LongBench v2（THUDM），503 条英文四选一选择题。
- 下载：https://huggingface.co/datasets/THUDM/LongBench-v2 （国内可走 hf-mirror 镜像）
- 论文：arXiv:2412.15204
- 放置路径：`data/data.json`（字段见 `notes/dataset-profile.md`）

## 结果口径

- **成功口径**：ACC 分母为成功评测样本；
- **全量口径**：分母为数据集全量，失败与未抽取到答案的样本记 0；
- 失败分类：超窗（context window）、敏感词拦截（sensitive words）、内容政策拦截（content policy）。

当前基线：gpt-5.6-sol，覆盖率 91.5%，成功口径 Overall 60.4%，全量口径 55.3%。详见 `notes/dataset-profile.md` 与 `results/result_summary.md`。

## 说明

- 原始成功结果 jsonl 含完整上下文，体积过大未入库；仓库内为去掉超长字段的精简版，可用 `make_slim_results.py` 重新生成。
- `config.json` 含真实 Key，已被 `.gitignore` 排除，请勿提交。
