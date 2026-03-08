package me.urninax.flagdelivery.contexts.ui.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.contexts.services.ContextKindService;
import me.urninax.flagdelivery.contexts.shared.ContextKindDTO;
import me.urninax.flagdelivery.contexts.ui.requests.CreateContextKindRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects/{projectKey}/context-kinds")
@RequiredArgsConstructor
@Validated
public class ContextKindsController{
    private final ContextKindService contextKindService;

    @PutMapping("/{contextKindKey}")
    public ResponseEntity<?> createOrUpdateContextKind(@PathVariable String projectKey,
                                                       @PathVariable @Size(min = 2, max = 64, message = "Context kind key should be 2-64 characters.")
                                                       @NotEmpty(message = "Context kind key cannot be empty") String contextKindKey,
                                                       @Valid @RequestBody CreateContextKindRequest request){
        ContextKindDTO contextKind = contextKindService.createOrUpdateContextKind(projectKey, contextKindKey, request);

        return ResponseEntity.ok(contextKind);
    }
}
