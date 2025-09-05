package com.csc435.workoutbuilder.config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.csc435.workoutbuilder.service.CustomOAuth2UserService;
import com.csc435.workoutbuilder.service.MyUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private MyUserDetailsService userDetailsService;
    private AuthenticationSuccessHandler loginSuccessHandler;
    private AuthenticationFailureHandler loginFailHandler;
    

    @Autowired
    public SecurityConfig(MyUserDetailsService userDetailsService, AuthenticationSuccessHandler loginSuccessHandler,
    AuthenticationFailureHandler loginFailHandler) {

        this.userDetailsService = userDetailsService;
        this.loginSuccessHandler = loginSuccessHandler;
        this.loginFailHandler = loginFailHandler;
    }

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private OAuthLoginFailHandler oAuthLoginFailHandler;

    @Autowired
    private OAuthLoginSuccessHandler oAuthLoginSuccessHandler;

    @Autowired
    private CustomLogoutSuccessHandler logoutSuccessHandler;

    @Autowired
    private AuthEntryPoint authEntryPoint;
   
    @Bean
    public BCryptPasswordEncoder passEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passEncoder());
        provider.setUserDetailsService(userDetailsService);

        return provider;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf((csrf) -> csrf.disable())
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers("/login","/login.html","/signup","/logout","/exercises","/oauth**").permitAll();
                auth.anyRequest().authenticated();
            })
            .exceptionHandling(exception ->
            exception.authenticationEntryPoint(authEntryPoint))
            
            .formLogin(login -> {
                login.loginProcessingUrl("/login");
                login.loginPage("/login.html");
                login.usernameParameter("username");
                login.passwordParameter("password");
                login.successHandler(loginSuccessHandler);
                login.failureHandler(loginFailHandler);

            })
            .oauth2Login(oauth2 -> {
                oauth2.loginPage("/login.html");
                oauth2.successHandler(oAuthLoginSuccessHandler);
                oauth2.failureHandler(oAuthLoginFailHandler);
                oauth2.userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService));
            })
            .logout(logout -> {
                logout.logoutUrl("/logout");
                logout.logoutSuccessHandler(logoutSuccessHandler);
                
            });

        return http.build();
    }
    @Bean
    public WebMvcConfigurer corsConf() {
        return new WebMvcConfigurer() {
            
        };
    }
    
}
