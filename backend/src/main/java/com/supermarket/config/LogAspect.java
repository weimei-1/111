package com.supermarket.config;

import com.supermarket.entity.OperationLog;
import com.supermarket.service.OperationLogService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@Aspect
@Component
public class LogAspect {

    @Autowired
    private OperationLogService operationLogService;

    @Pointcut("execution(* com.supermarket.controller.*.add*(..))")
    public void addPointcut() {}

    @Pointcut("execution(* com.supermarket.controller.*.update*(..))")
    public void updatePointcut() {}

    @Pointcut("execution(* com.supermarket.controller.*.delete*(..))")
    public void deletePointcut() {}

    @Pointcut("execution(* com.supermarket.controller.*.stockIn(..))")
    public void stockInPointcut() {}

    @AfterReturning("addPointcut()")
    public void afterAdd(JoinPoint joinPoint) {
        saveLog(joinPoint, "add");
    }

    @AfterReturning("updatePointcut()")
    public void afterUpdate(JoinPoint joinPoint) {
        saveLog(joinPoint, "update");
    }

    @AfterReturning("deletePointcut()")
    public void afterDelete(JoinPoint joinPoint) {
        saveLog(joinPoint, "delete");
    }

    @AfterReturning("stockInPointcut()")
    public void afterStockIn(JoinPoint joinPoint) {
        saveLog(joinPoint, "stock_in");
    }

    private void saveLog(JoinPoint joinPoint, String action) {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

            String module = resolveModule(joinPoint);
            String detail = buildDetail(joinPoint, action);

            OperationLog log = new OperationLog();
            log.setModule(module);
            log.setAction(action);
            log.setDetail(detail);

            Object userInfo = request.getAttribute("userInfo");
            if (userInfo != null) {
                try {
                    Long userId = (Long) userInfo.getClass().getMethod("getId").invoke(userInfo);
                    String userName = (String) userInfo.getClass().getMethod("getRealName").invoke(userInfo);
                    log.setUserId(userId);
                    log.setUserName(userName);
                } catch (Exception ignored) {}
            }

            operationLogService.save(log);
        } catch (Exception ignored) {}
    }

    private String resolveModule(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName().toLowerCase();
        if (className.contains("product")) return "product";
        if (className.contains("category")) return "category";
        if (className.contains("order")) return "order";
        if (className.contains("member")) return "member";
        if (className.contains("user")) return "user";
        if (className.contains("supplier")) return "supplier";
        if (className.contains("purchase")) return "purchase";
        if (className.contains("promotion")) return "promotion";
        return "other";
    }

    private String buildDetail(JoinPoint joinPoint, String action) {
        StringBuilder sb = new StringBuilder();
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg != null && arg.getClass().getName().contains("com.supermarket.entity")) {
                try {
                    Method getName = arg.getClass().getMethod("getName");
                    Object name = getName.invoke(arg);
                    if (name != null) {
                        sb.append(arg.getClass().getSimpleName()).append(": ").append(name);
                    }
                    Method getId = arg.getClass().getMethod("getId");
                    Object id = getId.invoke(arg);
                    if (id != null && name == null) {
                        sb.append(arg.getClass().getSimpleName()).append(" ID: ").append(id);
                    }
                } catch (Exception ignored) {}
            } else if (arg instanceof Long) {
                sb.append("ID: ").append(arg);
            }
        }
        if (sb.length() == 0) sb.append(action);
        return sb.toString();
    }
}