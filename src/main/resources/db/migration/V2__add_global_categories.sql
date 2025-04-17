ALTER TABLE categories
    ADD COLUMN is_default BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE categories
    ALTER COLUMN user_id DROP NOT NULL;

INSERT INTO categories (name, description, user_id, is_default)
VALUES
    ('Зарплата', 'Доход от основной работы', NULL, TRUE),
    ('Подарки', 'Денежные подарки', NULL, TRUE),
    ('Инвестиции', 'Доход от инвестиций', NULL, TRUE),
    ('Другое (Доход)', 'Прочие доходы', NULL, TRUE);

INSERT INTO categories (name, description, user_id, is_default)
VALUES
    ('Продукты', 'Покупка продуктов питания', NULL, TRUE),
    ('Транспорт', 'Расходы на транспорт', NULL, TRUE),
    ('Жилье', 'Оплата жилья и коммунальных услуг', NULL, TRUE),
    ('Развлечения', 'Расходы на отдых и развлечения', NULL, TRUE),
    ('Другое (Расход)', 'Прочие расходы', NULL, TRUE);