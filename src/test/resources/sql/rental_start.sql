INSERT INTO users (id, username, password, role, is_active, balance)
VALUES (1, 'testuser', 'hash123', 'USER', true, 1000.00);

INSERT INTO tariffs (id, name, description, price)
VALUES (1, 'Минутный', 'Описание', 5.00);

INSERT INTO scooter_models (id, name, price_per_minute, max_speed)
VALUES (1, 'Ninebot Max', 2.00, 25);

INSERT INTO rental_points (id, name, city, street, house_number, latitude, longitude)
VALUES (1, 'Центр', 'Минск', 'Независимости', '1', 53.9000, 27.5600);

INSERT INTO scooters (id, serial_number, model_id, rental_point_id, battery_level, latitude, longitude, status, mileage)
VALUES (1, 'SN-START', 1, 1, 100, 53.9000, 27.5600, 'AVAILABLE', 10.0);
