package com.paystream.inventory.utils;

import com.paystream.inventory.annotation.CacheKeyParam;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component("customKeyGenerator")
public class CacheKeyGenerator implements KeyGenerator {

    /**
     * Redis Cache의 Key 생성
     *
     * @param target
     * @param method
     * @param params
     * @return
     */
    @Override
    public Object generate(Object target, Method method, Object... params) {
        StringBuilder sb = new StringBuilder();
        Parameter[] methodParameters = method.getParameters();

        // 초기 클래스명과 메소드명 세팅
        sb.append(target.getClass().getSimpleName()).append(":").append(method.getName());

        // 각 파라미터 세팅
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];
            if (param == null) continue;

            if (methodParameters[i].isAnnotationPresent(CacheKeyParam.class)) {
                if (!sb.isEmpty()) sb.append(":");

                if (isSimpleType(param)) {
                    // 일반 파라미터 (예: hostId:123)
                    sb.append(methodParameters[i].getName()).append(":").append(param);
                } else {
                    // DTO 객체 (예: category:HOTEL:city:서울)
                    sb.append(extractFieldFromDto(param));
                }
            }

            // PageRequest이 있을 경우 추가
            if (param instanceof PageRequest p) {
                if (!sb.isEmpty()) sb.append(":");
                sb.append("page:").append(p.getPageNumber());
            }
        }

        return sb.toString();
    }

    // DTO 객체가 아닌 일반 클래스 타입인지 확인
    private boolean isSimpleType(Object param) {
        return param instanceof String
                || param instanceof Number
                || param instanceof Boolean
                || param.getClass().isEnum();
    }

    // DTO 객체를 Cache Key로 만들기 위한 메소드
    private String extractFieldFromDto(Object dto) {
        Map<String, Object> params = new TreeMap<>(); // 키 순서가 변경되지 않도록 사전순으로 정렬

        try {
            for (Field field : dto.getClass().getDeclaredFields()) {
                field.setAccessible(true); // private 필드 접근 허용, Reflection
                Object value = field.get(dto);
                if (value == null || value == "") continue; // 값이 null이면 키에 포함하지 않음.

                // dto를 순환하면서 Map에 담아줌.
                if (value instanceof Collection<?> collection) {
                    // value가 Collection 객체라면?
                    String sortedValue =
                            collection.stream()
                                    .map(Object::toString)
                                    .sorted() // 캐시에 담길때 값의 순서가 뒤바뀌면 캐시를 계속 생성하기 때문에 방지용
                                    .collect(Collectors.joining("-"));
                    params.put(field.getName(), "[" + sortedValue + "]");
                } else {
                    // 일반 타입인 경우
                    params.put(field.getName(), value);
                }
            }
        } catch (IllegalAccessException e) {
            return "error-key"; // 에러 상황시 캐시에 에러가 남도록 하여 개발자가 에러 상황을 인지하도록 함.
        }

        return params.entrySet().stream()
                .map(entry -> entry.getKey() + ":" + entry.getValue())
                .collect(Collectors.joining(":"));
    }
}
