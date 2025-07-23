package me.urninax.flagdelivery.organisation.utils.exceptions;

public class OrganisationAlreadyExistsException extends RuntimeException{
    public OrganisationAlreadyExistsException(){
        super("User is already in organisation");
    }
}
