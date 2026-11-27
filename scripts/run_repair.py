"""批量跑 repair-set.json 里的题目，调用 rsi-flow 后端的分段摘要流程。

用法：
    python scripts/run_repair.py --probe      # 只跑第 1 题并打印原始响应，用于确认接口结构
    python scripts/run_repair.py --only 1     # 只跑第 1 题
    python scripts/run_repair.py              # 跑全部（断点续跑，已完成的跳过）
    python scripts/run_repair.py --redo       # 清空已有结果重跑

结果写入 results/repair_run.jsonl，每行一题。
"""
import argparse
import json
import os
import sys
import time
import urllib.request

BASE = r"D:\rsi-flow-paper"
SET_PATH = os.path.join(BASE, "notes", "repair-set.json")
OUT_PATH = os.path.join(BASE, "results", "repair_run.jsonl")
FLOW_ID = "@com.horsecoder.common.whuTalent.flowTesting.core/FLOW_QA_MAPREDUCE"
URL = "http://127.0.0.1:39910/api/engine/flow"


def make_choices(item):
    return "A. {}\nB. {}\nC. {}\nD. {}".format(
        item["choice_A"], item["choice_B"], item["choice_C"], item["choice_D"]
    )


def build_body(item):
    return {
        "flowId": FLOW_ID,
        "params": {
            "question": item["question"],
            "choices": make_choices(item),
            "context": item["context"],
            "answer": item["answer"],
        },
    }


def post(body, timeout=2400):
    data = json.dumps(body, ensure_ascii=False).encode("utf-8")
    req = urllib.request.Request(
        URL, data=data, headers={"Content-Type": "application/json"}, method="POST"
    )
    with urllib.request.urlopen(req, timeout=timeout) as r:
        return json.loads(r.read().decode("utf-8"))


def dig(resp):
    """从 Status 包装里取出流程结果字典。不同版本可能包一层 data。"""
    if isinstance(resp, dict):
        for key in ("data", "result", "results"):
            v = resp.get(key)
            if isinstance(v, dict) and ("pred" in v or "judge" in v):
                return v
        if "pred" in resp or "judge" in resp:
            return resp
    return {}


def load_done():
    done = {}
    if os.path.exists(OUT_PATH):
        with open(OUT_PATH, encoding="utf-8") as f:
            for line in f:
                line = line.strip()
                if not line:
                    continue
                try:
                    r = json.loads(line)
                    done[str(r["no"])] = r
                except Exception:
                    pass
    return done


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--only", type=int, default=0, help="只跑第 N 题")
    ap.add_argument("--limit", type=int, default=0, help="只跑前 N 题")
    ap.add_argument("--probe", action="store_true", help="只跑第 1 题并打印原始响应")
    ap.add_argument("--redo", action="store_true", help="忽略已有结果，重跑")
    args = ap.parse_args()

    if not os.path.exists(SET_PATH):
        print("[错误] 没找到 notes/repair-set.json，先跑 pick_final_set.py")
        sys.exit(1)
    with open(SET_PATH, encoding="utf-8") as f:
        items = json.load(f)

    done = {} if args.redo else load_done()
    todo = [it for it in items if args.redo or str(it["no"]) not in done]
    if args.only:
        todo = [it for it in todo if it["no"] == args.only]
    if args.limit:
        todo = todo[: args.limit]
    if not todo:
        print("没有需要跑的题（全部已完成）。要重跑加 --redo")
        return

    print("待跑 %d 题，已完成 %d 题" % (len(todo), len(done)))
    out = open(OUT_PATH, "a", encoding="utf-8")

    for it in todo:
        print("\n" + "=" * 70)
        print("#%d %s | %s | %d 词 | 标准答案 %s | 基线 %s"
              % (it["no"], it["id"], it["sub_domain"], it["words"],
                 it["answer"], it["baseline_pred"]))
        t0 = time.time()
        try:
            resp = post(build_body(it))
        except Exception as e:
            print("[失败] %s: %s" % (type(e).__name__, str(e)[:200]))
            rec = {"no": it["no"], "id": it["id"], "error": "%s: %s" % (type(e).__name__, str(e)[:300])}
            out.write(json.dumps(rec, ensure_ascii=False) + "\n")
            out.flush()
            continue

        secs = time.time() - t0
        if args.probe:
            print("[原始响应]")
            print(json.dumps(resp, ensure_ascii=False)[:2000])

        res = dig(resp)
        pred = str(res.get("pred", "") or "")
        judge = str(res.get("judge", "") or "").lower()
        rec = {
            "no": it["no"],
            "id": it["id"],
            "sub_domain": it["sub_domain"],
            "words": it["words"],
            "answer": it["answer"],
            "baseline_pred": it["baseline_pred"],
            "pred": pred,
            "judge": judge == "true",
            "verdict": res.get("verdict", ""),
            "raw": res.get("raw", ""),
            "seconds": round(secs, 1),
            "raw_response": resp if not res else None,
        }
        out.write(json.dumps(rec, ensure_ascii=False) + "\n")
        out.flush()

        mark = "✓" if rec["judge"] else "✗"
        print("%s 预测=%s 标准=%s 耗时=%.1fs" % (mark, pred or "空", it["answer"], secs))
        if not res:
            print("[警告] 没解析到结果字段，检查 flow 是否注册成功")

        if args.probe:
            break

    out.close()
    print("\n结果已写入 " + OUT_PATH)


if __name__ == "__main__":
    main()
