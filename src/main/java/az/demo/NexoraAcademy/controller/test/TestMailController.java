package az.demo.NexoraAcademy.controller.test;

import az.demo.NexoraAcademy.service.notify.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestMailController {

    private final EmailService emailService;

    @PostMapping("/mail")
    public String send() {

        emailService.send(
                "test@gmail.com",
                "MailHog Test",
                "Hello from Nexora Academy!"
        );

        return "Mail sent";
    }

}