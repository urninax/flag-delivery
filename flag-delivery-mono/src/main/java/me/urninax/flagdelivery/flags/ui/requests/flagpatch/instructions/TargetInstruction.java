package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions;

public abstract class TargetInstruction extends BaseInstruction{
    @Override
    public boolean requiresEnvironmentKey(){
        return true;
    }
}
