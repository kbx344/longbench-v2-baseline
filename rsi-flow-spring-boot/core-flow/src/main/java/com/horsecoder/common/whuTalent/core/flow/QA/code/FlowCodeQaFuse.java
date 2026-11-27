package com.horsecoder.common.whuTalent.core.flow.QA.code;

import cc.eamon.open.flow.core.FlowEngine;
import cc.eamon.open.flow.core.stereotype.Flow;
import cc.eamon.open.flow.spring.FlowComponent;

/**
 * 把统计表、文档摘要与检索证据合成一份证据材料
 *
 * FLOW_QA_FUSE
 *
 * 裁决环节要同时看到三样东西：全库计数（硬事实）、跨文档摘要（结构）、
 * 原文命中句（措辞）。三者来源不同，在这里按固定体例拼起来，
 * 后面 DECIDE 只需要接一个 evidence 入参。
 *
 * @param evidence 原文命中证据
 * @param digest 文档摘要正文
 * @param statsTable 检索词全库统计表
 * @result evidence 合成后的证据材料
 */
@FlowComponent
public class FlowCodeQaFuse {

    @Flow("FLOW_QA_FUSE")
    public void flowCodeQaFuse(
            String evidence,
            String digest,
            String statsTable
    ) {
        String out;
        try {
            out = "【检索词全库统计】\n" + (statsTable == null ? "" : statsTable)
                    + "\n\n【三份文档的结构化摘要】\n" + (digest == null ? "" : digest)
                    + "\n\n【原文命中证据】\n" + (evidence == null ? "" : evidence);
        } catch (Exception e) {
            out = "[FUSE_FAILED] " + e.toString();
        }
        FlowEngine.instance().register("evidence", out);
    }
}
