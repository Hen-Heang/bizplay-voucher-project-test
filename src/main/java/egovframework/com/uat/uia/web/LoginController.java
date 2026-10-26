package egovframework.com.uat.uia.web;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import egovframework.com.cmm.LoginVO;
import egovframework.com.cmm.config.GlobalsProperties;
import egovframework.com.cmm.service.LoginService;
import egovframework.com.cmm.util.CmmUserDetailsHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 로그인 컨트롤러 — practice copy of olv LoginController (일반 로그인만)
 * <p>
 * olv 와 같이 Spring Security formLogin 을 쓰지 않고, 컨트롤러가 직접
 * DB 인증 → Authentication 생성 → 세션ID/CSRF 회전 → SecurityContext 저장을 수행한다.
 * olv 의 로그인 잠금, 허용 IP, 2차 인증(OTP), 접속 이력은 이 연습에서 제외.
 * 로그아웃은 SecurityConfig 의 LogoutFilter 가 POST /login/logout.do 로 처리한다.
 * </p>
 *
 * 작성자 : henheang
 * 작성일 : 2026-10-26
 */
@Controller
public class LoginController {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    private static final String MSG_LOGIN_FAIL = "아이디 또는 비밀번호가 올바르지 않습니다. / Invalid ID or password.";

    @Autowired
    private LoginService loginService;

    @Autowired
    private GlobalsProperties globalsProperties;

    @Autowired
    private SecurityContextRepository securityContextRepository;

    @Autowired
    private SessionAuthenticationStrategy sessionAuthenticationStrategy;

    /**
     * 로그인 화면
     */
    @GetMapping("/login/login.do")
    public String loginUsrView(
            @ModelAttribute("loginVO") LoginVO loginVO,
            @RequestParam(value = "loginMessage", required = false) String loginMessage,
            ModelMap model) throws Exception {

        // 이미 로그인 상태면 메인으로
        if (CmmUserDetailsHelper.isAuthenticated()) {
            return "redirect:" + globalsProperties.getSecurity().getLoginSuccessUrl();
        }

        model.addAttribute("loginMessage", loginMessage);
        return "egovframework/com/uat/uia/LoginUsr";
    }

    /**
     * 일반 로그인 처리
     */
    @PostMapping("/login/action.do")
    public String actionLogin(@ModelAttribute("loginVO") LoginVO loginVO,
                              HttpServletRequest request, HttpServletResponse response) throws Exception {

        // 1. 입력값 확인
        if (isBlank(loginVO.getId()) || isBlank(loginVO.getPassword())) {
            return redirectLoginWithMessage(MSG_LOGIN_FAIL);
        }

        // 2. DB 인증 (비밀번호 해시 비교)
        LoginVO resultVO = loginService.actionLogin(loginVO);

        // 3. 실패 — ID/비밀번호 중 어느 쪽이 틀렸는지 알려주지 않는다 (계정 존재 여부 노출 방지)
        if (isBlank(resultVO.getId())) {
            log.info("[LoginController] login failed: id={}", loginVO.getId());
            return redirectLoginWithMessage(MSG_LOGIN_FAIL);
        }

        // 4. 성공 — 세션/SecurityContext 수립
        resultVO.setIp(request.getRemoteAddr());
        resultVO.setPassword(null);
        request.getSession().setAttribute("loginVO", resultVO);
        actionSecurityProcess(resultVO, request, response);

        log.info("[LoginController] login success: id={}", resultVO.getId());
        return "redirect:" + globalsProperties.getSecurity().getLoginSuccessUrl();
    }

    /**
     * Spring Security 인증 처리 — olv LoginController.actionSecurityProcess 와 동일한 순서
     */
    private void actionSecurityProcess(LoginVO resultVO, HttpServletRequest request, HttpServletResponse response) {
        // 1. 인증 토큰 구성 — principal = LoginVO, 권한 = ROLE_USER
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        UsernamePasswordAuthenticationToken authResult =
                UsernamePasswordAuthenticationToken.authenticated(resultVO, null, authorities);

        // 2. 세션 고정 방어 — 세션ID(JSESSIONID) 재발급 + CSRF 토큰 회전
        sessionAuthenticationStrategy.onAuthentication(authResult, request, response);

        // 3. SecurityContext 저장 — 현재 요청(ThreadLocal) + HttpSession(다음 요청부터)
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authResult);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
    }

    private String redirectLoginWithMessage(String message) {
        return "redirect:" + globalsProperties.getSecurity().getLoginUrl()
                + "?loginMessage=" + URLEncoder.encode(message, StandardCharsets.UTF_8);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
