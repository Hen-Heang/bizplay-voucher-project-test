package egovframework.com.cmm.util;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import egovframework.com.cmm.LoginVO;

/**
 * 인증 사용자 정보 유틸리티 — practice copy of olv CmmUserDetailsHelper
 * <pre>
 * Boolean isAuth = CmmUserDetailsHelper.isAuthenticated();
 * LoginVO user = (LoginVO) CmmUserDetailsHelper.getAuthenticatedUser();
 * </pre>
 *
 * 작성자 : henheang
 * 작성일 : 2026-10-26
 */
public class CmmUserDetailsHelper {

    private CmmUserDetailsHelper() {
        // 유틸리티 클래스 인스턴스화 방지
    }

    /**
     * 인증된 사용자 여부
     */
    public static Boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        if (auth instanceof AnonymousAuthenticationToken) {
            return false;
        }
        return auth.isAuthenticated();
    }

    /**
     * 인증된 사용자 객체 (LoginVO) — 미인증이면 null
     */
    public static Object getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        if (auth instanceof AnonymousAuthenticationToken) {
            return null;
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof LoginVO) {
            return principal;
        }
        return null;
    }

    /**
     * 인증된 사용자의 권한 목록 (예: ["ROLE_USER"])
     */
    public static List<String> getAuthorities() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return Collections.emptyList();
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }
}
