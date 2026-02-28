package com.paystream.inventory.annotation;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DistributedLockAop {

    private final RedissonClient redissonClient;
    private final RedissonCallTransaction redissonCallTransaction; // 별도 트랜잭션 용

    @Around("@annotation(com.paystream.inventory.annotation.DistributedLock)")
    public Object distributeLock(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        DistributedLock distributedLock =
                methodSignature.getMethod().getAnnotation(DistributedLock.class);

        String lockKey =
                (String)
                        getDynamicValue(
                                methodSignature.getParameterNames(),
                                joinPoint.getArgs(),
                                distributedLock.key());
        long waitTime = distributedLock.waitTime();
        long leaseTime = distributedLock.leaseTime();
        TimeUnit timeUnit = distributedLock.timeUnit();

        // 락 획득
        RLock rLock = redissonClient.getLock(lockKey);

        try {
            // 모든 상품에 대해 한 번에 락을 획득 (원자적)
            if (!rLock.tryLock(waitTime, leaseTime, timeUnit)) {
                log.error("락 획득 실패 - lockKey: {}", lockKey);
                throw new IllegalStateException("시스템이 혼잡하여 취소 처리가 지연되고 있습니다.");
            }

            // 어노테이션이 적용된 서비스 로직 실행
            return redissonCallTransaction.proceed(joinPoint);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("작업 중 오류가 발생했습니다.");
        } finally {
            // 현재 쓰레드가 잡은 락이 맞는지 확인 후 해제
            if (rLock != null && rLock.isHeldByCurrentThread()) {
                rLock.unlock();
            }
        }
    }

    private Object getDynamicValue(String[] parameterNames, Object[] args, String key) {
        SpelExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();

        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }

        return parser.parseExpression(key).getValue(context, Object.class);
    }
}
