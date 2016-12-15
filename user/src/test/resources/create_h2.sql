DROP table IF EXISTS users;
DROP table IF EXISTS  clients;
DROP table IF EXISTS  services;

create table users (
  user_id varchar PRIMARY KEY,
  user_type varchar,  -- admin, customer, employee, partner
  first_name varchar,
  last_name varchar,
  email varchar,
  password varchar
);

CREATE UNIQUE INDEX email_idx ON user(email);

create table clients (
  client_id VARCHAR PRIMARY KEY,
  client_type VARCHAR,  -- server, mobile, api, standalone, browser etc
  client_secret VARCHAR,
  client_name VARCHAR,
  client_desc VARCHAR,
  scope VARCHAR,
  redirect_url VARCHAR,
  authenticate_class VARCHAR
);

create table services (
  service_id VARCHAR PRIMARY KEY,
  service_type VARCHAR,  -- api, ms
  service_name VARCHAR,
  service_desc VARCHAR,
  scope VARCHAR
);

INSERT INTO users VALUES('admin', 'admin', 'admin', 'admin', 'admin@networknt.com', 'admin');

INSERT INTO clients VALUES('f7d42348-c647-4efb-a52d-4c5787421e72', 'server', 'f6h1FTI8Q3-7UScPZDzfXA', 'PetStore Web Server', 'PetStore Web Server that calls PetStore API', 'petstore.r petstore.w', 'http://localhost:8080/authorization' );

INSERT INTO services VALUES ('AACT0001', 'ms', 'Account Service', 'A microservice that serves account information', 'a.r b.r');

