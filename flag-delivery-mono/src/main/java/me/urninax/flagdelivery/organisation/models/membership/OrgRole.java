package me.urninax.flagdelivery.organisation.models.membership;

public enum OrgRole{
    OWNER(4), ADMIN(3), WRITER(2), READER(1), NONE(0);

    private final int level;

    OrgRole(int level){
        this.level = level;
    }

    public boolean higherThan(OrgRole other){
        return this.level > other.level;
    }

    public boolean lowerThan(OrgRole other){
        return this.level < other.level;
    }

    public boolean higherOrEqual(OrgRole other){
        return this.level >= other.level;
    }

    public boolean lowerOrEqual(OrgRole other){
        return this.level <= other.level;
    }
}
