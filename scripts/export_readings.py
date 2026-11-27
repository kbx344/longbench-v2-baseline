"""把 repair-set.json 里的 10 道题导出成可读文本，供人工精读。

每题一份 txt，结构：
    题头（题号/子任务/答案/基线）
    题目与四个选项
    原文（逐行编号，引用时写 L001234）

用法：
    python scripts/export_readings.py
    python scripts/export_readings.py --only 1
"""
import argparse
import json
import os

BASE = r"D:\rsi-flow-paper"
SET_PATH = os.path.join(BASE, "notes", "repair-set.json")
OUT_DIR = os.path.join(BASE, "notes", "readings")


def export(item):
    no = item["no"]
    lines = []
    lines.append("=" * 78)
    lines.append("Q%02d  %s" % (no, item["sub_domain"]))
    lines.append("=" * 78)
    lines.append("题号      : %s" % item["id"])
    lines.append("领域      : %s / %s" % (item["domain"], item["sub_domain"]))
    lines.append("难度/长度 : %s / %s    词数: %d" % (item["difficulty"], item["length"], item["words"]))
    lines.append("标准答案  : %s" % item["answer"])
    lines.append("基线结果  : %s" % item["baseline_pred"])
    lines.append("")
    lines.append("-" * 78)
    lines.append("【题目】")
    lines.append(item["question"])
    lines.append("")
    lines.append("【选项】")
    lines.append("A. %s" % item["choice_A"])
    lines.append("B. %s" % item["choice_B"])
    lines.append("C. %s" % item["choice_C"])
    lines.append("D. %s" % item["choice_D"])
    lines.append("")
    lines.append("-" * 78)
    lines.append("【原文】每行带行号，引用时写 L001234；搜索用 Ctrl+F")
    lines.append("-" * 78)

    for i, ln in enumerate(item["context"].splitlines(), 1):
        lines.append("L%06d| %s" % (i, ln))

    path = os.path.join(OUT_DIR, "Q%02d.txt" % no)
    with open(path, "w", encoding="utf-8") as f:
        f.write("\n".join(lines))
    return path, len(lines)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--only", type=int, default=0)
    args = ap.parse_args()

    if not os.path.exists(SET_PATH):
        print("[错误] 没找到 notes/repair-set.json")
        return
    os.makedirs(OUT_DIR, exist_ok=True)

    with open(SET_PATH, encoding="utf-8") as f:
        items = json.load(f)

    for it in items:
        if args.only and it["no"] != args.only:
            continue
        path, n = export(it)
        size = os.path.getsize(path) / 1024
        print("Q%02d  -> %s  (%d 行, %.0f KB)" % (it["no"], path, n, size))

    print("\n打开方式：VSCode 或记事本，Ctrl+F 搜关键词")


if __name__ == "__main__":
    main()
