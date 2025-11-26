package projectSem4.com.service.client;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

//import projectSem4.com.model.dto.ForgotPasswordRequest;
//import projectSem4.com.model.dto.ResetPasswordRequest;
//import projectSem4.com.model.entities.Users;
import projectSem4.com.model.repositories.PasswordResetTokenRepository;

import projectSem4.com.model.repositories.UserRepository;

@Service
public class PasswordResetService {
//
//    private final UserRepository userRepo;
//    private final PasswordResetTokenRepository tokenRepo;
//    private final BCryptPasswordEncoder encoder;
//
//    @Autowired
//    public PasswordResetService(UserRepository userRepo,
//                                PasswordResetTokenRepository tokenRepo,
//                                BCryptPasswordEncoder encoder) {
//        this.userRepo = userRepo;
//        this.tokenRepo = tokenRepo;
//        this.encoder = encoder;
//    }
//
//    public String requestReset(ForgotPasswordRequest req) {
//        if (req == null || req.getEmail() == null || req.getEmail().isBlank()) return null;
//
//        Users user = userRepo.findByEmail(req.getEmail());
//        if (user == null) return null; // không tiết lộ
//
//        // vô hiệu hoá token cũ
//        tokenRepo.disableActiveTokens(user.getUserId());
//
//        // tạo token raw + hash
//        String raw = UUID.randomUUID().toString(); // gửi qua link cho user
//        byte[] hash = sha256(raw);                 // lưu DB
//
//        LocalDateTime exp = LocalDateTime.now().plusMinutes(30);
//        tokenRepo.insert(user.getUserId(), hash, exp);
//
//        return "/reset-password?token=" + raw; // PROD: gửi email; DEV: trả UI để click
//    }
//
//    public String resetPassword(ResetPasswordRequest req) {
//        if (req == null) return "Invalid request";
//        if (!req.getNewPassword().equals(req.getConfirmPassword())) return "Password confirmation does not match";
//
//        byte[] hash = sha256(req.getToken());
//        TokenRow row = tokenRepo.findByHash(hash);
//        if (row == null) return "Invalid or expired token";
//        if (row.isUsed()) return "Token already used";
//        if (row.getExpiresAt().isBefore(LocalDateTime.now())) return "Token expired";
//
//        Users user = userRepo.findById(row.getUserId());
//        if (user == null) return "User not found";
//
//        user.setPasswordHash(encoder.encode(req.getNewPassword()));
//        user.setUpdatedAt(LocalDateTime.now());
//        String result = userRepo.updateUser(user);
//        if (!result.toLowerCase().contains("thành công")) return "Could not update password";
//
//        tokenRepo.markUsed(hash);
//        return "OK";
//    }

    private byte[] sha256(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(raw.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
