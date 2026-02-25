package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions;

public abstract class RuleInstruction extends BaseInstruction{
    @Override
    public boolean requiresEnvironmentKey(){
        return true;
    }
}
