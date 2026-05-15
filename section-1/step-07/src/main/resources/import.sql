-- Customers
CREATE SEQUENCE IF NOT EXISTS customer_seq;

CREATE TABLE IF NOT EXISTS customers (
    id INT PRIMARY KEY DEFAULT nextval('customer_seq') NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL
);

INSERT INTO customers (id, first_name, last_name)
VALUES (1, 'Speedy', 'McWheels')
ON CONFLICT (id) DO NOTHING;
INSERT INTO customers (id, first_name, last_name)
VALUES (2, 'Zoom', 'Thunderfoot')
ON CONFLICT (id) DO NOTHING;
INSERT INTO customers (id, first_name, last_name)
VALUES (3, 'Vroom', 'Lightyear')
ON CONFLICT (id) DO NOTHING;
INSERT INTO customers (id, first_name, last_name)
VALUES (4, 'Turbo', 'Gearshift')
ON CONFLICT (id) DO NOTHING;
INSERT INTO customers (id, first_name, last_name)
VALUES (5, 'Drifty', 'Skiddy')
ON CONFLICT (id) DO NOTHING;

ALTER SEQUENCE customer_seq RESTART WITH 5;


-- Bookings
CREATE SEQUENCE IF NOT EXISTS booking_seq;

CREATE TABLE IF NOT EXISTS bookings (
    id INT PRIMARY KEY DEFAULT nextval('booking_seq') NOT NULL,
    dateFrom DATE,
    dateTo DATE,
    location VARCHAR(255),
    customer_id INT,
    CONSTRAINT fk_bookings_customer_id
       FOREIGN KEY (customer_id) 
       REFERENCES customers (id)
);

INSERT INTO bookings (id, customer_id, dateFrom, dateTo, location)
VALUES (1, 1, CURRENT_DATE + 1, CURRENT_DATE + 3, 'Verbier, Switzerland')
ON CONFLICT (id) DO NOTHING;
INSERT INTO bookings (id, customer_id, dateFrom, dateTo, location)
VALUES (2, 1, CURRENT_DATE + 14, CURRENT_DATE + 16, 'Sao Paulo, Brazil')
ON CONFLICT (id) DO NOTHING;
INSERT INTO bookings (id, customer_id, dateFrom, dateTo, location)
VALUES (3, 1, CURRENT_DATE + 30, CURRENT_DATE + 34, 'Antwerp, Belgium')
ON CONFLICT (id) DO NOTHING;

INSERT INTO bookings (id, customer_id, dateFrom, dateTo, location)
VALUES (4, 2, CURRENT_DATE + 2, CURRENT_DATE + 7, 'Tokyo, Japan')
ON CONFLICT (id) DO NOTHING;
INSERT INTO bookings (id, customer_id, dateFrom, dateTo, location)
VALUES (5, 2, CURRENT_DATE + 60, CURRENT_DATE + 65, 'Brisbane, Australia')
ON CONFLICT (id) DO NOTHING;

INSERT INTO bookings (id, customer_id, dateFrom, dateTo, location)
VALUES (7, 3, CURRENT_DATE + 3, CURRENT_DATE + 8, 'Missoula, Montana')
ON CONFLICT (id) DO NOTHING;
INSERT INTO bookings (id, customer_id, dateFrom, dateTo, location)
VALUES (8, 3, CURRENT_DATE + 35, CURRENT_DATE + 41, 'Singapore')
ON CONFLICT (id) DO NOTHING;
INSERT INTO bookings (id, customer_id, dateFrom, dateTo, location)
VALUES (9, 3, CURRENT_DATE + 90, CURRENT_DATE + 96, 'Capetown, South Africa')
ON CONFLICT (id) DO NOTHING;

INSERT INTO bookings (id, customer_id, dateFrom, dateTo, location)
VALUES (10, 4, CURRENT_DATE + 1, CURRENT_DATE + 6, 'Nuuk, Greenland')
ON CONFLICT (id) DO NOTHING;
INSERT INTO bookings (id, customer_id, dateFrom, dateTo, location)
VALUES (11, 4, CURRENT_DATE + 75, CURRENT_DATE + 80, 'Santiago de Chile')
ON CONFLICT (id) DO NOTHING;
INSERT INTO bookings (id, customer_id, dateFrom, dateTo, location)
VALUES (12, 4, CURRENT_DATE + 120, CURRENT_DATE + 127, 'Dubai')
ON CONFLICT (id) DO NOTHING;

ALTER SEQUENCE booking_seq RESTART WITH 12;

-- Drop the embeddings table. It will be recreated by the RAG ingestor
DROP TABLE IF EXISTS embeddings;