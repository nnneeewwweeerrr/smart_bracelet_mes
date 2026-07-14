package com.mes.aspect;

import com.mes.entity.ExceptionRecord;
import com.mes.entity.WorkOrder;
import com.mes.service.ExceptionRecordService;
import com.mes.service.WorkOrderService;
import com.mes.websocket.WebSocketPushService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Aspect
@Component
public class WebSocketPushAspect {

    @Autowired
    private WebSocketPushService webSocketPushService;

    @Autowired
    private ExceptionRecordService exceptionRecordService;

    @Autowired
    private WorkOrderService workOrderService;

    @Pointcut("execution(* com.mes.service.impl.ExceptionRecordServiceImpl.save(..))")
    public void exceptionSavePointcut() {}

    @Pointcut("execution(* com.mes.service.impl.ExceptionRecordServiceImpl.handleException(..))")
    public void exceptionHandlePointcut() {}

    @Pointcut("execution(* com.mes.service.impl.ExceptionRecordServiceImpl.resolveException(..))")
    public void exceptionResolvePointcut() {}

    @Pointcut("execution(* com.mes.controller.ProductionReportController.save(..))")
    public void productionReportSavePointcut() {}

    @AfterReturning(pointcut = "exceptionSavePointcut()", returning = "result")
    public void afterExceptionSave(JoinPoint joinPoint, Object result) {
        if (result instanceof Boolean && (Boolean) result) {
            Object[] args = joinPoint.getArgs();
            if (args.length > 0 && args[0] instanceof ExceptionRecord) {
                ExceptionRecord record = (ExceptionRecord) args[0];
                webSocketPushService.pushNewException(
                        record.getId(),
                        record.getExceptionNo(),
                        record.getExceptionType(),
                        record.getDescription()
                );
                log.info("异常上报成功，已推送WebSocket消息: {}", record.getExceptionNo());
            }
        }
    }

    @AfterReturning(pointcut = "exceptionHandlePointcut()", returning = "result")
    public void afterExceptionHandle(JoinPoint joinPoint, Object result) {
        if (result instanceof Boolean && (Boolean) result) {
            Object[] args = joinPoint.getArgs();
            if (args.length > 0 && args[0] instanceof Long) {
                Long id = (Long) args[0];
                webSocketPushService.pushExceptionHandled(id, 1, "系统");
                log.info("异常处理中，已推送WebSocket消息: id={}", id);
            }
        }
    }

    @AfterReturning(pointcut = "exceptionResolvePointcut()", returning = "result")
    public void afterExceptionResolve(JoinPoint joinPoint, Object result) {
        if (result instanceof Boolean && (Boolean) result) {
            Object[] args = joinPoint.getArgs();
            if (args.length > 0 && args[0] instanceof Long) {
                Long id = (Long) args[0];
                webSocketPushService.pushExceptionResolved(id);
                log.info("异常已闭环，已推送WebSocket消息: id={}", id);
            }
        }
    }

    @AfterReturning(pointcut = "productionReportSavePointcut()", returning = "result")
    public void afterProductionReport(JoinPoint joinPoint, Object result) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof Map) {
            Map<String, Object> requestBody = (Map<String, Object>) args[0];
            @SuppressWarnings("unchecked")
            Map<String, Object> reportData = (Map<String, Object>) requestBody.get("productionReport");
            if (reportData == null) {
                reportData = requestBody;
            }

            Object workOrderIdObj = reportData.get("workOrderId");
            Object goodQtyObj = reportData.get("goodQuantity");
            Object badQtyObj = reportData.get("badQuantity");

            if (workOrderIdObj != null) {
                try {
                    Long workOrderId = Long.valueOf(workOrderIdObj.toString());
                    int addQuantity = 0;
                    if (goodQtyObj != null) {
                        addQuantity += Integer.valueOf(goodQtyObj.toString());
                    }
                    if (badQtyObj != null) {
                        addQuantity += Integer.valueOf(badQtyObj.toString());
                    }

                    WorkOrder workOrder = workOrderService.getById(workOrderId);
                    String workOrderNo = workOrder != null ? workOrder.getWorkNo() : "WO-" + workOrderId;

                    webSocketPushService.pushProgressUpdate(workOrderId, workOrderNo, "生产报工", addQuantity);
                    log.info("生产报工成功，已推送WebSocket消息: workOrderId={}, addQty={}", workOrderId, addQuantity);
                } catch (Exception e) {
                    log.error("解析报工数据失败: {}", e.getMessage());
                }
            }
        }
    }
}
