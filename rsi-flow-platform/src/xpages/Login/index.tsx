'use client';

import React, { useEffect, useMemo, useRef, useState } from 'react';
import { Alert, Button, Select, Tag } from 'antd';
import type { LoginProps } from './types';

import VisualDiagram, { VisualToolbarConfig } from '@horsecoder/visual-core';

import {
  VisualCanvasConfig,
  VisualElementConfig,
  VisualLinkConfig,
} from '@horsecoder/visual-core';
import StandardModelEntityWrapper from '@/components/StandardModelEntity/wrapper';
import StandardModelLinkWrapper from '@/components/StandardModelLink/wrapper';
import { refreshCanvasLocked, setView } from './handler/handler/diagramHandler';
import request from '@/utils/axios';
import { QA_CASES, buildView } from './test/qa_flows';

/**
 * 后端直连地址。
 * 不走 Next 的 dev rewrite：那条代理 30 秒就断，Q09/Q01/Q02 这类长流程必被判失败。
 */
const ENGINE_BASE = process.env.NEXT_PUBLIC_ENGINE_BASE ?? 'http://127.0.0.1:39910';

/**
 * 组件布局: 登录
 * 十道题各自一套画布结构，点「运行」按对应 flowId 调后端执行
 */
const LoginPage: React.FC<LoginProps> = () => {
  // 内部数据: diagramRefInner(diagramRef)
  const diagramRef = useRef<any>(null);
  // 内部数据: 工具栏配置(toolbarConfig)
  const toolbarConfig: VisualToolbarConfig = useMemo(() => [], []);

  // 内部数据: 当前题号(qid)
  const [qid, setQid] = useState<string>('Q10');
  // 内部数据: 运行中(loading)
  const [loading, setLoading] = useState<boolean>(false);
  // 内部数据: 运行结果(out)
  const [out, setOut] = useState<any>(null);

  useEffect(() => {
    if (diagramRef.current) {
      refreshCanvasLocked(diagramRef, false);
      setView(diagramRef, buildView(qid) as any);
    }
  }, [qid]);

  const elementConfig: VisualElementConfig = useMemo(
    () => ({
      components: {
        model: {
          component: StandardModelEntityWrapper,
          minSize: {
            width: 100,
            height: 55,
          },
        },
      },
    }),
    [],
  );

  const linkConfig: VisualLinkConfig = useMemo(
    () => ({
      components: {
        relation: {
          component: StandardModelLinkWrapper,
          hasArrow: true,
          showName: true,
        },
      },
    }),
    [],
  );

  const canvasConfig: VisualCanvasConfig = useMemo(
    () => ({
      toolbarConfig,
      elementConfig,
      linkConfig,
      globalConfig: {
        gridSize: 25,
      },
    }),
    [toolbarConfig, elementConfig, linkConfig],
  );

  const runCase = async () => {
    const item = QA_CASES.find((c) => c.qid === qid);
    if (!item) return;
    setLoading(true);
    setOut(null);
    try {
      const res: any = await request(`${ENGINE_BASE}/api/engine/flow`, {
        method: 'POST',
        data: {
          flowId: item.flowId,
          params: item.stream ? { qid: item.qid, idleLimit: '3' } : { qid: item.qid },
        },
      });
      const value = res?.data?.value;
      if (value) {
        setOut(value);
      } else {
        setOut({ error: `返回体没有 value：${JSON.stringify(res).slice(0, 300)}` });
      }
    } catch (e: any) {
      setOut({ error: e?.message || String(e) });
    } finally {
      setLoading(false);
    }
  };

  const shownAnswer = out && !out.error ? out.answer : undefined;
  const shownCorrect = out && !out.error ? String(out.correct) : undefined;

  return (
    <>
      <div
        style={{
          position: 'fixed',
          top: 12,
          right: 16,
          zIndex: 20,
          display: 'flex',
          gap: 8,
          alignItems: 'center',
        }}
      >
        <Select
          value={qid}
          onChange={(v) => setQid(v)}
          style={{ width: 300 }}
          options={QA_CASES.map((c) => ({ value: c.qid, label: `${c.qid} ${c.name}` }))}
        />
        <Button type="primary" loading={loading} onClick={runCase}>
          {`运行`}
        </Button>
      </div>

      {out && (
        <div
          style={{
            position: 'fixed',
            left: 16,
            bottom: 16,
            zIndex: 20,
            maxWidth: 620,
            padding: '10px 14px',
            background: 'rgba(255,255,255,0.96)',
            border: '1px solid #e8e8e8',
            borderRadius: 8,
            boxShadow: '0 2px 12px rgba(0,0,0,0.12)',
          }}
        >
          {out.error ? (
            <Alert type="error" showIcon title={String(out.error)} />
          ) : (
            <>
              <div style={{ marginBottom: 6 }}>
                <span style={{ marginRight: 8 }}>答案</span>
                <Tag color="blue">{String(shownAnswer ?? '')}</Tag>
                <span style={{ marginRight: 8, marginLeft: 8 }}>判定</span>
                <Tag color={shownCorrect === 'true' ? 'green' : 'red'}>{String(shownCorrect ?? '')}</Tag>
              </div>
              {out.verdict ? (
                <div style={{ whiteSpace: 'pre-wrap', fontSize: 12, color: '#444', maxHeight: 220, overflow: 'auto' }}>
                  {String(out.verdict).slice(0, 600)}
                </div>
              ) : null}
            </>
          )}
        </div>
      )}

      <VisualDiagram ref={diagramRef} canvasConfig={canvasConfig} />
    </>
  );
};

export default LoginPage;
