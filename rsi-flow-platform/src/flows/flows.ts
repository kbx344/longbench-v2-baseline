import { register, load } from '@horsecoder/flow';
import { loadMy } from './flows_my';
import { flowRunFacade as coreFlowRunFacade } from '@/flows/com.horsecoder.common.whuTalent.flowTesting.core/Run';
import coreFlowRun from '@/flows/com.horsecoder.common.whuTalent.flowTesting.core/Run/flow.json';

export const loadAll = async () => {
  loadMy();
  register('@com.horsecoder.common.whuTalent.flowTesting.core/FLOW_CODE_RUN', coreFlowRunFacade);
  load('@com.horsecoder.common.whuTalent.flowTesting.core/FLOW_RUN', JSON.parse(JSON.stringify(coreFlowRun)))
}
