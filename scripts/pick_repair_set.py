"""从基线结果中筛出可用于「修复任务」的候选样本。

用法：python pick_repair_set.py
输出：控制台候选清单 + notes/repair-candidates.json（含完整题目与上下文，供后续跑批）
"""
import json
import os
import sys

BASE = r"D:\rsi-flow-paper"
DATA = os.path.join(BASE, "data", "data.json")
SLIM = os.path.join(BASE, "results", "gpt-5.6-sol_slim.jsonl")
FAILED = os.path.join(BASE, "results", "gpt-5.6-sol_failed.jsonl")


def load():
    with open(DATA, encoding="utf-8") as f:
        data = json.load(f)
    idx = {}
    for x in data:
        idx[x["_id"]] = dict(
            domain=x["domain"], sub=x["sub_domain"], diff=x["difficulty"],
            length=x["length"], words=len(x["context"].split()),
            answer=x["answer"], question=x["question"],
        )
    res = {}
    with open(SLIM, encoding="utf-8") as f:
        for line in f:
            r = json.loads(line)
            res[r["_id"]] = r
    fail = {}
    with open(FAILED, encoding="utf-8") as f:
        for line in f:
            r = json.loads(line)
            fail[r["_id"]] = r
    return data, idx, res, fail


def bucket(idx, res, fail):
    """把 503 条分成四类：超窗 / 答错 / 格式失效 / 平台拦截。"""
    out = {"context_window": [], "wrong": [], "invalid": [], "blocked": []}
    for _id, meta in idx.items():
        if _id in fail:
            kind = fail[_id].get("error_kind", "")
            if kind == "context_window":
                out["context_window"].append((_id, meta, fail[_id]))
            else:
                out["blocked"].append((_id, meta, fail[_id]))
        elif _id in res:
            r = res[_id]
            pred = (r.get("pred") or "").strip()
            if not pred:
                out["invalid"].append((_id, meta, r))
            elif not r.get("judge"):
                out["wrong"].append((_id, meta, r))
    return out


def show(title, rows, limit=25):
    print("\n" + "=" * 100)
    print(f"{title}  (共 {len(rows)} 条，列出前 {limit})")
    print("=" * 100)
    print(f"{'_id':<26}{'sub_domain':<30}{'length':<9}{'words':>9}  {'ans':<4}{'pred':<5}{'err'}")
    for _id, meta, extra in rows[:limit]:
        err = extra.get("error_kind", "") or ("pred_%s→%s" % (extra.get("pred"), meta["answer"]) if "pred" in extra else "")
        print(f"{_id:<26}{meta['sub'][:29]:<30}{meta['length']:<9}{meta['words']:>9}  {meta['answer']:<4}"
              f"{str(extra.get('pred')):<5}{err}")


def main():
    data, idx, res, fail = load()
    b = bucket(idx, res, fail)
    print(f"总数 {len(idx)} | 成功评测 {len(res)} | 失败记录 {len(fail)}")
    show("A 超窗（基线未评测，修复价值最高）", b["context_window"])
    show("B 答错（基线选中了错误选项）", b["wrong"])
    show("C 格式失效（pred 为空 / none of the above）", b["invalid"])
    show("D 平台拦截（敏感词 / 内容政策）", b["blocked"])

    # 超窗里挑词数在 25 万~65 万的（4 段后每段 ≤16 万，单段可送）
    mid = [r for r in b["context_window"] if 250000 <= r[1]["words"] <= 650000]
    print("\n" + "-" * 100)
    print(f"超窗中「4 段切分后每段 ≤16 万词」的候选：{len(mid)} 条")
    for _id, meta, extra in mid:
        print(f"  {_id}  {meta['sub']:<26} words={meta['words']:>8}")

    with open(os.path.join(BASE, "notes", "repair-candidates.json"), "w", encoding="utf-8") as f:
        json.dump({
            "context_window": [r[0] for r in b["context_window"]],
            "wrong": [r[0] for r in b["wrong"]],
            "invalid": [r[0] for r in b["invalid"]],
            "blocked": [r[0] for r in b["blocked"]],
            "mid_window": [r[0] for r in mid],
        }, f, ensure_ascii=False, indent=2)
    print("\n已写入 notes/repair-candidates.json")


if __name__ == "__main__":
    main()
