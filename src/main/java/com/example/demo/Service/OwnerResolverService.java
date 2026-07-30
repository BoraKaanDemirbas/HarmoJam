package com.example.demo.Service;

import com.example.demo.Model.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

// Bir isteğin "kime ait" olduğunu belirler:
// - Authorization header'ında geçerli bir Bearer token varsa -> üye kullanıcı,
//   kimlik "user:<userId>" formatında döner (hangi cihazdan girerse girsin sabit kalır).
// - Token yoksa/geçersizse -> misafir kullanıcı, X-Device-Id header'ındaki kimlik aynen kullanılır.
//
// Favoriler dışında ileride eklenecek her yeni "kullanıcıya özel veri" özelliği
// (playlist, geçmiş, ayarlar vb.) bu servisi kullanarak aynı mantığı tekrar edebilir.
@Service
public class OwnerResolverService {

    @Autowired
    private AuthService authService;

    public String resolve(String authorizationHeader, String deviceId) {
        String token = extractToken(authorizationHeader);
        if (token != null) {
            Optional<AppUser> user = authService.getUserByToken(token);
            if (user.isPresent()) {
                return "user:" + user.get().getId();
            }
        }

        if (deviceId == null || deviceId.isBlank()) {
            throw new IllegalArgumentException
                    ("Kimlik bulunamadı: giriş yapmalısınız ya da X-Device-Id header'ı göndermelisiniz.");
        }
        return deviceId;
    }

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
