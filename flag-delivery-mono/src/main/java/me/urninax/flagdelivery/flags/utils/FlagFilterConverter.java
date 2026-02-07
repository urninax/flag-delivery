package me.urninax.flagdelivery.flags.utils;

import me.urninax.flagdelivery.flags.ui.requests.ListAllFlagsRequest;
import me.urninax.flagdelivery.projectsenvs.utils.FilterParser;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FlagFilterConverter implements Converter<String, ListAllFlagsRequest>{
    @Nullable
    @Override
    public ListAllFlagsRequest convert(String source){
        Map<String, String> params = FilterParser.parse(source);

        return new ListAllFlagsRequest(
                params.getOrDefault("query", ""),
                FilterParser.splitList(params, "tags"),
                params.getOrDefault("maintainer", ""),
                params.getOrDefault("type", "")
        );
    }
}
