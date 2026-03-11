package me.urninax.flagdelivery.contexts.ui.requests;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.urninax.flagdelivery.projectsenvs.services.validation.KeyType;
import me.urninax.flagdelivery.projectsenvs.services.validation.ValidKey;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EvaluationContextRequest{
    private String key;

    @NotBlank(message = "Context kind cannot be empty.")
    @Size(min = 2, max = 20, message = "Context kind must be between 2 and 20 characters.")
    @ValidKey(type = KeyType.CONTEXTKIND)
    private String kind;

    @JsonIgnore
    @Builder.Default
    private Map<String, JsonNode> attributes = new HashMap<>();

    @JsonAnySetter
    public void addAttribute(String key, JsonNode value){
        if(this.attributes == null){
            this.attributes = new HashMap<>();
        }
        this.attributes.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, JsonNode> getAttributes(){
        return attributes;
    }

    @JsonIgnore
    @AssertTrue(message = "Invalid context structure: exceeded limits (nested depth or attributes) or missing required keys")
    public boolean isValid(){
        if("multi".equalsIgnoreCase(kind)){
            if(attributes.isEmpty()) return false;

            boolean subContextsHaveKeys = attributes.values().stream().allMatch(node ->
                    node.isObject() &&
                            node.has("key") &&
                            !node.get("key").isMissingNode() &&
                            !node.get("key").asText().isBlank()
            );

            if (!subContextsHaveKeys) return false;
        }else{
            if(key == null || key.isBlank()) return false;
        }

        return validateMap(attributes, new AtomicInteger(0));
    }

    private static final int MAX_DEPTH = 10;
    private static final int MAX_ATTRIBUTES = 300;

    private boolean validateMap(Map<String, JsonNode> attributes, AtomicInteger attributesCount){
        for(Map.Entry<String, JsonNode> entry : attributes.entrySet()){
            if(attributesCount.incrementAndGet() > MAX_ATTRIBUTES) return false;
            if(!validateNode(entry.getValue(), 1, attributesCount)) return false;
        }
        return true;
    }

    private boolean validateNode(JsonNode node, int depth, AtomicInteger attributesCount){
        if(depth > MAX_DEPTH) return false;

        if(node.isObject()){
            for(Map.Entry<String, JsonNode> entry : node.properties()){
                if(attributesCount.incrementAndGet() > MAX_ATTRIBUTES) return false;

                if(!validateNode(entry.getValue(), depth + 1, attributesCount)) return false;
            }
        }else if(node.isArray()){
            for(JsonNode item : node){
                if(!validateNode(item, depth + 1, attributesCount)) return false;
            }
        }
        return true;
    }

}
