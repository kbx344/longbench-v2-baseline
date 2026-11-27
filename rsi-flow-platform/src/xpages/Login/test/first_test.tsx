export const  first_test= {
    "id": "horsecoder",
    "elements": [
        {
            "id": "@com.horsecoder.workFlow/flowRecord",
            "name": "流程记录管理",
            "type": "model",
            "color": "#5cdbd3",
            "x": 50,
            "y": 75,
            "select": false,
            "locked": false,
            "pendant": {
                "data": {
                    "type": "model",
                    "detail": {
                        "id": "@com.horsecoder.workFlow/flowRecord",
                        "model": {
                            "module": "sys",
                            "modelTitle": "流程记录管理",
                            "name": "flowRecord",
                            "ifOpen": 0,
                            "id": "@com.horsecoder.workFlow/flowRecord",
                            "pk": [
                                "id"
                            ],
                            "fields": [
                                {
                                    "notes": "流程记录ID",
                                    "name": "id",
                                    "length": "32",
                                    "type": "String",
                                    "fieldTitle": "流程记录ID"
                                },
                                {
                                    "notes": "activiti流程ID",
                                    "editable": true,
                                    "notnull": true,
                                    "name": "activitiFlowId",
                                    "length": "32",
                                    "type": "String",
                                    "fieldTitle": "activiti流程ID",
                                    "filterTypes": [
                                        "equal",
                                        "in"
                                    ]
                                },
                                {
                                    "notes": "创建人ID",
                                    "editable": true,
                                    "notnull": true,
                                    "name": "userId",
                                    "length": "32",
                                    "type": "String",
                                    "fieldTitle": "创建人ID",
                                    "filterTypes": [
                                        "equal",
                                        "in"
                                    ]
                                },
                                {
                                    "notes": "流程类型",
                                    "editable": true,
                                    "defaultValue": "0",
                                    "notnull": true,
                                    "name": "type",
                                    "length": "5",
                                    "type": "Integer",
                                    "fieldTitle": "流程类型",
                                    "filterTypes": [
                                        "equal",
                                        "in"
                                    ]
                                },
                                {
                                    "notes": "状态（0:默认 1:运行中 2:成功 3:失败 4:取消）",
                                    "editable": true,
                                    "defaultValue": "0",
                                    "notnull": true,
                                    "name": "status",
                                    "length": "5",
                                    "fieldTitle": "状态",
                                    "type": "Integer",
                                    "filterTypes": [
                                        "equal",
                                        "in"
                                    ]
                                },
                                {
                                    "notes": "备注",
                                    "editable": true,
                                    "notnull": false,
                                    "name": "remark",
                                    "length": "256",
                                    "type": "String",
                                    "fieldTitle": "备注"
                                },
                                {
                                    "notes": "流程定义ID",
                                    "editable": true,
                                    "defaultValue": "0",
                                    "notnull": true,
                                    "name": "flowDefinitionId",
                                    "length": "32",
                                    "type": "String",
                                    "fieldTitle": "流程定义ID",
                                    "filterTypes": [
                                        "equal"
                                    ]
                                },
                                {
                                    "notes": "参数",
                                    "editable": true,
                                    "notnull": false,
                                    "name": "params",
                                    "length": "512",
                                    "type": "String",
                                    "fieldTitle": "参数"
                                },
                                {
                                    "notes": "标题",
                                    "editable": true,
                                    "notnull": true,
                                    "name": "title",
                                    "length": "128",
                                    "fieldTitle": "标题",
                                    "type": "String",
                                    "filterTypes": [
                                        "like",
                                        "equal"
                                    ]
                                },
                                {
                                    "notes": "更新时间",
                                    "editable": false,
                                    "defaultValue": "current_timestamp()",
                                    "notnull": true,
                                    "extra": "ON UPDATE current_timestamp()",
                                    "name": "gmtUpdate",
                                    "length": "0",
                                    "type": "DateTime",
                                    "fieldTitle": "更新时间",
                                    "filterTypes": [
                                        "between"
                                    ]
                                },
                                {
                                    "notes": "创建时间",
                                    "editable": false,
                                    "defaultValue": "current_timestamp()",
                                    "notnull": true,
                                    "name": "gmtCreate",
                                    "length": "0",
                                    "type": "DateTime",
                                    "fieldTitle": "创建时间",
                                    "filterTypes": [
                                        "between"
                                    ]
                                }
                            ],
                            "order": "gmtCreate desc"
                        },
                        "component": {
                            "id": "@com.horsecoder.workFlow/flowRecord",
                            "modelId": "@com.horsecoder.workFlow/flowRecord",
                            "isCollapse": false,
                            "x": 50,
                            "y": 75
                        },
                        "fields": [
                            {
                                "field": {
                                    "notes": "流程记录ID",
                                    "name": "id",
                                    "length": "32",
                                    "type": "String",
                                    "fieldTitle": "流程记录ID"
                                }
                            },
                            {
                                "field": {
                                    "notes": "activiti流程ID",
                                    "editable": true,
                                    "notnull": true,
                                    "name": "activitiFlowId",
                                    "length": "32",
                                    "type": "String",
                                    "fieldTitle": "activiti流程ID",
                                    "filterTypes": [
                                        "equal",
                                        "in"
                                    ]
                                },
                                "color": [
                                    "#ff6666",
                                    "#ffa940"
                                ]
                            },
                            {
                                "field": {
                                    "notes": "创建人ID",
                                    "editable": true,
                                    "notnull": true,
                                    "name": "userId",
                                    "length": "32",
                                    "type": "String",
                                    "fieldTitle": "创建人ID",
                                    "filterTypes": [
                                        "equal",
                                        "in"
                                    ]
                                }
                            },
                            {
                                "field": {
                                    "notes": "流程类型",
                                    "editable": true,
                                    "defaultValue": "0",
                                    "notnull": true,
                                    "name": "type",
                                    "length": "5",
                                    "type": "Integer",
                                    "fieldTitle": "流程类型",
                                    "filterTypes": [
                                        "equal",
                                        "in"
                                    ]
                                }
                            },
                            {
                                "field": {
                                    "notes": "状态（0:默认 1:运行中 2:成功 3:失败 4:取消）",
                                    "editable": true,
                                    "defaultValue": "0",
                                    "notnull": true,
                                    "name": "status",
                                    "length": "5",
                                    "fieldTitle": "状态",
                                    "type": "Integer",
                                    "filterTypes": [
                                        "equal",
                                        "in"
                                    ]
                                }
                            },
                            {
                                "field": {
                                    "notes": "备注",
                                    "editable": true,
                                    "notnull": false,
                                    "name": "remark",
                                    "length": "256",
                                    "type": "String",
                                    "fieldTitle": "备注"
                                }
                            },
                            {
                                "field": {
                                    "notes": "流程定义ID",
                                    "editable": true,
                                    "defaultValue": "0",
                                    "notnull": true,
                                    "name": "flowDefinitionId",
                                    "length": "32",
                                    "type": "String",
                                    "fieldTitle": "流程定义ID",
                                    "filterTypes": [
                                        "equal"
                                    ]
                                }
                            },
                            {
                                "field": {
                                    "notes": "参数",
                                    "editable": true,
                                    "notnull": false,
                                    "name": "params",
                                    "length": "512",
                                    "type": "String",
                                    "fieldTitle": "参数"
                                }
                            },
                            {
                                "field": {
                                    "notes": "标题",
                                    "editable": true,
                                    "notnull": true,
                                    "name": "title",
                                    "length": "128",
                                    "fieldTitle": "标题",
                                    "type": "String",
                                    "filterTypes": [
                                        "like",
                                        "equal"
                                    ]
                                }
                            },
                            {
                                "field": {
                                    "notes": "更新时间",
                                    "editable": false,
                                    "defaultValue": "current_timestamp()",
                                    "notnull": true,
                                    "extra": "ON UPDATE current_timestamp()",
                                    "name": "gmtUpdate",
                                    "length": "0",
                                    "type": "DateTime",
                                    "fieldTitle": "更新时间",
                                    "filterTypes": [
                                        "between"
                                    ]
                                }
                            },
                            {
                                "field": {
                                    "notes": "创建时间",
                                    "editable": false,
                                    "defaultValue": "current_timestamp()",
                                    "notnull": true,
                                    "name": "gmtCreate",
                                    "length": "0",
                                    "type": "DateTime",
                                    "fieldTitle": "创建时间",
                                    "filterTypes": [
                                        "between"
                                    ]
                                }
                            }
                        ],
                        "familyId": "com.horsecoder.workFlow",
                        "thirdParty": false,
                        "size": {
                            "width": 125,
                            "height": 75
                        }
                    }
                }
            }
        },
        {
            "id": "@com.horsecoder.workFlow/taskRecord",
            "name": "任务记录管理",
            "type": "model",
            "color": "#5cdbd3",
            "x": 350,
            "y": 75,
            "select": false,
            "locked": false,
            "pendant": {
                "data": {
                    "type": "model",
                    "detail": {
                        "id": "@com.horsecoder.workFlow/taskRecord",
                        "model": {
                            "module": "sys",
                            "modelTitle": "任务记录管理",
                            "name": "taskRecord",
                            "ifOpen": 1,
                            "id": "@com.horsecoder.workFlow/taskRecord",
                            "pk": [
                                "id"
                            ],
                            "fields": [
                                {
                                    "notes": "任务记录ID",
                                    "editable": false,
                                    "notnull": false,
                                    "name": "id",
                                    "length": "32",
                                    "type": "String",
                                    "fieldTitle": "任务记录ID",
                                    "filterType": "equal",
                                    "filterTypes": [
                                        "equal",
                                        "in"
                                    ]
                                },
                                {
                                    "notes": "activiti流程ID",
                                    "editable": true,
                                    "notnull": true,
                                    "name": "activitiFlowId",
                                    "length": "32",
                                    "type": "String",
                                    "fieldTitle": "activiti流程ID",
                                    "filterTypes": [
                                        "equal",
                                        "in"
                                    ]
                                },
                                {
                                    "notes": "activiti任务ID",
                                    "editable": true,
                                    "notnull": true,
                                    "name": "activitiTaskId",
                                    "length": "64",
                                    "type": "String",
                                    "fieldTitle": "activiti任务ID",
                                    "filterTypes": [
                                        "equal",
                                        "in"
                                    ]
                                },
                                {
                                    "notes": "状态（0:默认 1:运行中 2:成功 3:失败 4:取消）",
                                    "editable": true,
                                    "defaultValue": "0",
                                    "notnull": true,
                                    "name": "status",
                                    "length": "5",
                                    "fieldTitle": "状态",
                                    "type": "Integer",
                                    "filterTypes": [
                                        "equal",
                                        "in"
                                    ]
                                },
                                {
                                    "notes": "类型",
                                    "editable": true,
                                    "defaultValue": "0",
                                    "notnull": true,
                                    "name": "type",
                                    "length": "5",
                                    "type": "Integer",
                                    "fieldTitle": "类型",
                                    "filterTypes": [
                                        "equal",
                                        "in"
                                    ]
                                },
                                {
                                    "notes": "业务ID",
                                    "editable": true,
                                    "notnull": false,
                                    "name": "businessKey",
                                    "length": "32",
                                    "type": "String",
                                    "fieldTitle": "业务ID",
                                    "filterTypes": [
                                        "equal",
                                        "in"
                                    ]
                                },
                                {
                                    "notes": "任务定义ID",
                                    "editable": true,
                                    "defaultValue": "0",
                                    "notnull": true,
                                    "name": "taskDefinitionId",
                                    "length": "32",
                                    "type": "String",
                                    "fieldTitle": "任务定义ID"
                                },
                                {
                                    "notes": "更新时间",
                                    "editable": false,
                                    "defaultValue": "current_timestamp()",
                                    "notnull": true,
                                    "extra": "ON UPDATE current_timestamp()",
                                    "name": "gmtUpdate",
                                    "length": "0",
                                    "type": "DateTime",
                                    "fieldTitle": "更新时间",
                                    "filterTypes": [
                                        "between"
                                    ]
                                },
                                {
                                    "notes": "创建时间",
                                    "editable": false,
                                    "defaultValue": "current_timestamp()",
                                    "notnull": true,
                                    "name": "gmtCreate",
                                    "length": "0",
                                    "type": "DateTime",
                                    "fieldTitle": "创建时间",
                                    "filterTypes": [
                                        "between"
                                    ]
                                }
                            ],
                            "order": "gmtCreate desc"
                        },
                        "component": {
                            "id": "@com.horsecoder.workFlow/taskRecord",
                            "modelId": "@com.horsecoder.workFlow/taskRecord",
                            "isCollapse": false,
                            "x": 350,
                            "y": 75
                        },
                        "fields": [
                            {
                                "field": {
                                    "notes": "任务记录ID",
                                    "editable": false,
                                    "notnull": false,
                                    "name": "id",
                                    "length": "32",
                                    "type": "String",
                                    "fieldTitle": "任务记录ID",
                                    "filterType": "equal",
                                    "filterTypes": [
                                        "equal",
                                        "in"
                                    ]
                                },
                                "color": [
                                    "#5cdbd3",
                                    "#bae637"
                                ]
                            },
                            {
                                "field": {
                                    "notes": "activiti流程ID",
                                    "editable": true,
                                    "notnull": true,
                                    "name": "activitiFlowId",
                                    "length": "32",
                                    "type": "String",
                                    "fieldTitle": "activiti流程ID",
                                    "filterTypes": [
                                        "equal",
                                        "in"
                                    ]
                                },
                                "color": [
                                    "#ffa940",
                                    "#ff6666"
                                ]
                            },
                            {
                                "field": {
                                    "notes": "activiti任务ID",
                                    "editable": true,
                                    "notnull": true,
                                    "name": "activitiTaskId",
                                    "length": "64",
                                    "type": "String",
                                    "fieldTitle": "activiti任务ID",
                                    "filterTypes": [
                                        "equal",
                                        "in"
                                    ]
                                }
                            },
                            {
                                "field": {
                                    "notes": "状态（0:默认 1:运行中 2:成功 3:失败 4:取消）",
                                    "editable": true,
                                    "defaultValue": "0",
                                    "notnull": true,
                                    "name": "status",
                                    "length": "5",
                                    "fieldTitle": "状态",
                                    "type": "Integer",
                                    "filterTypes": [
                                        "equal",
                                        "in"
                                    ]
                                }
                            },
                            {
                                "field": {
                                    "notes": "类型",
                                    "editable": true,
                                    "defaultValue": "0",
                                    "notnull": true,
                                    "name": "type",
                                    "length": "5",
                                    "type": "Integer",
                                    "fieldTitle": "类型",
                                    "filterTypes": [
                                        "equal",
                                        "in"
                                    ]
                                }
                            },
                            {
                                "field": {
                                    "notes": "业务ID",
                                    "editable": true,
                                    "notnull": false,
                                    "name": "businessKey",
                                    "length": "32",
                                    "type": "String",
                                    "fieldTitle": "业务ID",
                                    "filterTypes": [
                                        "equal",
                                        "in"
                                    ]
                                }
                            },
                            {
                                "field": {
                                    "notes": "任务定义ID",
                                    "editable": true,
                                    "defaultValue": "0",
                                    "notnull": true,
                                    "name": "taskDefinitionId",
                                    "length": "32",
                                    "type": "String",
                                    "fieldTitle": "任务定义ID"
                                }
                            },
                            {
                                "field": {
                                    "notes": "更新时间",
                                    "editable": false,
                                    "defaultValue": "current_timestamp()",
                                    "notnull": true,
                                    "extra": "ON UPDATE current_timestamp()",
                                    "name": "gmtUpdate",
                                    "length": "0",
                                    "type": "DateTime",
                                    "fieldTitle": "更新时间",
                                    "filterTypes": [
                                        "between"
                                    ]
                                }
                            },
                            {
                                "field": {
                                    "notes": "创建时间",
                                    "editable": false,
                                    "defaultValue": "current_timestamp()",
                                    "notnull": true,
                                    "name": "gmtCreate",
                                    "length": "0",
                                    "type": "DateTime",
                                    "fieldTitle": "创建时间",
                                    "filterTypes": [
                                        "between"
                                    ]
                                }
                            }
                        ],
                        "familyId": "com.horsecoder.workFlow",
                        "thirdParty": false,
                        "size": {
                            "width": 125,
                            "height": 75
                        }
                    }
                }
            }
        },
        {
            "id": "@com.horsecoder.workFlow/taskAssignee",
            "name": "任务委派人管理",
            "type": "model",
            "color": "#5cdbd3",
            "x": 650,
            "y": 75,
            "select": false,
            "locked": false,
            "pendant": {
                "data": {
                    "type": "model",
                    "detail": {
                        "id": "@com.horsecoder.workFlow/taskAssignee",
                        "model": {
                            "module": "sys",
                            "modelTitle": "任务委派人管理",
                            "name": "taskAssignee",
                            "id": "@com.horsecoder.workFlow/taskAssignee",
                            "pk": [
                                "id"
                            ],
                            "fields": [
                                {
                                    "notes": "任务委派人ID",
                                    "name": "id",
                                    "length": "32",
                                    "type": "String",
                                    "fieldTitle": "任务委派人ID"
                                },
                                {
                                    "notes": "任务记录ID",
                                    "editable": true,
                                    "notnull": true,
                                    "name": "taskId",
                                    "length": "32",
                                    "type": "String",
                                    "fieldTitle": "任务记录ID",
                                    "filterTypes": [
                                        "equal",
                                        "in"
                                    ]
                                },
                                {
                                    "notes": "用户ID",
                                    "editable": true,
                                    "notnull": true,
                                    "name": "userId",
                                    "length": "32",
                                    "type": "String",
                                    "fieldTitle": "用户ID",
                                    "filterTypes": [
                                        "equal"
                                    ]
                                },
                                {
                                    "notes": "备注",
                                    "editable": true,
                                    "notnull": false,
                                    "name": "remark",
                                    "length": "256",
                                    "type": "String",
                                    "fieldTitle": "备注"
                                },
                                {
                                    "notes": "委派人类型(0:审批人 1:参与人)",
                                    "editable": true,
                                    "defaultValue": "0",
                                    "notnull": true,
                                    "name": "type",
                                    "length": "5",
                                    "type": "Integer",
                                    "fieldTitle": "委派人类型",
                                    "filterTypes": [
                                        "equal",
                                        "in"
                                    ]
                                },
                                {
                                    "notes": "状态",
                                    "editable": true,
                                    "defaultValue": "0",
                                    "notnull": true,
                                    "name": "status",
                                    "length": "5",
                                    "type": "Integer",
                                    "fieldTitle": "状态",
                                    "filterTypes": [
                                        "equal",
                                        "in"
                                    ]
                                },
                                {
                                    "notes": "更新时间",
                                    "editable": false,
                                    "defaultValue": "current_timestamp()",
                                    "notnull": true,
                                    "extra": "ON UPDATE current_timestamp()",
                                    "name": "gmtUpdate",
                                    "length": "0",
                                    "type": "DateTime",
                                    "fieldTitle": "更新时间",
                                    "filterTypes": [
                                        "between"
                                    ]
                                },
                                {
                                    "notes": "创建时间",
                                    "editable": false,
                                    "defaultValue": "current_timestamp()",
                                    "notnull": true,
                                    "name": "gmtCreate",
                                    "length": "0",
                                    "type": "DateTime",
                                    "fieldTitle": "创建时间",
                                    "filterTypes": [
                                        "between"
                                    ]
                                }
                            ],
                            "order": "gmtCreate desc"
                        },
                        "component": {
                            "id": "@com.horsecoder.workFlow/taskAssignee",
                            "modelId": "@com.horsecoder.workFlow/taskAssignee",
                            "isCollapse": false,
                            "x": 650,
                            "y": 75
                        },
                        "fields": [
                            {
                                "field": {
                                    "notes": "任务委派人ID",
                                    "name": "id",
                                    "length": "32",
                                    "type": "String",
                                    "fieldTitle": "任务委派人ID"
                                }
                            },
                            {
                                "field": {
                                    "notes": "任务记录ID",
                                    "editable": true,
                                    "notnull": true,
                                    "name": "taskId",
                                    "length": "32",
                                    "type": "String",
                                    "fieldTitle": "任务记录ID",
                                    "filterTypes": [
                                        "equal",
                                        "in"
                                    ]
                                },
                                "color": [
                                    "#bae637",
                                    "#5cdbd3"
                                ]
                            },
                            {
                                "field": {
                                    "notes": "用户ID",
                                    "editable": true,
                                    "notnull": true,
                                    "name": "userId",
                                    "length": "32",
                                    "type": "String",
                                    "fieldTitle": "用户ID",
                                    "filterTypes": [
                                        "equal"
                                    ]
                                }
                            },
                            {
                                "field": {
                                    "notes": "备注",
                                    "editable": true,
                                    "notnull": false,
                                    "name": "remark",
                                    "length": "256",
                                    "type": "String",
                                    "fieldTitle": "备注"
                                }
                            },
                            {
                                "field": {
                                    "notes": "委派人类型(0:审批人 1:参与人)",
                                    "editable": true,
                                    "defaultValue": "0",
                                    "notnull": true,
                                    "name": "type",
                                    "length": "5",
                                    "type": "Integer",
                                    "fieldTitle": "委派人类型",
                                    "filterTypes": [
                                        "equal",
                                        "in"
                                    ]
                                }
                            },
                            {
                                "field": {
                                    "notes": "状态",
                                    "editable": true,
                                    "defaultValue": "0",
                                    "notnull": true,
                                    "name": "status",
                                    "length": "5",
                                    "type": "Integer",
                                    "fieldTitle": "状态",
                                    "filterTypes": [
                                        "equal",
                                        "in"
                                    ]
                                }
                            },
                            {
                                "field": {
                                    "notes": "更新时间",
                                    "editable": false,
                                    "defaultValue": "current_timestamp()",
                                    "notnull": true,
                                    "extra": "ON UPDATE current_timestamp()",
                                    "name": "gmtUpdate",
                                    "length": "0",
                                    "type": "DateTime",
                                    "fieldTitle": "更新时间",
                                    "filterTypes": [
                                        "between"
                                    ]
                                }
                            },
                            {
                                "field": {
                                    "notes": "创建时间",
                                    "editable": false,
                                    "defaultValue": "current_timestamp()",
                                    "notnull": true,
                                    "name": "gmtCreate",
                                    "length": "0",
                                    "type": "DateTime",
                                    "fieldTitle": "创建时间",
                                    "filterTypes": [
                                        "between"
                                    ]
                                }
                            }
                        ],
                        "familyId": "com.horsecoder.workFlow",
                        "thirdParty": false,
                        "size": {
                            "width": 125,
                            "height": 75
                        }
                    }
                }
            }
        },
        {
            "id": "@com.horsecoder.workFlow/event",
            "name": "事件管理",
            "type": "model",
            "color": "#5cdbd3",
            "x": 50,
            "y": 195,
            "select": false,
            "locked": false,
            "pendant": {
                "data": {
                    "type": "model",
                    "detail": {
                        "id": "@com.horsecoder.workFlow/event",
                        "model": {
                            "module": "sys",
                            "modelTitle": "事件管理",
                            "name": "event",
                            "id": "@com.horsecoder.workFlow/event",
                            "pk": [
                                "id"
                            ],
                            "fields": [
                                {
                                    "notes": "事件ID",
                                    "name": "id",
                                    "length": "32",
                                    "type": "String",
                                    "fieldTitle": "事件ID"
                                },
                                {
                                    "notes": "更新时间",
                                    "editable": false,
                                    "defaultValue": "current_timestamp()",
                                    "notnull": true,
                                    "extra": "ON UPDATE current_timestamp()",
                                    "name": "gmtUpdate",
                                    "length": "0",
                                    "type": "DateTime",
                                    "fieldTitle": "更新时间",
                                    "filterTypes": [
                                        "between"
                                    ]
                                },
                                {
                                    "notes": "创建时间",
                                    "editable": false,
                                    "defaultValue": "current_timestamp()",
                                    "notnull": true,
                                    "name": "gmtCreate",
                                    "length": "0",
                                    "type": "DateTime",
                                    "fieldTitle": "创建时间",
                                    "filterTypes": [
                                        "between"
                                    ]
                                }
                            ],
                            "order": "gmtCreate desc"
                        },
                        "component": {
                            "id": "@com.horsecoder.workFlow/event",
                            "modelId": "@com.horsecoder.workFlow/event",
                            "isCollapse": false,
                            "x": 50,
                            "y": 195
                        },
                        "fields": [
                            {
                                "field": {
                                    "notes": "事件ID",
                                    "name": "id",
                                    "length": "32",
                                    "type": "String",
                                    "fieldTitle": "事件ID"
                                }
                            },
                            {
                                "field": {
                                    "notes": "更新时间",
                                    "editable": false,
                                    "defaultValue": "current_timestamp()",
                                    "notnull": true,
                                    "extra": "ON UPDATE current_timestamp()",
                                    "name": "gmtUpdate",
                                    "length": "0",
                                    "type": "DateTime",
                                    "fieldTitle": "更新时间",
                                    "filterTypes": [
                                        "between"
                                    ]
                                }
                            },
                            {
                                "field": {
                                    "notes": "创建时间",
                                    "editable": false,
                                    "defaultValue": "current_timestamp()",
                                    "notnull": true,
                                    "name": "gmtCreate",
                                    "length": "0",
                                    "type": "DateTime",
                                    "fieldTitle": "创建时间",
                                    "filterTypes": [
                                        "between"
                                    ]
                                }
                            }
                        ],
                        "familyId": "com.horsecoder.workFlow",
                        "thirdParty": false,
                        "size": {
                            "width": 125,
                            "height": 75
                        }
                    }
                }
            }
        },
        {
            "id": "@com.horsecoder.workFlow/gateway",
            "name": "网关管理",
            "type": "model",
            "color": "#5cdbd3",
            "x": 350,
            "y": 195,
            "select": false,
            "locked": false,
            "pendant": {
                "data": {
                    "type": "model",
                    "detail": {
                        "id": "@com.horsecoder.workFlow/gateway",
                        "model": {
                            "module": "sys",
                            "modelTitle": "网关管理",
                            "name": "gateway",
                            "id": "@com.horsecoder.workFlow/gateway",
                            "pk": [
                                "id"
                            ],
                            "fields": [
                                {
                                    "notes": "网关ID",
                                    "name": "id",
                                    "length": "32",
                                    "type": "String",
                                    "fieldTitle": "网关ID"
                                },
                                {
                                    "notes": "更新时间",
                                    "editable": false,
                                    "defaultValue": "current_timestamp()",
                                    "notnull": true,
                                    "extra": "ON UPDATE current_timestamp()",
                                    "name": "gmtUpdate",
                                    "length": "0",
                                    "type": "DateTime",
                                    "fieldTitle": "更新时间",
                                    "filterTypes": [
                                        "between"
                                    ]
                                },
                                {
                                    "notes": "创建时间",
                                    "editable": false,
                                    "defaultValue": "current_timestamp()",
                                    "notnull": true,
                                    "name": "gmtCreate",
                                    "length": "0",
                                    "type": "DateTime",
                                    "fieldTitle": "创建时间",
                                    "filterTypes": [
                                        "between"
                                    ]
                                }
                            ],
                            "order": "gmtCreate desc"
                        },
                        "component": {
                            "id": "@com.horsecoder.workFlow/gateway",
                            "modelId": "@com.horsecoder.workFlow/gateway",
                            "isCollapse": false,
                            "x": 350,
                            "y": 195
                        },
                        "fields": [
                            {
                                "field": {
                                    "notes": "网关ID",
                                    "name": "id",
                                    "length": "32",
                                    "type": "String",
                                    "fieldTitle": "网关ID"
                                }
                            },
                            {
                                "field": {
                                    "notes": "更新时间",
                                    "editable": false,
                                    "defaultValue": "current_timestamp()",
                                    "notnull": true,
                                    "extra": "ON UPDATE current_timestamp()",
                                    "name": "gmtUpdate",
                                    "length": "0",
                                    "type": "DateTime",
                                    "fieldTitle": "更新时间",
                                    "filterTypes": [
                                        "between"
                                    ]
                                }
                            },
                            {
                                "field": {
                                    "notes": "创建时间",
                                    "editable": false,
                                    "defaultValue": "current_timestamp()",
                                    "notnull": true,
                                    "name": "gmtCreate",
                                    "length": "0",
                                    "type": "DateTime",
                                    "fieldTitle": "创建时间",
                                    "filterTypes": [
                                        "between"
                                    ]
                                }
                            }
                        ],
                        "familyId": "com.horsecoder.workFlow",
                        "thirdParty": false,
                        "size": {
                            "width": 125,
                            "height": 75
                        }
                    }
                }
            }
        },
        {
            "id": "@com.horsecoder.workFlow/flowDefinition",
            "name": "流程定义管理",
            "type": "model",
            "color": "#5cdbd3",
            "x": 662.5999999999999,
            "y": 239.5999999999999,
            "select": false,
            "locked": false,
            "pendant": {
                "data": {
                    "type": "model",
                    "detail": {
                        "id": "@com.horsecoder.workFlow/flowDefinition",
                        "model": {
                            "module": "sys",
                            "modelTitle": "流程定义管理",
                            "name": "flowDefinition",
                            "ifOpen": 0,
                            "id": "@com.horsecoder.workFlow/flowDefinition",
                            "pk": [
                                "id"
                            ],
                            "fields": [
                                {
                                    "notes": "流程定义ID",
                                    "name": "id",
                                    "length": "32",
                                    "type": "String",
                                    "fieldTitle": "流程定义ID"
                                },
                                {
                                    "notes": "流程唯一ID",
                                    "editable": true,
                                    "notnull": true,
                                    "name": "flowDefinitionKey",
                                    "length": "32",
                                    "type": "String",
                                    "fieldTitle": "流程唯一ID",
                                    "filterTypes": [
                                        "equal",
                                        "in"
                                    ]
                                },
                                {
                                    "notes": "流程标题",
                                    "editable": true,
                                    "notnull": true,
                                    "name": "title",
                                    "length": "128",
                                    "fieldTitle": "流程标题",
                                    "type": "String",
                                    "filterTypes": [
                                        "equal",
                                        "like"
                                    ]
                                },
                                {
                                    "notes": "流程名称",
                                    "editable": true,
                                    "notnull": true,
                                    "name": "name",
                                    "length": "32",
                                    "type": "String",
                                    "fieldTitle": "流程名称",
                                    "filterTypes": [
                                        "equal"
                                    ]
                                },
                                {
                                    "notes": "bpmn文件流",
                                    "editable": true,
                                    "notnull": true,
                                    "name": "bpmnFileStream",
                                    "length": "512",
                                    "type": "String",
                                    "fieldTitle": "bpmn文件流"
                                },
                                {
                                    "notes": "状态（0:上线 1:下线）",
                                    "editable": true,
                                    "defaultValue": "0",
                                    "notnull": true,
                                    "name": "status",
                                    "length": "5",
                                    "type": "Integer",
                                    "fieldTitle": "状态",
                                    "filterTypes": [
                                        "equal",
                                        "in"
                                    ]
                                },
                                {
                                    "notes": "更新时间",
                                    "editable": false,
                                    "defaultValue": "current_timestamp()",
                                    "notnull": true,
                                    "extra": "ON UPDATE current_timestamp()",
                                    "name": "gmtUpdate",
                                    "length": "0",
                                    "type": "DateTime",
                                    "fieldTitle": "更新时间",
                                    "filterTypes": [
                                        "between"
                                    ]
                                },
                                {
                                    "notes": "创建时间",
                                    "editable": false,
                                    "defaultValue": "current_timestamp()",
                                    "notnull": true,
                                    "name": "gmtCreate",
                                    "length": "0",
                                    "type": "DateTime",
                                    "fieldTitle": "创建时间",
                                    "filterTypes": [
                                        "between"
                                    ]
                                }
                            ],
                            "order": "gmtCreate desc"
                        },
                        "component": {
                            "id": "@com.horsecoder.workFlow/flowDefinition",
                            "modelId": "@com.horsecoder.workFlow/flowDefinition",
                            "isCollapse": false,
                            "x": 662.5999999999999,
                            "y": 239.5999999999999
                        },
                        "fields": [
                            {
                                "field": {
                                    "notes": "流程定义ID",
                                    "name": "id",
                                    "length": "32",
                                    "type": "String",
                                    "fieldTitle": "流程定义ID"
                                },
                                "color": [
                                    "#95de64",
                                    "#ff85c0"
                                ]
                            },
                            {
                                "field": {
                                    "notes": "流程唯一ID",
                                    "editable": true,
                                    "notnull": true,
                                    "name": "flowDefinitionKey",
                                    "length": "32",
                                    "type": "String",
                                    "fieldTitle": "流程唯一ID",
                                    "filterTypes": [
                                        "equal",
                                        "in"
                                    ]
                                }
                            },
                            {
                                "field": {
                                    "notes": "流程标题",
                                    "editable": true,
                                    "notnull": true,
                                    "name": "title",
                                    "length": "128",
                                    "fieldTitle": "流程标题",
                                    "type": "String",
                                    "filterTypes": [
                                        "equal",
                                        "like"
                                    ]
                                }
                            },
                            {
                                "field": {
                                    "notes": "流程名称",
                                    "editable": true,
                                    "notnull": true,
                                    "name": "name",
                                    "length": "32",
                                    "type": "String",
                                    "fieldTitle": "流程名称",
                                    "filterTypes": [
                                        "equal"
                                    ]
                                }
                            },
                            {
                                "field": {
                                    "notes": "bpmn文件流",
                                    "editable": true,
                                    "notnull": true,
                                    "name": "bpmnFileStream",
                                    "length": "512",
                                    "type": "String",
                                    "fieldTitle": "bpmn文件流"
                                }
                            },
                            {
                                "field": {
                                    "notes": "状态（0:上线 1:下线）",
                                    "editable": true,
                                    "defaultValue": "0",
                                    "notnull": true,
                                    "name": "status",
                                    "length": "5",
                                    "type": "Integer",
                                    "fieldTitle": "状态",
                                    "filterTypes": [
                                        "equal",
                                        "in"
                                    ]
                                }
                            },
                            {
                                "field": {
                                    "notes": "更新时间",
                                    "editable": false,
                                    "defaultValue": "current_timestamp()",
                                    "notnull": true,
                                    "extra": "ON UPDATE current_timestamp()",
                                    "name": "gmtUpdate",
                                    "length": "0",
                                    "type": "DateTime",
                                    "fieldTitle": "更新时间",
                                    "filterTypes": [
                                        "between"
                                    ]
                                }
                            },
                            {
                                "field": {
                                    "notes": "创建时间",
                                    "editable": false,
                                    "defaultValue": "current_timestamp()",
                                    "notnull": true,
                                    "name": "gmtCreate",
                                    "length": "0",
                                    "type": "DateTime",
                                    "fieldTitle": "创建时间",
                                    "filterTypes": [
                                        "between"
                                    ]
                                }
                            }
                        ],
                        "familyId": "com.horsecoder.workFlow",
                        "thirdParty": false,
                        "size": {
                            "width": 125,
                            "height": 75
                        }
                    }
                }
            }
        },
        {
            "id": "@com.horsecoder.workFlow/taskDefinition",
            "name": "任务定义管理",
            "type": "model",
            "color": "#5cdbd3",
            "x": 50,
            "y": 315,
            "select": false,
            "locked": false,
            "pendant": {
                "data": {
                    "type": "model",
                    "detail": {
                        "id": "@com.horsecoder.workFlow/taskDefinition",
                        "model": {
                            "module": "sys",
                            "modelTitle": "任务定义管理",
                            "name": "taskDefinition",
                            "id": "@com.horsecoder.workFlow/taskDefinition",
                            "pk": [
                                "id"
                            ],
                            "fields": [
                                {
                                    "notes": "任务定义ID",
                                    "name": "id",
                                    "length": "32",
                                    "type": "String",
                                    "fieldTitle": "任务定义ID"
                                },
                                {
                                    "notes": "任务唯一ID",
                                    "editable": true,
                                    "notnull": true,
                                    "name": "taskDefinitionKey",
                                    "length": "32",
                                    "type": "String",
                                    "fieldTitle": "任务唯一ID",
                                    "filterTypes": [
                                        "equal",
                                        "in"
                                    ]
                                },
                                {
                                    "notes": "流程定义ID",
                                    "editable": true,
                                    "notnull": true,
                                    "name": "flowDefinitionId",
                                    "length": "32",
                                    "type": "String",
                                    "fieldTitle": "流程定义ID",
                                    "filterTypes": [
                                        "equal",
                                        "in"
                                    ]
                                },
                                {
                                    "notes": "流程引擎ID",
                                    "editable": true,
                                    "notnull": false,
                                    "name": "flowEngineId",
                                    "length": "64",
                                    "type": "String",
                                    "fieldTitle": "流程引擎ID"
                                },
                                {
                                    "notes": "任务片段",
                                    "editable": true,
                                    "notnull": true,
                                    "name": "taskXML",
                                    "length": "512",
                                    "type": "String",
                                    "fieldTitle": "任务片段"
                                },
                                {
                                    "notes": "审批后回调流程ID",
                                    "editable": true,
                                    "notnull": false,
                                    "name": "afterCheckCallbackFlowId",
                                    "length": "64",
                                    "type": "String",
                                    "fieldTitle": "审批后回调流程ID",
                                    "filterTypes": [
                                        "equal"
                                    ]
                                },
                                {
                                    "notes": "审批前回调流程ID",
                                    "editable": true,
                                    "notnull": false,
                                    "name": "beforeCheckCallbackFlowId",
                                    "length": "64",
                                    "type": "String",
                                    "fieldTitle": "审批前回调流程ID",
                                    "filterTypes": [
                                        "equal"
                                    ]
                                },
                                {
                                    "notes": "更新时间",
                                    "editable": false,
                                    "defaultValue": "current_timestamp()",
                                    "notnull": true,
                                    "extra": "ON UPDATE current_timestamp()",
                                    "name": "gmtUpdate",
                                    "length": "0",
                                    "type": "DateTime",
                                    "fieldTitle": "更新时间",
                                    "filterTypes": [
                                        "between"
                                    ]
                                },
                                {
                                    "notes": "创建时间",
                                    "editable": false,
                                    "defaultValue": "current_timestamp()",
                                    "notnull": true,
                                    "name": "gmtCreate",
                                    "length": "0",
                                    "type": "DateTime",
                                    "fieldTitle": "创建时间",
                                    "filterTypes": [
                                        "between"
                                    ]
                                }
                            ],
                            "order": "gmtCreate desc"
                        },
                        "component": {
                            "id": "@com.horsecoder.workFlow/taskDefinition",
                            "modelId": "@com.horsecoder.workFlow/taskDefinition",
                            "isCollapse": false,
                            "x": 50,
                            "y": 315
                        },
                        "fields": [
                            {
                                "field": {
                                    "notes": "任务定义ID",
                                    "name": "id",
                                    "length": "32",
                                    "type": "String",
                                    "fieldTitle": "任务定义ID"
                                }
                            },
                            {
                                "field": {
                                    "notes": "任务唯一ID",
                                    "editable": true,
                                    "notnull": true,
                                    "name": "taskDefinitionKey",
                                    "length": "32",
                                    "type": "String",
                                    "fieldTitle": "任务唯一ID",
                                    "filterTypes": [
                                        "equal",
                                        "in"
                                    ]
                                }
                            },
                            {
                                "field": {
                                    "notes": "流程定义ID",
                                    "editable": true,
                                    "notnull": true,
                                    "name": "flowDefinitionId",
                                    "length": "32",
                                    "type": "String",
                                    "fieldTitle": "流程定义ID",
                                    "filterTypes": [
                                        "equal",
                                        "in"
                                    ]
                                },
                                "color": [
                                    "#ff85c0",
                                    "#95de64"
                                ]
                            },
                            {
                                "field": {
                                    "notes": "流程引擎ID",
                                    "editable": true,
                                    "notnull": false,
                                    "name": "flowEngineId",
                                    "length": "64",
                                    "type": "String",
                                    "fieldTitle": "流程引擎ID"
                                }
                            },
                            {
                                "field": {
                                    "notes": "任务片段",
                                    "editable": true,
                                    "notnull": true,
                                    "name": "taskXML",
                                    "length": "512",
                                    "type": "String",
                                    "fieldTitle": "任务片段"
                                }
                            },
                            {
                                "field": {
                                    "notes": "审批后回调流程ID",
                                    "editable": true,
                                    "notnull": false,
                                    "name": "afterCheckCallbackFlowId",
                                    "length": "64",
                                    "type": "String",
                                    "fieldTitle": "审批后回调流程ID",
                                    "filterTypes": [
                                        "equal"
                                    ]
                                }
                            },
                            {
                                "field": {
                                    "notes": "审批前回调流程ID",
                                    "editable": true,
                                    "notnull": false,
                                    "name": "beforeCheckCallbackFlowId",
                                    "length": "64",
                                    "type": "String",
                                    "fieldTitle": "审批前回调流程ID",
                                    "filterTypes": [
                                        "equal"
                                    ]
                                }
                            },
                            {
                                "field": {
                                    "notes": "更新时间",
                                    "editable": false,
                                    "defaultValue": "current_timestamp()",
                                    "notnull": true,
                                    "extra": "ON UPDATE current_timestamp()",
                                    "name": "gmtUpdate",
                                    "length": "0",
                                    "type": "DateTime",
                                    "fieldTitle": "更新时间",
                                    "filterTypes": [
                                        "between"
                                    ]
                                }
                            },
                            {
                                "field": {
                                    "notes": "创建时间",
                                    "editable": false,
                                    "defaultValue": "current_timestamp()",
                                    "notnull": true,
                                    "name": "gmtCreate",
                                    "length": "0",
                                    "type": "DateTime",
                                    "fieldTitle": "创建时间",
                                    "filterTypes": [
                                        "between"
                                    ]
                                }
                            }
                        ],
                        "familyId": "com.horsecoder.workFlow",
                        "thirdParty": false,
                        "size": {
                            "width": 125,
                            "height": 75
                        }
                    }
                }
            }
        },
        {
            "id": "@com.horsecoder.workFlow/flowParams",
            "name": "流程参数",
            "type": "model",
            "color": "#5cdbd3",
            "x": 350,
            "y": 315,
            "select": false,
            "locked": false,
            "pendant": {
                "data": {
                    "type": "model",
                    "detail": {
                        "id": "@com.horsecoder.workFlow/flowParams",
                        "model": {
                            "module": "sys",
                            "modelTitle": "流程参数",
                            "name": "flowParams",
                            "id": "@com.horsecoder.workFlow/flowParams",
                            "pk": [
                                "id"
                            ],
                            "fields": [
                                {
                                    "notes": "流程参数ID",
                                    "name": "id",
                                    "length": "32",
                                    "type": "String",
                                    "fieldTitle": "流程参数ID"
                                },
                                {
                                    "notes": "参数名",
                                    "editable": true,
                                    "notnull": true,
                                    "name": "name",
                                    "length": "32",
                                    "fieldTitle": "参数名",
                                    "type": "String",
                                    "filterTypes": [
                                        "equal",
                                        "in"
                                    ]
                                },
                                {
                                    "notes": "参数类型",
                                    "editable": true,
                                    "notnull": false,
                                    "name": "type",
                                    "length": "32",
                                    "fieldTitle": "参数类型",
                                    "type": "String",
                                    "filterTypes": [
                                        "equal"
                                    ]
                                },
                                {
                                    "notes": "参数定义类型(0:流程入参1:节点展示参数2:节点输入参数3:节点输出参数4:流程输出参数5:流程展示参数)",
                                    "editable": true,
                                    "notnull": true,
                                    "name": "defineType",
                                    "length": "5",
                                    "fieldTitle": "参数定义类型",
                                    "type": "Integer",
                                    "filterTypes": [
                                        "equal",
                                        "in"
                                    ]
                                },
                                {
                                    "notes": "流程定义ID",
                                    "editable": true,
                                    "notnull": true,
                                    "name": "flowDefinitionId",
                                    "length": "32",
                                    "fieldTitle": "流程定义ID",
                                    "type": "String",
                                    "filterTypes": [
                                        "equal",
                                        "in"
                                    ]
                                },
                                {
                                    "notes": "任务定义ID",
                                    "editable": true,
                                    "notnull": false,
                                    "name": "taskDefinitionId",
                                    "length": "32",
                                    "fieldTitle": "任务定义ID",
                                    "type": "String",
                                    "filterTypes": [
                                        "equal",
                                        "in"
                                    ]
                                },
                                {
                                    "notes": "更新时间",
                                    "editable": false,
                                    "defaultValue": "current_timestamp()",
                                    "notnull": true,
                                    "extra": "ON UPDATE current_timestamp()",
                                    "name": "gmtUpdate",
                                    "length": "0",
                                    "type": "DateTime",
                                    "fieldTitle": "更新时间",
                                    "filterTypes": [
                                        "between"
                                    ]
                                },
                                {
                                    "notes": "创建时间",
                                    "editable": false,
                                    "defaultValue": "current_timestamp()",
                                    "notnull": true,
                                    "name": "gmtCreate",
                                    "length": "0",
                                    "type": "DateTime",
                                    "fieldTitle": "创建时间",
                                    "filterTypes": [
                                        "between"
                                    ]
                                }
                            ],
                            "order": "gmtCreate desc"
                        },
                        "component": {
                            "id": "@com.horsecoder.workFlow/flowParams",
                            "modelId": "@com.horsecoder.workFlow/flowParams",
                            "isCollapse": false,
                            "x": 350,
                            "y": 315
                        },
                        "fields": [
                            {
                                "field": {
                                    "notes": "流程参数ID",
                                    "name": "id",
                                    "length": "32",
                                    "type": "String",
                                    "fieldTitle": "流程参数ID"
                                }
                            },
                            {
                                "field": {
                                    "notes": "参数名",
                                    "editable": true,
                                    "notnull": true,
                                    "name": "name",
                                    "length": "32",
                                    "fieldTitle": "参数名",
                                    "type": "String",
                                    "filterTypes": [
                                        "equal",
                                        "in"
                                    ]
                                }
                            },
                            {
                                "field": {
                                    "notes": "参数类型",
                                    "editable": true,
                                    "notnull": false,
                                    "name": "type",
                                    "length": "32",
                                    "fieldTitle": "参数类型",
                                    "type": "String",
                                    "filterTypes": [
                                        "equal"
                                    ]
                                }
                            },
                            {
                                "field": {
                                    "notes": "参数定义类型(0:流程入参1:节点展示参数2:节点输入参数3:节点输出参数4:流程输出参数5:流程展示参数)",
                                    "editable": true,
                                    "notnull": true,
                                    "name": "defineType",
                                    "length": "5",
                                    "fieldTitle": "参数定义类型",
                                    "type": "Integer",
                                    "filterTypes": [
                                        "equal",
                                        "in"
                                    ]
                                }
                            },
                            {
                                "field": {
                                    "notes": "流程定义ID",
                                    "editable": true,
                                    "notnull": true,
                                    "name": "flowDefinitionId",
                                    "length": "32",
                                    "fieldTitle": "流程定义ID",
                                    "type": "String",
                                    "filterTypes": [
                                        "equal",
                                        "in"
                                    ]
                                }
                            },
                            {
                                "field": {
                                    "notes": "任务定义ID",
                                    "editable": true,
                                    "notnull": false,
                                    "name": "taskDefinitionId",
                                    "length": "32",
                                    "fieldTitle": "任务定义ID",
                                    "type": "String",
                                    "filterTypes": [
                                        "equal",
                                        "in"
                                    ]
                                }
                            },
                            {
                                "field": {
                                    "notes": "更新时间",
                                    "editable": false,
                                    "defaultValue": "current_timestamp()",
                                    "notnull": true,
                                    "extra": "ON UPDATE current_timestamp()",
                                    "name": "gmtUpdate",
                                    "length": "0",
                                    "type": "DateTime",
                                    "fieldTitle": "更新时间",
                                    "filterTypes": [
                                        "between"
                                    ]
                                }
                            },
                            {
                                "field": {
                                    "notes": "创建时间",
                                    "editable": false,
                                    "defaultValue": "current_timestamp()",
                                    "notnull": true,
                                    "name": "gmtCreate",
                                    "length": "0",
                                    "type": "DateTime",
                                    "fieldTitle": "创建时间",
                                    "filterTypes": [
                                        "between"
                                    ]
                                }
                            }
                        ],
                        "familyId": "com.horsecoder.workFlow",
                        "thirdParty": false,
                        "size": {
                            "width": 125,
                            "height": 75
                        }
                    }
                }
            }
        }
    ],
    "links": [
        {
            "id": "@com.horsecoder.workFlow/taskRecordList",
            "name": "流程记录的ID关联的任务记录列表",
            "color": "#ffa940",
            "type": "relation",
            "points": [
                {
                    "x": 531,
                    "y": 340
                },
                {
                    "x": 531,
                    "y": 365
                },
                {
                    "x": 667.5,
                    "y": 365
                },
                {
                    "x": 667.5,
                    "y": 250
                },
                {
                    "x": 804,
                    "y": 250
                },
                {
                    "x": 804,
                    "y": 275
                }
            ],
            "fromElementId": "@com.horsecoder.workFlow/flowRecord",
            "toElementId": "@com.horsecoder.workFlow/taskRecord",
            "pendant": {
                "data": {
                    "type": "relation",
                    "detail": {
                        "id": "@com.horsecoder.workFlow/taskRecordList",
                        "relation": {
                            "relationType": "ONE2N",
                            "modelMainKey": [
                                "activitiFlowId"
                            ],
                            "relationName": "taskRecordList",
                            "modelMainId": "@com.horsecoder.workFlow/flowRecord",
                            "modelRelatedKey": [
                                "activitiFlowId"
                            ],
                            "modelRelatedId": "@com.horsecoder.workFlow/taskRecord",
                            "relationTitle": "流程记录的ID关联的任务记录列表",
                            "id": "@com.horsecoder.workFlow/taskRecordList"
                        },
                        "link": {
                            "id": "@com.horsecoder.workFlow/taskRecordList",
                            "relationId": "@com.horsecoder.workFlow/taskRecordList",
                            "color": "#ffa940",
                            "points": []
                        },
                        "thirdParty": false
                    }
                }
            }
        },
        {
            "id": "@com.horsecoder.workFlow/flowRecord",
            "name": "任务记录的activiti流程ID关联的流程记录",
            "color": "#ff6666",
            "type": "relation",
            "points": [
                {
                    "x": 804,
                    "y": 340
                },
                {
                    "x": 804,
                    "y": 365
                },
                {
                    "x": 667.5,
                    "y": 365
                },
                {
                    "x": 667.5,
                    "y": 250
                },
                {
                    "x": 531,
                    "y": 250
                },
                {
                    "x": 531,
                    "y": 275
                }
            ],
            "fromElementId": "@com.horsecoder.workFlow/taskRecord",
            "toElementId": "@com.horsecoder.workFlow/flowRecord",
            "pendant": {
                "data": {
                    "type": "relation",
                    "detail": {
                        "id": "@com.horsecoder.workFlow/flowRecord",
                        "relation": {
                            "relationType": "ONE2ONE",
                            "modelMainKey": [
                                "activitiFlowId"
                            ],
                            "relationName": "flowRecord",
                            "modelMainId": "@com.horsecoder.workFlow/taskRecord",
                            "modelRelatedKey": [
                                "activitiFlowId"
                            ],
                            "modelRelatedId": "@com.horsecoder.workFlow/flowRecord",
                            "relationTitle": "任务记录的activiti流程ID关联的流程记录",
                            "id": "@com.horsecoder.workFlow/flowRecord"
                        },
                        "link": {
                            "id": "@com.horsecoder.workFlow/flowRecord",
                            "relationId": "@com.horsecoder.workFlow/flowRecord",
                            "color": "#ff6666",
                            "points": []
                        },
                        "thirdParty": false
                    }
                }
            }
        },
        {
            "id": "@com.horsecoder.workFlow/taskAssigneeList",
            "name": "任务记录的ID关联的任务委派人列表",
            "color": "#bae637",
            "type": "relation",
            "points": [
                {
                    "x": 858,
                    "y": 340
                },
                {
                    "x": 858,
                    "y": 390
                },
                {
                    "x": 994.5,
                    "y": 390
                },
                {
                    "x": 994.5,
                    "y": 250
                },
                {
                    "x": 1131,
                    "y": 250
                },
                {
                    "x": 1131,
                    "y": 275
                }
            ],
            "fromElementId": "@com.horsecoder.workFlow/taskRecord",
            "toElementId": "@com.horsecoder.workFlow/taskAssignee",
            "pendant": {
                "data": {
                    "type": "relation",
                    "detail": {
                        "id": "@com.horsecoder.workFlow/taskAssigneeList",
                        "relation": {
                            "relationType": "ONE2N",
                            "modelMainKey": [
                                "id"
                            ],
                            "relationName": "taskAssigneeList",
                            "modelMainId": "@com.horsecoder.workFlow/taskRecord",
                            "modelRelatedKey": [
                                "taskId"
                            ],
                            "modelRelatedId": "@com.horsecoder.workFlow/taskAssignee",
                            "relationTitle": "任务记录的ID关联的任务委派人列表",
                            "id": "@com.horsecoder.workFlow/taskAssigneeList"
                        },
                        "link": {
                            "id": "@com.horsecoder.workFlow/taskAssigneeList",
                            "relationId": "@com.horsecoder.workFlow/taskAssigneeList",
                            "color": "#bae637",
                            "points": []
                        },
                        "thirdParty": false
                    }
                }
            }
        },
        {
            "id": "@com.horsecoder.workFlow/taskRecord",
            "name": "任务委派人的任务记录ID关联的任务记录",
            "color": "#5cdbd3",
            "type": "relation",
            "points": [
                {
                    "x": 1131,
                    "y": 340
                },
                {
                    "x": 1131,
                    "y": 365
                },
                {
                    "x": 994.5,
                    "y": 365
                },
                {
                    "x": 994.5,
                    "y": 225
                },
                {
                    "x": 858,
                    "y": 225
                },
                {
                    "x": 858,
                    "y": 275
                }
            ],
            "fromElementId": "@com.horsecoder.workFlow/taskAssignee",
            "toElementId": "@com.horsecoder.workFlow/taskRecord",
            "pendant": {
                "data": {
                    "type": "relation",
                    "detail": {
                        "id": "@com.horsecoder.workFlow/taskRecord",
                        "relation": {
                            "relationType": "ONE2ONE",
                            "modelMainKey": [
                                "taskId"
                            ],
                            "relationName": "taskRecord",
                            "modelMainId": "@com.horsecoder.workFlow/taskAssignee",
                            "modelRelatedKey": [
                                "id"
                            ],
                            "modelRelatedId": "@com.horsecoder.workFlow/taskRecord",
                            "relationTitle": "任务委派人的任务记录ID关联的任务记录",
                            "id": "@com.horsecoder.workFlow/taskRecord"
                        },
                        "link": {
                            "id": "@com.horsecoder.workFlow/taskRecord",
                            "relationId": "@com.horsecoder.workFlow/taskRecord",
                            "color": "#5cdbd3",
                            "points": []
                        },
                        "thirdParty": false
                    }
                }
            }
        },
        {
            "id": "@com.horsecoder.workFlow/flowDefinition",
            "name": "任务定义的流程定义ID关联的流程定义",
            "color": "#95de64",
            "type": "relation",
            "points": [
                {
                    "x": 531,
                    "y": 580
                },
                {
                    "x": 531,
                    "y": 605
                },
                {
                    "x": 837.3,
                    "y": 605
                },
                {
                    "x": 837.3,
                    "y": 414.5999999999999
                },
                {
                    "x": 1143.6,
                    "y": 414.5999999999999
                },
                {
                    "x": 1143.6,
                    "y": 439.5999999999999
                }
            ],
            "fromElementId": "@com.horsecoder.workFlow/taskDefinition",
            "toElementId": "@com.horsecoder.workFlow/flowDefinition",
            "pendant": {
                "data": {
                    "type": "relation",
                    "detail": {
                        "id": "@com.horsecoder.workFlow/flowDefinition",
                        "relation": {
                            "relationType": "ONE2ONE",
                            "modelMainKey": [
                                "flowDefinitionId"
                            ],
                            "relationName": "flowDefinition",
                            "modelMainId": "@com.horsecoder.workFlow/taskDefinition",
                            "modelRelatedKey": [
                                "id"
                            ],
                            "modelRelatedId": "@com.horsecoder.workFlow/flowDefinition",
                            "relationTitle": "任务定义的流程定义ID关联的流程定义",
                            "id": "@com.horsecoder.workFlow/flowDefinition"
                        },
                        "link": {
                            "id": "@com.horsecoder.workFlow/flowDefinition",
                            "relationId": "@com.horsecoder.workFlow/flowDefinition",
                            "color": "#95de64",
                            "points": []
                        },
                        "thirdParty": false
                    }
                }
            }
        },
        {
            "id": "@com.horsecoder.workFlow/taskDefinition",
            "name": "流程定义的ID关联的任务定义",
            "color": "#ff85c0",
            "type": "relation",
            "points": [
                {
                    "x": 1143.6,
                    "y": 504.5999999999999
                },
                {
                    "x": 1143.6,
                    "y": 529.5999999999999
                },
                {
                    "x": 837.3,
                    "y": 529.5999999999999
                },
                {
                    "x": 837.3,
                    "y": 490
                },
                {
                    "x": 531,
                    "y": 490
                },
                {
                    "x": 531,
                    "y": 515
                }
            ],
            "fromElementId": "@com.horsecoder.workFlow/flowDefinition",
            "toElementId": "@com.horsecoder.workFlow/taskDefinition",
            "pendant": {
                "data": {
                    "type": "relation",
                    "detail": {
                        "id": "@com.horsecoder.workFlow/taskDefinition",
                        "relation": {
                            "relationType": "ONE2ONE",
                            "modelMainKey": [
                                "id"
                            ],
                            "relationName": "taskDefinition",
                            "modelMainId": "@com.horsecoder.workFlow/flowDefinition",
                            "modelRelatedKey": [
                                "flowDefinitionId"
                            ],
                            "modelRelatedId": "@com.horsecoder.workFlow/taskDefinition",
                            "relationTitle": "流程定义的ID关联的任务定义",
                            "id": "@com.horsecoder.workFlow/taskDefinition"
                        },
                        "link": {
                            "id": "@com.horsecoder.workFlow/taskDefinition",
                            "relationId": "@com.horsecoder.workFlow/taskDefinition",
                            "color": "#ff85c0",
                            "points": []
                        },
                        "thirdParty": false
                    }
                }
            }
        }
    ]
}