package egovframework.com.cmm.exception;

/**
 * 공통 시스템 예외 (System Exception) — practice copy of olv CmmException
 *
 * <p>
 * 인프라, 연동, DB 등 시스템 처리 중 발생하는 공통 예외를 의미합니다.
 * 에러 코드는 기본적으로 'E' + 4자리 숫자 형태를 사용합니다. (예: E0001)
 * </p>
 *
 * 작성자 : henheang
 * 작성일 : 2026-10-26
 */
public class CmmException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 에러 코드 (예: E0001) */
    private final String code;

    /**
     * @param code    에러 코드 (기본적으로 E + 4자리 숫자)
     * @param message 에러 메시지
     */
    public CmmException(String code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * @param code    에러 코드 (기본적으로 E + 4자리 숫자)
     * @param message 에러 메시지
     * @param cause   원인 예외
     */
    public CmmException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
