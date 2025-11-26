package projectSem4.com.interceptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import projectSem4.com.service.JwtProvider;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {
	
	@Autowired
	private JwtProvider jwtProvider;

	private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final Map<String, List<String>> ROLE_PATHS = Map.of(
        "ADMIN", List.of("/admin/**", "/seller/**", "/api/**", "/shop/**"),
        "SELLER", List.of("/shop/**","/seller/**", "/api/**", "/orders/**", "/api/ghn/**", "/api/seller/**"),
        "BUYER", List.of("/cart/**", "/api/cart/**", "/checkout/**", "/api/checkout/**", "/customer/**", "/api/customer/**", "/api/ghn/**", "/api/registerSeller/**"),
        "SHIPPER", List.of("api/shipper/**","api/shipment/**","api/shipment_history/**","shipper/**")
    );

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        try {
            String path = req.getRequestURI().substring(req.getContextPath().length());
            boolean isApi = path.startsWith("/api/");
            
         // 🧩 DEBUG: log mọi request đi qua Interceptor
            System.out.println("--------------------------------------------------");
            System.out.println("[JwtAuthInterceptor] Incoming path: " + path);
            System.out.println("Full URL: " + req.getRequestURL());
            System.out.println("Method: " + req.getMethod());

            // --- Lấy accessToken ---
            String accessToken = null;
            String authHeader = req.getHeader("Authorization");
           
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
            	
                accessToken = authHeader.substring(7);
               
            } else {
                Cookie[] cookies = req.getCookies();
                if (cookies != null) {
                    for (Cookie cookie : cookies) {
                        if ("ACCESS_TOKEN".equals(cookie.getName())) accessToken = cookie.getValue();
                    }
                }
            }

            HttpSession session = req.getSession(true);
            List<String> roles = null;
            Integer userId = null;

            // --- Token hợp lệ ---
            if (accessToken != null && jwtProvider.validateToken(accessToken)) {
            	 System.out.println("vao");
                Claims claims = jwtProvider.getClaims(accessToken);
                roles = jwtProvider.getRoles(accessToken);
                	
                // Ép về Integer ngay từ đầu
                
                userId = ((Number) claims.get("userId")).intValue();
                session.setAttribute("roles", roles);
                session.setAttribute("userId", userId);

                req.setAttribute("roles", roles);
                req.setAttribute("userId", userId);

            } else if (session.getAttribute("userId") != null && session.getAttribute("roles") != null) {
                userId = (Integer) session.getAttribute("userId");
                @SuppressWarnings("unchecked")
                List<String> rolesFromSession = (List<String>) session.getAttribute("roles");
                roles = rolesFromSession;

                req.setAttribute("userId", userId);
                req.setAttribute("roles", roles);
            }

            if (roles == null || roles.isEmpty()) {
                if (isApi) return writeError(res, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                else { res.sendRedirect("/login"); return false; }
            }

            return checkAuthorization(roles, path, res);

        } catch (Exception e) {
            e.printStackTrace();
            if (req.getRequestURI().startsWith("/api")) 
                return writeError(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal error: " + e.getMessage());
            else { res.sendRedirect("/login?error=internal"); return false; }
        }
    }

    private boolean checkAuthorization(List<String> roles, String path, HttpServletResponse res) throws IOException {
        System.out.println("[JwtAuthInterceptor] CheckAuthorization - roles=" + roles + ", path=" + path);
        
        for (String r : roles) {
            System.out.println(r);
        }
        List<String> checkRoles = new ArrayList<>(roles);
        checkRoles.addAll(List.of("BUYER", "SELLER", "SHIPPER"));

        for (String role : checkRoles) {
            List<String> allows = ROLE_PATHS.getOrDefault(role, List.of());
            for (String p : allows) {
                if (pathMatcher.match(p, path)) {
                	System.out.println("tra ve true");
                	System.out.println(path);
                	return true;
                }
                
            }
        }
        System.out.println("tra ve false");
        return redirectHome(roles, res);
    }

    private boolean writeError(HttpServletResponse res, int status, String message) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write("{\"error\": \"" + message + "\"}");
        return false;
    }

    private boolean redirectHome(List<String> roles, HttpServletResponse res) throws IOException {
    	for(String role : roles) {
    		System.out.println("vao redirec so");
    		if (role != null) {
                if (role.contains("SELLER")) { res.sendRedirect("/seller"); return false; }
                else if (role.contains("BUYER")) { res.sendRedirect("/"); return false; }
                else if (role.contains("ADMIN")) { res.sendRedirect("/admin/product/"); return false; }
                else if (role.contains("SHIPPER")) { res.sendRedirect("/shipper"); return false; }
            }
    	}
        
        res.sendRedirect("/home");
        return false;
    }
	
}
