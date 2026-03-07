package me.urninax.flagdelivery.flags.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.flags.models.FeatureFlag;
import me.urninax.flagdelivery.flags.models.FlagKind;
import me.urninax.flagdelivery.flags.models.FlagVariation;
import me.urninax.flagdelivery.flags.shared.ResolvedVariations;
import me.urninax.flagdelivery.flags.ui.requests.CreateFeatureFlagRequest;
import me.urninax.flagdelivery.flags.ui.requests.PatchVariationRequest;
import me.urninax.flagdelivery.flags.ui.requests.VariationDefaultsRequest;
import me.urninax.flagdelivery.flags.ui.requests.VariationRequest;
import me.urninax.flagdelivery.flags.utils.exceptions.VariationIndexOutOfBoundsException;
import me.urninax.flagdelivery.flags.utils.exceptions.VariationNotFoundException;
import me.urninax.flagdelivery.flags.utils.exceptions.VariationNotUniqueException;
import me.urninax.flagdelivery.flags.utils.exceptions.VariationTypesMismatchException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FlagVariationsService{

    public void createVariation(VariationRequest variationRequest, FeatureFlag flag){
        FlagVariation variation = transformVariation(variationRequest);

        validateVariationValueType(variation.getValue(), flag.getKind());
        validateVariationUniqueness(variation.getValue(), variation.getName(), flag.getVariations(), null);

        flag.addVariation(variation);
    }

    public void updateVariation(FeatureFlag flag, PatchVariationRequest request, UUID variationId){
        FlagVariation variation = flag.getVariations().stream()
                .filter(v -> Objects.equals(v.getId(), variationId))
                .findFirst()
                .orElseThrow(VariationNotFoundException::new);

        if(request.value() != null){
            validateVariationValueType(request.value(), flag.getKind());
        }

        validateVariationUniqueness(
                Objects.requireNonNullElse(request.value(), variation.getValue()),
                Objects.requireNonNullElse(request.name(), variation.getName()),
                flag.getVariations(),
                variationId
        );

        if(request.name() != null) variation.setName(request.name());
        if(request.description() != null) variation.setDescription(request.description());
        if(request.value() != null) variation.setValue(request.value());
    }

    public ResolvedVariations resolveAndValidateVariations(CreateFeatureFlagRequest request){
        List<FlagVariation> variations = request.variations() != null && !request.variations().isEmpty()
                ? transformVariations(request.variations())
                : defaultVariations();

        VariationDefaultsRequest defaultsRequest = Objects.requireNonNullElse(request.defaults(),
                new VariationDefaultsRequest(0, variations.size() - 1));

        int onIdx  = Objects.requireNonNullElse(defaultsRequest.onVariation(), 0);
        int offIdx = Objects.requireNonNullElse(defaultsRequest.offVariation(), variations.size() - 1);

        validateVariationBounds(variations, onIdx, offIdx);
        validateVariationsTypeConsistency(variations);
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
                .map(this::transformVariation)
                .toList();
    }

    public FlagVariation transformVariation(VariationRequest variationRequest){
        return FlagVariation.builder()
                .name(variationRequest.name())
                .value(variationRequest.value())
                .description(variationRequest.description())
                .build();
    }

    private void validateVariationBounds(List<FlagVariation> variations, int onIdx, int offIdx){
        if(onIdx >= variations.size() || offIdx >= variations.size()){
            throw new VariationIndexOutOfBoundsException();
        }
    }

    private void validateVariationsTypeConsistency(List<FlagVariation> variations){
        if(variations == null || variations.isEmpty()){
            return;
        }

        FlagKind firstType = FlagKind.from(variations.getFirst().getValue());

        boolean allSame = variations.stream()
                .skip(1)
                .map(v -> FlagKind.from(v.getValue()))
                .allMatch(kind -> kind == firstType);

        if(!allSame){
            throw new VariationTypesMismatchException();
        }
    }

    private void validateVariationValueType(JsonNode value, FlagKind expectedKind){
        if(FlagKind.from(value) != expectedKind){
            throw new VariationTypesMismatchException();
        }
    }

    private void validateVariationsUniqueness(List<FlagVariation> variations){
        long uniqueValuesCount = variations.stream()
                .map(FlagVariation::getValue)
                .distinct()
                .count();

        if(uniqueValuesCount < variations.size()){
            throw new VariationNotUniqueException();
        }

        long uniqueNamesCount = variations.stream()
                .map(FlagVariation::getName)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        long totalNamesCount = variations.stream()
                .map(FlagVariation::getName)
                .filter(Objects::nonNull)
                .count();

        if(uniqueNamesCount < totalNamesCount){
            throw new VariationNotUniqueException();
        }
    }

    private void validateVariationUniqueness(JsonNode value, String name, List<FlagVariation> existingVariations, UUID excludeId){
        boolean valueExists = existingVariations.stream()
                .filter(v -> excludeId == null || !Objects.equals(v.getId(), excludeId))
                .anyMatch(v -> v.getValue().equals(value));

        if(valueExists){
            throw new VariationNotUniqueException();
        }

        if(name != null){
            boolean nameExists = existingVariations.stream()
                    .filter(v -> excludeId == null || !Objects.equals(v.getId(), excludeId))
                    .anyMatch(v -> name.equals(v.getName()));

            if(nameExists){
                throw new VariationNotUniqueException();
            }
        }
    }
}
