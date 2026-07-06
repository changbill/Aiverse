package com.example.aiverse.support;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository·Querydsl 통합 테스트용 슬라이스 베이스.
 * {@link IntegrationTestSupport}와 달리 JPA 계층만 기동해 Security·Web·OpenAPI 등은 로딩하지 않는다.
 */
@IntegrationTest
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        TestcontainersConfiguration.class,
        RepositoryIntegrationTestConfiguration.class,
})
@Transactional
public abstract class RepositoryIntegrationTestSupport {
}
