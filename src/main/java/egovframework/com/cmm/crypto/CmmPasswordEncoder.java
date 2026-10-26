package egovframework.com.cmm.crypto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.springframework.stereotype.Component;

/**
 * eGovFrame 호환 비밀번호 인코더 — practice copy of olv CmmPasswordEncoder (단방향 해시 부분만)
 * <pre>
 * 알고리즘: Base64(SHA-256(salt + password)),  salt = 사용자 ID
 * </pre>
 *
 * 작성자 : henheang
 * 작성일 : 2026-10-26
 */
@Component
public class CmmPasswordEncoder {

    /**
     * eGovFrame 호환 비밀번호 해시 (단방향, salt 사용)
     *
     * @param password 평문 비밀번호
     * @param salt     솔트 (사용자 ID)
     * @return Base64 인코딩된 SHA-256 해시
     */
    public String encryptPassword(String password, String salt) {
        if (password == null || salt == null) {
            return "";
        }

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.reset();
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] hashValue = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashValue);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Password encryption failed", e);
        }
    }

    /**
     * eGovFrame 호환 비밀번호 검증 — 입력값을 해시하여 DB 저장값과 비교
     */
    public boolean matchesEgov(String rawPassword, String salt, String encodedPassword) {
        if (rawPassword == null || salt == null || encodedPassword == null) {
            return false;
        }
        return encodedPassword.equals(encryptPassword(rawPassword, salt));
    }
}
