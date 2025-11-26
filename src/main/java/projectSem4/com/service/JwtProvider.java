package projectSem4.com.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import projectSem4.com.model.entities.User;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

@Component
public class JwtProvider {


	@Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;
    

    @Value("${jwt.refreshExpiration}")
    private long refreshExpirationMs;

    private Key key;

    @PostConstruct
    public void init() {
    	System.out.println("🔑 jwtSecret = " + jwtSecret);
        this.key = Keys.hmacShaKeyFor(io.jsonwebtoken.io.Decoders.BASE64.decode(jwtSecret));
    }

    // --- GENERATE ACCESS TOKEN ---
    public String generateAccessToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("userId", user.getUserId().intValue())          // Ép Integer
                .claim("roles", Arrays.asList(user.getRoleId().toString())) // Lưu List<String>
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    // --- GENERATE REFRESH TOKEN ---
    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("userId", user.getUserId().intValue())
                .claim("roles", Arrays.asList(user.getRoleId().toString()))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    // --- VALIDATE TOKEN ---
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
        	e.printStackTrace();
            return false;
        }
    }

//    // --- LẤY CLAIMS ---
//    public Claims getClaimsFromToken(String token) {
//        return Jwts.parserBuilder()
//                .setSigningKey(key)
//                .build()
//                .parseClaimsJws(token)
//                .getBody();
//    }
//
//    // --- LẤY USERNAME (EMAIL) ---
//    public String getUsernameFromToken(String token) {
//        return getClaimsFromToken(token).getSubject();
//    }
//
//    // --- LẤY USERID ---
//    public Integer getUserIdFromToken(String token) {
//        Object userIdObj = getClaimsFromToken(token).get("userId");
//        if (userIdObj instanceof Integer) {
//            return (Integer) userIdObj;
//        } else if (userIdObj instanceof Number) {
//            return ((Number) userIdObj).intValue();
//        } else {
//            throw new IllegalStateException("userId in token is not a number");
//        }
//    }
//
//    // --- LẤY ROLES ---
//    @SuppressWarnings("unchecked")
//    public List<String> getUserRoleFromToken(String token) {
//        Object rolesObj = getClaimsFromToken(token).get("roles");
//        if (rolesObj instanceof List) {
//            return (List<String>) rolesObj;
//        } else {
//            throw new IllegalStateException("roles in token is not a List");
//        }
//    }
    
 // Lấy userId từ token
    public String getUserId(String token) {
    	try {
    		return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
		} catch (Exception e) {
			// TODO: handle exception
			throw new IllegalStateException("userId in token is not a number");
		}
        
    }

    // Lấy role từ token
    	public List<String> getRoles(String token) {
        Claims claims = getClaims(token);
        Object rolesClaim = claims.get("roles");

        if (rolesClaim == null) {
            return Collections.emptyList();
        }

        if (rolesClaim instanceof List<?>) {
            return ((List<?>) rolesClaim).stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
        }

        if (rolesClaim instanceof String) {
            return Arrays.stream(((String) rolesClaim).split(","))
                         .map(String::trim)
                         .filter(s -> !s.isEmpty())
                         .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    // Lấy username từ token (optional)
    public String getUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("username", String.class);
    }
    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}