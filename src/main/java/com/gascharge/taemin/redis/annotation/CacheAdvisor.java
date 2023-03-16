package com.gascharge.taemin.redis.annotation;

import com.gascharge.taemin.redis.access.RedisDao;
import com.gascharge.taemin.redis.access.RedisJson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Aspect
@Component
public class CacheAdvisor {
    private final RedisJson redisJson;
    private final RedisDao redisDao;

    @Around("@annotation(com.gascharge.taemin.redis.annotation.Cache)")
    public Object processCacheAnnotation(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Optional<String> optional = getKey(proceedingJoinPoint);

        if (optional.isEmpty()) {
            return proceedingJoinPoint.proceed();
        }

        String key = optional.get();

        Type genericReturnType = ((MethodSignature) proceedingJoinPoint.getSignature()).getMethod().getGenericReturnType();

        if (redisDao.hasKey(key)) return redisJson.get(key, genericReturnType);

        Object proceed = proceedingJoinPoint.proceed();

        log.debug("proceedingJoinPoint.proceed() = {}", proceed);

        if (!redisJson.set(key, proceed)) redisOpsFailWarn(key, proceed.toString(), "processCacheAnnotation", "set");

        return proceed;
    }

    @AfterReturning(pointcut = "@annotation(com.gascharge.taemin.redis.annotation.CachePut)", returning = "result")
    public void processCachePutAnnotation(JoinPoint joinPoint, Object result) {
        Optional<String> optional = getKey(joinPoint);

        if (optional.isEmpty()) return;

        String key = optional.get();
        log.debug("processCachePutAnnotation key : {}", key);

        if (!redisJson.set(key, result)) redisOpsFailWarn(key, result.toString(), "processCachePutAnnotation", "set");
    }

    @After("@annotation(com.gascharge.taemin.redis.annotation.CacheDelete)")
    public void processCacheDeleteAnnotation(JoinPoint joinPoint) {
        Optional<String> optional = getKey(joinPoint);
        if (optional.isEmpty()) return;

        String key = optional.get();
        log.debug("delete key : {}", key);

        redisDao.del(key);
    }

    private void redisOpsFailWarn(String key, String proceed, String methodName, String opsName) {
        log.warn("{} 메서드에서 레디스에 값 {}에 실패했습니다. key = {}, value = {}", methodName, opsName, key, proceed);
    }

    private Optional<String> getKey(JoinPoint joinPoint) {
        String parameterKey;
        String parameterValue;
        try {
            parameterKey = getParameterKey(joinPoint);
            parameterValue = getParameterValue(joinPoint);
        } catch (RuntimeException e) {
            log.warn(e.getMessage());
            return Optional.empty();
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(parameterKey);
        stringBuilder.append("::");
        stringBuilder.append(parameterValue);

        return Optional.of(stringBuilder.toString());
    }

    private static String getParameterKey(JoinPoint joinPoint) throws IllegalArgumentException {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Annotation[] annotations = method.getAnnotations();

        Optional<String> optional = getAttributeValue(annotations, "value");

        if (optional.isEmpty()) throw new IllegalArgumentException(method.getName() + " 의 메소드에서 value 속성 값을 찾을 수 없습니다.");
        return optional.get();
    }

    private static Optional<String> getAttributeValue(Annotation[] annotations, String attributeName) {
        for (Annotation annotation : annotations) {
            Object value = AnnotationUtils.getValue(annotation, attributeName);
            Object defaultValue = AnnotationUtils.getDefaultValue(annotation, attributeName);
            if (value != null && !value.equals(defaultValue)) return Optional.of(value.toString());
        }
        return Optional.empty();
    }

    private static String getParameterValue(JoinPoint joinPoint) throws IllegalArgumentException {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        Method method = signature.getMethod();

        Optional<String> optional = getAttributeValue(method.getAnnotations(), "key");
        if (optional.isEmpty()) throw new IllegalArgumentException(method.getName() + " 의 메소드에서 key 속성 값을 찾을 수 없습니다.");
        String key = optional.get();

        List<String> parameterNames = Arrays.stream(signature.getParameterNames()).toList();

        return parameterNames.stream()
                .filter(p -> p.equals(key))
                .map(p -> Objects.requireNonNull(joinPoint.getArgs()[parameterNames.indexOf(p)]))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(key + " 에 해당하는 파라미터를 찾을 수 없습니다.")).toString();
    }
}
