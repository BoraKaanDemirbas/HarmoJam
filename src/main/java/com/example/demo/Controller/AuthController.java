package com.example.demo.Controller;

import com.example.demo.Service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    public static class AuthRequest {
        public String username;
        public String email;    // sadece /register kullanır, /login'de yok sayılır
        public String password;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest request) {
        try {
            AuthService.AuthResult result = authService.register(request.username, request.email, request.password);
            return ResponseEntity.ok(toBody(result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            AuthService.AuthResult result = authService.login(request.username, request.password);
            return ResponseEntity.ok(toBody(result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        String token = extractToken(authHeader);
        return authService.getUserByToken(token)
                .<ResponseEntity<?>>map(user -> ResponseEntity.ok(Map.of(
                        "username", user.getUsername(),
                        "emailVerified", user.isEmailVerified()
                )))
                .orElse(ResponseEntity.status(401).body(Map.of("error", "Geçersiz oturum.")));
    }

    // E-postadaki (test modunda: konsoldaki) linke tıklanınca tarayıcı buraya gelir.
    // Sonucu frontend'deki verify-email.html sayfasına yönlendirerek gösteriyoruz,
    // böylece kullanıcı çıplak bir JSON yerine sitenin temasında bir sonuç ekranı görüyor.
    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        boolean success = authService.verifyEmail(token);
        String redirectUrl = success ? "/verify-email.html?status=success" : "/verify-email.html?status=error";
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirectUrl))
                .build();
    }

    // Hesabına giriş yapmış ama e-postasını henüz doğrulamamış kullanıcı için "tekrar gönder" butonu
    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        String token = extractToken(authHeader);
        return authService.getUserByToken(token)
                .<ResponseEntity<?>>map(user -> {
                    try {
                        authService.resendVerificationEmail(user.getId());
                        return ResponseEntity.ok().build();
                    } catch (IllegalArgumentException e) {
                        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
                    }
                })
                .orElse(ResponseEntity.status(401).body(Map.of("error", "Geçersiz oturum.")));
    }

    private Map<String, Object> toBody(AuthService.AuthResult result) {
        Map<String, Object> body = new HashMap<>();
        body.put("token", result.token);
        body.put("username", result.username);
        body.put("emailVerified", result.emailVerified);
        return body;
    }

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
