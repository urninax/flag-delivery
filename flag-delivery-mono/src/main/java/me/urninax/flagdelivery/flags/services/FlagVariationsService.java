package me.urninax.flagdelivery.flags.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.flags.models.FlagKind;
import me.urninax.flagdelivery.flags.models.FlagVariation;
import me.urninax.flagdelivery.flags.shared.ResolvedVariations;
import me.urninax.flagdelivery.flags.ui.requests.CreateFeatureFlagRequest;
import me.urninax.flagdelivery.flags.ui.requests.VariationDefaultsRequest;
import me.urninax.flagdelivery.flags.ui.requests.VariationRequest;
import me.urninax.flagdelivery.flags.utils.exceptions.VariationIndexOutOfBoundsException;
import me.urninax.flagdelivery.flags.utils.exceptions.VariationNotUniqueException;
import me.urninax.flagdelivery.flags.utils.exceptions.VariationTypesMismatchException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlagVariationsService{
    public ResolvedVariations resolveAndValidateVariations(CreateFeatureFlagRequest request){
        // map request to FlagVariation objects or get default true/false variations of empty
        List<FlagVariation> variations = request.variations() != null && !request.variations().isEmpty()
                ? transformVariations(request.variations())
                : defaultVariations();

        // map default variations indexes or get default
        VariationDefaultsRequest defaultsRequest = Objects.requireNonNullElse(request.defaults(),
                new VariationDefaultsRequest(0, variations.size() - 1));

        int onIdx  = Objects.requireNonNullElse(defaultsRequest.onVariation(), 0);
        int offIdx = Objects.requireNonNullElse(defaultsRequest.offVariation(), variations.size() - 1);

        validateVariationBounds(variations, onIdx, offIdx);
        validateVariationTypes(variations);
        validateVariationsUniqueness(variations);

        return new ResolvedVariations(variations, variations.get(onIdx), variations.get(offIdx));
    }

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

    private void validateVariationBounds(List<FlagVariation> variations, int onIdx, int offIdx){
        if(onIdx >= variations.size() || offIdx >= variations.size()){
            throw new VariationIndexOutOfBoundsException();
        }
    }

    private void validateVariationTypes(List<FlagVariation> variations){
        // map each variation to FlagKind and collect to set to find out if all variations have the same kind
        Set<FlagKind> variationsKinds = variations.stream()
                .map(v -> detectType(v.getValue()))
                .collect(Collectors.toSet());

        if(variationsKinds.size() > 1){
            throw new VariationTypesMismatchException();
        }
    }

    private void validateVariationsUniqueness(List<FlagVariation> variations){
        // compare set of variations with list. size should not change if unique
        if(new HashSet<>(variations).size() < variations.size()){
            throw new VariationNotUniqueException();
        }
    }

    public FlagKind detectType(JsonNode value){
        if(value.isBoolean()) return FlagKind.BOOLEAN;
        if(value.isNumber()) return FlagKind.NUMBER;
        if(value.isTextual()) return FlagKind.STRING;
        if(value.isObject() || value.isArray()) return FlagKind.JSON;

        return FlagKind.STRING;
    }
}
