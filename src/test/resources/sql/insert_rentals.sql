TRUNCATE TABLE rentals, scooters, rental_points, scooter_models, tariffs, users CASCADE;

-- пользователь для старта
INSERT INTO users (id, username, password, role, is_active, balance)
VALUES (1, 'testuser', 'hash123', 'USER', true, 1000.00);

-- пользователь для конца
INSERT INTO users (id, username, password, role, is_active, balance)
VALUES (2, 'finisher', 'hash123', 'USER', true, 1000.00);

-- 3. Создаем тариф, модель и точку
INSERT INTO tariffs (id, name, description, price)
VALUES (1, 'Минутный', 'Описание', 5.00);

INSERT INTO scooter_models (id, name, price_per_minute, max_speed)
VALUES (1, 'Ninebot Max', 2.00, 25);

INSERT INTO rental_points (id, name, city, street, house_number, latitude, longitude)
VALUES (1, 'Центр', 'Минск', 'Независимости', '1', 53.9000, 27.5600);

-- свобоный самокат
INSERT INTO scooters (id, serial_number, model_id, rental_point_id, battery_level, latitude, longitude, status, mileage)
VALUES (1, 'SN-START', 1, 1, 100, 53.9000, 27.5600, 'AVAILABLE', 10.0);

-- занятый самокат
INSERT INTO scooters (id, serial_number, model_id, rental_point_id, battery_level, latitude, longitude, status, mileage)
VALUES (2, 'SN-FINISH', 1, 1, 50, 53.9000, 27.5600, 'RENTED', 20.0);

-- активная аренда
INSERT INTO rentals (id, user_id, scooter_id, tariff_id, start_time, start_lat, start_lon, is_active)
VALUES (1, 2, 2, 1, CURRENT_TIMESTAMP, 53.9000, 27.5600, true);

ALTER SEQUENCE rentals_id_seq RESTART WITH 51;