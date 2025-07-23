package me.urninax.flagdelivery.organisation.models.membership;

public enum OrgRole{
    ADMIN(3), WRITER(2), READER(1);

    private final int level;

    OrgRole(int level){
        this.level = level;
    }

    public boolean atLeast(OrgRole other){
        return this.level >= other.level;
    }
}
