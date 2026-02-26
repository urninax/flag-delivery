package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions;

public abstract class PrerequisiteInstruction extends BaseInstruction{
    @Override
    public boolean requiresEnvironmentKey(){
        return true;
    }
}
