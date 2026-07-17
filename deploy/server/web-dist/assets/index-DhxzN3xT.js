import{y as Ee,_ as Se,u as Ce,z as Te,B as be,e as m,o as a,c as r,b as n,w as t,a as e,d as f,O as re,P as Ne,Q as Be,F as R,r as z,n as Y,N as E,i as _e,t as d,R as He,D as W,S as $e,G as pe,U as qe,I as $,k as F,L as xe,V as fe,T as Ie,f as g,A as ye,s as Ge,W as Ke,g as Qe,h as We,X as q,Y as Fe,Z as Ye,$ as je,a0 as de,a1 as ke,a2 as Ze,K as G,E as we,a3 as Je,a4 as en,a5 as nn,a6 as tn}from"./index-B4mkkY3O.js";import{w as U}from"./websocket-Bnmr3Dnp.js";function sn(K){return Ee({url:"/ai/chat",method:"post",data:K})}function on(K){return Ee({url:"/ai/history/recent",method:"get",params:K})}const an={class:"ai-assistant"},ln={key:0,class:"ai-panel"},cn={class:"ai-header"},un={class:"ai-logo"},rn={class:"ai-body"},dn={class:"chat-container"},_n={class:"chat-avatar"},pn={class:"chat-bubble"},fn={class:"chat-content"},vn={key:0,class:"chat-sources"},hn={class:"sources-title"},mn={class:"sources-list"},gn=["onClick"],yn={class:"source-title"},kn={class:"chat-time"},wn={key:0,class:"loading-indicator"},En={class:"ai-footer"},Sn={class:"input-wrapper"},Cn=["disabled"],Tn={class:"quick-questions"},bn=["onClick"],Nn={key:0,class:"badge"},$n={__name:"AiAssistant",setup(K){const X=Ce(),S=g(!1),y=g(""),h=g([]),x=g(!1),V=g(0),T=Ge({}),M=["焊接温度标准是多少？","屏幕贴合有气泡怎么处理？","如何进行报工操作？","防水测试怎么做？","工单状态有哪些？","生产计划如何制定？","SMT贴片工艺流程","产品质量检验标准"],I={"焊接温度标准是多少？":{answer:`智能手环生产中，各工序焊接温度标准如下：

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

**质量目标**：成品合格率≥99%，客户投诉率<0.1%`,sources:[{type:"document",title:"成品检验规范",content:"成品合格率≥99%，客户投诉率<0.1%"},{type:"knowledge_graph",title:"质量标准",content:"心率误差±2bpm，血氧误差±2%，防水IP68"}]}};function te(l){return l==="knowledge_graph"?"primary":"success"}function D(l){const i=h.value.length-1;T[i]||(T[i]=[]);const c=T[i].indexOf(l);c>-1?T[i].splice(c,1):T[i].push(l)}function P(){S.value=!S.value,S.value&&(V.value=0,ye(()=>{k()}))}function k(){const l=document.querySelector(".chat-container");l&&(l.scrollTop=l.scrollHeight)}function O(l){const i=new Date(l),c=String(i.getHours()).padStart(2,"0"),b=String(i.getMinutes()).padStart(2,"0");return`${c}:${b}`}function se(){return 3e3+Math.random()*1e3}function C(l){const i=Object.keys(I);for(const c of i)if(l===c||l.includes(c)||c.includes(l))return I[c];return null}function B(){var _,w,N;if(!y.value.trim()||x.value)return;const l=y.value.trim();y.value="",h.value.push({role:"user",content:l,time:O(new Date)}),x.value=!0,k();const i=C(l),c=se();if(i){setTimeout(()=>{h.value.push({role:"assistant",content:i.answer,time:O(new Date),sources:i.sources}),x.value=!1,k()},c);return}const b=h.value.slice(0,-1).map(v=>({role:v.role,content:v.content}));sn({question:l,userId:((w=(_=X.userInfo)==null?void 0:_.id)==null?void 0:w.toString())||"anonymous",userName:((N=X.userInfo)==null?void 0:N.realName)||"用户",history:b}).then(v=>{var J,ee;const A=((J=v==null?void 0:v.data)==null?void 0:J.answer)||"抱歉，无法获取回答",Z=((ee=v==null?void 0:v.data)==null?void 0:ee.sources)||[];setTimeout(()=>{h.value.push({role:"assistant",content:A,time:O(new Date),sources:Z}),x.value=!1,k()},c)}).catch(()=>{setTimeout(()=>{h.value.push({role:"assistant",content:"抱歉，AI服务暂时不可用，请稍后再试。",time:O(new Date),sources:[]}),x.value=!1,k()},c)})}function oe(l){y.value=l,B()}function ae(l){l&&(S.value||(S.value=!0,V.value=0),y.value=l,ye(()=>{B()}))}function Q(l){var c;const i=(c=l==null?void 0:l.detail)==null?void 0:c.question;i&&ae(i)}async function j(){var l,i;try{const c=(i=(l=X.userInfo)==null?void 0:l.id)==null?void 0:i.toString();if(c){const b=await on({userId:c,limit:10}),_=(b==null?void 0:b.data)||[];h.value=_.map(w=>({role:w.answer?"assistant":"user",content:w.answer||w.question,time:O(w.createTime)}))}}catch{console.log("加载历史记录失败")}}return Te(()=>{j(),window.addEventListener("ask-ai",Q)}),be(()=>{window.removeEventListener("ask-ai",Q)}),(l,i)=>{const c=m("el-icon"),b=m("el-tag");return a(),r("div",an,[n(Ie,{name:"slide-up"},{default:t(()=>[S.value?(a(),r("div",ln,[e("div",cn,[e("div",un,[n(c,{size:20},{default:t(()=>[n(f(re))]),_:1})]),i[1]||(i[1]=e("div",{class:"ai-title"},[e("span",{class:"title-text"},"AI生产助手"),e("span",{class:"title-desc"},"智能手环MES系统")],-1)),e("button",{class:"close-btn",onClick:P},[n(c,null,{default:t(()=>[n(f(Ne))]),_:1})])]),e("div",rn,[e("div",dn,[n(Be,{name:"msg-fade",tag:"div"},{default:t(()=>[(a(!0),r(R,null,z(h.value,(_,w)=>(a(),r("div",{key:w,class:Y(["chat-item",{user:_.role==="user",ai:_.role==="assistant"}])},[e("div",_n,[_.role==="user"?(a(),E(c,{key:0,size:18},{default:t(()=>[n(f(_e))]),_:1})):(a(),E(c,{key:1,size:18},{default:t(()=>[n(f(re))]),_:1}))]),e("div",pn,[e("div",fn,d(_.content),1),_.sources&&_.sources.length>0?(a(),r("div",vn,[e("div",hn,[n(c,{size:12},{default:t(()=>[n(f(He))]),_:1}),i[2]||(i[2]=e("span",null,"引用来源",-1))]),e("div",mn,[(a(!0),r(R,null,z(_.sources,(N,v)=>{var A;return a(),r("div",{key:v,class:"source-item",onClick:Z=>D(v)},[n(b,{type:te(N.type),size:"small"},{default:t(()=>[W(d(N.type==="knowledge_graph"?"知识图谱":"文档"),1)]),_:2},1032,["type"]),e("span",yn,d(N.title),1),n(c,{size:10,class:Y(["expand-icon",{expanded:(A=T[w])==null?void 0:A.includes(v)}])},{default:t(()=>[n(f($e))]),_:1},8,["class"])],8,gn)}),128)),(a(!0),r(R,null,z(_.sources,(N,v)=>{var A;return pe((a(),r("div",{key:"detail-"+v,class:"source-detail"},[e("pre",null,d(N.content),1)])),[[qe,(A=T[w])==null?void 0:A.includes(v)]])}),128))])])):$("",!0),e("div",kn,d(_.time),1)])],2))),128))]),_:1}),x.value?(a(),r("div",wn,[...i[3]||(i[3]=[e("div",{class:"loading-pulse"},null,-1),e("span",{class:"loading-text"},"AI正在思考...",-1)])])):$("",!0)])]),e("div",En,[e("div",Sn,[pe(e("input",{"onUpdate:modelValue":i[0]||(i[0]=_=>y.value=_),type:"text",placeholder:"请输入您的问题...",class:"chat-input",onKeydown:F(B,["enter"])},null,544),[[xe,y.value]]),e("button",{class:"send-btn",onClick:B,disabled:!y.value.trim()||x.value},[n(c,null,{default:t(()=>[n(f(fe))]),_:1})],8,Cn)]),e("div",Tn,[i[4]||(i[4]=e("span",{class:"quick-label"},"快速提问：",-1)),(a(),r(R,null,z(M,_=>e("button",{key:_,class:"quick-btn",onClick:w=>oe(_)},d(_),9,bn)),64))])])])):$("",!0)]),_:1}),e("button",{class:"ai-toggle-btn",onClick:P},[n(c,{size:22},{default:t(()=>[n(f(re))]),_:1}),S.value?$("",!0):(a(),r("span",Nn,d(V.value),1))])])}}},xn=Se($n,[["__scopeId","data-v-29395689"]]),In={class:"logo"},Mn={class:"logo-icon"},Dn={key:0,class:"logo-text"},Pn={key:0,class:"sidebar-user"},On={class:"user-info"},An={class:"user-name"},Rn={class:"user-role"},Ln={class:"menu-section"},zn={key:0,class:"section-title"},Un={class:"header-left"},Xn={key:0,class:"search-dropdown"},Vn=["onClick","onMouseenter"],Bn={class:"item-title"},Hn={class:"item-path"},qn={key:1,class:"search-dropdown"},Gn={class:"header-right"},Kn={class:"header-datetime"},Qn={class:"date-text"},Wn={class:"time-text"},Fn={class:"notification-trigger"},Yn={key:0,class:"notification-badge"},jn={class:"notification-dropdown"},Zn={class:"notification-header"},Jn={key:0,class:"notification-empty"},et={key:1,class:"notification-list"},nt=["onClick"],tt={class:"notification-content"},st={class:"notification-title-text"},ot={class:"notification-desc"},at={class:"notification-time"},lt={key:2,class:"notification-footer"},it={class:"user-dropdown-trigger"},ct={class:"header-username"},ut={class:"content-wrapper"},rt={class:"breadcrumb-bar"},dt={class:"breadcrumb-left"},_t={class:"page-title"},pt={__name:"index",setup(K){const X=Ke(),S=Qe(),y=Ce(),h=g(!1),x=g(""),V=g("");let T=null;const M=g(0),I=g([]),te=g(null),D=g(""),P=g(!1),k=g(0),O=g(null),se=G(()=>{const s=[];return ve.value.forEach(p=>{p.children&&p.children.length>0?p.children.forEach(H=>{s.push(H)}):s.push(p)}),s}),C=G(()=>{if(!D.value.trim())return[];const s=D.value.toLowerCase().trim();return se.value.filter(o=>o.title.toLowerCase().includes(s)||o.path.toLowerCase().includes(s))});function B(){C.value.length!==0&&(k.value=(k.value+1)%C.value.length)}function oe(){C.value.length!==0&&(k.value=(k.value-1+C.value.length)%C.value.length)}function ae(){C.value.length>0&&Q(C.value[k.value])}function Q(s){S.push("/"+s.path),D.value="",P.value=!1,k.value=0}function j(s){O.value&&!O.value.contains(s.target)&&(P.value=!1)}function l(){const s=new Date,o=s.getFullYear(),p=String(s.getMonth()+1).padStart(2,"0"),H=String(s.getDate()).padStart(2,"0"),ie=["周日","周一","周二","周三","周四","周五","周六"][s.getDay()];x.value=`${o}年${p}月${H}日 ${ie}`;const ce=String(s.getHours()).padStart(2,"0"),ue=String(s.getMinutes()).padStart(2,"0"),ne=String(s.getSeconds()).padStart(2,"0");V.value=`${ce}:${ue}:${ne}`}Te(()=>{l(),T=setInterval(l,1e3),document.addEventListener("click",j),i()}),be(()=>{T&&clearInterval(T),document.removeEventListener("click",j),document.removeEventListener("message-update",c)});function i(){I.value=U.getMessages(),M.value=U.getUnreadCount(),document.addEventListener("message-update",c)}function c(s){I.value=s.detail.messages,M.value=s.detail.count}function b(s){s==="mark-all-read"&&N()}function _(s){s&&(I.value=U.getMessages().slice(0,10))}function w(s){U.markAsRead(s.id),M.value=U.getUnreadCount(),s.type==="NEW_EXCEPTION"?S.push("/exception-workbench"):s.type==="PROGRESS_UPDATE"&&S.push("/production-progress")}function N(){U.markAllAsRead(),M.value=0,I.value=U.getMessages().slice(0,10),we.success("已全部标记为已读")}function v(){S.push("/exception-workbench")}function A(s){return{NEW_EXCEPTION:Ne,EXCEPTION_HANDLED:fe,EXCEPTION_RESOLVED:fe,PROGRESS_UPDATE:Je}[s]||de}function Z(s){return{NEW_EXCEPTION:"icon-warning",EXCEPTION_HANDLED:"icon-info",EXCEPTION_RESOLVED:"icon-success",PROGRESS_UPDATE:"icon-success"}[s]||"icon-info"}function J(s){return{NEW_EXCEPTION:"新异常告警",EXCEPTION_HANDLED:"异常已处理",EXCEPTION_RESOLVED:"异常已解决",PROGRESS_UPDATE:"生产进度更新"}[s]||"系统消息"}function ee(s){const o=s.payload;return s.type==="NEW_EXCEPTION"?`【${["","设备故障","质量异常","物料短缺","工艺问题","人员异常","其他"][o.exceptionType]||o.exceptionTypeName||"异常"}】${o.workOrderNo} - ${(o.exceptionDesc||"").slice(0,30)}${(o.exceptionDesc||"").length>30?"...":""}`:s.type==="EXCEPTION_HANDLED"?`异常 ${o.exceptionNo} 已开始处理`:s.type==="EXCEPTION_RESOLVED"?`异常 ${o.exceptionNo} 已解决`:s.type==="PROGRESS_UPDATE"?`工单 ${o.workOrderNo} 在 ${o.processName} 工序新增报工 ${o.addQuantity} 件`:"查看详情"}const ve=G(()=>{var o;const s=((o=y.userInfo)==null?void 0:o.role)||"admin";return nn(s)}),Me=G(()=>{var o;const s=((o=y.userInfo)==null?void 0:o.role)||"admin";return tn[s]||s}),De=G(()=>X.path),he=G(()=>X.meta.title||"");function Pe(){h.value=!h.value}function Oe(s){s==="logout"&&en.confirm("确定要退出登录吗？","提示",{confirmButtonText:"确定",cancelButtonText:"取消",type:"warning"}).then(()=>{y.logout(),we.success("退出成功"),S.push("/login")}).catch(()=>{})}return(s,o)=>{const p=m("el-icon"),H=m("el-avatar"),le=m("el-menu-item"),ie=m("el-sub-menu"),ce=m("el-menu"),ue=m("el-aside"),ne=m("el-dropdown"),Ae=m("el-dropdown-item"),Re=m("el-dropdown-menu"),Le=m("el-header"),me=m("el-breadcrumb-item"),ze=m("el-breadcrumb"),Ue=m("router-view"),Xe=m("el-main"),ge=m("el-container");return a(),E(ge,{class:"layout-container"},{default:t(()=>[n(ue,{width:h.value?"64px":"240px",class:"sidebar"},{default:t(()=>[e("div",In,[e("div",Mn,[n(p,{size:20},{default:t(()=>[n(f(We))]),_:1})]),h.value?$("",!0):(a(),r("span",Dn,"智能手环MES"))]),h.value?$("",!0):(a(),r("div",Pn,[n(H,{size:36,class:"user-avatar"},{default:t(()=>[n(p,{size:18},{default:t(()=>[n(f(_e))]),_:1})]),_:1}),e("div",On,[e("div",An,d(f(y).userInfo.realName||"管理员"),1),e("div",Rn,d(Me.value),1)])])),e("div",Ln,[h.value?$("",!0):(a(),r("div",zn,"导航菜单")),n(ce,{"default-active":De.value,collapse:h.value,"collapse-transition":!1,"unique-opened":!0,router:"",class:"sidebar-menu"},{default:t(()=>[(a(!0),r(R,null,z(ve.value,u=>(a(),r(R,{key:u.path||u.title},[u.children&&u.children.length>0?(a(),E(ie,{key:0,index:u.title},{title:t(()=>[n(p,null,{default:t(()=>[(a(),E(q(u.icon)))]),_:2},1024),e("span",null,d(u.title),1)]),default:t(()=>[(a(!0),r(R,null,z(u.children,L=>(a(),E(le,{key:L.path,index:"/"+L.path},{title:t(()=>[W(d(L.title),1)]),default:t(()=>[n(p,null,{default:t(()=>[(a(),E(q(L.icon)))]),_:2},1024)]),_:2},1032,["index"]))),128))]),_:2},1032,["index"])):(a(),E(le,{key:1,index:"/"+u.path},{title:t(()=>[W(d(u.title),1)]),default:t(()=>[n(p,null,{default:t(()=>[(a(),E(q(u.icon)))]),_:2},1024)]),_:2},1032,["index"]))],64))),128))]),_:1},8,["default-active","collapse"])])]),_:1},8,["width"]),n(ge,{class:"main-container"},{default:t(()=>[n(Le,{class:"header"},{default:t(()=>[e("div",Un,[n(p,{class:"collapse-btn",onClick:Pe},{default:t(()=>[h.value?(a(),E(f(Ye),{key:1})):(a(),E(f(Fe),{key:0}))]),_:1}),e("div",{class:"header-search",ref_key:"searchRef",ref:O},[n(p,{class:"search-icon"},{default:t(()=>[n(f(je))]),_:1}),pe(e("input",{type:"text","onUpdate:modelValue":o[0]||(o[0]=u=>D.value=u),placeholder:"搜索菜单、功能...",class:"search-input",onFocus:o[1]||(o[1]=u=>P.value=!0),onKeydown:[F(B,["down"]),F(oe,["up"]),F(ae,["enter"]),o[2]||(o[2]=F(u=>P.value=!1,["esc"]))]},null,544),[[xe,D.value]]),P.value&&D.value&&C.value.length>0?(a(),r("div",Xn,[(a(!0),r(R,null,z(C.value,(u,L)=>(a(),r("div",{key:u.path,class:Y(["search-item",{active:k.value===L}]),onClick:Ve=>Q(u),onMouseenter:Ve=>k.value=L},[n(p,{class:"item-icon"},{default:t(()=>[(a(),E(q(u.icon)))]),_:2},1024),e("span",Bn,d(u.title),1),e("span",Hn,"/"+d(u.path),1)],42,Vn))),128))])):$("",!0),P.value&&D.value&&C.value.length===0?(a(),r("div",qn,[...o[3]||(o[3]=[e("div",{class:"search-empty"},"没有找到相关菜单",-1)])])):$("",!0)],512)]),e("div",Gn,[e("div",Kn,[e("span",Qn,d(x.value),1),e("span",Wn,d(V.value),1)]),n(ne,{ref_key:"notificationRef",ref:te,onCommand:b,trigger:"click",onVisibleChange:_},{dropdown:t(()=>[e("div",jn,[e("div",Zn,[o[4]||(o[4]=e("span",{class:"notification-title"},"实时消息",-1)),e("span",{class:"notification-action",onClick:ke(N,["stop"])},"全部已读")]),I.value.length===0?(a(),r("div",Jn,[n(p,{size:48,class:"empty-icon"},{default:t(()=>[n(f(de))]),_:1}),o[5]||(o[5]=e("span",null,"暂无消息",-1))])):(a(),r("div",et,[(a(!0),r(R,null,z(I.value,u=>(a(),r("div",{key:u.id,class:Y(["notification-item",{"notification-unread":!u.read}]),onClick:L=>w(u)},[e("div",{class:Y(["notification-icon-wrapper",Z(u.type)])},[n(p,null,{default:t(()=>[(a(),E(q(A(u.type))))]),_:2},1024)],2),e("div",tt,[e("div",st,d(J(u.type)),1),e("div",ot,d(ee(u)),1),e("div",at,d(u.time),1)])],10,nt))),128))])),I.value.length>0?(a(),r("div",lt,[e("span",{class:"notification-action",onClick:ke(v,["stop"])},"查看更多")])):$("",!0)])]),default:t(()=>[e("div",Fn,[n(p,{class:"notification-icon"},{default:t(()=>[n(f(de))]),_:1}),M.value>0?(a(),r("span",Yn,d(M.value>99?"99+":M.value),1)):$("",!0)])]),_:1},512),n(ne,{onCommand:Oe,trigger:"click"},{dropdown:t(()=>[n(Re,{class:"user-dropdown"},{default:t(()=>[n(Ae,{command:"logout"},{default:t(()=>[n(p,null,{default:t(()=>[n(f(Ze))]),_:1}),o[6]||(o[6]=e("span",null,"退出登录",-1))]),_:1})]),_:1})]),default:t(()=>[e("div",it,[n(H,{size:32,class:"header-avatar"},{default:t(()=>[n(p,{size:16},{default:t(()=>[n(f(_e))]),_:1})]),_:1}),e("span",ct,d(f(y).userInfo.realName||"管理员"),1),n(p,{class:"arrow-icon"},{default:t(()=>[n(f($e))]),_:1})])]),_:1})])]),_:1}),e("div",ut,[e("div",rt,[e("div",dt,[e("h2",_t,d(he.value||"首页看板"),1),n(ze,{separator:"/",class:"breadcrumb"},{default:t(()=>[n(me,{to:{path:"/dashboard"}},{default:t(()=>[...o[7]||(o[7]=[W("首页",-1)])]),_:1}),n(me,null,{default:t(()=>[W(d(he.value),1)]),_:1})]),_:1})])]),n(Xe,{class:"main-content"},{default:t(()=>[n(Ue,null,{default:t(({Component:u})=>[n(Ie,{name:"fade",mode:"out-in"},{default:t(()=>[(a(),E(q(u)))]),_:2},1024)]),_:1})]),_:1})])]),_:1}),n(xn)]),_:1})}}},ht=Se(pt,[["__scopeId","data-v-ac620fb1"]]);export{ht as default};
