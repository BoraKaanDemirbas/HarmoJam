package com.example.demo.Service;

import com.example.demo.Model.AppUser;
import com.example.demo.Model.AuthToken;
import com.example.demo.Model.EmailVerificationToken;
import com.example.demo.Repository.AppUserRepository;
import com.example.demo.Repository.AuthTokenRepository;
import com.example.demo.Repository.EmailVerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class AuthService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final int VERIFICATION_TOKEN_VALID_HOURS = 24;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private AuthTokenRepository authTokenRepository;

    @Autowired
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Autowired
    private EmailService emailService;

    // Doğrulama linkinin başına eklenecek adres. application.properties'te tanımlı değilse
    // (örn. henüz eklenmediyse) varsayılan olarak localhost:9090 kullanılır.
    @Value("${app.base-url:http://localhost:9090}")
    private String appBaseUrl;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // register/login sonrası Controller'a token + kullanıcı bilgisini birlikte döndürmek için
    public static class AuthResult {
        public final String token;
        public final String username;
        public final boolean emailVerified;

        public AuthResult(String token, String username, boolean emailVerified) {
            this.token = token;
            this.username = username;
            this.emailVerified = emailVerified;
        }
    }

    // Kayıt ol: kullanıcı adı/e-posta boşta mı kontrol et, şifreyi hashle, kaydet,
    // doğrulama e-postasını (test modunda: log'a) gönder, ardından direkt giriş yapmış say.
    // NOT: doğrulanmamış olmak girişi ENGELLEMEZ, sadece frontend'de uyarı gösterilir.
    public AuthResult register(String username, String email, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Kullanıcı adı ve şifre boş olamaz.");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("E-posta adresi boş olamaz.");
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("Geçerli bir e-posta adresi girin.");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Şifre en az 6 karakter olmalı.");
        }
        if (appUserRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Bu kullanıcı adı zaten alınmış.");
        }
        if (appUserRepository.existsByEmail(email.trim())) {
            throw new IllegalArgumentException("Bu e-posta adresi zaten kullanılıyor.");
        }

        AppUser user = new AppUser(username, email.trim(), passwordEncoder.encode(password));
        appUserRepository.save(user);

        sendVerificationEmail(user);

        String token = createToken(user.getId());
        return new AuthResult(token, user.getUsername(), user.isEmailVerified());
    }

    // Giriş yap: kullanıcıyı bul, şifreyi doğrula, yeni bir token üret.
    // Eski token'lar silinmiyor -> aynı anda birden fazla cihazda giriş yapılabilir.
    public AuthResult login(String username, String password) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Kullanıcı adı veya şifre hatalı."));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Kullanıcı adı veya şifre hatalı.");
        }

        String token = createToken(user.getId());
        return new AuthResult(token, user.getUsername(), user.isEmailVerified());
    }

    // Authorization header'ından gelen token'ı doğrulayıp kullanıcıyı döndürür.
    public Optional<AppUser> getUserByToken(String token) {
        if (token == null || token.isBlank()) return Optional.empty();
        return authTokenRepository.findByToken(token)
                .flatMap(authToken -> appUserRepository.findById(authToken.getUserId()));
    }

    // Doğrulama linkine tıklanınca (GET /auth/verify-email?token=...) çağrılır.
    // Token geçerli ve süresi dolmamışsa hesabı doğrulanmış işaretler.
    public boolean verifyEmail(String verificationToken) {
        Optional<EmailVerificationToken> tokenOpt = emailVerificationTokenRepository.findByToken(verificationToken);
        if (tokenOpt.isEmpty()) {
            return false;
        }

        EmailVerificationToken evt = tokenOpt.get();
        if (evt.getExpiresAt().isBefore(LocalDateTime.now())) {
            emailVerificationTokenRepository.deleteById(evt.getToken());
            return false;
        }

        Optional<AppUser> userOpt = appUserRepository.findById(evt.getUserId());
        if (userOpt.isEmpty()) {
            return false;
        }

        AppUser user = userOpt.get();
        user.setEmailVerified(true);
        appUserRepository.save(user);

        emailVerificationTokenRepository.deleteByUserId(user.getId());
        return true;
    }

    // Hesabındaki "Resend verification email" butonuna basınca çağrılır.
    public void resendVerificationEmail(Long userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı."));

        if (user.isEmailVerified()) {
            throw new IllegalArgumentException("Bu hesap zaten doğrulanmış.");
        }

        sendVerificationEmail(user);
    }

    // Yeni bir doğrulama token'ı üretir (varsa eskisini siler) ve e-postayı gönderir.
    private void sendVerificationEmail(AppUser user) {
        emailVerificationTokenRepository.deleteByUserId(user.getId());

        String token = UUID.randomUUID().toString();
        EmailVerificationToken evt = new EmailVerificationToken(
                token,
                user.getId(),
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(VERIFICATION_TOKEN_VALID_HOURS)
        );
        emailVerificationTokenRepository.save(evt);

        String link = appBaseUrl + "/auth/verify-email?token=" + token;
        emailService.sendVerificationEmail(user.getEmail(), user.getUsername(), link);
    }

    private String createToken(Long userId) {
        String token = UUID.randomUUID().toString();
        authTokenRepository.save(new AuthToken(token, userId));
        return token;
    }
}
