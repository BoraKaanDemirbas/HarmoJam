package com.example.demo.Service;

import com.example.demo.Model.AppUser;
import com.example.demo.Model.AuthToken;
import com.example.demo.Repository.AppUserRepository;
import com.example.demo.Repository.AuthTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private AuthTokenRepository authTokenRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Kayıt ol: kullanıcı adı boşta mı kontrol et, şifreyi hashle, kaydet,
    // ardından direkt giriş yapmış say (token üret) - kullanıcı ekstra login yapmasın diye.
    public String register(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Kullanıcı adı ve şifre boş olamaz.");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Şifre en az 6 karakter olmalı.");
        }
        if (appUserRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Bu kullanıcı adı zaten alınmış.");
        }

        AppUser user = new AppUser(username, passwordEncoder.encode(password));
        appUserRepository.save(user);

        return createToken(user.getId());
    }

    // Giriş yap: kullanıcıyı bul, şifreyi doğrula, yeni bir token üret.
    // Eski token'lar silinmiyor -> aynı anda birden fazla cihazda giriş yapılabilir.
    public String login(String username, String password) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Kullanıcı adı veya şifre hatalı."));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Kullanıcı adı veya şifre hatalı.");
        }

        return createToken(user.getId());
    }

    // Authorization header'ından gelen token'ı doğrulayıp kullanıcıyı döndürür.
    public Optional<AppUser> getUserByToken(String token) {
        if (token == null || token.isBlank()) return Optional.empty();
        return authTokenRepository.findByToken(token)
                .flatMap(authToken -> appUserRepository.findById(authToken.getUserId()));
    }

    private String createToken(Long userId) {
        String token = UUID.randomUUID().toString();
        authTokenRepository.save(new AuthToken(token, userId));
        return token;
    }
}
