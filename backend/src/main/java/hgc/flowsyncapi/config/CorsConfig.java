package hgc.flowsyncapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置 —— 允许前端在 Vercel 上访问后端 Railway 接口
 * <p>
 * 前后端分离部署时，浏览器会阻止跨域请求，
 * 这个配置告诉后端允许来自前端的请求。
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许所有来源（Vercel 域名不确定时可使用）
        config.addAllowedOriginPattern("*");
        // 允许携带凭证（cookie、header 等）
        config.setAllowCredentials(true);
        // 允许所有请求方法（GET、POST、PUT、DELETE 等）
        config.addAllowedMethod("*");
        // 允许所有请求头
        config.addAllowedHeader("*");
        // 暴露所有响应头
        config.addExposedHeader("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}