package com.choikang.back.config;

import com.choikang.back.security.filter.APICheckFilter;
import com.choikang.back.security.handler.LoginSuccessHandler;
import com.choikang.back.security.util.JWTUtil;
import com.choikang.back.service.OAuth2UserService;
import jakarta.servlet.Filter;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

//SpringSecurity 설정 담당
//JWT 필터와 인가 규칙 설정
@Configuration
@EnableWebSecurity
@Log4j2
public class SecurityConfig {
    private final OAuth2UserService oAuth2UserService;

    public SecurityConfig(@Lazy OAuth2UserService oAuth2UserService) {
        this.oAuth2UserService = oAuth2UserService;
    }

    @Bean(name = "customSecurityFilterChain")
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 토큰 비활성화
        http.csrf(csrf -> csrf.disable());

        // 인증에 따른 접근 설정, 로그인페이지 외에는 로그인하지 않은 상태일 시 접근 불가
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/oauth2/kakao").permitAll()
                        .anyRequest().permitAll()
//                .anyRequest().authenticated()
        );

        http.oauth2Login(oaUth2Configuer -> oaUth2Configuer
                        .loginPage("/login") // 로그인 페이지 경로 설정
                        .successHandler(loginSuccessHandler()) // 로그인 성공 시 실행되는 핸들러
                        .userInfoEndpoint(userInfo -> userInfo.userService(
                                oAuth2UserService))) // OAuth2 제공자로 부터 받은 사용자 정보를 처리하는 커스텀 시스템 지정. 사용자 정보를 애플리케이션의 사용자 모델로 변환하거나 추가적인 사용자 정보 처리 로직을 수행할 수 있음
                .formLogin(form -> form.disable()); // 폼 로그인 비활성화

        // (이전 페이지가 없는 경우) 로그인 뒤 메인 페이지로 이동
        http.formLogin(c -> {
            c.defaultSuccessUrl("/");
        });

//         비밀번호는 따로 없지만 UsernamePasswordAuthenticationFilter.class를 참조 역할로 사용하고 있음
        http.addFilterBefore(apiCheckFilter(), UsernamePasswordAuthenticationFilter.class);

        // 로그아웃
        http.logout(logout -> logout.logoutSuccessUrl("/"));

        return http.build();
    }

    @Bean
    public JWTUtil jwtUtil() {
        return new JWTUtil();
    }

    @Bean
    public LoginSuccessHandler loginSuccessHandler() {
        return new LoginSuccessHandler();
    }

    @Bean
    public APICheckFilter apiCheckFilter() {
        return new APICheckFilter("/", jwtUtil());
    }
}