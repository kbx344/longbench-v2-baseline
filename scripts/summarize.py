# -*- coding: utf-8 -*-
"""
汇总基线结果，生成实验表。
读取 ../results/ 下的 jsonl，按 Overall / Easy / Hard / Short / Medium / Long
以及 6 大类 domain 统计准确率（%），输出到控制台和 result_summary.txt。

用法（在 scripts 目录）：
  python summarize.py                              # 汇总 results 下所有 jsonl
  python summarize.py --file ../results/xxx.jsonl
"""
import argparse
import json
import os
import collections

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
RESULT_DIR = os.path.join(BASE_DIR, "..", "results")
SUMMARY_PATH = os.path.join(BASE_DIR, "..", "results", "result_summary.txt")

DOMAINS = [
    "Single-Document QA",
    "Multi-Document QA",
    "Long In-context Learning",
    "Long-dialogue History Understanding",
    "Code Repository Understanding",
    "Long Structured Data Understanding",
]


def load_jsonl(path):
    rows = []
    with open(path, encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if line:
                rows.append(json.loads(line))
    return rows


def acc(rows):
    if not rows:
        return None
    return round(100 * sum(1 for r in rows if r["judge"]) / len(rows), 1)


def summarize_one(path):
    rows = load_jsonl(path)
    if not rows:
        return None
    name = os.path.splitext(os.path.basename(path))[0]
    fail_path = path[:-6] + "_failed.jsonl"
    fail_rows = load_jsonl(fail_path) if os.path.exists(fail_path) else []

    buckets = {
        "Overall": rows,
        "Easy": [r for r in rows if r["difficulty"] == "easy"],
        "Hard": [r for r in rows if r["difficulty"] == "hard"],
        "Short": [r for r in rows if r["length"] == "short"],
        "Medium": [r for r in rows if r["length"] == "medium"],
        "Long": [r for r in rows if r["length"] == "long"],
    }
    for dom in DOMAINS:
        buckets[dom] = [r for r in rows if r["domain"] == dom]

    line = {"model": name, "n": len(rows)}
    for k, v in buckets.items():
        line[k] = acc(v)
    # 未抽取到答案（pred=None）的数量，作为分析指标
    line["invalid"] = sum(1 for r in rows if r["pred"] is None)
    total = len(rows) + len(fail_rows)
    fail_counter = collections.Counter(classify_fail(r) for r in fail_rows)
    line["total"] = total
    line["coverage"] = round(100 * len(rows) / total, 1) if total else None
    line["fail"] = f"{fail_counter['context_window']}/{fail_counter['sensitive']}/{fail_counter['policy']}"
    return line

def classify_fail(rec):
    e = rec.get("error", "").lower()
    if "sensitive_words" in e:
        return "sensitive"
    if "content policy" in e or "blocked" in e:
        return "policy"
    return "context_window"



def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--file", type=str, default=None)
    args = parser.parse_args()

    if args.file:
        files = [args.file]
    else:
        files = [
            os.path.join(RESULT_DIR, f)
            for f in os.listdir(RESULT_DIR)
            if f.endswith(".jsonl") and not f.endswith("_failed.jsonl")
        ]

    results = [summarize_one(p) for p in files]
    results = [r for r in results if r]
    if not results:
        print("results 目录下还没有 jsonl 结果。")
        return

    columns = ["Overall", "Easy", "Hard", "Short", "Medium", "Long"] + DOMAINS
    header = ["Model", "N", "Total", "Coverage", "Invalid", "Fail(窗/敏/政)"] + columns

    lines = ["\t".join(header)]
    for r in results:
        row = [
            r["model"], str(r["n"]), str(r["total"]),
            "-" if r["coverage"] is None else str(r["coverage"]),
            str(r["invalid"]), r["fail"],
        ]
        for c in columns:
            v = r.get(c)
            row.append("-" if v is None else str(v))
        lines.append("\t".join(row))

    table = "\n".join(lines)
    print(table)

    with open(SUMMARY_PATH, "w", encoding="utf-8") as f:
        f.write(table + "\n")
    print(f"\n已保存: {SUMMARY_PATH}")


if __name__ == "__main__":
    main()
