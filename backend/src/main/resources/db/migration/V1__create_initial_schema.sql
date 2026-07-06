CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `email` VARCHAR(255) NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `nickname` VARCHAR(20) NOT NULL,
    `role` VARCHAR(20) NOT NULL DEFAULT 'USER',
    `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    `profile_url` VARCHAR(2048) NULL,
    `introduction` TEXT NULL,
    `credit_balance` INT NOT NULL DEFAULT 0,
    `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `updated_at` DATETIME(6) NULL,
    CONSTRAINT `pk_user` PRIMARY KEY (`id`),
    CONSTRAINT `uk_user_email` UNIQUE (`email`),
    CONSTRAINT `uk_user_nickname` UNIQUE (`nickname`),
    CONSTRAINT `ck_user_role` CHECK (`role` IN ('USER', 'ADMIN')),
    CONSTRAINT `ck_user_status` CHECK (`status` IN ('ACTIVE', 'DELETED')),
    CONSTRAINT `ck_user_credit_balance` CHECK (`credit_balance` >= 0)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `refresh_token` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `token_hash` VARCHAR(255) NOT NULL,
    `expires_at` DATETIME(6) NOT NULL,
    `revoked_at` DATETIME(6) NULL,
    `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT `pk_refresh_token` PRIMARY KEY (`id`),
    CONSTRAINT `uk_refresh_token_token_hash` UNIQUE (`token_hash`),
    CONSTRAINT `fk_refresh_token_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    INDEX `idx_refresh_token_user_active` (`user_id`, `revoked_at`, `expires_at`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(30) NOT NULL,
    `slug` VARCHAR(50) NOT NULL,
    `display_order` INT NOT NULL,
    `active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `updated_at` DATETIME(6) NULL,
    CONSTRAINT `pk_category` PRIMARY KEY (`id`),
    CONSTRAINT `uk_category_name` UNIQUE (`name`),
    CONSTRAINT `uk_category_slug` UNIQUE (`slug`),
    CONSTRAINT `uk_category_display_order` UNIQUE (`display_order`),
    CONSTRAINT `ck_category_display_order` CHECK (`display_order` > 0),
    INDEX `idx_category_active_order` (`active`, `display_order`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(30) NOT NULL,
    `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT `pk_tag` PRIMARY KEY (`id`),
    CONSTRAINT `uk_tag_name` UNIQUE (`name`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `asset` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `creator_id` BIGINT NOT NULL,
    `title` VARCHAR(200) NOT NULL,
    `description` VARCHAR(2000) NULL,
    `asset_type` VARCHAR(20) NOT NULL,
    `category_id` BIGINT NOT NULL,
    `preview_object_key` VARCHAR(1024) NULL,
    `original_object_key` VARCHAR(1024) NOT NULL,
    `original_filename` VARCHAR(255) NOT NULL,
    `content_type` VARCHAR(100) NOT NULL,
    `file_size` BIGINT NOT NULL,
    `price_credit` INT NOT NULL,
    `ai_tool` VARCHAR(100) NULL,
    `license_type` VARCHAR(20) NOT NULL,
    `status` VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    `view_count` BIGINT NOT NULL DEFAULT 0,
    `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `updated_at` DATETIME(6) NULL,
    `deleted_at` DATETIME(6) NULL,
    CONSTRAINT `pk_asset` PRIMARY KEY (`id`),
    CONSTRAINT `fk_asset_creator` FOREIGN KEY (`creator_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_asset_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`),
    CONSTRAINT `ck_asset_type` CHECK (`asset_type` IN ('IMAGE', 'VIDEO', 'MUSIC')),
    CONSTRAINT `ck_asset_file_size` CHECK (`file_size` > 0),
    CONSTRAINT `ck_asset_price_credit` CHECK (`price_credit` >= 0),
    CONSTRAINT `ck_asset_license_type` CHECK (`license_type` IN ('PERSONAL', 'COMMERCIAL')),
    CONSTRAINT `ck_asset_status` CHECK (`status` IN ('PUBLISHED', 'DELETED')),
    CONSTRAINT `ck_asset_view_count` CHECK (`view_count` >= 0),
    INDEX `idx_asset_status_created` (`status`, `created_at`, `id`),
    INDEX `idx_asset_creator_status_created` (`creator_id`, `status`, `created_at`, `id`),
    INDEX `idx_asset_category_status_created` (`category_id`, `status`, `created_at`, `id`),
    INDEX `idx_asset_type_status_created` (`asset_type`, `status`, `created_at`, `id`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `asset_tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `asset_id` BIGINT NOT NULL,
    `tag_id` BIGINT NOT NULL,
    CONSTRAINT `pk_asset_tag` PRIMARY KEY (`id`),
    CONSTRAINT `uk_asset_tag_asset_tag` UNIQUE (`asset_id`, `tag_id`),
    CONSTRAINT `fk_asset_tag_asset` FOREIGN KEY (`asset_id`) REFERENCES `asset` (`id`),
    CONSTRAINT `fk_asset_tag_tag` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`id`),
    INDEX `idx_asset_tag_tag_asset` (`tag_id`, `asset_id`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `credit_product` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `code` VARCHAR(30) NOT NULL,
    `name` VARCHAR(100) NOT NULL,
    `credit_amount` INT NOT NULL,
    `bonus_credit` INT NOT NULL DEFAULT 0,
    `price` INT NOT NULL,
    `display_order` INT NOT NULL,
    `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT `pk_credit_product` PRIMARY KEY (`id`),
    CONSTRAINT `uk_credit_product_code` UNIQUE (`code`),
    CONSTRAINT `uk_credit_product_display_order` UNIQUE (`display_order`),
    CONSTRAINT `ck_credit_product_credit_amount` CHECK (`credit_amount` > 0),
    CONSTRAINT `ck_credit_product_bonus_credit` CHECK (`bonus_credit` >= 0),
    CONSTRAINT `ck_credit_product_price` CHECK (`price` > 0),
    CONSTRAINT `ck_credit_product_display_order` CHECK (`display_order` > 0),
    CONSTRAINT `ck_credit_product_status` CHECK (`status` IN ('ACTIVE', 'INACTIVE')),
    INDEX `idx_credit_product_status_order` (`status`, `display_order`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `payment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `credit_product_id` BIGINT NOT NULL,
    `idempotency_key` VARCHAR(255) NOT NULL,
    `amount` INT NOT NULL,
    `method` VARCHAR(30) NOT NULL,
    `status` VARCHAR(30) NOT NULL,
    `transaction_key` VARCHAR(255) NULL,
    `paid_at` DATETIME(6) NULL,
    `failed_reason` VARCHAR(500) NULL,
    `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT `pk_payment` PRIMARY KEY (`id`),
    CONSTRAINT `uk_payment_user_idempotency` UNIQUE (`user_id`, `idempotency_key`),
    CONSTRAINT `uk_payment_transaction_key` UNIQUE (`transaction_key`),
    CONSTRAINT `fk_payment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_payment_credit_product` FOREIGN KEY (`credit_product_id`) REFERENCES `credit_product` (`id`),
    CONSTRAINT `ck_payment_amount` CHECK (`amount` > 0),
    CONSTRAINT `ck_payment_method` CHECK (`method` IN ('MOCK')),
    CONSTRAINT `ck_payment_status` CHECK (`status` IN ('SUCCESS', 'FAILED')),
    INDEX `idx_payment_user_created` (`user_id`, `created_at`, `id`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `credit_transaction` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `payment_id` BIGINT NULL,
    `type` VARCHAR(20) NOT NULL,
    `amount` INT NOT NULL,
    `balance_after` INT NOT NULL,
    `reason` VARCHAR(500) NOT NULL,
    `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT `pk_credit_transaction` PRIMARY KEY (`id`),
    CONSTRAINT `fk_credit_transaction_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_credit_transaction_payment` FOREIGN KEY (`payment_id`) REFERENCES `payment` (`id`),
    CONSTRAINT `ck_credit_transaction_type` CHECK (`type` IN ('CHARGE', 'PURCHASE', 'SALE')),
    CONSTRAINT `ck_credit_transaction_amount_nonzero` CHECK (`amount` <> 0),
    CONSTRAINT `ck_credit_transaction_balance_after` CHECK (`balance_after` >= 0),
    INDEX `idx_credit_transaction_user_created` (`user_id`, `created_at`, `id`),
    INDEX `idx_credit_transaction_payment` (`payment_id`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `purchase` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `asset_id` BIGINT NOT NULL,
    `credit_transaction_id` BIGINT NOT NULL,
    `idempotency_key` VARCHAR(255) NOT NULL,
    `purchase_price_credit` INT NOT NULL,
    `license_type` VARCHAR(20) NOT NULL,
    `purchased_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT `pk_purchase` PRIMARY KEY (`id`),
    CONSTRAINT `uk_purchase_user_asset` UNIQUE (`user_id`, `asset_id`),
    CONSTRAINT `uk_purchase_user_idempotency` UNIQUE (`user_id`, `idempotency_key`),
    CONSTRAINT `uk_purchase_credit_transaction` UNIQUE (`credit_transaction_id`),
    CONSTRAINT `fk_purchase_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_purchase_asset` FOREIGN KEY (`asset_id`) REFERENCES `asset` (`id`),
    CONSTRAINT `fk_purchase_credit_transaction` FOREIGN KEY (`credit_transaction_id`) REFERENCES `credit_transaction` (`id`),
    CONSTRAINT `ck_purchase_price_credit` CHECK (`purchase_price_credit` >= 0),
    CONSTRAINT `ck_purchase_license_type` CHECK (`license_type` IN ('PERSONAL', 'COMMERCIAL')),
    INDEX `idx_purchase_user_purchased` (`user_id`, `purchased_at`, `id`),
    INDEX `idx_purchase_asset_purchased` (`asset_id`, `purchased_at`, `id`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `download` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `purchase_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `asset_id` BIGINT NOT NULL,
    `downloaded_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT `pk_download` PRIMARY KEY (`id`),
    CONSTRAINT `fk_download_purchase` FOREIGN KEY (`purchase_id`) REFERENCES `purchase` (`id`),
    CONSTRAINT `fk_download_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_download_asset` FOREIGN KEY (`asset_id`) REFERENCES `asset` (`id`),
    INDEX `idx_download_purchase_downloaded` (`purchase_id`, `downloaded_at`, `id`),
    INDEX `idx_download_user_downloaded` (`user_id`, `downloaded_at`, `id`),
    INDEX `idx_download_asset_downloaded` (`asset_id`, `downloaded_at`, `id`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `creator_settlement` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `creator_id` BIGINT NOT NULL,
    `purchase_id` BIGINT NOT NULL,
    `asset_id` BIGINT NOT NULL,
    `gross_credit` INT NOT NULL,
    `platform_fee_credit` INT NOT NULL,
    `settlement_credit` INT NOT NULL,
    `status` VARCHAR(20) NOT NULL DEFAULT 'SETTLED',
    `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `settled_at` DATETIME(6) NULL,
    CONSTRAINT `pk_creator_settlement` PRIMARY KEY (`id`),
    CONSTRAINT `uk_creator_settlement_purchase` UNIQUE (`purchase_id`),
    CONSTRAINT `fk_creator_settlement_creator` FOREIGN KEY (`creator_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_creator_settlement_purchase` FOREIGN KEY (`purchase_id`) REFERENCES `purchase` (`id`),
    CONSTRAINT `fk_creator_settlement_asset` FOREIGN KEY (`asset_id`) REFERENCES `asset` (`id`),
    CONSTRAINT `ck_creator_settlement_gross_credit` CHECK (`gross_credit` >= 0),
    CONSTRAINT `ck_creator_settlement_platform_fee` CHECK (`platform_fee_credit` >= 0),
    CONSTRAINT `ck_creator_settlement_credit` CHECK (`settlement_credit` >= 0),
    CONSTRAINT `ck_creator_settlement_sum` CHECK (`platform_fee_credit` + `settlement_credit` = `gross_credit`),
    CONSTRAINT `ck_creator_settlement_status` CHECK (`status` IN ('SETTLED')),
    INDEX `idx_creator_settlement_creator_created` (`creator_id`, `created_at`, `id`),
    INDEX `idx_creator_settlement_asset_created` (`asset_id`, `created_at`, `id`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
