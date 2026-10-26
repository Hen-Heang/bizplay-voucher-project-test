package egovframework.com.cmm.mapper;

import org.apache.ibatis.annotations.Mapper;

import egovframework.com.cmm.LoginVO;

/**
 * 로그인 Mapper — practice copy of olv LoginMapper (actionLogin 만)
 *
 * 작성자 : henheang
 * 작성일 : 2026-10-26
 */
@Mapper
public interface LoginMapper {

    /** ID + 해시 비밀번호가 일치하는 활성 관리자 조회 (없으면 null) */
    LoginVO actionLogin(LoginVO vo) throws Exception;
}
