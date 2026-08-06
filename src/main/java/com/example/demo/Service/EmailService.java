package com.example.demo.Service;


import org.aspectj.weaver.patterns.IToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;


@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    public void sendVerificationEmail(String toEmail, String username, String verificationLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(toEmail);
            message.setSubject("HarmoJam - E-posta adresini doğrula");
            message.setText(
                    "Merhaba " + username + ",\n\n" +
                    "HarmoJam hesabını doğrulamak için aşağıdaki linke tıkla:\n\n" +
                    verificationLink + "\n\n" +
                    "Bu link 24 saat geçerlidir. Bu isteği sen yapmadıysan bu e-postayı yok sayabilirsin."
            );
            mailSender.send(message);
            logger.info("Doğrulama e-postası gönderildi: {}",toEmail);

        }catch (Exception e){
            logger.error("E-posta gönderilemedi ({}): {}", toEmail, e.getMessage());
            logger.error("Detay:", e);
        }




//        logger.info("==================== [TEST MODU] E-POSTA DOĞRULAMA ====================");
//        logger.info("Gerçek bir mail GÖNDERİLMEDİ, sadece test için buraya yazdırıldı.");
//        logger.info("Alıcı   : {} <{}>", username, toEmail);
//        logger.info("Konu    : HarmoJam - E-posta adresini doğrula");
//        logger.info("Link    : {}", verificationLink);
//        logger.info("=========================================================================");
    }
}
