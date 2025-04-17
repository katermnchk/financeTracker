ALTER TABLE categories ADD COLUMN category_type VARCHAR(50);
UPDATE categories SET category_type = 'INCOME' WHERE name IN ('Зарплата', 'Подарки', 'Инвестиции', 'Другое (Доход)');
UPDATE categories SET category_type = 'EXPENSE' WHERE name IN ('Продукты', 'Транспорт', 'Жилье', 'Развлечения', 'Другое (Расход)');