package me.urninax.flagdelivery.contexts.shared;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.TreeMap;

@Getter
@Setter
@Builder
public class SingleContextInstanceDTO{
    private String key;
    private String kind;

    @Builder.Default
    private Map<String, JsonNode> attributes = new TreeMap<>();
}
