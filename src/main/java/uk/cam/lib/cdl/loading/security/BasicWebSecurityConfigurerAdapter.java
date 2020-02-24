/*

package uk.cam.lib.cdl.loading.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;

import java.util.ArrayList;
import java.util.List;

*/
/**
 * FOR TESTING PURPOSES ONLY, DISABLE FOR RELEASE.
 *//*


@Configuration
@EnableWebSecurity
@Order(2)
public class BasicWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

    @Value("${dl-loading-ui.auth.basic.users}")
    private String users;

    @Value("${dl-loading-ui.auth.basic.admins}")
    private String admins;

    @Bean
    @Override
    public UserDetailsService userDetailsService() {
        // NOTE deprecated as this should not be used in production. Will be replaced by SAML.

        List<UserDetails> userDetails = new ArrayList<>();

        // Setup Users
        String[] userArray = users.split(",");
        for (String value : userArray) {
            String[] user = value.split(":");
            String username = user[0];
            String password = user[1];
            userDetails.add(
                User.withDefaultPasswordEncoder()
                    .username(username)
                    .password(password)
                    .roles("USER")
                    .build());
        }

        // Setup Admins
        String[] adminArray = admins.split(",");
        for (String s : adminArray) {
            String[] user = s.split(":");
            String username = user[0];
            String password = user[1];
            userDetails.add(
                User.withDefaultPasswordEncoder()
                    .username(username)
                    .password(password)
                    .roles("USER", "ADMIN")
                    .build());
        }

        return new InMemoryUserDetailsManager(userDetails);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .authorizeRequests()
            .antMatchers("/js/**").permitAll()
            .antMatchers("/css/**").permitAll()
            .antMatchers("/img/**").permitAll()
            .antMatchers("/webjars/**").permitAll()
            .antMatchers("/login/**").permitAll()
            .anyRequest().authenticated()
            .and()

            .formLogin()
            .loginPage("/login/login.html")
            .permitAll()
            .and()

            // Required for allowing Iframe embedding from same origin.
            .headers().frameOptions().disable()
            .addHeaderWriter(new XFrameOptionsHeaderWriter(XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN))
            .and()

            .logout()
            .logoutUrl("/logout")
            .logoutSuccessUrl("/login/login.html")
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID");
    }


}
*/
