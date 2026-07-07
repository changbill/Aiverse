package com.example.aiverse.support;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

import com.example.aiverse.repository.impl.AssetRepositoryImpl;
import com.example.aiverse.repository.impl.AssetTagRepositoryImpl;
import com.example.aiverse.repository.impl.CategoryRepositoryImpl;
import com.example.aiverse.repository.impl.CreatorSettlementRepositoryImpl;
import com.example.aiverse.repository.impl.CreditProductRepositoryImpl;
import com.example.aiverse.repository.impl.CreditTransactionRepositoryImpl;
import com.example.aiverse.repository.impl.DownloadRepositoryImpl;
import com.example.aiverse.repository.impl.PaymentRepositoryImpl;
import com.example.aiverse.repository.impl.PurchaseRepositoryImpl;
import com.example.aiverse.repository.impl.RefreshTokenRepositoryImpl;
import com.example.aiverse.repository.impl.TagRepositoryImpl;
import com.example.aiverse.repository.impl.UserRepositoryImpl;
import com.example.aiverse.repository.querydsl.AssetQuerydslRepository;
import com.example.aiverse.repository.querydsl.CreatorSettlementQuerydslRepository;

@TestConfiguration
@Import({
        UserRepositoryImpl.class,
        CategoryRepositoryImpl.class,
        TagRepositoryImpl.class,
        AssetRepositoryImpl.class,
        AssetQuerydslRepository.class,
        AssetTagRepositoryImpl.class,
        RefreshTokenRepositoryImpl.class,
        PurchaseRepositoryImpl.class,
        CreditProductRepositoryImpl.class,
        PaymentRepositoryImpl.class,
        CreditTransactionRepositoryImpl.class,
        CreatorSettlementRepositoryImpl.class,
        CreatorSettlementQuerydslRepository.class,
        DownloadRepositoryImpl.class,
})
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
public class RepositoryIntegrationTestConfiguration {
}
