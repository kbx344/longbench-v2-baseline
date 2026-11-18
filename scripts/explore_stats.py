# -*- coding: utf-8 -*-
"""
读取 ../data/data.json，统计 LongBench v2 的分布画像：
总题数、答案分布、难度/长度分布、6 大类与子任务条数、上下文词数区间。

用法（在 scripts 目录）：
  python explore_stats.py
"""
import json
import os
from collections import Counter

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DATA_PATH = os.path.join(BASE_DIR, "..", "data", "data.json")


def percentile(values, p):
    """取第 p 百分位（p 取 50 即中位数）。"""
    if not values:
        return None
    s = sorted(values)
    k = (len(s) - 1) * p / 100
    f = int(k)
    c = min(f + 1, len(s) - 1)
    return s[f] + (s[c] - s[f]) * (k - f)


def main():
    with open(DATA_PATH, encoding="utf-8") as f:
        data = json.load(f)

    print(f"总题数: {len(data)}")

    print("\n[答案分布]")
    for k, v in Counter(r["answer"] for r in data).most_common():
        print(f"  {k}: {v}")

    print("\n[难度分布]")
    for k, v in Counter(r["difficulty"] for r in data).most_common():
        print(f"  {k}: {v}")

    print("\n[长度分布]")
    for k, v in Counter(r["length"] for r in data).most_common():
        print(f"  {k}: {v}")

    print("\n[6 大类 domain]")
    for k, v in Counter(r["domain"] for r in data).most_common():
        print(f"  {k}: {v}")

    print("\n[子任务 sub_domain]")
    for k, v in Counter(r["sub_domain"] for r in data).most_common():
        print(f"  {k}: {v}")

    print("\n[上下文词数（按长度档）]")
    for length in ["short", "medium", "long"]:
        words = [len(r["context"].split()) for r in data if r["length"] == length]
        if not words:
            continue
        print(
            f"  {length}: n={len(words)} min={min(words)} "
            f"median={round(percentile(words, 50))} "
            f"mean={round(sum(words) / len(words))} max={max(words)}"
        )


if __name__ == "__main__":
    main()
