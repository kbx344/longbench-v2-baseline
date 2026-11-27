import { user_excel_test } from './user_excel_test';

/**
 * 十道题各自的后端流程结构与画布映射。
 * 画布上的节点/连线与后端 flow json 一一对应；
 * 点「运行」时按 flowId 调后端，由后端整条流程执行。
 */

export interface QaNode {
  id: string;
  title: string;
  reference: string;
  ins: string[];
  outs: string[];
}

export interface QaCase {
  qid: string;
  no: string;
  name: string;
  flowId: string;
  stream?: boolean;
  nodes: QaNode[];
  routes: Array<[string, string]>;
}

const NS = '@com.horsecoder.common.whuTalent.flowTesting.core';

const STREAM_NODES: QaNode[] = [
  { id: 'SPLIT', title: '按窗口长度顺序切段', reference: 'FLOW_QA_SPLIT_CHUNKS', ins: ['qid'], outs: ['question', 'choices', 'answerKey', 'segments'] },
  { id: 'STREAM', title: '逐段读并滚动更新笔记', reference: 'FLOW_QA_STREAM_READ', ins: ['segments', 'question', 'choices', 'idleLimit'], outs: ['memo', 'segCount', 'segInfo'] },
  { id: 'DECIDE', title: '按笔记做单选裁决', reference: 'FLOW_QA_DECIDE_MEMO', ins: ['memo', 'question', 'choices', 'answerKey'], outs: ['answer', 'correct'] },
];
const STREAM_ROUTES: Array<[string, string]> = [['start', 'SPLIT'], ['SPLIT', 'STREAM'], ['STREAM', 'DECIDE'], ['DECIDE', 'end']];

export const QA_CASES: QaCase[] = [
  {
    qid: 'Q01',
    no: 'Q01',
    name: '定位失败转选项',
    flowId: `${NS}/FLOW_QA_Q01`,
    nodes: [
      { id: 'Q01_SPLIT', title: '载入并切段', reference: 'FLOW_QA_SPLIT', ins: ['qid'], outs: ['question', 'choices', 'answerKey', 'segments'] },
      { id: 'Q01_LOCATE', title: '实体定位尝试', reference: 'FLOW_QA_LOCATE', ins: ['question', 'segments'], outs: ['locateReport', 'usable', 'propositions'] },
      { id: 'Q01_HYPOTHESIZE', title: '选项压成命题', reference: 'FLOW_QA_HYPOTHESIZE', ins: ['question', 'choices'], outs: ['propositions'] },
      { id: 'Q01_GATHER', title: '分区取证', reference: 'FLOW_QA_GATHER', ins: ['propositions', 'segments'], outs: ['evidence'] },
      { id: 'Q01_DECIDE', title: '裁决与判分', reference: 'FLOW_QA_DECIDE', ins: ['question', 'choices', 'evidence', 'answerKey'], outs: ['answer', 'correct'] },
    ],
    routes: [['start', 'Q01_SPLIT'], ['Q01_SPLIT', 'Q01_LOCATE'], ['Q01_LOCATE', 'Q01_GATHER'], ['Q01_LOCATE', 'Q01_HYPOTHESIZE'], ['Q01_HYPOTHESIZE', 'Q01_GATHER'], ['Q01_GATHER', 'Q01_DECIDE'], ['Q01_DECIDE', 'end']],
  },
  {
    qid: 'Q02',
    no: 'Q02',
    name: '多文档摘要后裁决',
    flowId: `${NS}/FLOW_QA_Q02`,
    nodes: [
      { id: 'Q02_SPLIT', title: '按文档分册切段', reference: 'FLOW_QA_SPLIT_DOCS', ins: ['qid'], outs: ['question', 'choices', 'answerKey', 'segments'] },
      { id: 'Q02_SUMMARIZE', title: '逐份文档结构化摘要', reference: 'FLOW_QA_SUMMARIZE', ins: ['segments'], outs: ['bookSummaries', 'digest'] },
      { id: 'Q02_HYPOTHESIZE', title: '选项压成可证伪命题', reference: 'FLOW_QA_HYPOTHESIZE_LITERAL', ins: ['question', 'choices'], outs: ['propositions'] },
      { id: 'Q02_GATHER', title: '按命题回原文取证', reference: 'FLOW_QA_GATHER_STATS', ins: ['propositions', 'segments'], outs: ['evidence', 'statsTable'] },
      { id: 'Q02_FUSE', title: '摘要与证据合成', reference: 'FLOW_QA_FUSE', ins: ['evidence', 'digest', 'statsTable'], outs: ['evidence'] },
      { id: 'Q02_DECIDE', title: '裁决与判分', reference: 'FLOW_QA_DECIDE_SCOPED', ins: ['question', 'choices', 'evidence', 'answerKey'], outs: ['answer', 'correct'] },
    ],
    routes: [['start', 'Q02_SPLIT'], ['Q02_SPLIT', 'Q02_SUMMARIZE'], ['Q02_SUMMARIZE', 'Q02_HYPOTHESIZE'], ['Q02_HYPOTHESIZE', 'Q02_GATHER'], ['Q02_GATHER', 'Q02_FUSE'], ['Q02_FUSE', 'Q02_DECIDE'], ['Q02_DECIDE', 'end']],
  },
  {
    qid: 'Q03',
    no: 'Q03',
    name: '代码仓题（超窗通用底座）',
    flowId: `${NS}/FLOW_QA_STREAM`,
    stream: true,
    nodes: STREAM_NODES,
    routes: STREAM_ROUTES,
  },
  {
    qid: 'Q04',
    no: 'Q04',
    name: '表格题：列投影 → 数值扫描 → 裁决',
    flowId: `${NS}/FLOW_QA_TABLE`,
    nodes: [
      { id: 'PROJECT', title: '认表头并投影出窄表', reference: 'FLOW_QA_PROJECT_COLS', ins: ['qid'], outs: ['question', 'choices', 'answerKey', 'segments', 'colInfo'] },
      { id: 'SCAN', title: '扫描窄表的数值极值（纯计算）', reference: 'FLOW_QA_TABLE_SCAN', ins: ['segments'], outs: ['memo', 'facts', 'segInfo'] },
      { id: 'DECIDE', title: '按笔记做单选裁决', reference: 'FLOW_QA_DECIDE_MEMO', ins: ['memo', 'question', 'choices', 'answerKey'], outs: ['answer', 'correct'] },
    ],
    routes: [['start', 'PROJECT'], ['PROJECT', 'SCAN'], ['SCAN', 'DECIDE'], ['DECIDE', 'end']],
  },
  {
    qid: 'Q05',
    no: 'Q05',
    name: '多文档对照：切实体 → 探针 → 分桶 → 裁决',
    flowId: `${NS}/FLOW_QA_Q05`,
    nodes: [
      { id: 'SPLIT', title: '按题干实体切分材料（纯代码）', reference: 'FLOW_QA_SPLIT_ENTITY', ins: ['qid'], outs: ['question', 'choices', 'answerKey', 'segments', 'books'] },
      { id: 'PROBE', title: '四个选项翻译成原文检索词', reference: 'FLOW_QA_PROBE_BUILD', ins: ['question', 'choices'], outs: ['propositions'] },
      { id: 'GATHER', title: '按命题 × 文档分桶取证（纯代码）', reference: 'FLOW_QA_GATHER_BUCKETS', ins: ['propositions', 'segments'], outs: ['memo', 'statsTable'] },
      { id: 'DECIDE', title: '并排对照两家做单选裁决', reference: 'FLOW_QA_DECIDE_COMMON', ins: ['memo', 'question', 'choices', 'answerKey'], outs: ['answer', 'correct'] },
    ],
    routes: [['start', 'SPLIT'], ['SPLIT', 'PROBE'], ['PROBE', 'GATHER'], ['GATHER', 'DECIDE'], ['DECIDE', 'end']],
  },
  {
    qid: 'Q06',
    no: 'Q06',
    name: '用户手册题（超窗通用底座）',
    flowId: `${NS}/FLOW_QA_STREAM`,
    stream: true,
    nodes: STREAM_NODES,
    routes: STREAM_ROUTES,
  },
  {
    qid: 'Q07',
    no: 'Q07',
    name: '排除题：探针 → 因果句取证 → 排除裁决',
    flowId: `${NS}/FLOW_QA_Q07`,
    nodes: [
      { id: 'SPLIT', title: '按 15 万字符顺序切段（纯代码）', reference: 'FLOW_QA_SPLIT_CHUNKS', ins: ['qid'], outs: ['question', 'choices', 'answerKey', 'segments'] },
      { id: 'PROBE', title: '四个选项翻译成原文检索词', reference: 'FLOW_QA_PROBE_BUILD', ins: ['question', 'choices'], outs: ['propositions'] },
      { id: 'GATHER', title: '按选项收集因果句（纯代码）', reference: 'FLOW_QA_GATHER_CAUSE', ins: ['propositions', 'segments'], outs: ['memo', 'statsTable'] },
      { id: 'DECIDE', title: '排除法裁决：判哪一项没被当成原因', reference: 'FLOW_QA_DECIDE_NOT', ins: ['memo', 'question', 'choices', 'answerKey', 'statsTable'], outs: ['answer', 'correct', 'verdict'] },
    ],
    routes: [['start', 'SPLIT'], ['SPLIT', 'PROBE'], ['PROBE', 'GATHER'], ['GATHER', 'DECIDE'], ['DECIDE', 'end']],
  },
  {
    qid: 'Q08',
    no: 'Q08',
    name: '相机说明书题（超窗通用底座）',
    flowId: `${NS}/FLOW_QA_STREAM`,
    stream: true,
    nodes: STREAM_NODES,
    routes: STREAM_ROUTES,
  },
  {
    qid: 'Q09',
    no: 'Q09',
    name: '矛盾题：全库楼层扫描 → 描述矛盾裁决',
    flowId: `${NS}/FLOW_QA_Q09`,
    nodes: [
      { id: 'SPLIT', title: '按 15 万字符顺序切段（纯代码）', reference: 'FLOW_QA_SPLIT_CHUNKS', ins: ['qid'], outs: ['question', 'choices', 'answerKey', 'segments'] },
      { id: 'SCAN', title: '全库楼层提及扫描（纯代码）', reference: 'FLOW_QA_ENTITY_SCAN', ins: ['question', 'segments'], outs: ['memo', 'statsTable'] },
      { id: 'DECIDE', title: '矛盾裁决：判同一住处是否前后不一致', reference: 'FLOW_QA_DECIDE_CONFLICT', ins: ['memo', 'question', 'choices', 'answerKey', 'statsTable'], outs: ['answer', 'correct', 'verdict'] },
    ],
    routes: [['start', 'SPLIT'], ['SPLIT', 'SCAN'], ['SCAN', 'DECIDE'], ['DECIDE', 'end']],
  },
  {
    qid: 'Q10',
    no: 'Q10',
    name: '跨年数值题：占比三元组扫描 → 差值裁决',
    flowId: `${NS}/FLOW_QA_Q10`,
    nodes: [
      { id: 'SPLIT', title: '按 15 万字符顺序切段（纯代码）', reference: 'FLOW_QA_SPLIT_CHUNKS', ins: ['qid'], outs: ['question', 'choices', 'answerKey', 'segments'] },
      { id: 'NUMSCAN', title: '扫两地年报的占比三元组（纯代码）', reference: 'FLOW_QA_NUM_SCAN', ins: ['segments'], outs: ['stats', 'memo'] },
      { id: 'CALC', title: '算年份差值并匹配选项', reference: 'FLOW_QA_CALC_DECIDE', ins: ['stats', 'memo', 'question', 'choices', 'answerKey'], outs: ['answer', 'correct', 'verdict'] },
    ],
    routes: [['start', 'SPLIT'], ['SPLIT', 'NUMSCAN'], ['NUMSCAN', 'CALC'], ['CALC', 'end']],
  },
];

const COLORS = ['#5cdbd3', '#95de64', '#ffa940', '#b37feb', '#ffd666', '#69c0ff', '#ff9c6e', '#87e8de', '#d3adf7', '#ffadd2'];

const clone = <T,>(o: T): T => JSON.parse(JSON.stringify(o));
const pseudoField = (name: string) => ({ notes: name, name, length: '256', type: 'String', fieldTitle: name, editable: false, notnull: false });

const makeElement = (id: string, title: string, x: number, y: number, fields: any[], color: string) => {
  const t: any = clone(user_excel_test.elements[1]);
  t.id = id;
  t.name = title;
  t.x = x;
  t.y = y;
  t.color = color;
  const d = t.pendant.data.detail;
  d.id = id;
  d.model.id = id;
  d.model.name = id;
  d.model.modelTitle = title;
  d.model.fields = fields;
  d.model.pk = fields.length > 0 ? [fields[0].name] : ['id'];
  d.component.id = id;
  d.component.modelId = id;
  d.component.x = x;
  d.component.y = y;
  d.fields = fields.map((field) => ({ field }));
  delete d.skill;
  return t;
};

const makeLink = (id: string, name: string, from: string, to: string, p1: { x: number; y: number }, p2: { x: number; y: number }) => {
  const t: any = clone(user_excel_test.links[0]);
  t.id = id;
  t.name = name;
  t.points = [p1, p2];
  t.fromElementId = from;
  t.toElementId = to;
  t.pendant.data.detail.id = id;
  t.pendant.data.detail.relation.id = id;
  t.pendant.data.detail.relation.modelMainId = from;
  t.pendant.data.detail.relation.modelRelatedId = to;
  t.pendant.data.detail.link.id = id;
  t.pendant.data.detail.link.relationId = id;
  return t;
};

/** 把一道题的流程结构摊成画布上的节点与连线 */
export const buildView = (qid: string) => {
  const item = QA_CASES.find((c) => c.qid === qid) ?? QA_CASES[0];
  const gapX = 330;
  const baseX = 80;
  const baseY = 240;

  const pos: Record<string, { x: number; y: number }> = {};
  item.nodes.forEach((n, i) => {
    pos[n.id] = { x: baseX + i * gapX, y: baseY };
  });

  const elements = item.nodes.map((n, i) => {
    const fields = [...n.ins, ...n.outs].map(pseudoField);
    return makeElement(n.id, n.title, pos[n.id].x, pos[n.id].y, fields, COLORS[i % COLORS.length]);
  });

  const links = item.routes
    .filter(([from, to]) => from !== 'start' && to !== 'end' && pos[from] && pos[to])
    .map(([from, to], i) =>
      makeLink(
        `link_${item.qid}_${i}`,
        `${from} → ${to}`,
        from,
        to,
        { x: pos[from].x + 125, y: pos[from].y + 37 },
        { x: pos[to].x, y: pos[to].y + 37 },
      ),
    );

  return { id: item.qid, elements, links };
};
