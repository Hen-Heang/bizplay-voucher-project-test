package egovframework.com.cmm.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 웹 요청 관련 공통 유틸 — practice copy of olv CmmWebUtil (요청 유형 판별 부분만)
 * <p>
 * 작성자 : henheang
 * 작성일 : 2026-10-26
 */
public final class CmmWebUtil {

    private CmmWebUtil() {
    }

    /**
     * AJAX 요청 여부 (jQuery $.ajax 는 X-Requested-With 헤더를 자동으로 보냄)
     */
    public static boolean isAjaxRequest(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }

    /**
     * JSON 응답이 필요한 요청 여부 (AJAX 또는 Accept: application/json)
     */
    public static boolean isJsonRequest(HttpServletRequest request) {
        if (isAjaxRequest(request)) {
            return true;
        }
        String accept = request.getHeader("Accept");
        return accept != null && accept.contains("application/json");
    }
}
