CREATE SEQUENCE IF NOT EXISTS car_info_id_seq;

-- Drop the car_info table and recrete it
DROP TABLE IF EXISTS car_info;
CREATE TABLE IF NOT EXISTS car_info (
    id INT PRIMARY KEY DEFAULT nextval('car_info_id_seq'),
    make VARCHAR(255) NOT NULL,
    model VARCHAR(255) NOT NULL,
    year INT NOT NULL,
    status VARCHAR(20) NOT NULL
);

INSERT INTO car_info (id, make, model, year, status)
VALUES (1, 'Mercedes-Benz', 'C-Class', EXTRACT(YEAR FROM CURRENT_DATE) - 2, 'RENTED')
ON CONFLICT (id) DO NOTHING;

INSERT INTO car_info (id, make, model, year, status)
VALUES (2, 'BMW', 'X5', EXTRACT(YEAR FROM CURRENT_DATE) - 1, 'AT_CLEANING')
ON CONFLICT (id) DO NOTHING;

INSERT INTO car_info (id, make, model, year, status)
VALUES (3, 'Audi', 'Q4', EXTRACT(YEAR FROM CURRENT_DATE) - 1, 'RENTED')
ON CONFLICT (id) DO NOTHING;

INSERT INTO car_info (id, make, model, year, status)
VALUES (4, 'Nissan', 'Altima', EXTRACT(YEAR FROM CURRENT_DATE) - 8, 'AT_CLEANING')
ON CONFLICT (id) DO NOTHING;

INSERT INTO car_info (id, make, model, year, status)
VALUES (5, 'Ford', 'Focus', EXTRACT(YEAR FROM CURRENT_DATE) - 12, 'RENTED')
ON CONFLICT (id) DO NOTHING;

INSERT INTO car_info (id, make, model, year, status)
VALUES (6, 'Toyota', 'Corolla', EXTRACT(YEAR FROM CURRENT_DATE) - 3, 'RENTED')
ON CONFLICT (id) DO NOTHING;

INSERT INTO car_info (id, make, model, year, status)
VALUES (7, 'Honda', 'Civic', EXTRACT(YEAR FROM CURRENT_DATE) - 4, 'RENTED')
ON CONFLICT (id) DO NOTHING;

INSERT INTO car_info (id, make, model, year, status)
VALUES (8, 'Ford', 'F-150', EXTRACT(YEAR FROM CURRENT_DATE) - 2, 'AT_CLEANING')
ON CONFLICT (id) DO NOTHING;

ALTER SEQUENCE car_info_id_seq RESTART WITH 8;