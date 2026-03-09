package me.urninax.flagdelivery.contexts.ui.exceptions;

import me.urninax.flagdelivery.shared.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class ContextKindNotFoundException extends ApiException{
    public ContextKindNotFoundException(){
        super("Context kind was not found", HttpStatus.NOT_FOUND, "CONTEXT_KIND_NOT_FOUND");
    }
}
