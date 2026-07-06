INSERT INTO `category` (`id`, `name`, `slug`, `display_order`, `active`)
VALUES (1, 'NATURE', 'nature', 1, TRUE),
       (2, 'PEOPLE', 'people', 2, TRUE),
       (3, 'BUSINESS', 'business', 3, TRUE),
       (4, 'TECHNOLOGY', 'technology', 4, TRUE),
       (5, 'FANTASY', 'fantasy', 5, TRUE),
       (6, 'ABSTRACT', 'abstract', 6, TRUE),
       (7, 'LIFESTYLE', 'lifestyle', 7, TRUE),
       (8, 'OTHER', 'other', 8, TRUE);

INSERT INTO `credit_product` (
    `id`,
    `code`,
    `name`,
    `credit_amount`,
    `bonus_credit`,
    `price`,
    `display_order`,
    `status`
)
VALUES (1, 'BASIC', 'Basic', 500, 0, 5000, 1, 'ACTIVE'),
       (2, 'PLUS', 'Plus', 1000, 100, 10000, 2, 'ACTIVE'),
       (3, 'PRO', 'Pro', 3000, 500, 30000, 3, 'ACTIVE');
