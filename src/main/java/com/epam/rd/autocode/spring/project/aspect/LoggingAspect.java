package com.epam.rd.autocode.spring.project.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LoggingAspect {

    private static final Set<String> SENSITIVE_FIELDS = new HashSet<>(Arrays.asList(
            "password", "passwordConfirm"));

    /**
     * Logs all methods in the Service layer.
     * Logs method entry, exit, execution time, and exceptions.
     */
    @Around("execution(* com.epam.rd.autocode.spring.project.service..*(..))")
    public Object logServiceLayer(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        Object[] args = joinPoint.getArgs();

        // Mask sensitive data in arguments
        String sanitizedArgs = sanitizeArguments(args, signature.getMethod());

        log.info("[SERVICE] → {}.{} with args: {}", className, methodName, sanitizedArgs);
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            log.info("[SERVICE] ← {}.{} completed in {}ms", className, methodName, duration);
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[SERVICE] ✗ {}.{} failed after {}ms: {}", className, methodName, duration, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Logs all methods in the Controller layer.
     * Logs incoming requests and responses.
     */
    @Around("execution(* com.epam.rd.autocode.spring.project.controller..*(..))")
    public Object logControllerLayer(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        Object[] args = joinPoint.getArgs();

        String sanitizedArgs = sanitizeArguments(args, signature.getMethod());

        log.info("[CONTROLLER] → {}.{} args: {}", className, methodName, sanitizedArgs);
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            BindingResult bindingResult = findBindingResult(args);

            if (bindingResult != null && bindingResult.hasErrors()) {
                String errors = bindingResult.getAllErrors().stream()
                        .map(error -> {
                            String code = error.getCode();

                            if (error instanceof FieldError) {
                                return String.format("[%s: %s]", ((FieldError) error).getField(), code);
                            }

                            return String.format("[%s: %s]", error.getObjectName(), code);
                        })
                        .collect(Collectors.joining(", "));

                log.warn("[CONTROLLER] ⚠ {}.{} VALIDATION FAIL in {}ms. View: '{}'. Errors: {}",
                        className, methodName, duration, result, errors);
            } else {
                log.info("[CONTROLLER] ← {}.{} returned '{}' in {}ms", className, methodName, result, duration);
            }

            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[CONTROLLER] ✗ {}.{} failed in {}ms: {}", className, methodName, duration, e.getMessage());
            throw e;
        }
    }

    private BindingResult findBindingResult(Object[] args) {
        if (args == null) return null;
        for (Object arg : args) {
            if (arg instanceof BindingResult) {
                return (BindingResult) arg;
            }
        }
        return null;
    }

    /**
     * Sanitizes method arguments by masking sensitive data.
     * Checks both field names in DTOs and parameter names.
     */
    private String sanitizeArguments(Object[] args, Method method) {
        if (args == null || args.length == 0) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");
        Parameter[] parameters = method.getParameters();

        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }

            Object arg = args[i];
            String paramName = i < parameters.length ? parameters[i].getName() : "arg" + i;

            if (arg instanceof BindingResult) {
                sb.append(paramName).append("=[BindingResult]");
                continue;
            }
            if (arg instanceof org.springframework.ui.Model) {
                sb.append(paramName).append("=[Model]");
                continue;
            }

            // Check if parameter name is sensitive
            if (isSensitiveField(paramName)) {
                sb.append(paramName).append("=*********");
            } else if (arg == null) {
                sb.append(paramName).append("=null");
            } else {
                // Check if the object contains sensitive fields (for DTOs)
                String argString = maskSensitiveFieldsInObject(arg);
                sb.append(paramName).append("=").append(argString);
            }
        }

        sb.append("]");
        return sb.toString();
    }

    /**
     * Masks sensitive fields in DTOs by checking field names.
     */
    private String maskSensitiveFieldsInObject(Object obj) {
        if (obj == null) {
            return "null";
        }

        String objString = obj.toString();


        for (String sensitiveField : SENSITIVE_FIELDS) {
            objString = objString.replaceAll(
                    "(?i)" + sensitiveField + "=([^,)\\]]+)",
                    sensitiveField + "=*********");
        }

        return objString;
    }

    /**
     * Checks if a field name is sensitive.
     */
    private boolean isSensitiveField(String fieldName) {
        if (fieldName == null) {
            return false;
        }
        return SENSITIVE_FIELDS.stream()
                .anyMatch(sensitive -> fieldName.toLowerCase().contains(sensitive.toLowerCase()));
    }
}
