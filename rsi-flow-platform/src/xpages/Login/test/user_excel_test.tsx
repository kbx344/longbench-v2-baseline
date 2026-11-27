const familyId = 'com.horsecoder.agent';
const inputId = `@${familyId}/userInputAgent`;
const excelId = `@${familyId}/userExcelWriterAgent`;
const relationId = `@${familyId}/userInputToExcel`;

const userFields = [
  { notes: '用户记录ID', name: 'id', length: '32', type: 'String', fieldTitle: '用户记录ID' },
  { notes: '姓名', editable: true, notnull: true, name: 'name', length: '64', type: 'String', fieldTitle: '姓名' },
  { notes: '邮箱', editable: true, notnull: true, name: 'email', length: '128', type: 'String', fieldTitle: '邮箱' },
  { notes: '手机号', editable: true, notnull: false, name: 'phone', length: '32', type: 'String', fieldTitle: '手机号' },
];

const excelFields = [
  { notes: '写入任务ID', name: 'id', length: '32', type: 'String', fieldTitle: '写入任务ID' },
  { notes: '来源用户记录ID', editable: true, notnull: true, name: 'userId', length: '32', type: 'String', fieldTitle: '用户记录ID' },
  ...userFields.slice(1),
];

export const user_excel_test = {
  id: 'horsecoder',
  elements: [
    {
      id: inputId,
      name: '处理用户输入信息',
      type: 'model',
      color: '#5cdbd3',
      x: 100,
      y: 175,
      select: false,
      locked: false,
      pendant: {
        data: {
          type: 'model',
          detail: {
            id: inputId,
            model: {
              module: 'sys', modelTitle: '处理用户输入信息', name: 'userInputAgent',
              id: inputId, pk: ['id'], fields: userFields, order: 'id desc',
            },
            component: { id: inputId, modelId: inputId, isCollapse: false, x: 100, y: 175 },
            fields: userFields.map((field) => ({ field })),
            familyId,
            thirdParty: false,
            size: { width: 125, height: 75 },
          },
        },
      },
    },
    {
      id: excelId,
      name: '调用Excel写入Skill',
      type: 'model',
      color: '#95de64',
      x: 400,
      y: 175,
      select: false,
      locked: false,
      pendant: {
        data: {
          type: 'model',
          detail: {
            id: excelId,
            model: {
              module: 'sys', modelTitle: '调用Excel写入Skill', name: 'userExcelWriterAgent',
              id: excelId, pk: ['id'], fields: excelFields, order: 'id desc',
            },
            component: { id: excelId, modelId: excelId, isCollapse: false, x: 400, y: 175 },
            fields: excelFields.map((field) => ({ field })),
            // Execution metadata for a future skill-node executor; the current canvas only renders model nodes.
            skill: {
              name: 'office-xlsx',
              operation: 'append',
              workbook: 'src/xpages/Login/test/user_information.xlsx',
              sheet: '用户信息',
              columns: { userId: '用户ID', name: '姓名', email: '邮箱', phone: '手机号' },
              input: { userId: 'userInputAgent.id', name: 'userInputAgent.name', email: 'userInputAgent.email', phone: 'userInputAgent.phone' },
            },
            familyId,
            thirdParty: false,
            size: { width: 125, height: 75 },
          },
        },
      },
    },
  ],
  links: [
    {
      id: relationId,
      name: '传递用户信息',
      color: '#ffa940',
      type: 'relation',
      points: [
        { x: 225, y: 212.5 },
        { x: 400, y: 212.5 },
      ],
      fromElementId: inputId,
      toElementId: excelId,
      pendant: {
        data: {
          type: 'relation',
          detail: {
            id: relationId,
            relation: {
              relationType: 'ONE2ONE',
              modelMainKey: ['id'],
              relationName: 'userInputToExcel',
              modelMainId: inputId,
              modelRelatedKey: ['userId'],
              modelRelatedId: excelId,
              relationTitle: '将处理后的用户信息传递给Excel写入节点',
              id: relationId,
            },
            link: { id: relationId, relationId, color: '#ffa940', points: [] },
            thirdParty: false,
          },
        },
      },
    },
  ],
};
