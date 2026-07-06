package com.example.aiverse.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Security·MockMvc·애플리케이션 기동 등 전체 컨텍스트가 필요한 통합 테스트용 베이스.
 * Repository 테스트는 {@link RepositoryIntegrationTestSupport}를 사용한다.
 */
@IntegrationTest
@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@Transactional
public abstract class IntegrationTestSupport {
}
