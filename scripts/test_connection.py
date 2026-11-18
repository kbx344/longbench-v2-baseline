# -*- coding: utf-8 -*-
"""
Thingo 平台最小连通测试脚本。
作用：只发一条很短的聊天请求，验证 Key、Base URL、模型名是否填对。
不读取数据集、不消耗长上下文 token。

使用步骤（在 scripts 目录 PowerShell）：
1. 复制 config.example.json 为 config.json，填入 base_url / model；
2. 设置环境变量（不要把 Key 写进代码）：
     $env:THINGO_API_KEY="你的 Key"
3. 运行：
     python test_connection.py
"""
import json
import os
import sys

from openai import OpenAI

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
CONFIG_PATH = os.path.join(BASE_DIR, "config.json")


def load_config():
    if not os.path.exists(CONFIG_PATH):
        print("[错误] 没找到 config.json，请先复制 config.example.json 并填写。")
        sys.exit(1)
    with open(CONFIG_PATH, encoding="utf-8") as f:
        return json.load(f)


def main():
    cfg = load_config()
    api_key = os.environ.get(cfg.get("api_key_env", "THINGO_API_KEY"))
    if not api_key:
        print("[错误] 环境变量 %s 未设置。" % cfg.get("api_key_env", "THINGO_API_KEY"))
        print('       $env:THINGO_API_KEY="你的 Key"')
        sys.exit(1)

    client = OpenAI(
        api_key=api_key,
        base_url=cfg["base_url"],
        timeout=cfg.get("timeout_seconds", 120),
    )

    print("Base URL :", cfg["base_url"])
    print("Model    :", cfg["model"])
    print("正在发送测试请求...\n")

    try:
        resp = client.chat.completions.create(
            model=cfg["model"],
            temperature=cfg.get("temperature", 0.1),
            max_tokens=cfg.get("max_tokens", 1024),
            messages=[
                {"role": "user", "content": "Reply with exactly: The correct answer is (B)"}
            ],
        )
    except Exception as e:
        print("[请求失败]", type(e).__name__)
        print(str(e))
        print("\n常见原因：401=Key 错/没传；404=模型名写错；429=余额不足或限流；400=参数格式问题。")
        sys.exit(1)

    content = resp.choices[0].message.content
    print("[模型返回]")
    print(content)
    print("\n[成功] 连通正常，可以进入下一步跑基线评测。")


if __name__ == "__main__":
    main()
