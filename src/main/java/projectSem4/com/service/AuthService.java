package projectSem4.com.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;


import projectSem4.com.dto.AuthResponse;
import projectSem4.com.dto.LoginRequest;

import projectSem4.com.dto.RegisterRequest;
import projectSem4.com.model.entities.RefreshToken;
import projectSem4.com.model.entities.User;
import projectSem4.com.model.entities.UserDevice;
import projectSem4.com.model.repositories.RefreshTokenRepository;
import projectSem4.com.model.repositories.UserDeviceRepository;
import projectSem4.com.model.repositories.UserRepository;

@Service
public class AuthService {

    // TTL chỉ để hiển thị trong email; TTL thực tế do OtpService quản lý
    @Value("${app.security.otp-ttl-minutes:10}")
    private long otpTtlMinutes;

	  private final UserRepository userRepo;
	   private final TokenDeviceService tokenDeviceService;
	    private final JwtProvider jwtProvider;
   private final RefreshTokenRepository refreshTokenRepo;
    private final EmailService emailService;
    private final OtpService otpService;
 @Autowired
	    private  RefreshTokenRepository tokenRepo;
	    @Autowired
	    private  UserDeviceRepository deviceRepo;

	    public AuthService(UserRepository userRepo,
	                       TokenDeviceService tokenDeviceService,
	                       JwtProvider jwtProvider,
  RefreshTokenRepository refreshTokenRepo,
                       UserDeviceRepository deviceRepo,
                       EmailService emailService,
                       OtpService otpService) {
	        this.userRepo = userRepo;
	        this.tokenDeviceService = tokenDeviceService;
	        this.jwtProvider = jwtProvider;
this.refreshTokenRepo = refreshTokenRepo;
        this.deviceRepo = deviceRepo;
        this.emailService = emailService;
        this.otpService = otpService;
    }

    // ===== LOGIN =====
	    public AuthResponse login(LoginRequest req, String userAgent, String ipAddress, String deviceId, String deviceType) {
	        // 1. Kiểm tra user
	        User user = userRepo.findByUsername(req.getEmail());
	        if (user == null) {
	            throw new RuntimeException("Email không tồn tại");
	        }   

	        if (!BCrypt.checkpw(req.getPassword(), user.getPasswordHash())) {
	            throw new RuntimeException("Invalid credentials");
	        }

	        // 2. Sinh AccessToken + RefreshToken
	        String accessToken = jwtProvider.generateAccessToken(user);
	        String refreshToken = jwtProvider.generateRefreshToken(user);

	        // 3. Lưu RefreshToken vào DB (bổ sung deviceId)
	        tokenDeviceService.createOrUpdateRefreshToken(user.getUserId(), refreshToken, LocalDateTime.now().plusDays(7),deviceId);

	        // 4. Lưu hoặc cập nhật thông tin thiết bị
	        UserDevice device = new UserDevice();
	        device.setUserId(user.getUserId());
	        device.setDeviceId(deviceId);
	        device.setDeviceType(deviceType);
	        device.setUserAgent(userAgent);
	        device.setIpAddress(ipAddress);
	        tokenDeviceService.saveOrUpdateUserDevice(device);

	        // 5. Lấy roles
	        List<String> roles = new ArrayList<>();
	        roles.add(user.getRoleId());

	        // 6. Trả response
	        return new AuthResponse(accessToken, refreshToken, user.getUserId(), roles);
	    }
	    
	   

	    public void logoutByTokenAndDevice(Long requestUserId, String refreshToken, String deviceId) {
	        var a = tokenDeviceService.getByToken(refreshToken);
	        if (a == null) return; // idempotent

	        var ownerUserId = a.getUserId();
	        String tokenDeviceId = a.getDeviceId();

	        if (!ownerUserId.equals(requestUserId) || !deviceId.equals(tokenDeviceId)) {
            throw new RuntimeException("Token does not belong to the current user/device");
	        
	        }

	        tokenRepo.revokeByTokenAndDevice(refreshToken, deviceId);
	        deviceRepo.touchLastSeen(requestUserId, deviceId);
	    }

	    public AuthResponse refreshToken(String oldRefreshToken) {
	        RefreshToken rt = tokenDeviceService.getByToken(oldRefreshToken);
	        		if(rt == null) {
	        			new RuntimeException("Invalid refresh token");
	        		}
	        		
	                

	        if (rt.getExpiresAt().isBefore(LocalDateTime.now()) || rt.isRevoked()) {
	            throw new RuntimeException("Token expired or revoked");
	        }

	        User user = userRepo.findById(rt.getUserId());
	        		if (user == null) {
	    	        	throw new RuntimeException("User not found");
	    	        }    
	               

	        String newAccessToken = jwtProvider.generateAccessToken(user);
	        String newRefreshToken = jwtProvider.generateRefreshToken(user);

	        // Cập nhật lại Refresh Token mới
	        tokenDeviceService.createOrUpdateRefreshToken(user.getUserId(), newRefreshToken, LocalDateTime.now().plusDays(7),rt.getDeviceId());
	        

	        List<String> roles = new ArrayList<>();
	        roles.add(user.getRoleId());
	        
	        return new AuthResponse(newAccessToken, newRefreshToken,user.getUserId(),roles);
	    }
 // ===== REGISTER =====
    public String register(RegisterRequest req) {
        if (req == null) return "Invalid request";
        if (req.getPassword() == null || !req.getPassword().equals(req.getConfirmPassword())) {
            return "Password confirmation does not match";
        }
        if (req.getEmail() == null || req.getEmail().isBlank()) return "Email is required";
        if (userRepo.existsByEmail(req.getEmail())) return "Email already exists";

        String hash = BCrypt.hashpw(req.getPassword(), BCrypt.gensalt());
        LocalDateTime now = LocalDateTime.now();

        User u = new User(
                null, "BUYER", req.getFullName(), req.getEmail(), hash,
                null, req.getPhoneNumber(), 1, null, now, now);

        try {
            String result = userRepo.createUser(u);
            return result.toLowerCase().contains("success")
                    || result.toLowerCase().contains("thành công") ? "OK" : result;
        } catch (DuplicateKeyException e) {
            return "Email already exists";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error creating account";
        }
    }

    // ===== FORGOT PASSWORD qua OTP 4 số =====
    public void requestPasswordReset(String email, String userAgent, String ipAddress) {
        User user = userRepo.findByEmail(email);
        if (user == null) return; // do not leak information

        // Generate OTP via OtpService
        String code = otpService.generate(email);

        // Send OTP email
        emailService.sendOtpEmail(user, code);

        // (optional) dev log
//        System.out.println("[DEBUG] OTP for " + email + " = " + code + " (valid ~" + otpTtlMinutes + " mins)");
    }

    public void resetPasswordWithOtp(String email, String otp, String newPassword, String confirmPassword) {
        if (email == null || email.isBlank()) throw new RuntimeException("Email is required");
        if (otp == null || otp.isBlank()) throw new RuntimeException("OTP is required");
        if (newPassword == null || !newPassword.equals(confirmPassword)) {
            throw new RuntimeException("Password confirmation does not match");
        }

        // Validate OTP via OtpService
        boolean ok = otpService.verify(email, otp);
        if (!ok) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        User user = userRepo.findByEmail(email);
        if (user == null) throw new RuntimeException("User not found");

        String hash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        userRepo.updatePasswordHash(user.getUserId(), hash);

        // Invalidate OTP + revoke refresh tokens to force re-login
        otpService.invalidate(email);
        refreshTokenRepo.revokeAllByUser(user.getUserId());
    }

    // ===== Flow tách bước (đã verify OTP trước, chỉ đổi mật khẩu) =====
    public void updatePasswordAfterOtpVerified(String email, String password, String confirmPassword) {
        if (password == null || !password.equals(confirmPassword)) {
            throw new RuntimeException("Password confirmation does not match");
        }
        User user = userRepo.findByEmail(email);
        if (user == null) throw new RuntimeException("User not found");

        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        userRepo.updatePasswordHash(user.getUserId(), hash);
        refreshTokenRepo.revokeAllByUser(user.getUserId());
    }
}
