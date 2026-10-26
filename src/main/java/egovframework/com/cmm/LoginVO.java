package egovframework.com.cmm;

import java.io.Serializable;

/**
 * 로그인 사용자 VO — practice copy of olv LoginVO (fields used in this project only)
 * <p>
 * 로그인 성공 후 Spring Security Authentication 의 principal 로 저장되어
 * HttpSession 에 보관된다. 세션에 저장되므로 Serializable 이어야 한다.
 * </p>
 *
 * 작성자 : henheang
 * 작성일 : 2026-10-26
 */
public class LoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 사용자 번호 (PK) */
    private String userNo;

    /** 로그인 ID */
    private String id;

    /** 사용자명 */
    private String name;

    /** 비밀번호 (입력값 → 해시값으로 바뀌어 조회 조건으로만 사용, 세션 저장 전 제거) */
    private String password;

    /** 접속 IP */
    private String ip;

    public String getUserNo() { return userNo; }
    public void setUserNo(String userNo) { this.userNo = userNo; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
}
