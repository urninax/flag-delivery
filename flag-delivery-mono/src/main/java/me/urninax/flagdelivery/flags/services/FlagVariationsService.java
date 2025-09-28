package me.urninax.flagdelivery.flags.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.flags.models.FlagVariation;
import me.urninax.flagdelivery.flags.ui.requests.VariationRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FlagVariationsService{
    public List<FlagVariation> defaultVariations(){
        JsonNode trueNode = JsonNodeFactory.instance.booleanNode(true);
        JsonNode falseNode = JsonNodeFactory.instance.booleanNode(false);

        FlagVariation trueVar = FlagVariation.builder()
                .name("Enabled")
                .value(trueNode)
                .description("Serves true")
                .build();

        FlagVariation falseVar = FlagVariation.builder()
                .name("Disabled")
                .value(falseNode)
                .description("Serves false")
                .build();

        return List.of(trueVar, falseVar);
    }

    public List<FlagVariation> transformVariations(List<VariationRequest> variationRequests){
        return variationRequests.stream()
                .map(reqVariation ->
                        FlagVariation.builder()
                                .name(reqVariation.name())
                                .value(reqVariation.value())
                                .description(reqVariation.description())
                                .build()
                )
                .toList();
    }
}
