SET client_encoding = 'UTF8';

CREATE TABLE basket_items (
    user_id bigint NOT NULL,
    item_id bigint NOT NULL
);

CREATE TABLE category (
    id bigint NOT NULL,
    name character varying(255),
    parent_id bigint
);

CREATE TABLE client (
    id bigint NOT NULL,
    confirmation_code character varying(255),
    email character varying(255),
    first_name character varying(255),
    is_non_locked boolean DEFAULT true NOT NULL,
    last_name character varying(255),
    login character varying(255),
    password character varying(255),
    patronymic character varying(255)
);

CREATE TABLE client_item (
    id bigint NOT NULL,
    quantity integer NOT NULL,
    item_id bigint NOT NULL,
    order_id bigint
);

CREATE TABLE client_roles (
    client_id bigint NOT NULL,
    roles character varying(255)
);

CREATE TABLE image (
    id bigint NOT NULL,
    image oid
);

CREATE TABLE item (
    id bigint NOT NULL,
    characteristics character varying(50000),
    code character varying(255),
    count bigint,
    created_on timestamp without time zone,
    description character varying(50000),
    image varchar(255),
    name character varying(255),
    price double precision,
    weight double precision,
    category_id bigint NOT NULL
);

CREATE TABLE item_additional_images (
    item_id bigint NOT NULL,
    additional_images_id bigint NOT NULL
);


CREATE TABLE orders (
    id bigint NOT NULL,
    city character varying(255),
    country character varying(255),
    phone_number character varying(255),
    street character varying(255),
    zip_code character varying(9),
    created_on timestamp without time zone,
    last_update timestamp without time zone,
    order_status character varying(255),
    payment_method character varying(255),
    track_number character varying(255),
    client_id bigint,
    manager_id bigint
);

CREATE SEQUENCE hibernate_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE basket_items
    ADD CONSTRAINT basket_items_pkey PRIMARY KEY (user_id, item_id);

ALTER TABLE category
    ADD CONSTRAINT category_pkey PRIMARY KEY (id);

ALTER TABLE client
    ADD CONSTRAINT client_pkey PRIMARY KEY (id);

ALTER TABLE client_item
    ADD CONSTRAINT client_item_pkey PRIMARY KEY (id);

ALTER TABLE image
    ADD CONSTRAINT image_pkey PRIMARY KEY (id);

ALTER TABLE item
    ADD CONSTRAINT item_pkey PRIMARY KEY (id);

ALTER TABLE item_additional_images
    ADD CONSTRAINT item_additional_images_pkey PRIMARY KEY (item_id, additional_images_id);

ALTER TABLE orders
    ADD CONSTRAINT orders_pkey PRIMARY KEY (id);


ALTER TABLE item_additional_images
    ADD CONSTRAINT uk_additional_images_id UNIQUE (additional_images_id);


ALTER TABLE basket_items
    ADD CONSTRAINT fk_basket_items_id FOREIGN KEY (item_id) REFERENCES client_item(id);

ALTER TABLE basket_items
    ADD CONSTRAINT fk_basket_user_id FOREIGN KEY (user_id) REFERENCES client(id);

ALTER TABLE category
    ADD CONSTRAINT fk_parent_id FOREIGN KEY (parent_id) REFERENCES category(id);

ALTER TABLE client_item
    ADD CONSTRAINT fk_order_id FOREIGN KEY (order_id) REFERENCES orders(id);

ALTER TABLE client_item
    ADD CONSTRAINT fk_ordered_item_id FOREIGN KEY (item_id) REFERENCES item(id);

ALTER TABLE client_roles
    ADD CONSTRAINT fk_client_id FOREIGN KEY (client_id) REFERENCES client(id);

ALTER TABLE item
    ADD CONSTRAINT fk_category_id FOREIGN KEY (category_id) REFERENCES category(id);

ALTER TABLE item_additional_images
    ADD CONSTRAINT fk_additional_images_id FOREIGN KEY (additional_images_id) REFERENCES image(id);

ALTER TABLE item_additional_images
    ADD CONSTRAINT fk_item_id FOREIGN KEY (item_id) REFERENCES item(id);

ALTER TABLE orders
    ADD CONSTRAINT fk_client_id FOREIGN KEY (client_id) REFERENCES client(id);

ALTER TABLE orders
    ADD CONSTRAINT fk_manager_id FOREIGN KEY (manager_id) REFERENCES client(id);


INSERT INTO client (id, confirmation_code, email, first_name, is_non_locked, last_name, login, password, patronymic) VALUES (79, NULL, 'goconnell@bernhard.com', 'Семён', true, 'Буков', 'CemenBukov', '$2a$08$2kTbaIVXieWDn.My.CQMhOslcXrPFPJLF9kvljX/hoVsOasebQLSu', NULL);

INSERT INTO client_roles (client_id, roles) VALUES (79, 'MANAGER');
INSERT INTO client_roles (client_id, roles) VALUES (79, 'USER');
INSERT INTO client_roles (client_id, roles) VALUES (79, 'ADMIN');