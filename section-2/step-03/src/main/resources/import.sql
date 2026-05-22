CREATE SEQUENCE IF NOT EXISTS car_info_id_seq;

CREATE TABLE IF NOT EXISTS car_info (
    id INT PRIMARY KEY DEFAULT nextval('car_info_id_seq'),
    make VARCHAR(255) NOT NULL,
    model VARCHAR(255) NOT NULL,
    year INT NOT NULL,
    condition VARCHAR(255),
    status VARCHAR(20) NOT NULL
);

INSERT INTO car_info (id, make, model, year, condition, status)
VALUES (1, 'Mercedes-Benz', 'C-Class', EXTRACT(YEAR FROM CURRENT_DATE) - 2, 'Minor dent on passenger door', 'RENTED')
ON CONFLICT (id) DO NOTHING;

INSERT INTO car_info (id, make, model, year, condition, status)
VALUES (2, 'BMW', 'X5', EXTRACT(YEAR FROM CURRENT_DATE) - 1, 'Recently serviced, excellent condition', 'IN_MAINTENANCE')
ON CONFLICT (id) DO NOTHING;

INSERT INTO car_info (id, make, model, year, condition, status)
VALUES (3, 'Audi', 'Q4', EXTRACT(YEAR FROM CURRENT_DATE) - 1, 'Brake pads recently replaced', 'RENTED')
ON CONFLICT (id) DO NOTHING;

INSERT INTO car_info (id, make, model, year, condition, status)
VALUES (4, 'Nissan', 'Altima', EXTRACT(YEAR FROM CURRENT_DATE) - 8, 'Interior needs cleaning', 'AT_CLEANING')
ON CONFLICT (id) DO NOTHING;

INSERT INTO car_info (id, make, model, year, condition, status)
VALUES (5, 'Ford', 'Focus', EXTRACT(YEAR FROM CURRENT_DATE) - 12, 'High mileage, engine issues', 'RENTED')
ON CONFLICT (id) DO NOTHING;

INSERT INTO car_info (id, make, model, year, condition, status)
VALUES (6, 'Toyota', 'Corolla', EXTRACT(YEAR FROM CURRENT_DATE) - 3, 'Like new, no issues', 'RENTED')
ON CONFLICT (id) DO NOTHING;

INSERT INTO car_info (id, make, model, year, condition, status)
VALUES (7, 'Honda', 'Civic', EXTRACT(YEAR FROM CURRENT_DATE) - 4, 'Good condition, minor wear and tear', 'RENTED')
ON CONFLICT (id) DO NOTHING;

INSERT INTO car_info (id, make, model, year, condition, status)
VALUES (8, 'Ford', 'F-150', EXTRACT(YEAR FROM CURRENT_DATE) - 2, 'Small scratch on rear bumper', 'IN_MAINTENANCE')
ON CONFLICT (id) DO NOTHING;

ALTER SEQUENCE car_info_id_seq RESTART WITH 8;
