package egovframework.com.cmm.service;

import egovframework.com.cmm.LoginVO;

/**
 * 로그인 서비스 인터페이스 — practice copy of olv LoginService (actionLogin 만)
 *
 * 작성자 : henheang
 * 작성일 : 2026-10-26
 */
public interface LoginService {

    /**
     * 일반 로그인 — 성공 시 사용자 정보, 실패 시 id 가 비어 있는 LoginVO
     */
    LoginVO actionLogin(LoginVO vo) throws Exception;
}
