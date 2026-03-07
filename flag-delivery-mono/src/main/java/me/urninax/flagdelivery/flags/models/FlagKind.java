package me.urninax.flagdelivery.flags.models;

import com.fasterxml.jackson.databind.JsonNode;

public enum FlagKind{
    BOOLEAN, STRING, NUMBER, JSON;

    public static FlagKind from(JsonNode value){
        if(value.isBoolean()) return BOOLEAN;
        if(value.isNumber()) return NUMBER;
        if(value.isTextual()) return STRING;
        if(value.isObject() || value.isArray()) return JSON;

        return STRING;
    }
}
