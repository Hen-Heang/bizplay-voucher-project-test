package egovframework.com.cmm.exception;

/**
 * 공통 업무 예외 (Business Exception) — practice copy of olv CmmBizException
 *
 * <p>
 * 비즈니스 로직 처리 중 발생하는 예외를 의미합니다.
 * 에러 코드는 기본적으로 'M' + 4자리 숫자 형태를 사용합니다. (예: M0001)
 * </p>
 *
 * 작성자 : henheang
 * 작성일 : 2026-10-26
 */
public class CmmBizException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 에러 코드 (예: M0001) */
    private final String code;

    /**
     * @param code    에러 코드 (기본적으로 M + 4자리 숫자)
     * @param message 에러 메시지
     */
    public CmmBizException(String code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * @param code    에러 코드 (기본적으로 M + 4자리 숫자)
     * @param message 에러 메시지
     * @param cause   원인 예외
     */
    public CmmBizException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
