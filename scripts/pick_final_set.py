"""确认并导出 10 道修复题集（超窗 7 + 答错 3）。

输出：notes/repair-set.json —— 含完整题目、选项、标准答案、基线状态，供跑批脚本直接读取。
"""
import json
import os

BASE = r"D:\rsi-flow-paper"
DATA = os.path.join(BASE, "data", "data.json")
SLIM = os.path.join(BASE, "results", "gpt-5.6-sol_slim.jsonl")
FAILED = os.path.join(BASE, "results", "gpt-5.6-sol_failed.jsonl")

# 超窗 7 条
OVER = [
    "66ed3960821e116aacb1f616",
    "67040983bb02136c067cd996",
    "66ec56dd821e116aacb1cd0e",
    "66f7f382bb02136c067c3a6c",
    "66f43414821e116aacb310a1",
    "66ed24c5821e116aacb1eee6",
    "66f3c5f2821e116aacb2eccf",
]
# 答错 3 条
WRONG = [
    "66faafd9bb02136c067c74a8",
    "670aac92bb02136c067d218a",
    "66f167c3821e116aacb274f4",
]


def main():
    with open(DATA, encoding="utf-8") as f:
        data = {x["_id"]: x for x in json.load(f)}
    res, fail = {}, {}
    with open(SLIM, encoding="utf-8") as f:
        for line in f:
            r = json.loads(line)
            res[r["_id"]] = r
    with open(FAILED, encoding="utf-8") as f:
        for line in f:
            r = json.loads(line)
            fail[r["_id"]] = r

    items = []
    print(f"{'#':<3}{'id':<26}{'sub_domain':<26}{'diff':<6}{'words':>8}{'ans':>5}{'base':>6}  {'paras':>6}")
    print("-" * 96)
    for i, _id in enumerate(OVER + WRONG, 1):
        x = data[_id]
        words = len(x["context"].split())
        base = "未评测" if _id in fail else ("空" if not (res.get(_id, {}).get("pred") or "").strip()
                                            else res[_id]["pred"])
        n = 4
        print(f"{i:<3}{_id:<26}{x['sub_domain']:<26}{x['difficulty']:<6}{words:>8}{x['answer']:>5}{base:>6}  {words // n:>6}")
        items.append({
            "no": i,
            "group": "over_window" if i <= len(OVER) else "wrong_answer",
            "id": _id,
            "domain": x["domain"],
            "sub_domain": x["sub_domain"],
            "difficulty": x["difficulty"],
            "length": x["length"],
            "words": words,
            "answer": x["answer"],
            "baseline_pred": base,
            "baseline_error": fail.get(_id, {}).get("error_kind", ""),
            "question": x["question"],
            "choice_A": x["choice_A"],
            "choice_B": x["choice_B"],
            "choice_C": x["choice_C"],
            "choice_D": x["choice_D"],
            "context": x["context"],
        })

    q = items[0]
    print("\n=== 第 1 题题目预览 ===")
    print(q["question"][:300])
    print(f"A. {q['choice_A']}\nB. {q['choice_B']}\nC. {q['choice_C']}\nD. {q['choice_D']}")

    out = os.path.join(BASE, "notes", "repair-set.json")
    with open(out, "w", encoding="utf-8") as f:
        json.dump(items, f, ensure_ascii=False)
    print(f"\n已导出 {len(items)} 条 → {out}  ({os.path.getsize(out) / 1e6:.1f} MB)")


if __name__ == "__main__":
    main()
