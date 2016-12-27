DROP table IF EXISTS users;
DROP table IF EXISTS  clients;
DROP table IF EXISTS  services;

create table users (
  user_id varchar PRIMARY KEY,
  user_type varchar,  -- admin, customer, employee, partner
  first_name varchar,
  last_name varchar,
  email varchar,
  password varchar,
  create_dt DATE,
  update_dt DATE
);

CREATE UNIQUE INDEX email_idx ON users(email);

create table clients (
  client_id VARCHAR PRIMARY KEY,
  client_type VARCHAR,  -- server, mobile, service, standalone, browser etc
  client_secret VARCHAR,
  client_name VARCHAR,
  client_desc VARCHAR,
  scope VARCHAR,
  redirect_url VARCHAR,
  authenticate_class VARCHAR,
  owner_id VARCHAR,
  create_dt DATE,
  update_dt DATE
);

create table services (
  service_id VARCHAR PRIMARY KEY,
  service_type VARCHAR,  -- api, ms
  service_name VARCHAR,
  service_desc VARCHAR,
  scope VARCHAR,
  owner_id VARCHAR,
  create_dt DATE,
  update_dt DATE
);

INSERT INTO users(user_id, user_type, first_name, last_name, email, password) VALUES('admin', 'admin', 'admin', 'admin', 'admin@networknt.com', '1000:5b39342c202d37372c203132302c202d3132302c2034372c2032332c2034352c202d34342c202d31362c2034372c202d35392c202d35362c2039302c202d352c202d38322c202d32385d:949e6fcf9c4bb8a3d6a8c141a3a9182a572fb95fe8ccdc93b54ba53df8ef2e930f7b0348590df0d53f242ccceeae03aef6d273a34638b49c559ada110ec06992');

INSERT INTO clients (client_id, client_type, client_secret, client_name, client_desc, scope, redirect_url, authenticate_class, owner_id) VALUES('f7d42348-c647-4efb-a52d-4c5787421e72', 'server', 'f6h1FTI8Q3-7UScPZDzfXA', 'PetStore Web Server', 'PetStore Web Server that calls PetStore API', 'petstore.r petstore.w', 'http://localhost:8080/authorization', null, 'admin' );
INSERT INTO clients (client_id, client_type, client_secret, client_name, client_desc, scope, redirect_url, authenticate_class, owner_id) VALUES('59f347a0-c92d-11e6-9d9d-cec0c932ce01', 'server', 'f6h1FTI8Q3-7UScPZDzfXA', 'PetStore Web Server', 'PetStore Web Server that calls PetStore API', 'petstore.r petstore.w', 'http://localhost:8080/authorization', null, 'admin' );

INSERT INTO services (service_id, service_type, service_name, service_desc, scope, owner_id) VALUES ('AACT0001', 'ms', 'Account Service', 'A microservice that serves account information', 'a.r b.r', 'admin');

