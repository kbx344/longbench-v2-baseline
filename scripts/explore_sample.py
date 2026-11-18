# -*- coding: utf-8 -*-
"""
数据集样本预览，用于人工读懂每类问题长什么样。

用法（在 scripts 目录）：
  python explore_sample.py [序号]                 # 看第 N 条（从 0 开始）
  python explore_sample.py --domain "大类名" --n 2
  python explore_sample.py --sub-domain "子任务名" --n 2
"""
import argparse
import json
import os

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DATA_PATH = os.path.join(BASE_DIR, "..", "data", "data.json")


def show(item):
    print("=" * 70)
    print(f"_id: {item['_id']}")
    print(f"domain: {item['domain']} | sub_domain: {item['sub_domain']}")
    print(f"difficulty: {item['difficulty']} | length: {item['length']}")
    print(f"question: {item['question']}")
    print(f"A) {item['choice_A']}")
    print(f"B) {item['choice_B']}")
    print(f"C) {item['choice_C']}")
    print(f"D) {item['choice_D']}")
    print(f"answer: {item['answer']}")
    preview = " ".join(item["context"].split()[:300])
    print(f"context(前 300 词): {preview}")
    print("=" * 70)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("index", nargs="?", type=int, default=None)
    parser.add_argument("--domain", type=str, default=None)
    parser.add_argument("--sub-domain", type=str, default=None)
    parser.add_argument("--n", type=int, default=1)
    args = parser.parse_args()

    with open(DATA_PATH, encoding="utf-8") as f:
        data = json.load(f)

    if args.index is not None:
        show(data[args.index])
        return

    pool = data
    if args.domain:
        pool = [r for r in pool if r["domain"] == args.domain]
    if args.sub_domain:
        pool = [r for r in pool if r["sub_domain"] == args.sub_domain]

    for item in pool[: args.n]:
        show(item)


if __name__ == "__main__":
    main()
