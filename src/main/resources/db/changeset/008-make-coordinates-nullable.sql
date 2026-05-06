--liquibase formatted sql

--changeset admin:make-coordinates-nullable-1
ALTER TABLE rental_points ALTER COLUMN latitude DROP NOT NULL;
ALTER TABLE rental_points ALTER COLUMN longitude DROP NOT NULL;
