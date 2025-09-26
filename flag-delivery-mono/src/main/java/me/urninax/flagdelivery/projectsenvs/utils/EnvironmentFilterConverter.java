package me.urninax.flagdelivery.projectsenvs.utils;

import me.urninax.flagdelivery.projectsenvs.ui.models.requests.environment.ListAllEnvironmentsRequest;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EnvironmentFilterConverter implements Converter<String, ListAllEnvironmentsRequest>{
    @Override
    public ListAllEnvironmentsRequest convert(String source){
        Map<String, String> map = FilterParser.parse(source);

        return new ListAllEnvironmentsRequest(
                map.getOrDefault("query", ""),
                FilterParser.splitList(map, "tags")
        );
    }
}
