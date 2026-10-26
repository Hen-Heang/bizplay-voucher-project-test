package egovframework.com.cmm.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import egovframework.com.cmm.LoginVO;
import egovframework.com.cmm.crypto.CmmPasswordEncoder;
import egovframework.com.cmm.mapper.LoginMapper;
import egovframework.com.cmm.service.LoginService;

/**
 * 로그인 서비스 구현 — practice copy of olv LoginServiceImpl (actionLogin 만)
 * <p>
 * 입력 비밀번호를 eGov 방식(SHA-256, salt=ID)으로 해시한 뒤
 * DB 의 해시값과 SQL 조건으로 비교한다. 평문 비밀번호는 저장/비교하지 않는다.
 * </p>
 *
 * 작성자 : henheang
 * 작성일 : 2026-10-26
 */
@Service("loginService")
public class LoginServiceImpl implements LoginService {

    @Autowired
    private LoginMapper loginMapper;

    @Autowired
    private CmmPasswordEncoder passwordEncoder;

    @Override
    public LoginVO actionLogin(LoginVO vo) throws Exception {
        String encPassword = passwordEncoder.encryptPassword(vo.getPassword(), vo.getId());
        vo.setPassword(encPassword);

        LoginVO result = loginMapper.actionLogin(vo);
        if (result != null && result.getId() != null && !result.getId().isEmpty()) {
            return result;
        }
        return new LoginVO();
    }
}
