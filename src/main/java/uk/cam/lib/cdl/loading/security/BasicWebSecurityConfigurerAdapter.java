package uk.cam.lib.cdl.loading.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSecurity
public class BasicWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

    @Autowired
    private MyBasicAuthenticationEntryPoint authenticationEntryPoint;

    @Value("${dl-loading-ui.auth.basic.users}")
    private String users;

    @Value("${dl-loading-ui.auth.basic.admins}")
    private String admins;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {

        /** TODO Replace with LDAP auth ***/

        // Setup Users
        String[] userArray = users.split(",");
        for (int i = 0; i < userArray.length; i++) {
            String[] user = userArray[i].split(":");
            String username = user[0];
            String password = user[1];

            auth.inMemoryAuthentication()
                .withUser(username).password(passwordEncoder().encode(password))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"));
        }

        // Setup Admins
        String[] adminArray = admins.split(",");
        for (int i = 0; i < adminArray.length; i++) {
            String[] user = adminArray[i].split(":");
            String username = user[0];
            String password = user[1];

            List authorities = new ArrayList();
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));

            auth.inMemoryAuthentication()
                .withUser(username).password(passwordEncoder().encode(password))
                .authorities(authorities);

        }

    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .authorizeRequests()
            .antMatchers("/js").permitAll()
            .antMatchers("/css").permitAll()
            .antMatchers("/img").permitAll()
            .antMatchers("/login/**").permitAll()
            .anyRequest().authenticated()
            .and()
            .httpBasic()
            .authenticationEntryPoint(authenticationEntryPoint);

        http.addFilterAfter(new CustomFilter(),
            BasicAuthenticationFilter.class);

        http.logout()
            .logoutUrl("/logout")
            .logoutSuccessUrl("/login/login.html")
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
