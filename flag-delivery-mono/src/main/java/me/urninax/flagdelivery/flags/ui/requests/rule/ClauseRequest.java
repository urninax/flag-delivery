package me.urninax.flagdelivery.flags.ui.requests.rule;

import me.urninax.flagdelivery.flags.models.rule.ClauseOp;

import java.util.List;

public record ClauseRequest(
        String contextKind,
        String attribute,
        ClauseOp op,
        boolean negate,
        List<String> values
){
}
