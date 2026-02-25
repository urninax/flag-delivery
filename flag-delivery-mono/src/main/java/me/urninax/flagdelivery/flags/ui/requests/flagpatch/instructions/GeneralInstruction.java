package me.urninax.flagdelivery.flags.ui.requests.flagpatch.instructions;

public abstract class GeneralInstruction extends BaseInstruction{
    @Override
    public boolean requiresEnvironmentKey(){
        return true;
    }
}
