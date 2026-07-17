import{y as Ee,_ as Me,u as Se,z as Ne,B as Ce,e as y,o as a,c as _,b as t,w as n,a as e,d as v,O as ue,P as Te,Q as Qe,F as W,r as z,n as j,N as C,i as he,t as f,R as Ve,D as F,S as Oe,G as pe,U as Be,I as $,k as Y,L as xe,V as _e,T as be,f as E,A as we,s as He,W as Ge,X as qe,g as Ke,h as Fe,Y as G,Z as Ye,$ as je,a0 as Je,a1 as de,a2 as ye,a3 as Ze,K as q,E as ke,a4 as et,a5 as tt,a6 as nt,a7 as st}from"./index-DUlG9vo0.js";function ot(V){return Ee({url:"/ai/chat",method:"post",data:V})}function at(V){return Ee({url:"/ai/history/recent",method:"get",params:V})}const it={class:"ai-assistant"},lt={key:0,class:"ai-panel"},ct={class:"ai-header"},rt={class:"ai-logo"},ut={class:"ai-body"},dt={class:"chat-container"},ht={class:"chat-avatar"},pt={class:"chat-bubble"},_t={class:"chat-content"},ft={key:0,class:"chat-sources"},mt={class:"sources-title"},gt={class:"sources-list"},vt=["onClick"],wt={class:"source-title"},yt={class:"chat-time"},kt={key:0,class:"loading-indicator"},Et={class:"ai-footer"},Mt={class:"input-wrapper"},St=["disabled"],Nt={class:"quick-questions"},Ct=["onClick"],Tt={key:0,class:"badge"},Ot={__name:"AiAssistant",setup(V){const l=Se(),i=E(!1),c=E(""),d=E([]),k=E(!1),x=E(0),M=He({}),D=["焊接温度标准是多少？","屏幕贴合有气泡怎么处理？","如何进行报工操作？","防水测试怎么做？","工单状态有哪些？","生产计划如何制定？","SMT贴片工艺流程","产品质量检验标准"],T={"焊接温度标准是多少？":{answer:`智能手环生产中，各工序焊接温度标准如下：

1. **SMT回流焊**：
   - 预热区：150-180℃，时间60-90秒
   - 恒温区：180-200℃，时间60-120秒
   - 回流区：最高温度235-245℃，峰值时间20-40秒
   - 冷却区：降温速率≤4℃/秒

2. **烙铁手焊**：
   - 普通焊点：320-360℃，时间2-3秒
   - 热敏元件：280-320℃，时间1-2秒

3. **注意事项**：
   - 每日首件必须测温板验证温度曲线
   - 每4小时抽查一次焊接温度
   - 焊接后需冷却30秒以上才可进行下一工序`,sources:[{type:"knowledge_graph",title:"SMT工艺规范",content:"回流焊温度曲线：预热150-180℃/60-90s，恒温180-200℃/60-120s，回流235-245℃/20-40s"},{type:"document",title:"焊接作业指导书",content:"烙铁温度标准：普通焊点320-360℃，热敏元件280-320℃"}]},"屏幕贴合有气泡怎么处理？":{answer:`屏幕贴合出现气泡的处理方案：

**一、气泡产生原因**
1. 贴合环境洁净度不足（灰尘颗粒）
2. 贴合压力不均匀或压力不足
3. OCA胶厚度不匹配
4. 贴合速度过快，空气未完全排出

**二、解决方法**
1. **轻微气泡（直径<0.1mm）**：
   - 放入脱泡机，0.6MPa压力，60℃，30分钟
   - 95%以上轻微气泡可消除

2. **中度气泡（直径0.1-0.3mm）**：
   - 先脱泡处理，如仍存在则返工
   - 用专用解胶液拆除屏幕，清洁后重新贴合

3. **严重气泡（直径>0.3mm）**：
   - 直接报废处理，不得流入下道工序

**三、预防措施**
- 贴合车间洁净度保持万级以上
- 每日校准贴合机压力和速度
- OCA胶存储温度控制在25±5℃`,sources:[{type:"document",title:"屏幕贴合作业指导书",content:"脱泡参数：压力0.6MPa，温度60℃，时间30分钟"},{type:"knowledge_graph",title:"贴合不良处理",content:"气泡不良率应控制在0.5%以下"}]},"如何进行报工操作？":{answer:`生产工人报工操作流程：

**操作步骤**
1. 登录智能手环或MES系统
2. 进入"生产报工"模块
3. 扫描工单号二维码或手动输入工单号
4. 选择当前工序（SMT/焊接/组装/测试/包装）
5. 输入本次报工数量（良品数+不良品数）
6. 如产生不良品，选择不良原因
7. 确认提交，系统自动更新工单进度

**注意事项**
- 报工前请确认产品质量检验合格
- 每2小时报工一次，或每完成100PCS报工一次
- 不良品需单独放置并标识
- 如发现异常请立即停线并上报班组长

**系统路径**：生产管理 → 生产报工 → 新增报工`,sources:[{type:"document",title:"报工操作手册",content:"报工频率：每2小时或每100PCS报工一次"},{type:"knowledge_graph",title:"报工流程",content:"扫码→选工序→填数量→选不良原因→提交"}]},"防水测试怎么做？":{answer:`智能手环防水测试操作规范：

**测试标准**
- IP68级防水：5米水深，持续30分钟
- 测试水温：25±5℃

**测试步骤**
1. **外观检查**：确认密封圈完好，按键、充电口无异物
2. **气压初检**：
   - 充气压力：100kPa
   - 保压时间：30秒
   - 压降≤50Pa为合格
3. **浸水测试**（抽样）：
   - 将样品放入5米水深（或加压等效）
   - 保持30分钟
   - 取出后擦干，检查内部有无进水
4. **功能验证**：
   - 触摸屏功能正常
   - 按键响应正常
   - 心率、血氧传感器正常
   - 充电功能正常

**测试频率**
- 每批次首件必测
- 量产中每2小时抽测5PCS
- 不良率超过1%时全检`,sources:[{type:"document",title:"防水测试作业指导书",content:"IP68测试标准：5米水深，30分钟"},{type:"knowledge_graph",title:"质量检验标准",content:"气压测试压降≤50Pa为合格"}]},"工单状态有哪些？":{answer:`生产工单共有4种状态：

**1. 待生产（状态码：0）**
- 工单已创建但尚未开始
- 可进行编辑、修改、删除操作
- 可查看BOM、工艺路线等生产准备信息

**2. 生产中（状态码：1）**
- 工单已启动，正在生产
- 不允许修改工单基本信息
- 实时统计生产进度和良品率
- 可查看各工序报工明细

**3. 已完成（状态码：2）**
- 工单全部生产完成并质检合格
- 自动计算工单成本和效率
- 生成生产报表和质量分析

**4. 已取消（状态码：3）**
- 订单取消或其他原因终止生产
- 仅待生产状态的工单可取消
- 取消后不可恢复

**状态流转**：待生产 → 生产中 → 已完成
（待生产也可直接取消）`,sources:[{type:"knowledge_graph",title:"工单状态机",content:"待生产→生产中→已完成，待生产也可→已取消"}]},"生产计划如何制定？":{answer:`生产计划制定流程：

**一、计划制定依据**
1. 销售订单需求
2. 产品BOM和工艺路线
3. 设备产能和人员配置
4. 物料库存情况
5. 标准工时数据

**二、制定步骤**
1. **需求汇总**：汇总本周/本月销售订单
2. **产能核算**：
   - 设备产能：SMT线 8000点/天
   - 组装线：2000台/天/线
   - 测试线：3000台/天/线
3. **物料检查**：确认关键物料库存充足
4. **计划排产**：
   - 优先安排交期紧急订单
   - 同产品合并生产减少换线
   - 均衡分配各产线负荷
5. **计划发布**：系统发布后通知各车间

**三、调整规则**
- 计划变更需提前24小时通知
- 紧急插单需生产经理审批
- 每周一上午召开生产计划例会`,sources:[{type:"document",title:"生产计划管理规范",content:"计划变更需提前24小时通知，紧急插单需生产经理审批"},{type:"knowledge_graph",title:"产能数据",content:"SMT线 8000点/天，组装线 2000台/天/线"}]},SMT贴片工艺流程:{answer:`SMT贴片标准工艺流程：

**1. 上料**
- 核对物料编码、规格、数量
- 按站位表上料，首件确认
- 换料需双人复核签字

**2. 锡膏印刷**
- 锡膏型号：SAC305
- 印刷厚度：0.12-0.15mm
- 钢网清洁：每10块板自动清洗一次
- 印刷后目视检查，无连锡、少锡

**3. 贴片**
- 高速机贴装CHIP元件（0201/0402等）
- 泛用机贴装IC和异型元件
- 贴装精度：±0.05mm
- 每小时抽检贴装偏位

**4. 回流焊**
- 温度曲线：预热→恒温→回流→冷却
- 峰值温度：235-245℃
- 每日首件测温板验证

**5. AOI检测**
- 全自动光学检测
- 检测项目：缺件、偏位、立碑、连锡、少锡
- 不良品标记后人工复核

**6. 返修（如有）**
- 不良品标识、隔离
- 专人返修，记录返修原因

**关键控制点**：锡膏印刷质量、贴装精度、回流温度曲线`,sources:[{type:"document",title:"SMT作业指导书",content:"贴装精度±0.05mm，每日首件测温板"},{type:"knowledge_graph",title:"SMT流程",content:"上料→锡膏印刷→贴片→回流焊→AOI检测→返修"}]},产品质量检验标准:{answer:`智能手环成品质量检验标准：

**一、外观检验**
- 外壳无划伤、变形、色差
- 屏幕无划痕、亮点、暗斑（亮点≤1个且直径<0.1mm）
- 按键手感一致，无卡滞
- 表带扣合顺畅，无松动

**二、功能检验**
1. 触摸屏：全区域触摸灵敏，无死角
2. 按键：按压力度150-250g，寿命10万次
3. 心率监测：误差±2bpm
4. 血氧监测：误差±2%
5. 震动马达：震动强度3级可调
6. 蓝牙：连接距离≥10米
7. 充电：充电电流≥100mA，充满时间≤2小时
8. 防水：IP68级，5米水深30分钟

**三、可靠性测试（抽样）**
- 高低温循环：-20℃~60℃，10个循环
- 跌落测试：1.2米高度，6面各1次
- 按键寿命：10万次
- 表带拉力：50N持续10秒

**四、包装检验**
- 配件齐全（主机、充电线、说明书、保修卡）
- 包装完好，标识清晰
- 序列号与包装箱一致

**质量目标**：成品合格率≥99%，客户投诉率<0.1%`,sources:[{type:"document",title:"成品检验规范",content:"成品合格率≥99%，客户投诉率<0.1%"},{type:"knowledge_graph",title:"质量标准",content:"心率误差±2bpm，血氧误差±2%，防水IP68"}]}};function X(r){return r==="knowledge_graph"?"primary":"success"}function I(r){const u=d.value.length-1;M[u]||(M[u]=[]);const h=M[u].indexOf(r);h>-1?M[u].splice(h,1):M[u].push(r)}function A(){i.value=!i.value,i.value&&(x.value=0,we(()=>{S()}))}function S(){const r=document.querySelector(".chat-container");r&&(r.scrollTop=r.scrollHeight)}function R(r){const u=new Date(r),h=String(u.getHours()).padStart(2,"0"),b=String(u.getMinutes()).padStart(2,"0");return`${h}:${b}`}function se(){return 3e3+Math.random()*1e3}function O(r){const u=Object.keys(T);for(const h of u)if(r===h||r.includes(h)||h.includes(r))return T[h];return null}function B(){var m,N,P;if(!c.value.trim()||k.value)return;const r=c.value.trim();c.value="",d.value.push({role:"user",content:r,time:R(new Date)}),k.value=!0,S();const u=O(r),h=se();if(u){setTimeout(()=>{d.value.push({role:"assistant",content:u.answer,time:R(new Date),sources:u.sources}),k.value=!1,S()},h);return}const b=d.value.slice(0,-1).map(w=>({role:w.role,content:w.content}));ot({question:r,userId:((N=(m=l.userInfo)==null?void 0:m.id)==null?void 0:N.toString())||"anonymous",userName:((P=l.userInfo)==null?void 0:P.realName)||"用户",history:b}).then(w=>{var ee,te;const L=((ee=w==null?void 0:w.data)==null?void 0:ee.answer)||"抱歉，无法获取回答",Z=((te=w==null?void 0:w.data)==null?void 0:te.sources)||[];setTimeout(()=>{d.value.push({role:"assistant",content:L,time:R(new Date),sources:Z}),k.value=!1,S()},h)}).catch(()=>{setTimeout(()=>{d.value.push({role:"assistant",content:"抱歉，AI服务暂时不可用，请稍后再试。",time:R(new Date),sources:[]}),k.value=!1,S()},h)})}function oe(r){c.value=r,B()}function ae(r){r&&(i.value||(i.value=!0,x.value=0),c.value=r,we(()=>{B()}))}function K(r){var h;const u=(h=r==null?void 0:r.detail)==null?void 0:h.question;u&&ae(u)}async function J(){var r,u;try{const h=(u=(r=l.userInfo)==null?void 0:r.id)==null?void 0:u.toString();if(h){const b=await at({userId:h,limit:10}),m=(b==null?void 0:b.data)||[];d.value=m.map(N=>({role:N.answer?"assistant":"user",content:N.answer||N.question,time:R(N.createTime)}))}}catch{console.log("加载历史记录失败")}}return Ne(()=>{J(),window.addEventListener("ask-ai",K)}),Ce(()=>{window.removeEventListener("ask-ai",K)}),(r,u)=>{const h=y("el-icon"),b=y("el-tag");return a(),_("div",it,[t(be,{name:"slide-up"},{default:n(()=>[i.value?(a(),_("div",lt,[e("div",ct,[e("div",rt,[t(h,{size:20},{default:n(()=>[t(v(ue))]),_:1})]),u[1]||(u[1]=e("div",{class:"ai-title"},[e("span",{class:"title-text"},"AI生产助手"),e("span",{class:"title-desc"},"智能手环MES系统")],-1)),e("button",{class:"close-btn",onClick:A},[t(h,null,{default:n(()=>[t(v(Te))]),_:1})])]),e("div",ut,[e("div",dt,[t(Qe,{name:"msg-fade",tag:"div"},{default:n(()=>[(a(!0),_(W,null,z(d.value,(m,N)=>(a(),_("div",{key:N,class:j(["chat-item",{user:m.role==="user",ai:m.role==="assistant"}])},[e("div",ht,[m.role==="user"?(a(),C(h,{key:0,size:18},{default:n(()=>[t(v(he))]),_:1})):(a(),C(h,{key:1,size:18},{default:n(()=>[t(v(ue))]),_:1}))]),e("div",pt,[e("div",_t,f(m.content),1),m.sources&&m.sources.length>0?(a(),_("div",ft,[e("div",mt,[t(h,{size:12},{default:n(()=>[t(v(Ve))]),_:1}),u[2]||(u[2]=e("span",null,"引用来源",-1))]),e("div",gt,[(a(!0),_(W,null,z(m.sources,(P,w)=>{var L;return a(),_("div",{key:w,class:"source-item",onClick:Z=>I(w)},[t(b,{type:X(P.type),size:"small"},{default:n(()=>[F(f(P.type==="knowledge_graph"?"知识图谱":"文档"),1)]),_:2},1032,["type"]),e("span",wt,f(P.title),1),t(h,{size:10,class:j(["expand-icon",{expanded:(L=M[N])==null?void 0:L.includes(w)}])},{default:n(()=>[t(v(Oe))]),_:1},8,["class"])],8,vt)}),128)),(a(!0),_(W,null,z(m.sources,(P,w)=>{var L;return pe((a(),_("div",{key:"detail-"+w,class:"source-detail"},[e("pre",null,f(P.content),1)])),[[Be,(L=M[N])==null?void 0:L.includes(w)]])}),128))])])):$("",!0),e("div",yt,f(m.time),1)])],2))),128))]),_:1}),k.value?(a(),_("div",kt,[...u[3]||(u[3]=[e("div",{class:"loading-pulse"},null,-1),e("span",{class:"loading-text"},"AI正在思考...",-1)])])):$("",!0)])]),e("div",Et,[e("div",Mt,[pe(e("input",{"onUpdate:modelValue":u[0]||(u[0]=m=>c.value=m),type:"text",placeholder:"请输入您的问题...",class:"chat-input",onKeydown:Y(B,["enter"])},null,544),[[xe,c.value]]),e("button",{class:"send-btn",onClick:B,disabled:!c.value.trim()||k.value},[t(h,null,{default:n(()=>[t(v(_e))]),_:1})],8,St)]),e("div",Nt,[u[4]||(u[4]=e("span",{class:"quick-label"},"快速提问：",-1)),(a(),_(W,null,z(D,m=>e("button",{key:m,class:"quick-btn",onClick:N=>oe(m)},f(m),9,Ct)),64))])])])):$("",!0)]),_:1}),e("button",{class:"ai-toggle-btn",onClick:A},[t(h,{size:22},{default:n(()=>[t(v(ue))]),_:1}),i.value?$("",!0):(a(),_("span",Tt,f(x.value),1))])])}}},xt=Me(Ot,[["__scopeId","data-v-29395689"]]);class bt{constructor(){this.ws=null,this.mockMode=!1,this.mockTimer=null,this.listeners={},this.connected=!1,this.exceptionCount=0,this.messages=[],this.maxMessages=50}connect(){if(this.mockMode){this.connected=!0;return}const i=`${window.location.protocol==="https:"?"wss:":"ws:"}//${window.location.host}/ws/mes`;this.ws=new WebSocket(i),this.ws.onopen=()=>{this.connected=!0,console.log("WebSocket connected:",i)},this.ws.onmessage=c=>{try{const d=JSON.parse(c.data);this.handleMessage(d)}catch(d){console.error("WebSocket message parse error:",d)}},this.ws.onclose=()=>{this.connected=!1,console.log("WebSocket disconnected"),setTimeout(()=>this.connect(),3e3)},this.ws.onerror=c=>{this.connected=!1,console.error("WebSocket error:",c),setTimeout(()=>this.connect(),3e3)}}handleMessage(l){const{type:i,payload:c}=l;this.listeners[i]&&this.listeners[i].forEach(d=>d(c)),this.addMessage(i,c)}addMessage(l,i){const c=new Date,d={id:Date.now(),type:l,payload:i,time:c.toLocaleString("zh-CN",{hour12:!1}),timestamp:c.getTime(),read:!1};this.messages.unshift(d),this.messages.length>this.maxMessages&&(this.messages=this.messages.slice(0,this.maxMessages)),this.notifyMessageUpdate()}getMessages(){return this.messages}getUnreadCount(){return this.messages.filter(l=>!l.read).length}markAsRead(l){const i=this.messages.find(c=>c.id===l);i&&(i.read=!0)}markAllAsRead(){this.messages.forEach(l=>l.read=!0)}clearMessages(){this.messages=[],this.notifyMessageUpdate()}notifyMessageUpdate(){window.dispatchEvent(new CustomEvent("message-update",{detail:{count:this.getUnreadCount(),messages:this.messages.slice(0,10)}}))}on(l,i){this.listeners[l]||(this.listeners[l]=[]),this.listeners[l].push(i)}off(l,i){if(!this.listeners[l])return;const c=this.listeners[l].indexOf(i);c>-1&&this.listeners[l].splice(c,1)}startMockPush(){this.mockTimer&&clearInterval(this.mockTimer),this.mockTimer=setInterval(()=>{const l=Math.random();l<.4?this.mockExceptionPush():l<.9&&this.mockProgressPush()},8e3)}mockExceptionPush(){if(this.exceptionCount>=3)return;this.exceptionCount++;const l=[{type:1,typeName:"设备故障"},{type:2,typeName:"质量异常"},{type:3,typeName:"物料短缺"}],i=l[Math.floor(Math.random()*l.length)],c=["张操作员","李质检","王工人","赵仓管"],d=["WO-2024001","WO-2024004","WO-2024006"],k=["智能健康手环 Pro","智能运动手环 Lite"],x=["设备出现异常告警，需要紧急处理","发现不良品率上升，需排查原因","物料库存不足，可能导致停产","工艺参数波动，需确认调整"],M=new Date,T="EX-"+M.toISOString().slice(0,10).replace(/-/g,"")+String(Math.floor(Math.random()*900)+100),X={id:Date.now(),exceptionNo:T,workOrderNo:d[Math.floor(Math.random()*d.length)],productName:k[Math.floor(Math.random()*k.length)],exceptionType:i.type,exceptionTypeName:i.typeName,exceptionDesc:x[Math.floor(Math.random()*x.length)],status:0,reporter:c[Math.floor(Math.random()*c.length)],handler:"",createTime:M.toLocaleString("zh-CN",{hour12:!1}).replace(/\//g,"-"),imageUrl:""};this.handleMessage({type:"NEW_EXCEPTION",payload:X}),Ge({title:"新异常告警",message:`【${i.typeName}】${X.workOrderNo} - ${X.exceptionDesc.slice(0,20)}...`,type:"warning",duration:5e3,onClick:()=>{window.dispatchEvent(new CustomEvent("navigate-to-exception"))}})}mockProgressPush(){const l=[{id:1,workOrderNo:"WO-2024001",addQty:Math.floor(Math.random()*20)+5},{id:4,workOrderNo:"WO-2024004",addQty:Math.floor(Math.random()*15)+3},{id:6,workOrderNo:"WO-2024006",addQty:Math.floor(Math.random()*25)+8}],i=l[Math.floor(Math.random()*l.length)],c=["组装","测试","包装"],d=c[Math.floor(Math.random()*c.length)],k=["张工人","李工人","王工人"][Math.floor(Math.random()*3)],x={workOrderId:i.id,workOrderNo:i.workOrderNo,processName:d,addQuantity:i.addQty,worker:k,reportTime:new Date().toLocaleString("zh-CN",{hour12:!1}).replace(/\//g,"-")};this.handleMessage({type:"PROGRESS_UPDATE",payload:x})}simulateException(){this.mockExceptionPush()}simulateProgress(l,i){const d={workOrderId:l,workOrderNo:{1:"WO-2024001",4:"WO-2024004",6:"WO-2024006"}[l]||"WO-2024001",processName:"组装",addQuantity:i||10,worker:"模拟工人",reportTime:new Date().toLocaleString("zh-CN",{hour12:!1}).replace(/\//g,"-")};this.handleMessage({type:"PROGRESS_UPDATE",payload:d})}disconnect(){this.mockTimer&&(clearInterval(this.mockTimer),this.mockTimer=null),this.ws&&(this.ws.close(),this.ws=null),this.connected=!1}}const Q=new bt,Pt={class:"logo"},$t={class:"logo-icon"},Dt={key:0,class:"logo-text"},It={key:0,class:"sidebar-user"},At={class:"user-info"},Rt={class:"user-name"},Lt={class:"user-role"},Wt={class:"menu-section"},Ut={key:0,class:"section-title"},zt={class:"header-left"},Xt={key:0,class:"search-dropdown"},Qt=["onClick","onMouseenter"],Vt={class:"item-title"},Bt={class:"item-path"},Ht={key:1,class:"search-dropdown"},Gt={class:"header-right"},qt={class:"header-datetime"},Kt={class:"date-text"},Ft={class:"time-text"},Yt={class:"notification-trigger"},jt={key:0,class:"notification-badge"},Jt={class:"notification-dropdown"},Zt={class:"notification-header"},en={key:0,class:"notification-empty"},tn={key:1,class:"notification-list"},nn=["onClick"],sn={class:"notification-content"},on={class:"notification-title-text"},an={class:"notification-desc"},ln={class:"notification-time"},cn={key:2,class:"notification-footer"},rn={class:"user-dropdown-trigger"},un={class:"header-username"},dn={class:"content-wrapper"},hn={class:"breadcrumb-bar"},pn={class:"breadcrumb-left"},_n={class:"page-title"},fn={__name:"index",setup(V){const l=qe(),i=Ke(),c=Se(),d=E(!1),k=E(""),x=E("");let M=null;const D=E(0),T=E([]),X=E(null),I=E(""),A=E(!1),S=E(0),R=E(null),se=q(()=>{const s=[];return fe.value.forEach(g=>{g.children&&g.children.length>0?g.children.forEach(H=>{s.push(H)}):s.push(g)}),s}),O=q(()=>{if(!I.value.trim())return[];const s=I.value.toLowerCase().trim();return se.value.filter(o=>o.title.toLowerCase().includes(s)||o.path.toLowerCase().includes(s))});function B(){O.value.length!==0&&(S.value=(S.value+1)%O.value.length)}function oe(){O.value.length!==0&&(S.value=(S.value-1+O.value.length)%O.value.length)}function ae(){O.value.length>0&&K(O.value[S.value])}function K(s){i.push("/"+s.path),I.value="",A.value=!1,S.value=0}function J(s){R.value&&!R.value.contains(s.target)&&(A.value=!1)}function r(){const s=new Date,o=s.getFullYear(),g=String(s.getMonth()+1).padStart(2,"0"),H=String(s.getDate()).padStart(2,"0"),le=["周日","周一","周二","周三","周四","周五","周六"][s.getDay()];k.value=`${o}年${g}月${H}日 ${le}`;const ce=String(s.getHours()).padStart(2,"0"),re=String(s.getMinutes()).padStart(2,"0"),ne=String(s.getSeconds()).padStart(2,"0");x.value=`${ce}:${re}:${ne}`}Ne(()=>{r(),M=setInterval(r,1e3),document.addEventListener("click",J),u()}),Ce(()=>{M&&clearInterval(M),document.removeEventListener("click",J),document.removeEventListener("message-update",h)});function u(){T.value=Q.getMessages(),D.value=Q.getUnreadCount(),document.addEventListener("message-update",h)}function h(s){T.value=s.detail.messages,D.value=s.detail.count}function b(s){s==="mark-all-read"&&P()}function m(s){s&&(T.value=Q.getMessages().slice(0,10))}function N(s){Q.markAsRead(s.id),D.value=Q.getUnreadCount(),s.type==="NEW_EXCEPTION"?i.push("/exception-workbench"):s.type==="PROGRESS_UPDATE"&&i.push("/production-progress")}function P(){Q.markAllAsRead(),D.value=0,T.value=Q.getMessages().slice(0,10),ke.success("已全部标记为已读")}function w(){i.push("/exception-workbench")}function L(s){return{NEW_EXCEPTION:Te,EXCEPTION_HANDLED:_e,EXCEPTION_RESOLVED:_e,PROGRESS_UPDATE:et}[s]||de}function Z(s){return{NEW_EXCEPTION:"icon-warning",EXCEPTION_HANDLED:"icon-info",EXCEPTION_RESOLVED:"icon-success",PROGRESS_UPDATE:"icon-success"}[s]||"icon-info"}function ee(s){return{NEW_EXCEPTION:"新异常告警",EXCEPTION_HANDLED:"异常已处理",EXCEPTION_RESOLVED:"异常已解决",PROGRESS_UPDATE:"生产进度更新"}[s]||"系统消息"}function te(s){const o=s.payload;return s.type==="NEW_EXCEPTION"?`【${["","设备故障","质量异常","物料短缺","工艺问题","人员异常","其他"][o.exceptionType]||o.exceptionTypeName||"异常"}】${o.workOrderNo} - ${(o.exceptionDesc||"").slice(0,30)}${(o.exceptionDesc||"").length>30?"...":""}`:s.type==="EXCEPTION_HANDLED"?`异常 ${o.exceptionNo} 已开始处理`:s.type==="EXCEPTION_RESOLVED"?`异常 ${o.exceptionNo} 已解决`:s.type==="PROGRESS_UPDATE"?`工单 ${o.workOrderNo} 在 ${o.processName} 工序新增报工 ${o.addQuantity} 件`:"查看详情"}const fe=q(()=>{var o;const s=((o=c.userInfo)==null?void 0:o.role)||"admin";return nt(s)}),Pe=q(()=>{var o;const s=((o=c.userInfo)==null?void 0:o.role)||"admin";return st[s]||s}),$e=q(()=>l.path),me=q(()=>l.meta.title||"");function De(){d.value=!d.value}function Ie(s){s==="logout"&&tt.confirm("确定要退出登录吗？","提示",{confirmButtonText:"确定",cancelButtonText:"取消",type:"warning"}).then(()=>{c.logout(),ke.success("退出成功"),i.push("/login")}).catch(()=>{})}return(s,o)=>{const g=y("el-icon"),H=y("el-avatar"),ie=y("el-menu-item"),le=y("el-sub-menu"),ce=y("el-menu"),re=y("el-aside"),ne=y("el-dropdown"),Ae=y("el-dropdown-item"),Re=y("el-dropdown-menu"),Le=y("el-header"),ge=y("el-breadcrumb-item"),We=y("el-breadcrumb"),Ue=y("router-view"),ze=y("el-main"),ve=y("el-container");return a(),C(ve,{class:"layout-container"},{default:n(()=>[t(re,{width:d.value?"64px":"240px",class:"sidebar"},{default:n(()=>[e("div",Pt,[e("div",$t,[t(g,{size:20},{default:n(()=>[t(v(Fe))]),_:1})]),d.value?$("",!0):(a(),_("span",Dt,"智能手环MES"))]),d.value?$("",!0):(a(),_("div",It,[t(H,{size:36,class:"user-avatar"},{default:n(()=>[t(g,{size:18},{default:n(()=>[t(v(he))]),_:1})]),_:1}),e("div",At,[e("div",Rt,f(v(c).userInfo.realName||"管理员"),1),e("div",Lt,f(Pe.value),1)])])),e("div",Wt,[d.value?$("",!0):(a(),_("div",Ut,"导航菜单")),t(ce,{"default-active":$e.value,collapse:d.value,"collapse-transition":!1,"unique-opened":!0,router:"",class:"sidebar-menu"},{default:n(()=>[(a(!0),_(W,null,z(fe.value,p=>(a(),_(W,{key:p.path||p.title},[p.children&&p.children.length>0?(a(),C(le,{key:0,index:p.title},{title:n(()=>[t(g,null,{default:n(()=>[(a(),C(G(p.icon)))]),_:2},1024),e("span",null,f(p.title),1)]),default:n(()=>[(a(!0),_(W,null,z(p.children,U=>(a(),C(ie,{key:U.path,index:"/"+U.path},{title:n(()=>[F(f(U.title),1)]),default:n(()=>[t(g,null,{default:n(()=>[(a(),C(G(U.icon)))]),_:2},1024)]),_:2},1032,["index"]))),128))]),_:2},1032,["index"])):(a(),C(ie,{key:1,index:"/"+p.path},{title:n(()=>[F(f(p.title),1)]),default:n(()=>[t(g,null,{default:n(()=>[(a(),C(G(p.icon)))]),_:2},1024)]),_:2},1032,["index"]))],64))),128))]),_:1},8,["default-active","collapse"])])]),_:1},8,["width"]),t(ve,{class:"main-container"},{default:n(()=>[t(Le,{class:"header"},{default:n(()=>[e("div",zt,[t(g,{class:"collapse-btn",onClick:De},{default:n(()=>[d.value?(a(),C(v(je),{key:1})):(a(),C(v(Ye),{key:0}))]),_:1}),e("div",{class:"header-search",ref_key:"searchRef",ref:R},[t(g,{class:"search-icon"},{default:n(()=>[t(v(Je))]),_:1}),pe(e("input",{type:"text","onUpdate:modelValue":o[0]||(o[0]=p=>I.value=p),placeholder:"搜索菜单、功能...",class:"search-input",onFocus:o[1]||(o[1]=p=>A.value=!0),onKeydown:[Y(B,["down"]),Y(oe,["up"]),Y(ae,["enter"]),o[2]||(o[2]=Y(p=>A.value=!1,["esc"]))]},null,544),[[xe,I.value]]),A.value&&I.value&&O.value.length>0?(a(),_("div",Xt,[(a(!0),_(W,null,z(O.value,(p,U)=>(a(),_("div",{key:p.path,class:j(["search-item",{active:S.value===U}]),onClick:Xe=>K(p),onMouseenter:Xe=>S.value=U},[t(g,{class:"item-icon"},{default:n(()=>[(a(),C(G(p.icon)))]),_:2},1024),e("span",Vt,f(p.title),1),e("span",Bt,"/"+f(p.path),1)],42,Qt))),128))])):$("",!0),A.value&&I.value&&O.value.length===0?(a(),_("div",Ht,[...o[3]||(o[3]=[e("div",{class:"search-empty"},"没有找到相关菜单",-1)])])):$("",!0)],512)]),e("div",Gt,[e("div",qt,[e("span",Kt,f(k.value),1),e("span",Ft,f(x.value),1)]),t(ne,{ref_key:"notificationRef",ref:X,onCommand:b,trigger:"click",onVisibleChange:m},{dropdown:n(()=>[e("div",Jt,[e("div",Zt,[o[4]||(o[4]=e("span",{class:"notification-title"},"实时消息",-1)),e("span",{class:"notification-action",onClick:ye(P,["stop"])},"全部已读")]),T.value.length===0?(a(),_("div",en,[t(g,{size:48,class:"empty-icon"},{default:n(()=>[t(v(de))]),_:1}),o[5]||(o[5]=e("span",null,"暂无消息",-1))])):(a(),_("div",tn,[(a(!0),_(W,null,z(T.value,p=>(a(),_("div",{key:p.id,class:j(["notification-item",{"notification-unread":!p.read}]),onClick:U=>N(p)},[e("div",{class:j(["notification-icon-wrapper",Z(p.type)])},[t(g,null,{default:n(()=>[(a(),C(G(L(p.type))))]),_:2},1024)],2),e("div",sn,[e("div",on,f(ee(p.type)),1),e("div",an,f(te(p)),1),e("div",ln,f(p.time),1)])],10,nn))),128))])),T.value.length>0?(a(),_("div",cn,[e("span",{class:"notification-action",onClick:ye(w,["stop"])},"查看更多")])):$("",!0)])]),default:n(()=>[e("div",Yt,[t(g,{class:"notification-icon"},{default:n(()=>[t(v(de))]),_:1}),D.value>0?(a(),_("span",jt,f(D.value>99?"99+":D.value),1)):$("",!0)])]),_:1},512),t(ne,{onCommand:Ie,trigger:"click"},{dropdown:n(()=>[t(Re,{class:"user-dropdown"},{default:n(()=>[t(Ae,{command:"logout"},{default:n(()=>[t(g,null,{default:n(()=>[t(v(Ze))]),_:1}),o[6]||(o[6]=e("span",null,"退出登录",-1))]),_:1})]),_:1})]),default:n(()=>[e("div",rn,[t(H,{size:32,class:"header-avatar"},{default:n(()=>[t(g,{size:16},{default:n(()=>[t(v(he))]),_:1})]),_:1}),e("span",un,f(v(c).userInfo.realName||"管理员"),1),t(g,{class:"arrow-icon"},{default:n(()=>[t(v(Oe))]),_:1})])]),_:1})])]),_:1}),e("div",dn,[e("div",hn,[e("div",pn,[e("h2",_n,f(me.value||"首页看板"),1),t(We,{separator:"/",class:"breadcrumb"},{default:n(()=>[t(ge,{to:{path:"/dashboard"}},{default:n(()=>[...o[7]||(o[7]=[F("首页",-1)])]),_:1}),t(ge,null,{default:n(()=>[F(f(me.value),1)]),_:1})]),_:1})])]),t(ze,{class:"main-content"},{default:n(()=>[t(Ue,null,{default:n(({Component:p})=>[t(be,{name:"fade",mode:"out-in"},{default:n(()=>[(a(),C(G(p)))]),_:2},1024)]),_:1})]),_:1})])]),_:1}),t(xt)]),_:1})}}},gn=Me(fn,[["__scopeId","data-v-ac620fb1"]]);export{gn as default};
