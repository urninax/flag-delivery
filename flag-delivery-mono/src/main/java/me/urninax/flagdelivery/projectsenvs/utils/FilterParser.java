package me.urninax.flagdelivery.projectsenvs.utils;

import java.util.*;

public class FilterParser{
    public static Map<String, String> parse(String source){
        if (source.isBlank()) {
            return Map.of();
        }

        Map<String, String> map = new HashMap<>();
        for (String part : source.split(",")) {
            int idx = part.indexOf(':');
            if (idx > 0) {
                String key = part.substring(0, idx).trim();
                String value = part.substring(idx + 1).trim();
                if (!value.isEmpty()) {
                    map.put(key, value);
                }
            }
        }

        return map;
    }

    public static List<String> splitList(Map<String, String> map, String key){
        return Optional.ofNullable(map.get(key))
                .map(v -> Arrays.asList(v.split(" ")))
                .orElse(List.of());
    }
}
