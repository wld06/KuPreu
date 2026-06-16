-- Initial schema. Generated from the JPA entities (Hibernate MySQLDialect)
-- so it matches what spring.jpa.hibernate.ddl-auto=validate expects.

create table attribute (Id binary(16) not null, name varchar(255) not null, primary key (Id)) engine=InnoDB;
create table brand (Id binary(16) not null, name varchar(255) not null, primary key (Id)) engine=InnoDB;
create table category (Id binary(16) not null, name varchar(255) not null, primary key (Id)) engine=InnoDB;
create table date_dim (id binary(16) not null, date datetime(6) not null, primary key (id)) engine=InnoDB;
create table postal_code (id binary(16) not null, city varchar(255) not null, code varchar(255) not null, primary key (id)) engine=InnoDB;
create table price_snapshot (price decimal(10,2) not null, uuid binary(16) not null, id_date_end binary(16), id_date_start binary(16) not null, id_product binary(16) not null, id_store binary(16) not null, primary key (id_date_start, id_product, id_store)) engine=InnoDB;
create table product (id binary(16) not null, ean varchar(255) not null, name varchar(255) not null, stock_qty integer, id_brand binary(16), id_subcategory binary(16) not null, id_unit binary(16) not null, primary key (id)) engine=InnoDB;
create table shopping_list (id binary(16) not null, created_at datetime(6) not null, name varchar(255) not null, id_user binary(16) not null, primary key (id)) engine=InnoDB;
create table shopping_list_item (id binary(16) not null, quantity integer not null, price_snapshot_id binary(16) not null, id_list binary(16) not null, primary key (id)) engine=InnoDB;
create table store (id binary(16) not null, address varchar(255) not null, id_postal_code binary(16), id_chain binary(16) not null, primary key (id)) engine=InnoDB;
create table subcategory (id binary(16) not null, name varchar(255) not null, id_category binary(16) not null, primary key (id)) engine=InnoDB;
create table supermarket_chain (Id binary(16) not null, name varchar(255) not null, primary key (Id)) engine=InnoDB;
create table unit_of_measure (Id binary(16) not null, name varchar(255) not null, symbol varchar(255) not null, primary key (Id)) engine=InnoDB;
create table users (id binary(16) not null, createdAt datetime(6) not null, email varchar(255) not null, is_admin bit not null, name varchar(255) not null, password varchar(255) not null, surname varchar(255) not null, username varchar(255) not null, id_postal_code binary(16), primary key (id)) engine=InnoDB;

alter table attribute add constraint UKhpwum0iq12fs4ej5d0tgy6wsn unique (name);
alter table brand add constraint UKrdxh7tq2xs66r485cc8dkxt77 unique (name);
alter table category add constraint UK46ccwnsi9409t36lurvtyljak unique (name);
alter table date_dim add constraint UKqi8wn6y7pr68ypi275hgdbcdl unique (date);
alter table postal_code add constraint UKkxaqs1uvf03c1vrc32yr0fuhq unique (code);
alter table price_snapshot add constraint UKkmcscrika17fidy814i1ndfu0 unique (uuid);
alter table product add constraint UK28khsd8d7h95pbf1iiq7r779v unique (ean);
alter table shopping_list add constraint uq_shopping_list_user_name unique (id_user, name);
alter table subcategory add constraint UKe060alu3238gwu0mvhgh6xkhd unique (name);
alter table supermarket_chain add constraint UK8mc8q7ato3607twhqn1tww6tm unique (name);
alter table unit_of_measure add constraint UK7nw8dkyxq0iy0a603giba4ttg unique (name);
alter table users add constraint UK6dotkott2kjsp8vw4d0m25fb7 unique (email);
alter table users add constraint UKr43af9ap4edm43mmtq01oddj6 unique (username);

alter table price_snapshot add constraint FKf74c78twvx0cxx2khk2hjse7k foreign key (id_date_end) references date_dim (id);
alter table price_snapshot add constraint FKqq8lnv3ueie5nhquoc44fbfbi foreign key (id_date_start) references date_dim (id);
alter table price_snapshot add constraint FKjrwdbc0p4ynvkqprmoug00ct3 foreign key (id_product) references product (id);
alter table price_snapshot add constraint FK7gesgsxe8mufjyn00bo0igu1s foreign key (id_store) references store (id);
alter table product add constraint FK55evcjddwhtr4pbaggns9nqc8 foreign key (id_brand) references brand (Id);
alter table product add constraint FK9urkb4e9cim6qhm7k61edb5ww foreign key (id_subcategory) references subcategory (id);
alter table product add constraint FK1rto0pmtfbl74sdekw8pewpkb foreign key (id_unit) references unit_of_measure (Id);
alter table shopping_list add constraint FKga9i254k6hf7laws52b90pou9 foreign key (id_user) references users (id);
alter table shopping_list_item add constraint FKhdyowo43j49t3p8srt8yc4jpo foreign key (price_snapshot_id) references price_snapshot (uuid);
alter table shopping_list_item add constraint FK1qvgc3uou31prkqiutunud9vt foreign key (id_list) references shopping_list (id);
alter table store add constraint FKoej0lypnppc7vhmeaii2p3m0u foreign key (id_postal_code) references postal_code (id);
alter table store add constraint FK9mw717a6cb2x95lalqn73k5wx foreign key (id_chain) references supermarket_chain (Id);
alter table subcategory add constraint FKk8ffo5tjobbow8vywcpyhrgyo foreign key (id_category) references category (Id);
alter table users add constraint FK4e1gesi97n3oqgygsxsqk4nt8 foreign key (id_postal_code) references postal_code (id);
