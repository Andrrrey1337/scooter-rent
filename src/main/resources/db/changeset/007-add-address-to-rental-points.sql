--liquibase formatted sql

--changeset admin:add-address-to-rental-points-1
ALTER TABLE rental_points ADD COLUMN city VARCHAR(50);
ALTER TABLE rental_points ADD COLUMN street VARCHAR(50);
ALTER TABLE rental_points ADD COLUMN house_number VARCHAR(10);
