package com.example.aiverse.support;

import com.example.aiverse.entity.Asset;
import com.example.aiverse.entity.LicenseType;
import com.example.aiverse.entity.User;

import jakarta.persistence.EntityManager;

public final class TestPurchaseSupport {

    private TestPurchaseSupport() {
    }

    public static void insertPurchase(
            EntityManager entityManager,
            User buyer,
            Asset asset,
            String idempotencyKey
    ) {
        entityManager.createNativeQuery("""
                INSERT INTO credit_transaction (user_id, type, amount, balance_after, reason, created_at)
                VALUES (:userId, 'PURCHASE', :amount, 0, 'integration-test', NOW(6))
                """)
                .setParameter("userId", buyer.getId())
                .setParameter("amount", -asset.getPriceCredit())
                .executeUpdate();

        Number creditTransactionId = (Number) entityManager
                .createNativeQuery("SELECT LAST_INSERT_ID()")
                .getSingleResult();

        entityManager.createNativeQuery("""
                INSERT INTO purchase (
                    user_id, asset_id, credit_transaction_id, idempotency_key,
                    purchase_price_credit, license_type, purchased_at
                )
                VALUES (
                    :userId, :assetId, :creditTransactionId, :idempotencyKey,
                    :price, :licenseType, NOW(6)
                )
                """)
                .setParameter("userId", buyer.getId())
                .setParameter("assetId", asset.getId())
                .setParameter("creditTransactionId", creditTransactionId.longValue())
                .setParameter("idempotencyKey", idempotencyKey)
                .setParameter("price", asset.getPriceCredit())
                .setParameter("licenseType", LicenseType.PERSONAL.name())
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();
    }
}
