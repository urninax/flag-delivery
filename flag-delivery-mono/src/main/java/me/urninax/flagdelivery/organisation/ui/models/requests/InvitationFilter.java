package me.urninax.flagdelivery.organisation.ui.models.requests;

import lombok.Data;
import me.urninax.flagdelivery.organisation.models.invitation.InvitationStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Data
public class InvitationFilter{
    private List<InvitationStatus> status;
    private String email;
    private String invitedBy;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate createdFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate createdTo;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate expiresFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate expiresTo;
}
