package au.org.aodn.nrmn.restapi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final long MAX_AGE_SECS = 3600;

    @Autowired
    GlobalRequestInterceptor globalRequestInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("*")
            .allowedMethods("HEAD", "OPTIONS", "GET", "POST", "PUT", "PATCH", "DELETE")
            .maxAge(MAX_AGE_SECS);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(globalRequestInterceptor)
            .addPathPatterns("/api/**/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        /* Fallback mapping - forward to index.html - refer to
         * https://docs.spring.io/spring-framework/docs/3.0.0.M3/reference/html/ch16s04.html
         * for ordering of handler mappings - we want this one last */
        registry.setOrder(Integer.MAX_VALUE);

        registry.addViewController("/{spring:\\w+}")
            .setViewName("forward:/");
        registry.addViewController("/**/{spring:\\w+}")
            .setViewName("forward:/");
        registry.addViewController("/{spring:\\w+}/**{spring:?!(\\.js|\\.css)$}")
            .setViewName("forward:/");
    }
}
