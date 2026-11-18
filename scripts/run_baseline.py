# -*- coding: utf-8 -*-
"""
LongBench v2 基线评测脚本（适配 Thingo / OpenAI 兼容接口）。

流程（对应官方 pred.py 的简化版）：
  1. 读取 ../data/data.json；
  2. 用 0-shot 选择题模板拼 prompt；
  3. 调模型，从 content 中正则抽取 A/B/C/D；
  4. 与标准答案比对，写 jsonl（断点续跑，已跑过的 _id 不重复调用）；
  5. 汇总在 summarize.py 完成，这里只负责逐条预测落盘。

用法（在 scripts 目录）：
  $env:THINGO_API_KEY="sk-xxxx"
  python run_baseline.py --limit 5     # 试跑前 5 条
  python run_baseline.py               # 全量跑
  python run_baseline.py --cot         # 开 CoT 两阶段
  python run_baseline.py --model gpt-5.6-terra
"""
import argparse
import json
import os
import re
import time

from tqdm import tqdm
from openai import OpenAI

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DATA_PATH = os.path.join(BASE_DIR, "..", "data", "data.json")
RESULT_DIR = os.path.join(BASE_DIR, "..", "results")
CONFIG_PATH = os.path.join(BASE_DIR, "config.json")

# 0-shot 模板（与官方 prompts/0shot.txt 一致）
TEMPLATE_0SHOT = """Please read the following text and answer the question below.

<text>
$DOC$
</text>

What is the correct answer to this question: $Q$
Choices:
(A) $C_A$
(B) $C_B$
(C) $C_C$
(D) $C_D$

Format your response as follows: "The correct answer is (insert answer here)"."""

# CoT 第一步：先让模型分步思考
TEMPLATE_COT = """Please read the following text and answer the question below.

<text>
$DOC$
</text>

What is the correct answer to this question: $Q$
Choices:
(A) $C_A$
(B) $C_B$
(C) $C_C$
(D) $C_D$

Let's think step by step:"""

# CoT 第二步：基于思考结果抽取最终答案
TEMPLATE_COT_ANS = """Please read the following text and answer the question below.

The text is too long and omitted here.

What is the correct answer to this question: $Q$
Choices:
(A) $C_A$
(B) $C_B$
(C) $C_C$
(D) $C_D$

Let's think step by step: $COT$

Based on the above, what is the single, most likely answer choice? Format your response as follows: "The correct answer is (insert answer here)"."""

# 答案抽取：优先匹配 "The correct answer is (X)"，再退到裸字母
ANS_RE = re.compile(r"The correct answer is\s*\(?([A-D])\)?", re.IGNORECASE)
ANS_RE_NAIVE = re.compile(r"\b([A-D])\b")


def load_config():
    if os.path.exists(CONFIG_PATH):
        with open(CONFIG_PATH, encoding="utf-8") as f:
            return json.load(f)
    return {}


def build_prompt(item, template):
    return (
        template.replace("$DOC$", item["context"].strip())
        .replace("$Q$", item["question"].strip())
        .replace("$C_A$", item["choice_A"].strip())
        .replace("$C_B$", item["choice_B"].strip())
        .replace("$C_C$", item["choice_C"].strip())
        .replace("$C_D$", item["choice_D"].strip())
    )


def extract_answer(response_text):
    if not response_text:
        return None
    text = response_text.replace("*", "")
    m = ANS_RE.search(text)
    if m:
        return m.group(1).upper()
    m = ANS_RE_NAIVE.search(text)
    return m.group(1).upper() if m else None


def call_llm(client, model, prompt, cfg, max_tokens):
    last_err = None
    for attempt in range(cfg.get("max_retry", 5)):
        try:
            resp = client.chat.completions.create(
                model=model,
                messages=[{"role": "user", "content": prompt}],
                temperature=cfg.get("temperature", 0.1),
                max_tokens=max_tokens,
            )
            return resp.choices[0].message.content
        except Exception as e:  # 网络/限流/服务端错误，重试
            last_err = e
            time.sleep(1.5 * (attempt + 1))
    raise last_err


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--model", "-m", type=str, default=None)
    parser.add_argument("--limit", "-l", type=int, default=0, help="只跑前 N 条，0 表示全量")
    parser.add_argument("--cot", action="store_true", help="开启 CoT 两阶段评测")
    parser.add_argument("--tag", type=str, default="", help="结果文件后缀")
    args = parser.parse_args()

    cfg = load_config()
    api_key = os.environ.get(cfg.get("api_key_env", "THINGO_API_KEY"))
    if not api_key:
        raise SystemExit("环境变量 THINGO_API_KEY 未设置。")

    max_words = cfg.get("max_context_words", 350000)

    model = args.model or cfg["model"]
    client = OpenAI(
        api_key=api_key,
        base_url=cfg["base_url"],
        timeout=cfg.get("timeout_seconds", 120),
        

    )

    with open(DATA_PATH, encoding="utf-8") as f:
        data = json.load(f)

    if args.limit > 0:
        data = data[: args.limit]

    os.makedirs(RESULT_DIR, exist_ok=True)
    suffix = "_cot" if args.cot else ""
    if args.tag:
        suffix += "_" + args.tag
    out_file = os.path.join(RESULT_DIR, f"{model}{suffix}.jsonl")
    fail_file = os.path.join(RESULT_DIR, f"{model}{suffix}_failed.jsonl")


    # 断点续跑：已存在的 _id 跳过
    done = {}
    for path in (out_file, fail_file):
        if os.path.exists(path):
            with open(path, encoding="utf-8") as f:
                for line in f:
                    line = line.strip()
                    if line:
                        done[json.loads(line)["_id"]] = 0

    todo = [item for item in data if item["_id"] not in done]
    print(f"模型: {model} | 共 {len(data)} 条 | 已完成 {len(done)} | 本次待跑 {len(todo)} | CoT: {args.cot}")

    fout = open(out_file, "a", encoding="utf-8")
    ffail = open(fail_file, "a", encoding="utf-8")

    for item in tqdm(todo):
        words = len(item["context"].split())
        if words > max_words:
            rec ={
                "_id": item["_id"],
                "domain": item["domain"],
                "sub_domain": item["sub_domain"],
                "difficulty": item["difficulty"],
                "length": item["length"],
                "context_words": words,
                "error_kind": "context_window",
                "error": f"context {words} words > max_context_words {max_words}",
            }
            ffail.write(json.dumps(rec, ensure_ascii=False) + "\n")
            ffail.flush()
            continue

        prompt = build_prompt(item, TEMPLATE_COT if args.cot else TEMPLATE_0SHOT)
        try:
            if args.cot:
                cot = call_llm(client, model, prompt, cfg, max_tokens=1024)
                ans_prompt = build_prompt(item, TEMPLATE_COT_ANS).replace("$COT$", cot.strip())
                output = call_llm(client, model, ans_prompt, cfg, max_tokens=cfg.get("max_tokens", 1024))
                item["response_cot"] = cot
            else:
                output = call_llm(client, model, prompt, cfg, max_tokens=cfg.get("max_tokens", 1024))
        except Exception as e:
            err = str(e)
            kind = "context_window" if "context window" in err.lower() else \
            "rate_limit" if "429" in err else "other"
            rec = {
                "_id": item["_id"],
                "domain": item["domain"],
                "sub_domain": item["sub_domain"],
                "difficulty": item["difficulty"],
                "length": item["length"],
                "context_words": len(item["context"].split()),
                "error_kind": kind,
                "error": err[:300],
            }
            ffail.write(json.dumps(rec, ensure_ascii=False) + "\n")
            ffail.flush()
            print(f"\n[_id {item['_id']}] 失败({kind}): {err[:120]}")
            continue




        pred = extract_answer(output)
        item["response"] = output
        item["pred"] = pred
        item["judge"] = pred == item["answer"]
        # 上下文只保留前 1000 词，避免结果文件过大
        item["context_preview"] = " ".join(item["context"].split()[:1000])
        fout.write(json.dumps(item, ensure_ascii=False) + "\n")
        fout.flush()

    fout.close()
    ffail.close()

    print(f"\n结果已写入: {out_file}")


if __name__ == "__main__":
    main()
