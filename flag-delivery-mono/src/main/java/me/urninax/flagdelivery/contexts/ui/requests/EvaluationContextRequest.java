package me.urninax.flagdelivery.contexts.ui.requests;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.urninax.flagdelivery.contexts.ui.requests.validation.ValidEvaluationContext;
import me.urninax.flagdelivery.projectsenvs.services.validation.KeyType;
import me.urninax.flagdelivery.projectsenvs.services.validation.ValidKey;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@ValidEvaluationContext
public class EvaluationContextRequest{
    private String key;

    @NotBlank(message = "Context kind cannot be empty.")
    @Size(min = 2, max = 20, message = "Context kind must be between 2 and 20 characters.")
    @ValidKey(type = KeyType.EVALUATION_CONTEXTKIND)
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

}
