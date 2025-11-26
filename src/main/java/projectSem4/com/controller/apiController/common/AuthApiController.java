package projectSem4.com.controller.apiController.common;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import projectSem4.com.dto.AuthResponse;
import projectSem4.com.dto.LoginRequest;
import projectSem4.com.dto.LogoutRequest;
import projectSem4.com.dto.RegisterRequest;
import projectSem4.com.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {
	@Autowired
    private AuthService authService;

 @PostMapping("/login")
 public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request,
         							HttpServletRequest httpRequest) {
     String userAgent = httpRequest.getHeader("User-Agent");
     String ipAddress = httpRequest.getRemoteAddr();
     String deviceId = request.getDeviceId();     // thêm field này vào LoginRequest
     String deviceType = request.getDeviceType(); // thêm field này vào LoginRequest

     return ResponseEntity.ok(
             authService.login(request, userAgent, ipAddress, deviceId, deviceType)
     );
 }

 @PostMapping("/refresh")
 public ResponseEntity<?> refresh(@RequestBody String refreshToken) {
     return ResponseEntity.ok(authService.refreshToken(refreshToken));
 }

 @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request,@RequestBody LogoutRequest req) {
	 Long userId = Long.valueOf((String) request.getAttribute("userId"));
	    authService.logoutByTokenAndDevice(userId, req.getRefreshToken(), req.getDeviceId());
	    return ResponseEntity.ok(Map.of("message","Logout success"));
    }
 @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        // Validate dữ liệu nếu muốn (email, password, etc)
        String result = authService.register(request);

        if ("OK".equals(result)) {
            // trả JSON success
            return ResponseEntity.ok(Map.of(
                "message", "Registration successful! Please log in."
            ));
        } else {
            // trả JSON lỗi
            return ResponseEntity.badRequest().body(Map.of(
                "error", result
            ));
        }
    }
}
