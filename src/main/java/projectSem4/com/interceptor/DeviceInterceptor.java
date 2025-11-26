package projectSem4.com.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import projectSem4.com.model.utils.DeviceType;
import projectSem4.com.model.utils.DeviceUtils;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class DeviceInterceptor implements HandlerInterceptor {
	@Override
	public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
		String ua = req.getHeader("User-Agent");
		DeviceType type = DeviceUtils.detect(ua);
		req.setAttribute("deviceType", type);
		req.setAttribute("isMobile", type == DeviceType.MOBILE);
		req.setAttribute("isTablet", type == DeviceType.TABLET);
		req.setAttribute("isDesktop", type == DeviceType.DESKTOP);
		return true;
	}
}
