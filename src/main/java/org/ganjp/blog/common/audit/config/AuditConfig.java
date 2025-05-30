package org.ganjp.blog.common.audit.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.ganjp.blog.common.audit.interceptor.AuthenticationAuditInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.Executor;

/**
 * Configuration for audit logging functionality.
 */
@Configuration
@EnableAspectJAutoProxy
@EnableAsync
@EnableConfigurationProperties(AuditProperties.class)
@ConditionalOnProperty(name = "audit.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class AuditConfig implements WebMvcConfigurer {

    private final AuditProperties auditProperties;
    
    @Autowired
    @Lazy
    private AuthenticationAuditInterceptor authenticationAuditInterceptor;

    /**
     * Configure async executor for audit logging to prevent blocking main threads
     */
    @Bean(name = "auditTaskExecutor")
    public Executor auditTaskExecutor() {
        AuditProperties.ThreadPoolConfig config = auditProperties.getThreadPool();
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(config.getCorePoolSize());
        executor.setMaxPoolSize(config.getMaxPoolSize());
        executor.setQueueCapacity(config.getQueueCapacity());
        executor.setThreadNamePrefix(config.getThreadNamePrefix());
        executor.setKeepAliveSeconds(config.getKeepAliveSeconds());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    /**
     * ObjectMapper for JSON serialization in audit logs
     */
    @Bean("auditObjectMapper")
    public ObjectMapper auditObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        return mapper;
    }

    /**
     * Register authentication audit interceptor
     */
    @Override
    public void addInterceptors(@org.springframework.lang.NonNull InterceptorRegistry registry) {
        if (auditProperties.isAuditAuthenticationEvents()) {
            registry.addInterceptor(authenticationAuditInterceptor)
                    .addPathPatterns("/v*/auth/**");
        }
    }
}
