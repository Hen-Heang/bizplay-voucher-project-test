package egovframework.com.cmm.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.csrf.MissingCsrfTokenException;

import egovframework.com.cmm.util.CmmWebUtil;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Spring Security 설정 — practice copy of olv SecurityConfig (web 모드만)
 * <p>
 * Form Login + Session 기반 인증. 로그인 처리는 LoginController 가
 * SecurityContextHolder 에 Authentication 을 직접 저장한다 (formLogin 미사용).
 * olv 의 api 모드(JWT/Stateless), cookie CSRF 저장소는 이 연습 프로젝트에서 제외.
 * </p>
 *
 * 작성자 : henheang
 * 작성일 : 2026-10-26
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private GlobalsProperties globalsProperties;

    /**
     * 웹 모듈용 SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http, CsrfTokenRepository csrfTokenRepository) throws Exception {
        GlobalsProperties.Security sec = globalsProperties.getSecurity();

        log.info("Security: WEB 모드 (Session) — loginUrl={}, csrfEnabled={}, publicUrls={}",
                sec.getLoginUrl(), sec.isCsrfEnabled(), sec.getPublicUrls());

        http
            // ① CSRF 보호 — POST 요청은 서버가 발급한 _csrf 토큰이 있어야 통과
            .csrf(csrf -> {
                if (sec.isCsrfEnabled()) {
                    csrf.csrfTokenRepository(csrfTokenRepository)
                        .csrfTokenRequestHandler(csrfTokenRequestHandler());
                } else {
                    csrf.disable();
                }
            })

            // ② 보안 헤더 — 같은 사이트에서만 iframe 허용 (클릭재킹 방어)
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
                .contentTypeOptions(contentType -> {})
            )

            // ③ 세션 고정 방어 (formLogin 용 선언 — 실제 회전은 LoginController 가 수행)
            .sessionManagement(session -> session
                .sessionFixation(fixation -> fixation.changeSessionId())
            )

            // ④ URL 접근 제어
            .authorizeHttpRequests(auth -> {
                // 정적 리소스
                auth.requestMatchers("/static/**", "/favicon.ico").permitAll();

                // 공개 URL — application.yaml 의 globals.security.public-urls
                List<String> publicUrls = sec.getPublicUrls();
                if (publicUrls != null && !publicUrls.isEmpty()) {
                    auth.requestMatchers(publicUrls.toArray(new String[0])).permitAll();
                }

                // 나머지 모든 요청은 로그인 필요
                auth.anyRequest().authenticated();
            })

            // ⑤ 로그인은 컨트롤러에서 직접 처리
            .formLogin(form -> form.disable())

            // ⑥ 로그아웃 — POST {logoutUrl} 을 LogoutFilter 가 처리
            .logout(logout -> logout
                .logoutUrl(sec.getLogoutUrl())
                .logoutSuccessUrl(sec.getLoginUrl())
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )

            // ⑦ 예외 처리 — AJAX 는 401 JSON, 화면 요청은 로그인 페이지로
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    if (CmmWebUtil.isAjaxRequest(request)) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"code\":\"401\",\"message\":\"세션이 만료되었습니다.\"}");
                    } else {
                        response.sendRedirect(request.getContextPath() + sec.getLoginUrl());
                    }
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    if (accessDeniedException instanceof MissingCsrfTokenException
                            || accessDeniedException.getCause() instanceof MissingCsrfTokenException) {
                        // 세션 만료로 CSRF 토큰이 사라진 경우 → 다시 로그인
                        if (CmmWebUtil.isAjaxRequest(request)) {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"code\":\"401\",\"message\":\"세션이 만료되었습니다.\"}");
                        } else {
                            response.sendRedirect(request.getContextPath() + sec.getLoginUrl());
                        }
                    } else {
                        new AccessDeniedHandlerImpl().handle(request, response, accessDeniedException);
                    }
                })
            );

        return http.build();
    }

    /**
     * SecurityContext 를 HttpSession 에 저장 — 로그인 컨트롤러가 사용
     */
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    /**
     * CSRF 토큰 저장소 (세션) — 필터 체인과 로그인 시 토큰 회전 전략이 같은 저장소를 공유
     */
    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        return new HttpSessionCsrfTokenRepository();
    }

    /**
     * 로그인 성공 시점에 세션ID 재발급 + CSRF 토큰 회전 (세션 고정 / 토큰 고정 방어)
     */
    @Bean
    public SessionAuthenticationStrategy sessionAuthenticationStrategy(CsrfTokenRepository csrfTokenRepository) {
        CsrfAuthenticationStrategy csrfStrategy = new CsrfAuthenticationStrategy(csrfTokenRepository);
        csrfStrategy.setRequestHandler(csrfTokenRequestHandler());

        return new CompositeSessionAuthenticationStrategy(
                List.of(new ChangeSessionIdAuthenticationStrategy(), csrfStrategy));
    }

    /**
     * CSRF 토큰 요청 핸들러 — 요청 속성명 _csrf (Thymeleaf ${_csrf} 로 사용)
     */
    private CsrfTokenRequestAttributeHandler csrfTokenRequestHandler() {
        CsrfTokenRequestAttributeHandler handler = new CsrfTokenRequestAttributeHandler();
        handler.setCsrfRequestAttributeName("_csrf");
        return handler;
    }

    /**
     * 기본 UserDetailsService (빈 저장소)
     * <p>
     * Spring Boot 의 "Using generated security password" 자동 사용자 생성을 막는다.
     * 실제 인증은 LoginService 가 DB 로 수행한다.
     * </p>
     */
    @Bean
    @ConditionalOnMissingBean(UserDetailsService.class)
    public UserDetailsService defaultUserDetailsService() {
        return new InMemoryUserDetailsManager();
    }
}
