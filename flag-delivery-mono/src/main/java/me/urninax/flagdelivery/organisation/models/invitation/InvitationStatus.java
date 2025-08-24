package me.urninax.flagdelivery.organisation.models.invitation;

public enum InvitationStatus{
    PENDING, ACCEPTED, DECLINED, REVOKED, EXPIRED;

    public boolean isActive(){
        return this == PENDING;
    }
}
