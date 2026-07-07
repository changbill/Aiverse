package com.example.aiverse.repository.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.aiverse.entity.CreditTransaction;
import com.example.aiverse.entity.CreditTransactionType;

public interface CreditTransactionJpaRepository extends JpaRepository<CreditTransaction, Long> {

    @Query("""
            SELECT creditTransaction
            FROM CreditTransaction creditTransaction
            WHERE creditTransaction.user.id = :userId
            AND (:type IS NULL OR creditTransaction.type = :type)
            ORDER BY creditTransaction.createdAt DESC, creditTransaction.id DESC
            """)
    Page<CreditTransaction> search(
            @Param("userId") Long userId,
            @Param("type") CreditTransactionType type,
            Pageable pageable
    );
}
