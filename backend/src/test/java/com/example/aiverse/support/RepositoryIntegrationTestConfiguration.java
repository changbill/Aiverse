package com.example.aiverse.support;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

import com.example.aiverse.repository.impl.AssetRepositoryImpl;
import com.example.aiverse.repository.impl.AssetTagRepositoryImpl;
import com.example.aiverse.repository.impl.CategoryRepositoryImpl;
import com.example.aiverse.repository.impl.PurchaseRepositoryImpl;
import com.example.aiverse.repository.impl.RefreshTokenRepositoryImpl;
import com.example.aiverse.repository.impl.TagRepositoryImpl;
import com.example.aiverse.repository.impl.UserRepositoryImpl;

@TestConfiguration
@Import({
        UserRepositoryImpl.class,
        CategoryRepositoryImpl.class,
        TagRepositoryImpl.class,
        AssetRepositoryImpl.class,
        AssetTagRepositoryImpl.class,
        RefreshTokenRepositoryImpl.class,
        PurchaseRepositoryImpl.class,
})
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
public class RepositoryIntegrationTestConfiguration {
}
