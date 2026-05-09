INSERT INTO scooter_models (id, name, price_per_minute, max_speed)
VALUES (1, 'Ninebot Pro', 5.00, 25);

INSERT INTO rental_points (id, name, latitude, longitude)
VALUES (1, 'Октябрьская', 53.9000, 27.5667);

--  ДОСТУПНЫЙ самокат
INSERT INTO scooters (id, serial_number, model_id, rental_point_id, battery_level, latitude, longitude, status, mileage)
VALUES (1, 'SN-100', 1, 1, 95, 53.9000, 27.5667, 'AVAILABLE', 15.5);

-- ЗАНЯТЫЙ самокат
INSERT INTO scooters (id, serial_number, model_id, rental_point_id, battery_level, latitude, longitude, status, mileage)
VALUES (2, 'SN-200', 1, 1, 80, 53.9000, 27.5667, 'RENTED', 42.0);
