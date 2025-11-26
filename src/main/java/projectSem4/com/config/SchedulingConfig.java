package projectSem4.com.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SchedulingConfig {
    // Chỉ cần annotation này để Spring Boot tự bật Scheduler
}
