package com.example.inventory.util;

import com.querydsl.core.types.dsl.BooleanExpression;

public class QueryDslUtils {

    /**
     * 여러 BooleanExpression을 AND 연산으로 null 안전하게 조합 모든 인자가 null이면 null을 반환한다 * @param expressions 조합할
     * BooleanExpression 목록
     *
     * @return 조합된 BooleanExpression 또는 모든 인자가 null일 경우 null
     */
    public static BooleanExpression combineAnd(BooleanExpression... expressions) {
        BooleanExpression result = null;

        for (BooleanExpression expression : expressions) {
            if (expression != null) {
                if (result == null) {
                    // 첫 번째 non-null 조건으로 시작
                    result = expression;
                } else {
                    // 이후 non-null 조건은 AND로 연결
                    result = result.and(expression);
                }
            }
        }
        return result;
    }
}
