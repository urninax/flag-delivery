package me.urninax.flagdelivery.flags.utils.exceptions.rule;

import me.urninax.flagdelivery.shared.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class RuleNotFoundException extends ApiException{
    public RuleNotFoundException(){
        super("Rule was not found", HttpStatus.NOT_FOUND, "RULE_NOT_FOUND");
    }
}
