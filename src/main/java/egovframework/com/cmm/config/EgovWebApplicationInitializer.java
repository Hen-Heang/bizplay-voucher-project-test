package egovframework.com.cmm.config;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 애플리케이션 진입점 (practice copy of olv EgovWebApplicationInitializer)
 *
 * <p>
 * - scanBasePackages = "egovframework" : egovframework.com.** 하위 @Controller/@Service 등록<br>
 * - @MapperScan(annotationClass = Mapper.class) : @Mapper 가 붙은 인터페이스만 MyBatis Mapper 로 등록
 * </p>
 *
 * <p>
 * olv 는 WAR 배포를 위해 SpringBootServletInitializer 를 상속하지만,
 * 이 연습 프로젝트는 내장 Tomcat(main) 으로만 실행하므로 상속하지 않는다.
 * </p>
 *
 * 작성자 : henheang
 * 작성일 : 2026-10-26
 */
@SpringBootApplication(scanBasePackages = "egovframework")
@MapperScan(
    basePackages = "egovframework.com",
    annotationClass = Mapper.class
)
public class EgovWebApplicationInitializer {

    /**
     * 내장 Tomcat 으로 실행
     *
     * @param args 명령행 인자
     */
    public static void main(String[] args) {
        SpringApplication.run(EgovWebApplicationInitializer.class, args);
    }
}
