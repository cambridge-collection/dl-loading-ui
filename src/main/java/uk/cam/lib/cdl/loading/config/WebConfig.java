package uk.cam.lib.cdl.loading.config;

import nz.net.ultraq.thymeleaf.LayoutDialect;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.extras.springsecurity5.dialect.SpringSecurityDialect;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;
import uk.cam.lib.cdl.loading.security.tags.RoleDialect;


@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Value("${spring.application.static.path}")
    private String staticPath;

    private ApplicationContext applicationContext;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
            .addResourceHandler("/webjars/**")
            .addResourceLocations("/webjars/")
            .resourceChain(false);
        registry.addResourceHandler("/**").addResourceLocations(staticPath);

    }

    // FIXME: Is this actually called? This doesn't implement ApplicationContextAware
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Bean
    public SpringResourceTemplateResolver templateResolver(){
        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setApplicationContext(this.applicationContext);
        templateResolver.setPrefix("classpath:/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        // Templates should be automatically updated when modified.
        templateResolver.setCacheable(false);

        return templateResolver;
    }

    @Bean
    public SpringTemplateEngine templateEngine(){
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setEnableSpringELCompiler(true); // Compiled SpringEL should speed up executions
        templateEngine.setTemplateResolver(templateResolver());
        templateEngine.addDialect(new RoleDialect()); // our custom role dialect
        templateEngine.addDialect(new LayoutDialect()); // from layout-dialect
        templateEngine.addDialect(new SpringSecurityDialect()); // from springsecurity extras
        templateEngine.addDialect(new Java8TimeDialect()); // Adds #temporals object for date formatting
        return templateEngine;
    }

    @Bean
    public ThymeleafViewResolver viewResolver(){
        ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
        viewResolver.setTemplateEngine(templateEngine());
        return viewResolver;
    }

/*    @Bean
    public FilterRegistrationBean<AWSXRayServletFilter> filterRegistrationBean() {

        FilterRegistrationBean < AWSXRayServletFilter > registrationBean = new FilterRegistrationBean<>();
        AWSXRayServletFilter filter = new AWSXRayServletFilter("dl-loading-ui-segment");

        registrationBean.setFilter(filter);
        registrationBean.addUrlPatterns("/edit/collection/*");
        //registrationBean.setOrder(2);
        return registrationBean;

   }*/

}
