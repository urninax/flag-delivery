package me.urninax.flagdelivery.projectsenvs.utils;

import me.urninax.flagdelivery.projectsenvs.ui.models.requests.ListAllProjectsRequest;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class FilterConverter implements Converter<String, ListAllProjectsRequest>{
    @Override
    public ListAllProjectsRequest convert(String source){
        if (source.isBlank()) {
            return new ListAllProjectsRequest("", List.of(), List.of());
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

        List<String> tags = Optional.ofNullable(map.get("tags"))
                .map(v -> Arrays.asList(v.split(" ")))
                .orElse(List.of());

        List<String> keys = Optional.ofNullable(map.get("keys"))
                .map(v -> Arrays.asList(v.split(" ")))
                .orElse(List.of());

        return new ListAllProjectsRequest(
                Optional.ofNullable(map.get("query")).orElse(""),
                tags,
                keys
        );
    }
}
