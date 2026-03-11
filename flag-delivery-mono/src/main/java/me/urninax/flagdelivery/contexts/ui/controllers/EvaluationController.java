package me.urninax.flagdelivery.contexts.ui.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.contexts.services.EvaluationService;
import me.urninax.flagdelivery.contexts.ui.requests.EvaluationContextRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects/{projectKey}/environments/{environmentKey}/flags")
@RequiredArgsConstructor
public class EvaluationController{
    private final EvaluationService evaluationService;

    @PostMapping("/evaluate")
    public ResponseEntity<?> evaluate(@PathVariable String projectKey,
                                      @PathVariable String environmentKey,
                                      @RequestBody @Valid EvaluationContextRequest request){
        JsonNode evaluationResult = evaluationService.evaluate(projectKey, environmentKey, request);
        return ResponseEntity.ok(evaluationResult);
    }
}
