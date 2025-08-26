package me.urninax.flagdelivery.organisation.services;

import me.urninax.flagdelivery.organisation.shared.InvitationMailDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class MailService{
    private final JavaMailSender mailSender;

    @Value("${app.mail.default-from}")
    private String defaultFrom;

    @Autowired
    public MailService(JavaMailSender mailSender){
        this.mailSender = mailSender;
    }

    public void sendInvitation(InvitationMailDTO inv){
        String subject = String.format("You're invited to join %s", inv.getOrganisationName());
        String template = """
                You have been invited to join {{orgName}}.
                
                Role: {{role}}
                Message from the inviter: {{message}}
                
                To accept the invitation, please send POST request to the following URL:
                flagdelivery.urninax.me/api/v1/invitations/{{uuid}}.{{token}}/accept
                
                To decline, send POST request to:
                flagdelivery.urninax.me/api/v1/invitations/{{uuid}}.{{token}}/decline
                
                This invitation will expire on {{expirationDate}}
                
                If you were not expecting this invitation, you can safely ignore this email.
                
                Best regards,
                The FlagDelivery Team
                """;
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("MMM dd, yyyy HH:mm 'UTC'", Locale.ENGLISH);

        String formattedExpirationDate = inv.getExpiresAt()
                .atZone(ZoneId.of("UTC")).format(formatter);

        String result = template.replace("{{orgName}}", inv.getOrganisationName())
                .replace("{{uuid}}", inv.getInvitationId().toString())
                .replace("{{role}}", inv.getRole().toString())
                .replace("{{message}}", inv.getMessage())
                .replace("{{token}}", inv.getToken())
                .replace("{{expirationDate}}", formattedExpirationDate);


        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(defaultFrom);
        msg.setTo(inv.getEmail());
        msg.setSubject(subject);
        msg.setText(result);

        mailSender.send(msg);
    }

    public void sendInviteAcceptedGreeting(InvitationMailDTO inv){
        String subject = String.format("✅ You have joined %s", inv.getOrganisationName());
        String template = """
                You have successfully accepted the invitation and joined the organisation {{orgName}} on FlagDelivery.
                
                Your assigned role: {{role}}
                This role gives you access according to the organisation’s permissions.
                
                You can now start collaborating with your team.
                
                If you were not expecting this email, please ignore it.
                
                Best regards,
                The FlagDelivery Team
                """;
        String result = template.replace("{{orgName}}", inv.getOrganisationName())
                .replace("{{role}}", inv.getRole().toString());

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(defaultFrom);
        msg.setTo(inv.getEmail());
        msg.setSubject(subject);
        msg.setText(result);

        mailSender.send(msg);
    }
}
