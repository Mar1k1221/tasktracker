
CREATE TABLE taskscontroller(
id BIGSERIAL PRIMARY KEY,
title VARCHAR(100) NOT NULL,
status VARCHAR(50) NOT NULL DEFAULT 'NEW',
priority VARCHAR(50) NOT NULL,
created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
SELECT * FROM taskscontroller;
INSERT INTO taskscontroller (title,status,priority)
VALUES
('Разобраться в SQL','NEW','HIGH'),
('Сделать домашнее задание','DONE', 'MEDIUM'),
('Отправить на проверку Олегу','IN_PROGRESS','LOW');

SELECT status, COUNT(*) AS total
FROM taskscontroller
GROUP BY status;

SELECT priority, COUNT(*) AS total
FROM taskscontroller
GROUP BY priority;

DROP TABLE IF EXISTS products;

CREATE TABLE products(
id BIGSERIAL PRIMARY KEY,
title VARCHAR(30) NOT NULL,
price INTEGER NOT NULL,
category VARCHAR(30) NOT NULL
);
INSERT INTO products(title,price,category)
VALUES
('Огурцы',300,'ОВОЩИ'),
('Малина',550,'ЯГОДЫ'),
('Банан',220,'ФРУКТЫ');
SELECT * FROM products;

SELECT id,title,price,category
FROM products
WHERE price > 250;

SELECT category, COUNT(*) AS total
FROM products
GROUP BY category;

SELECT AVG(price) AS average_price
FROM products;

DROP TABLE IF EXISTS books;

CREATE TABLE books(
id BIGSERIAL PRIMARY KEY,
title VARCHAR(100) NOT NULL,
author VARCHAR(100) NOT NULL,
year INTEGER NOT NULL,
circulation INTEGER NOT NULL
); 
INSERT INTO books(title,author,year,circulation)
VALUES
('Мастер и Маргарита', 'Михаил Булгаков', 1967, 50000),
('Преступление и наказание', 'Федор Достоевский', 1866, 30000),
('Война и мир', 'Лев Толстой', 1869, 40000),
('1984', 'Джордж Оруэлл', 1949, 100000),
('Гарри Поттер и философский камень', 'Джоан Роулинг', 1997, 150000),
('Властелин Колец', 'Джон Толкин', 1954, 80000),
('Мертвые души', 'Николай Гоголь', 1842, 25000),
('Евгений Онегин', 'Александр Пушкин', 1833, 35000),
('Мартин Иден', 'Джек Лондон', 1909, 45000),
('Вино из одуванчиков', 'Рэй Брэдбери', 1957, 60000);

SELECT*FROM books;

SELECT author
FROM books
WHERE author = 'Лев Толстой';


SELECT year
FROM books
WHERE year > 1957;

UPDATE books
SET circulation = 50000
WHERE LOWER(title) = LOWER('ВОЙНА И МИР');

SELECT * FROM books;


DELETE FROM books WHERE id=3;

SELECT * FROM books
ORDER BY year ASC;





