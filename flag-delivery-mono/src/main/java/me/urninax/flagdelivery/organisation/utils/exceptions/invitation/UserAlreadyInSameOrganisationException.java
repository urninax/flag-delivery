package me.urninax.flagdelivery.organisation.utils.exceptions.invitation;

import me.urninax.flagdelivery.shared.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class UserAlreadyInSameOrganisationException extends ApiException {
  public UserAlreadyInSameOrganisationException() {
    super("User is already in the same organisation as the inviter.", HttpStatus.CONFLICT, "USER_ALREADY_IN_SAME_ORG");
  }
}
