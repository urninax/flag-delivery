package me.urninax.flagdelivery.organisation.ui.models.requests;

import lombok.Data;
import me.urninax.flagdelivery.organisation.models.membership.OrgRole;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Data
public class MembersFilter{
    private List<OrgRole> roles;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate lastSeenAfter;
}
