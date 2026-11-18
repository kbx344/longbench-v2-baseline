# -*- coding: utf-8 -*-
"""
生成易读的 Markdown 汇总表（分三张窄表），输出到 ../results/result_summary.md。
不改动原有 result_summary.txt。
"""
import json
import os
import collections

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
RESULT_DIR = os.path.join(BASE_DIR, "..", "results")
DATA_PATH = os.path.join(BASE_DIR, "..", "data", "data.json")
MD_PATH = os.path.join(RESULT_DIR, "result_summary.md")

DOMAINS = [
    ("Single-Document QA", "SD-QA"),
    ("Multi-Document QA", "MD-QA"),
    ("Long In-context Learning", "LIL"),
    ("Long-dialogue History Understanding", "Dialogue"),
    ("Code Repository Understanding", "Code"),
    ("Long Structured Data Understanding", "Structured"),
]


def load_jsonl(path):
    rows = []
    if not os.path.exists(path):
        return rows
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


def classify_fail(rec):
    e = rec.get("error", "").lower()
    if "sensitive_words" in e:
        return "sensitive"
    if "content policy" in e or "blocked" in e:
        return "policy"
    return "context_window"


def summarize(path, all_data):
    rows = load_jsonl(path)
    if not rows:
        return None
    name = os.path.splitext(os.path.basename(path))[0]
    fail_path = path[:-6] + "_failed.jsonl"
    fail_rows = load_jsonl(fail_path)

    fail_counter = collections.Counter(classify_fail(r) for r in fail_rows)
    total = len(rows) + len(fail_rows)
    ok_by_id = {r["_id"]: r for r in rows}

    info = {
        "model": name,
        "n": len(rows),
        "total": total,
        "coverage": round(100 * len(rows) / total, 1) if total else None,
        "invalid": sum(1 for r in rows if r["pred"] is None),
        "fail": f"{fail_counter['context_window']}/{fail_counter['sensitive']}/{fail_counter['policy']}",
    }

    # 成功口径：分母为成功样本
    buckets = {
        "Overall": rows,
        "Easy": [r for r in rows if r["difficulty"] == "easy"],
        "Hard": [r for r in rows if r["difficulty"] == "hard"],
        "Short": [r for r in rows if r["length"] == "short"],
        "Medium": [r for r in rows if r["length"] == "medium"],
        "Long": [r for r in rows if r["length"] == "long"],
    }
    for full, short in DOMAINS:
        buckets[short] = [r for r in rows if r["domain"] == full]
    scores = {k: acc(v) for k, v in buckets.items()}

    # 全量口径：分母为数据集全量，失败/Invalid 记 0
    def full_acc(filter_fn):
        tot = [x for x in all_data if filter_fn(x)]
        if not tot:
            return None
        correct = sum(1 for x in tot if x["_id"] in ok_by_id and ok_by_id[x["_id"]]["judge"])
        return round(100 * correct / len(tot), 1)

    full = {
        "Overall": full_acc(lambda x: True),
        "Easy": full_acc(lambda x: x["difficulty"] == "easy"),
        "Hard": full_acc(lambda x: x["difficulty"] == "hard"),
        "Short": full_acc(lambda x: x["length"] == "short"),
        "Medium": full_acc(lambda x: x["length"] == "medium"),
        "Long": full_acc(lambda x: x["length"] == "long"),
    }
    for full_name, short in DOMAINS:
        full[short] = full_acc(lambda x, d=full_name: x["domain"] == d)

    return info, scores, full


def fmt(v):
    return "-" if v is None else str(v)


def main():
    with open(DATA_PATH, encoding="utf-8") as f:
        all_data = json.load(f)

    files = [
        os.path.join(RESULT_DIR, f)
        for f in os.listdir(RESULT_DIR)
        if f.endswith(".jsonl") and not f.endswith("_failed.jsonl")
    ]
    results = [r for r in (summarize(p, all_data) for p in files) if r]
    if not results:
        print("results 目录下还没有 jsonl 结果。")
        return

    lines = ["# 基线结果汇总", ""]

    # 表 1 概览
    lines += ["## 1. 概览", ""]
    lines.append("| Model | N | Total | Coverage | Invalid | Fail(窗/敏/政) |")
    lines.append("|---|---|---|---|---|---|")
    for info, _, _ in results:
        lines.append(
            f"| {info['model']} | {info['n']} | {info['total']} | "
            f"{fmt(info['coverage'])}% | {info['invalid']} | {info['fail']} |"
        )
    lines.append("")

    # 表 2 难度 / 长度（成功口径 / 全量口径）
    lines += ["## 2. 难度与长度（成功口径 / 全量口径）", ""]
    dim2 = ["Overall", "Easy", "Hard", "Short", "Medium", "Long"]
    lines.append("| Model | " + " | ".join(dim2) + " |")
    lines.append("|" + "---|" * (len(dim2) + 1))
    for info, scores, full in results:
        row = [info["model"]] + [f"{fmt(scores[k])} / {fmt(full[k])}" for k in dim2]
        lines.append("| " + " | ".join(row) + " |")
    lines.append("")

    # 表 3 六大类（成功口径 / 全量口径）
    lines += ["## 3. 六大类领域（成功口径 / 全量口径）", ""]
    dim3 = [short for _, short in DOMAINS]
    lines.append("| Model | " + " | ".join(dim3) + " |")
    lines.append("|" + "---|" * (len(dim3) + 1))
    for info, scores, full in results:
        row = [info["model"]] + [f"{fmt(scores[k])} / {fmt(full[k])}" for k in dim3]
        lines.append("| " + " | ".join(row) + " |")
    lines.append("")

    lines += [
        "> 口径：成功口径 ACC 分母为成功评测样本；全量口径分母为数据集全量，失败/Invalid 记 0。",
        "> Fail 格式为 超窗/敏感词/政策拦截。",
        "> 缩写：SD-QA=Single-Document QA，MD-QA=Multi-Document QA，LIL=Long In-context Learning，",
        "> Dialogue=Long-dialogue History Understanding，Code=Code Repository Understanding，Structured=Long Structured Data Understanding。",
    ]

    table = "\n".join(lines)
    print(table)
    with open(MD_PATH, "w", encoding="utf-8") as f:
        f.write(table + "\n")
    print(f"\n已保存: {MD_PATH}")


if __name__ == "__main__":
    main()
