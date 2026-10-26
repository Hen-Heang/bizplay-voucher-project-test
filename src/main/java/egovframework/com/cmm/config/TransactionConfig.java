package egovframework.com.cmm.config;

import java.util.Collections;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionInterceptor;

/**
 * 트랜잭션 관리 설정 — practice copy of olv TransactionConfig
 * <p>
 * 1) AOP 기반 묵시적 트랜잭션: 모든 *Impl 클래스에 자동 적용
 *    - select/get/find/list/count 메서드 → readOnly
 *    - 그 외 메서드 → REQUIRED + rollbackFor Exception
 * 2) @Transactional 지원: 개별 메서드에서 전파 속성 등 오버라이드 가능
 * </p>
 *
 * 작성자 : henheang
 * 작성일 : 2026-10-26
 */
@Configuration
@EnableTransactionManagement
public class TransactionConfig {

    private static final Logger log = LoggerFactory.getLogger(TransactionConfig.class);

    /** egovframework.com 하위 패키지의 이름이 Impl 로 끝나는 클래스의 모든 메서드 */
    private static final String TX_POINTCUT_EXPRESSION =
            "execution(* egovframework.com..*Impl.*(..))";

    @Autowired
    private DataSource dataSource;

    @Bean
    public PlatformTransactionManager transactionManager() {
        log.info("Initializing TransactionManager (DataSource: {})", dataSource.getClass().getSimpleName());

        DataSourceTransactionManager tm = new DataSourceTransactionManager();
        tm.setDataSource(dataSource);
        return tm;
    }

    /**
     * AOP 트랜잭션 어드바이스 — 메서드 이름으로 readOnly / 쓰기 트랜잭션을 결정
     * <p>
     * 빈 이름을 txAdvice 로 지정하여 @EnableTransactionManagement 가
     * 자동 생성하는 transactionInterceptor 와 이름 충돌을 방지한다.
     * </p>
     */
    @SuppressWarnings("deprecation")
    @Bean
    public TransactionInterceptor txAdvice() {
        log.info("Initializing Transaction Advice (AOP) — Pointcut: {}", TX_POINTCUT_EXPRESSION);

        // 쓰기: 기존 트랜잭션 참여(없으면 생성) + 모든 Exception(checked 포함) 시 롤백
        RuleBasedTransactionAttribute writeAttr = new RuleBasedTransactionAttribute();
        writeAttr.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        writeAttr.setRollbackRules(
                Collections.singletonList(new RollbackRuleAttribute(Exception.class)));

        // 읽기: readOnly — PostgreSQL 은 이 트랜잭션 안의 INSERT/UPDATE/DELETE 를 거부한다
        RuleBasedTransactionAttribute readAttr = new RuleBasedTransactionAttribute();
        readAttr.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        readAttr.setReadOnly(true);

        NameMatchTransactionAttributeSource source = new NameMatchTransactionAttributeSource();
        source.addTransactionalMethod("select*", readAttr);
        source.addTransactionalMethod("get*", readAttr);
        source.addTransactionalMethod("find*", readAttr);
        source.addTransactionalMethod("list*", readAttr);
        source.addTransactionalMethod("count*", readAttr);
        source.addTransactionalMethod("*", writeAttr);

        return new TransactionInterceptor(transactionManager(), source);
    }

    /**
     * AOP Advisor (포인트컷 + 어드바이스 결합)
     */
    @Bean
    public Advisor txAdvisor() {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression(TX_POINTCUT_EXPRESSION);
        return new DefaultPointcutAdvisor(pointcut, txAdvice());
    }
}
