package fun.javierchen.ainocodeapplication.config;

import fun.javierchen.ainocodeapplication.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Slf4j
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new JwtAuthInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/user/login", "/user/register");
    }

    static class JwtAuthInterceptor implements HandlerInterceptor {
        private static final Logger log = LoggerFactory.getLogger(JwtAuthInterceptor.class);
        
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            String header = request.getHeader("Authorization");
            log.info("Authorization header: {}", header);
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                log.info("Parsing token: {}", token.substring(0, Math.min(20, token.length())) + "...");
                try {
                    Long userId = JwtUtil.parseToken(token);
                    log.info("Parsed userId from token: {}", userId);
                    request.setAttribute("loginUserId", userId);
                } catch (Exception e) {
                    log.error("Failed to parse JWT token: {}", e.getMessage(), e);
                }
            } else {
                log.info("No valid Authorization header found");
            }
            return true;
        }
    }
}
