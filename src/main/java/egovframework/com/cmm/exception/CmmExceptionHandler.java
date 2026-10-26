package egovframework.com.cmm.exception;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import egovframework.com.cmm.util.CmmWebUtil;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 공통 예외 처리기 (Exception Handler 일원화) — practice copy of olv CmmExceptionHandler
 *
 * <p>
 * CmmBizException (M코드), CmmException (E코드)에 대한 응답을 정의합니다.
 * 요청 방식(Ajax/JSON vs 일반 화면)에 따라 JSON 또는 alert 메시지를 구분하여 반환합니다.
 * 화면 전환을 방지하기 위해 일반 화면 요청에서는 javascript(alert + history.back) 형태의 스크립트를 반환합니다.
 * </p>
 *
 * <p>
 * olv 와의 차이: olv 는 globals.security.mode == "api" 이면 항상 JSON 을 반환하지만,
 * 이 연습 프로젝트에는 GlobalsProperties 가 없으므로 요청 헤더로만 판단합니다.
 * </p>
 *
 * 작성자 : henheang
 * 작성일 : 2026-10-26
 */
@ControllerAdvice
public class CmmExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(CmmExceptionHandler.class);

    private static final String DEFAULT_SYSTEM_ERROR_CODE = "E9999";
    private static final String DEFAULT_SYSTEM_ERROR_MESSAGE = "시스템 오류가 발생하였습니다.";
    private static final String DEFAULT_VALIDATION_MESSAGE = "입력값이 올바르지 않습니다.";

    // =========================================================================
    // [1] CmmBizException (공통 업무 예외 - M + 4자리 숫자)
    // =========================================================================

    /**
     * 비즈니스 로직 위반 시 발생. (HTTP 400 Bad Request)
     */
    @ExceptionHandler(CmmBizException.class)
    public Object handleCmmBizException(HttpServletRequest request, CmmBizException ex) {
        log.warn("[CmmBizException] URI={}, code={}, message={}",
                request.getRequestURI(), ex.getCode(), ex.getMessage());

        if (CmmWebUtil.isJsonRequest(request)) {
            return buildJsonError(HttpStatus.BAD_REQUEST, ex.getCode(), ex.getMessage());
        }

        return buildHtmlAlertAndBack(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // =========================================================================
    // [2] CmmException (공통 일반/시스템 예외 - E + 4자리 숫자)
    // =========================================================================

    /**
     * 인프라 또는 명시적 시스템 오류 발생 시. (HTTP 500 Internal Server Error)
     */
    @ExceptionHandler(CmmException.class)
    public Object handleCmmException(HttpServletRequest request, CmmException ex) {
        log.error("[CmmException] URI={}, code={}, message={}",
                request.getRequestURI(), ex.getCode(), ex.getMessage(), ex);

        if (CmmWebUtil.isJsonRequest(request)) {
            return buildJsonError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getCode(), ex.getMessage());
        }

        return buildHtmlAlertAndBack(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // =========================================================================
    // [3] Validation 예외 (@Valid 실패 — MethodArgumentNotValidException 은 BindException 의 하위 클래스)
    // =========================================================================

    @ExceptionHandler(BindException.class)
    public Object handleBindException(HttpServletRequest request, BindException ex) {
        log.warn("[BindException] URI={}, errors={}", request.getRequestURI(),
                ex.getBindingResult().getErrorCount());

        List<Map<String, String>> fieldErrors = extractFieldErrors(ex.getBindingResult().getFieldErrors());

        if (CmmWebUtil.isJsonRequest(request)) {
            return buildValidationJsonError(fieldErrors);
        }

        String alertMsg = DEFAULT_VALIDATION_MESSAGE;
        if (!fieldErrors.isEmpty()) {
            alertMsg += "\n - " + fieldErrors.get(0).get("message");
        }
        return buildHtmlAlertAndBack(alertMsg, HttpStatus.BAD_REQUEST);
    }

    // =========================================================================
    // [3.5] 정적 리소스 없음 (favicon.ico, Chrome DevTools 자동 요청 등) - 로그 노이즈 방지
    // =========================================================================

    @ExceptionHandler(NoResourceFoundException.class)
    public Object handleNoResourceFound(HttpServletRequest request, NoResourceFoundException ex) {
        if (request.getRequestURI().startsWith("/.well-known/")) {
            return ResponseEntity.ok("{}");
        }

        log.debug("[NoResourceFound] URI={}", request.getRequestURI());

        if (CmmWebUtil.isJsonRequest(request)) {
            return buildJsonError(HttpStatus.NOT_FOUND, null, "요청한 리소스를 찾을 수 없습니다.");
        }

        return buildHtmlAlertAndBack("요청한 리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
    }

    // =========================================================================
    // [4] 기타 최상위 Exception 처리 (Uncaught Exceptions)
    // =========================================================================

    @ExceptionHandler(Exception.class)
    public Object handleException(HttpServletRequest request, Exception ex) {
        log.error("[Exception] URI={}, message={}",
                request.getRequestURI(), ex.getMessage(), ex);

        if (CmmWebUtil.isJsonRequest(request)) {
            return buildJsonError(HttpStatus.INTERNAL_SERVER_ERROR,
                    DEFAULT_SYSTEM_ERROR_CODE, DEFAULT_SYSTEM_ERROR_MESSAGE);
        }

        return buildHtmlAlertAndBack(DEFAULT_SYSTEM_ERROR_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // =========================================================================
    // 내부 공통 유틸 메서드
    // =========================================================================

    private List<Map<String, String>> extractFieldErrors(List<FieldError> fieldErrors) {
        List<Map<String, String>> errors = new ArrayList<>();
        for (FieldError fe : fieldErrors) {
            Map<String, String> error = new HashMap<>();
            error.put("field", fe.getField());
            error.put("message", fe.getDefaultMessage());
            error.put("rejectedValue", fe.getRejectedValue() != null
                    ? fe.getRejectedValue().toString() : "");
            errors.add(error);
        }
        return errors;
    }

    private ResponseEntity<Map<String, Object>> buildValidationJsonError(
            List<Map<String, String>> fieldErrors) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("errorCode", "M9000"); // Validation Default Code
        body.put("message", DEFAULT_VALIDATION_MESSAGE);
        body.put("errors", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    private ResponseEntity<Map<String, Object>> buildJsonError(
            HttpStatus status, String errorCode, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("status", status.value());
        if (errorCode != null) {
            body.put("errorCode", errorCode);
        }
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }

    /**
     * 알림창(alert)을 띄우고 이전 화면으로 돌아가는 HTML 응답 반환 (화면 전환 방지)
     *
     * <p>
     * 메시지를 JavaScript 문자열 안에 넣으므로 반드시 escape 한다.
     * (\ ' " 줄바꿈, 그리고 &lt;/script&gt; 로 스크립트 블록이 끊기지 않도록 &lt; &gt; 도 escape)
     * </p>
     */
    private ResponseEntity<String> buildHtmlAlertAndBack(String message, HttpStatus status) {
        String safeMessage = message == null ? "" : message
                .replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\"", "\\\"")
                .replace("<", "\\u003C")
                .replace(">", "\\u003E")
                .replace("\r", "")
                .replace("\n", "\\n");

        String html = "<script>alert('" + safeMessage + "'); history.back();</script>";
        return ResponseEntity.status(status)
                .header("Content-Type", "text/html; charset=UTF-8")
                .body(html);
    }
}
