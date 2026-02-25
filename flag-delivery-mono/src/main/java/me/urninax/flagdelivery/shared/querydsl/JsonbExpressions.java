package me.urninax.flagdelivery.shared.querydsl;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;

import java.util.Collection;
import java.util.Set;

public class JsonbExpressions{
    public static BooleanExpression containsAll(Path<Set<String>> path, Collection<String> values){
        if (values == null || values.isEmpty()) return Expressions.asBoolean(true).isTrue();

        String json = "[\"" + String.join("\",\"", values) + "\"]";
        return Expressions.booleanTemplate("{0} @> {1}::jsonb", path, json);
    }
}
