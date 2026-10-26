package egovframework.com.cmm.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * yml 의 globals.* 설정 바인딩 — practice copy of olv GlobalsProperties (security 부분만)
 *
 * 작성자 : henheang
 * 작성일 : 2026-10-26
 */
@Component
@ConfigurationProperties(prefix = "globals")
public class GlobalsProperties {

    private Security security = new Security();

    public Security getSecurity() { return security; }
    public void setSecurity(Security security) { this.security = security; }

    /** globals.security.* */
    public static class Security {

        /** 로그인 화면 URL */
        private String loginUrl = "/login/login.do";

        /** 로그아웃 처리 URL (POST, Spring Security LogoutFilter 가 처리) */
        private String logoutUrl = "/login/logout.do";

        /** 로그인 성공 후 이동 URL */
        private String loginSuccessUrl = "/";

        /** CSRF 보호 사용 여부 */
        private boolean csrfEnabled = true;

        /** 로그인 없이 접근 가능한 URL 목록 */
        private List<String> publicUrls = new ArrayList<>();

        public String getLoginUrl() { return loginUrl; }
        public void setLoginUrl(String loginUrl) { this.loginUrl = loginUrl; }

        public String getLogoutUrl() { return logoutUrl; }
        public void setLogoutUrl(String logoutUrl) { this.logoutUrl = logoutUrl; }

        public String getLoginSuccessUrl() { return loginSuccessUrl; }
        public void setLoginSuccessUrl(String loginSuccessUrl) { this.loginSuccessUrl = loginSuccessUrl; }

        public boolean isCsrfEnabled() { return csrfEnabled; }
        public void setCsrfEnabled(boolean csrfEnabled) { this.csrfEnabled = csrfEnabled; }

        public List<String> getPublicUrls() { return publicUrls; }
        public void setPublicUrls(List<String> publicUrls) { this.publicUrls = publicUrls; }
    }
}
