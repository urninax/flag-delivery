package me.urninax.flagdelivery.user.utils.exceptions;

public class PasswordMismatchException extends RuntimeException{
    public PasswordMismatchException(String message){
        super(message);
    }
}
