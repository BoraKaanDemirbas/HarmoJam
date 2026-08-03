package com.example.demo.Repository;

import com.example.demo.Model.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, String> {
    Optional<EmailVerificationToken> findByToken(String token);

    // Yeni link üretilirken (kayıt ya da "tekrar gönder") kullanıcının eski, hâlâ geçerli
    // token'larını temizlemek için — aynı anda birden fazla geçerli link dolaşmasın diye.
    void deleteByUserId(Long userId);
}
