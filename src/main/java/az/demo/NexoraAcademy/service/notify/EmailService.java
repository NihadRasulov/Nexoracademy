package az.demo.NexoraAcademy.service.notify;

import az.demo.NexoraAcademy.config.MailProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Thin wrapper around JavaMailSender. Deliberately swallows send failures
 * (no SMTP server configured is the default/dev state) — email delivery is
 * a best-effort side channel and must never block registration, password
 * reset, or notification flows whose real state already lives in the DB.
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;

    public void send(String to, String subject, String body) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(mailProperties.getFrom());
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        try {
            mailSender.send(message);

            log.info("Email sent successfully to {}", to);

        } catch (MailException e) {

            log.error("Email sending failed.", e);

        }
    }
}
