package projectSem4.com.model.repositories;

import java.time.LocalDateTime;

import projectSem4.com.model.entities.PasswordResetToken;



public interface PasswordResetTokenRepository {

    /** H?y c�c token chua d�ng c?a user v� t?o token m?i */
    void createOrReplace(Integer userId,
                         String tokenPlain,
                         LocalDateTime expiresAt,
                         String userAgent,
                         String requestIp);

    /** L?y token c�n h?n, chua d�ng (tr? null n?u kh�ng c�) */
    PasswordResetToken findValidByToken(String tokenPlain);

    /** ��nh d?u 1 token d� d�ng */
    void markUsed(Integer tokenId);

    /** H?y (d�nh d?u used) t?t c? token chua d�ng c?a 1 user */
    void invalidateAllByUserId(Integer userId);
}
