package projectSem4.com.controller.webController.common;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;

import projectSem4.com.dto.AuthResponse;
import projectSem4.com.dto.LoginRequest;
import projectSem4.com.dto.RegisterRequest;
import projectSem4.com.model.entities.RefreshToken;
import projectSem4.com.model.entities.User;
import projectSem4.com.model.repositories.UserRepository;
import projectSem4.com.service.AuthService;
import projectSem4.com.service.JwtProvider;
import projectSem4.com.service.TokenDeviceService;
import projectSem4.com.service.admin.ShopService;
import projectSem4.com.service.shipper.ShipperService;

@Controller
public class LoginController {

	private final AuthService authService;
	@Autowired
	private ShopService shopService;
	
	@Autowired
	private ShipperService shipperService;
	
	 private static final ObjectMapper objectMapper = new ObjectMapper();

	    @Autowired private TokenDeviceService tokenDeviceService;
	    @Autowired private JwtProvider jwtProvider;
	    @Autowired private UserRepository userRepo;

	@Autowired
	public LoginController(AuthService authService) {
		this.authService = authService;
	}

	@GetMapping("/login")
	public String loginPage(Model model, HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value = "redirect", required = false) String redirect) {
		
		HttpSession session = request.getSession(false);

        // --- 1️⃣ Session fallback ---
        if (session != null && session.getAttribute("userId") != null) {
            try {
                Integer userId = (Integer) session.getAttribute("userId");
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) session.getAttribute("roles");
                return redirectByRole(userId, roles, session);
            } catch (Exception e) { e.printStackTrace(); }
        }

        if (redirect != null) request.getSession(true).setAttribute("redirectAfterLogin", redirect);

        // --- 2️⃣ JWT check ---
        Cookie[] cookies = request.getCookies();
        String accessToken = null, refreshToken = null;
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("ACCESS_TOKEN".equals(c.getName())) accessToken = c.getValue();
                if ("REFRESH_TOKEN".equals(c.getName())) refreshToken = c.getValue();
            }
        }

        if (accessToken != null && jwtProvider.validateToken(accessToken)) {
        	 Integer userId = (Integer) request.getAttribute("userId");
             List<String> roles = (List<String>) request.getAttribute("roles");

            session = request.getSession(true);
            session.setAttribute("userId", userId);
            session.setAttribute("roles", roles);

            return redirectByRole(userId, roles, session);
        }

        // --- 3️⃣ Refresh token ---
        if (refreshToken != null) {
            try {
                RefreshToken rt = tokenDeviceService.getByToken(refreshToken);
                if (rt != null && !rt.isRevoked() && rt.getExpiresAt().isAfter(LocalDateTime.now())) {
                    User user = userRepo.findById(rt.getUserId());
                    if (user != null) {
                        String newAccess = jwtProvider.generateAccessToken(user);
                        String newRefresh = jwtProvider.generateRefreshToken(user);

                        tokenDeviceService.createOrUpdateRefreshToken(
                                user.getUserId(), newRefresh, LocalDateTime.now().plusDays(7), rt.getDeviceId()
                        );

                        response.addCookie(cookieWith("ACCESS_TOKEN", newAccess, 60 * 15));
                        response.addCookie(cookieWith("REFRESH_TOKEN", newRefresh, 60 * 60 * 24 * 7));

                        session = request.getSession(true);
                        Integer userIdInt = user.getUserId().intValue();
                        session.setAttribute("userId", userIdInt);
                        List<String> roles = Arrays.asList(user.getRoleId().toString());
                        session.setAttribute("roles", roles);

                        return redirectByRole(userIdInt, roles, session);
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
		
		
		if (!model.containsAttribute("registerRequest")) {
			model.addAttribute("registerRequest", new RegisterRequest());
		}
		if (!model.containsAttribute("activeTab")) {
			model.addAttribute("activeTab", "login");
			
		}
		 model.addAttribute("loginRequest", new LoginRequest());
		return "client/common/login";
	}

	@PostMapping("/login")
	public String loginSubmit(@ModelAttribute LoginRequest req, HttpServletRequest request,
			HttpServletResponse response, Model model, RedirectAttributes ra) {
		try {
			AuthResponse auth = authService.login(req, request.getHeader("User-Agent"), request.getRemoteAddr(), "WEB",
					"WEB_BROWSER");

			if (auth == null) {
				ra.addFlashAttribute("error", "Invalid email or password");
				ra.addFlashAttribute("activeTab", "login");
				return "redirect:/login";
			}

			HttpSession session = request.getSession(true);
			Integer userId = auth.getUserId();
			session.setAttribute("userId", userId);
			session.setAttribute("roles", auth.getRoles());

			response.addCookie(cookieWith("ACCESS_TOKEN", auth.getAccessToken(), 60 * 15));
			response.addCookie(cookieWith("REFRESH_TOKEN", auth.getRefreshToken(), 60 * 60 * 24 * 7));

			String redirectAfter = (String) session.getAttribute("redirectAfterLogin");
			if (redirectAfter != null) {
				session.removeAttribute("redirectAfterLogin");
				return "redirect:" + redirectAfter;
			}

			return redirectByRole(userId, auth.getRoles(), session);

		} catch (Exception e) {
			model.addAttribute("error", e.getMessage());
			return "client/common/login";
		}
	}

	@PostMapping("/do-login")
	public String doLogin(@RequestParam("user-email") String email, @RequestParam("user-password") String password,
			jakarta.servlet.http.HttpServletRequest request, RedirectAttributes ra, HttpSession session, HttpServletResponse response) {

		LoginRequest req = new LoginRequest();
		req.setEmail(email);
		req.setPassword(password);

		
		String userAgent = request.getHeader("User-Agent");
		String ipAddress = request.getRemoteAddr();
		String deviceId = session.getId();
		String deviceType = "WEB_BROWSER";

		AuthResponse auth;
		try {
			auth = authService.login(req, userAgent, ipAddress, deviceId, deviceType);
		} catch (RuntimeException ex) {
			ra.addFlashAttribute("error", ex.getMessage() != null ? ex.getMessage() : "Invalid email or password");
			ra.addFlashAttribute("activeTab", "login");
			return "redirect:/login";
		}

		if (auth == null) {
			ra.addFlashAttribute("error", "Invalid email or password");
			ra.addFlashAttribute("activeTab", "login");
			return "redirect:/login";
		}

		Integer userId = auth.getUserId();
		session.setAttribute("userId", auth.getUserId());
		String role = (auth.getRoles() != null && !auth.getRoles().isEmpty())
				? String.valueOf(auth.getRoles().get(0)).toUpperCase()
				: "";
		
		session.setAttribute("role", role);
		session.setAttribute("roles", auth.getRoles());
		session.setAttribute("accessToken", auth.getAccessToken());
		session.setAttribute("refreshToken", auth.getRefreshToken());
		response.addCookie(cookieWith("ACCESS_TOKEN", auth.getAccessToken(), 60 * 15));
		response.addCookie(cookieWith("REFRESH_TOKEN", auth.getRefreshToken(), 60 * 60 * 24 * 7));


		return redirectByRole(userId, auth.getRoles(), session);
	}

	@GetMapping("/register")
	public String registerPage(Model model) {
		if (!model.containsAttribute("registerRequest")) {
			model.addAttribute("registerRequest", new RegisterRequest());
		}
		model.addAttribute("activeTab", "register");
		return "client/common/login";
	}

	@PostMapping("/register")
	public String handleRegister(@Valid @ModelAttribute("registerRequest") RegisterRequest request,
			BindingResult binding, RedirectAttributes ra) {
		if (binding.hasErrors()) {
			ra.addFlashAttribute("org.springframework.validation.BindingResult.registerRequest", binding);
			ra.addFlashAttribute("registerRequest", request);
			ra.addFlashAttribute("registerError", "Please check your input");
			ra.addFlashAttribute("activeTab", "register");
			return "redirect:/login";
		}

		String result = authService.register(request);
		if ("OK".equals(result)) {
			ra.addFlashAttribute("registerSuccess", "Registration successful! Please log in.");
			ra.addFlashAttribute("activeTab", "login");
			return "redirect:/login";
		} else {
			ra.addFlashAttribute("registerRequest", request);
			ra.addFlashAttribute("registerError", result);
			ra.addFlashAttribute("activeTab", "register");
			return "redirect:/login";
		}
	}

	// === REDIRECT BY ROLE ===
	private String redirectByRole(Integer userId, List<String> roles, HttpSession session) {
		try {
			System.out.println(roles.contains("SELLER"));
			for (String role : roles) {
				if (role.contains("ADMIN")) {
					return "redirect:/admin/product/";
				} else if (role.contains("SELLER")) {
					int shopId = shopService.getShopIdByUser(userId);
					if (shopId > 0)
						session.setAttribute("shopId", shopId);
					return "redirect:/seller/profile";
				} else if (role.contains("BUYER")) {
					return "redirect:/home";
				} else if (role.contains("SHIPPER")) {
					int shipperId = shipperService.getByIdUser(userId).getShipperID();
					session.setAttribute("shipperId", shipperId);
					return "redirect:/shipper";
				}
				else {
					return "redirect:/logout";
				}
			}

			return "redirect:/login";
		} catch (Exception e) {
			e.printStackTrace();
			return "redirect:/home";
		}

	}

	// === COOKIE HELPER ===
	private Cookie cookieWith(String name, String value, int maxAgeSeconds) {
		Cookie cookie = new Cookie(name, value);
		cookie.setHttpOnly(true);
		cookie.setPath("/");
		cookie.setMaxAge(maxAgeSeconds);
		return cookie;
	}
	
	// === LOGOUT ===
    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        Integer userId = null;

        if (session != null && session.getAttribute("userId") != null) {
            userId = (Integer) session.getAttribute("userId");
        }

        // 1️⃣ Thu hồi tất cả refresh token và devices của user nếu có
        if (userId != null) {
            tokenDeviceService.revokeAllTokensAndDevices(userId);
        }

        // 2️⃣ Xóa session web
        if (session != null) {
            session.invalidate();
        }

        // 3️⃣ Xóa cookie JWT khỏi trình duyệt
        Cookie clearAccess = new Cookie("ACCESS_TOKEN", null);
        clearAccess.setHttpOnly(true);
        clearAccess.setPath("/");
        clearAccess.setMaxAge(0); // xóa cookie ngay lập tức

        Cookie clearRefresh = new Cookie("REFRESH_TOKEN", null);
        clearRefresh.setHttpOnly(true);
        clearRefresh.setPath("/");
        clearRefresh.setMaxAge(0);

        response.addCookie(clearAccess);
        response.addCookie(clearRefresh);

        return "redirect:/login";
    }
}
