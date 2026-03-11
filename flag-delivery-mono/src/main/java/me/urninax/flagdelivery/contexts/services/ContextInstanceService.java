package me.urninax.flagdelivery.contexts.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.contexts.models.ContextInstance;
import me.urninax.flagdelivery.contexts.shared.SingleContextInstanceDTO;
import me.urninax.flagdelivery.contexts.ui.requests.EvaluationContextRequest;
import me.urninax.flagdelivery.shared.exceptions.BadRequestException;
import me.urninax.flagdelivery.shared.utils.EntityMapper;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ContextInstanceService{
    private static final int MAX_DEPTH = 10;

    private final EntityMapper entityMapper;
    private final ObjectMapper canonicalMapper = JsonMapper.builder()
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
            .build();
    private final Clock clock;

    public List<ContextInstance> handleEvaluationContext(EvaluationContextRequest request){
        List<SingleContextInstanceDTO> contextInstances = new ArrayList<>();

        if(request.getKind().equals("multi")){
            contextInstances.addAll(splitMultiContext(request.getAttributes()));
        }else{
            contextInstances.add(entityMapper.toSingleContextInstanceDTO(request));
        }

        return contextInstances.stream()
                .map(context -> ContextInstance.builder()
                        .hash(calculateHash(canonicalizeContext(context)))
                        .body(canonicalMapper.valueToTree(context))
                        .updatedAt(clock.instant())
                        .build()).toList();
    }

    private List<SingleContextInstanceDTO> splitMultiContext(Map<String, JsonNode> multiContextDetails){
        List<SingleContextInstanceDTO> result = new ArrayList<>();

        multiContextDetails.forEach((kind, value) -> {
            if(!value.isObject()){
                throw new BadRequestException("Context kind '" + kind + "' must be a JSON object");
            }

            Map<String, JsonNode> attributes = value.propertyStream()
                    .filter(entry -> !entry.getKey().equals("key"))
                    .collect(HashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), HashMap::putAll);

            JsonNode keyNode = value.path("key");
            if(keyNode.isMissingNode() || keyNode.isNull()){
                throw new BadRequestException("Key is missing for one of the context instances");
            }

            SingleContextInstanceDTO dto = SingleContextInstanceDTO.builder()
                    .kind(kind)
                    .key(keyNode.asText())
                    .attributes(attributes)
                    .build();

            result.add(dto);
        });

        return result;
    }

    private String calculateHash(SingleContextInstanceDTO context){
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] contextBytes = canonicalMapper.writeValueAsBytes(context);
            byte[] hashBytes = digest.digest(contextBytes);

            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashBytes);
        }catch(NoSuchAlgorithmException | JsonProcessingException e){
            throw new RuntimeException(e);
        }
    }

    private SingleContextInstanceDTO canonicalizeContext(SingleContextInstanceDTO context){
        Map<String, JsonNode> sortedAttributes = new TreeMap<>();

        context.getAttributes().forEach((key, value) -> {
            sortedAttributes.put(key, sortJsonNode(value, 0));
        });

        return SingleContextInstanceDTO.builder()
                .key(context.getKey())
                .kind(context.getKind())
                .attributes(sortedAttributes)
                .build();
    }

    private JsonNode sortJsonNode(JsonNode node, int currentDepth) {
        if(currentDepth > MAX_DEPTH) {
            throw new BadRequestException("Context nesting exceeds the maximum allowed depth of " + MAX_DEPTH);
        }

        if (!node.isObject()) {
            return node;
        }

        ObjectNode sortedNode = canonicalMapper.createObjectNode();

        Map<String, JsonNode> sortedMap = new TreeMap<>();

        node.forEachEntry((key, value) -> {
            sortedMap.put(key, sortJsonNode(value, currentDepth + 1));
        });

        sortedNode.setAll(sortedMap);
        return sortedNode;
    }


}
