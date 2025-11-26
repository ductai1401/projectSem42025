package projectSem4.com.config;

import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class UrlUploadSetting implements WebMvcConfigurer{
	@Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Mọi request /uploads/** → C:/data/uploads/
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:C:/data/uploads/");
    }
}
