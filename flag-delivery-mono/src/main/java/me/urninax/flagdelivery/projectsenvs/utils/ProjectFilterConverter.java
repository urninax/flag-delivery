package me.urninax.flagdelivery.projectsenvs.utils;

import me.urninax.flagdelivery.projectsenvs.ui.models.requests.project.ListAllProjectsRequest;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ProjectFilterConverter implements Converter<String, ListAllProjectsRequest>{
    @Override
    public ListAllProjectsRequest convert(String source){
        Map<String, String> map = FilterParser.parse(source);

        return new ListAllProjectsRequest(
                map.getOrDefault("query", ""),
                FilterParser.splitList(map, "tags"),
                FilterParser.splitList(map, "keys")
        );
    }
}
