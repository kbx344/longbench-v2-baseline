# -*- coding: utf-8 -*-
"""
将原始成功结果 jsonl 精简为可提交版本：
去掉超长 context / context_preview，response 截断到前 200 字符。
输入: ../results/<model>.jsonl
输出: ../results/<model>_slim.jsonl
"""
import json
import os
import sys

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
RESULT_DIR = os.path.join(BASE_DIR, "..", "results")

KEEP = [
    "_id", "domain", "sub_domain", "difficulty", "length",
    "question", "choice_A", "choice_B", "choice_C", "choice_D",
    "answer", "pred", "judge",
]

RESPONSE_LIMIT = 200


def slim_one(row):
    out = {k: row.get(k) for k in KEEP}
    resp = row.get("response") or ""
    out["response"] = resp[:RESPONSE_LIMIT]
    return out


def main():
    model = sys.argv[1] if len(sys.argv) > 1 else "gpt-5.6-sol"
    src = os.path.join(RESULT_DIR, f"{model}.jsonl")
    dst = os.path.join(RESULT_DIR, f"{model}_slim.jsonl")

    rows = []
    with open(src, encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if line:
                rows.append(json.loads(line))

    with open(dst, "w", encoding="utf-8") as f:
        for row in rows:
            f.write(json.dumps(slim_one(row), ensure_ascii=False) + "\n")

    size_kb = round(os.path.getsize(dst) / 1024, 1)
    print(f"原始 {len(rows)} 条 -> 精简版 {dst}（{size_kb} KB）")


if __name__ == "__main__":
    main()
