package projectSem4.com.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import projectSem4.com.interceptor.DeviceInterceptor;
import projectSem4.com.interceptor.JwtAuthInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Autowired
	JwtAuthInterceptor jwtAuth;
	@Autowired
	DeviceInterceptor device; // optional

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(device).addPathPatterns("/**");
		registry.addInterceptor(jwtAuth).addPathPatterns("/api/**", "/admin/**", "/shop/**").excludePathPatterns(
				"/register", "/error", "/css/**", "/js/**", "/images/**", "/webjars/**", "/static/**", "/assets/**",
				"/admin/assets/**", "/favicon.ico", "/api/orders/**", "/api/auth/**");

	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// ✅ map URL /uploads/** tới thư mục D:/uploads/

		registry.addResourceHandler("/uploads/**").addResourceLocations("file:D:/uploads/").setCachePeriod(3600);
	}
	
	
}
