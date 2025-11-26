package projectSem4.com.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import projectSem4.com.model.entities.RefreshToken;
import projectSem4.com.model.entities.UserDevice;
import projectSem4.com.model.repositories.RefreshTokenRepository;
import projectSem4.com.model.repositories.UserDeviceRepository;

@Service
public class TokenDeviceService {

    private final RefreshTokenRepository refreshTokenRepo;
    private final UserDeviceRepository userDeviceRepo;

    public TokenDeviceService(RefreshTokenRepository refreshTokenRepo,
                              UserDeviceRepository userDeviceRepo) {
        this.refreshTokenRepo = refreshTokenRepo;
        this.userDeviceRepo = userDeviceRepo;
    }

    // Quản lý Refresh Token
 // Refresh Token
    public void createOrUpdateRefreshToken(Integer userId, String token, LocalDateTime expiresAt, String deviceId) {
        RefreshToken rt = refreshTokenRepo.findByUserIdAndDeviceId(userId, deviceId);

        if (rt != null) {
            
            rt.setToken(token);
            rt.setIssuedAt(LocalDateTime.now());
            rt.setExpiresAt(expiresAt);
            rt.setRevoked(false);
            refreshTokenRepo.updateToken(rt); // update
        } else {
            RefreshToken rt1 = new RefreshToken();
            rt1.setUserId(userId);
            rt1.setDeviceId(deviceId);
            rt1.setToken(token);
            rt1.setIssuedAt(LocalDateTime.now());
            rt1.setExpiresAt(expiresAt);
            rt1.setRevoked(false);
            refreshTokenRepo.save(rt1); // insert mới
        }
    }

    public RefreshToken getByToken(String token) {
        return refreshTokenRepo.findByToken(token);
    }

    public void revokeTokenByDevice(Integer userId, String deviceId) {
        refreshTokenRepo.revokeByUserAndDevice(userId, deviceId);
    }

    public void revokeAllTokens(Integer userId) {
        refreshTokenRepo.revokeAllByUser(userId);
    }

    // User Device
    public void saveOrUpdateUserDevice(UserDevice device) {
        Optional<UserDevice> existing = userDeviceRepo.findByDeviceId(device.getDeviceId());
        if (existing.isPresent()) {
            userDeviceRepo.updateDeviceInfo(
                existing.get().getUserDeviceId(),
                device.getIpAddress(),
                device.getUserAgent(),
                device.getDeviceType(),
                LocalDateTime.now()
            );
        } else {
            device.setLastSeenAt(LocalDateTime.now());
            userDeviceRepo.save(device);
        }
    }

    public List<UserDevice> getDevicesByUser(int userId) {
        return userDeviceRepo.findByUserId(userId);
    }

    public void removeDevice(int deviceId) {
        userDeviceRepo.deleteById(deviceId);
    }

    // Logout everywhere
    public void revokeAllTokensAndDevices(Integer userId) {
        refreshTokenRepo.revokeAllByUser(userId);
        userDeviceRepo.deleteByUserId(userId); // hoặc chỉ update lastSeenAt tuỳ nhu cầu
    }
}
