package com.example.demo.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

// ŞU AN İÇİN TEST MODU: gerçek bir mail sunucusuna (SMTP) bağlanmıyor, doğrulama linkini
// sadece konsola/log dosyasına yazdırıyor. Test ederken kayıt olduktan sonra Spring Boot
// konsolunda "[TEST MODU]" ile başlayan satırı bulup linki kopyalayıp tarayıcıda açabilirsin.
//
// İleride gerçek Gmail/SMTP'ye geçmek istediğinde:
//   1) pom.xml'e spring-boot-starter-mail bağımlılığını ekle
//   2) application.properties'e spring.mail.host/port/username/password ayarlarını gir
//   3) Bu sınıfın içine JavaMailSender enjekte edip sendVerificationEmail metodunun içini değiştir
// AuthService veya Controller'da HİÇBİR DEĞİŞİKLİK gerekmeyecek — onlar sadece bu servisi çağırıyor.
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    public void sendVerificationEmail(String toEmail, String username, String verificationLink) {
        logger.info("==================== [TEST MODU] E-POSTA DOĞRULAMA ====================");
        logger.info("Gerçek bir mail GÖNDERİLMEDİ, sadece test için buraya yazdırıldı.");
        logger.info("Alıcı   : {} <{}>", username, toEmail);
        logger.info("Konu    : HarmoJam - E-posta adresini doğrula");
        logger.info("Link    : {}", verificationLink);
        logger.info("=========================================================================");
    }
}
