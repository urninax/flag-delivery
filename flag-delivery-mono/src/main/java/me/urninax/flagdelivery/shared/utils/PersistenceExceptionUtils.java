package me.urninax.flagdelivery.shared.utils;

import org.springframework.core.NestedExceptionUtils;

import java.sql.SQLException;

public final class PersistenceExceptionUtils{
    private PersistenceExceptionUtils(){}

    public static boolean isUniqueException(Throwable t){
        Throwable root = NestedExceptionUtils.getMostSpecificCause(t);

        if(root instanceof SQLException sql){
            return "23505".equals(sql.getSQLState());
        }

        return false;
    }
}
