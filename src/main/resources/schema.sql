CREATE TYPE dish_type AS ENUM ('START', 'MAIN', 'DESSERT');
CREATE TYPE category AS ENUM ('VEGETABLE', 'ANIMAL', 'MARINE', 'DAIRY', 'OTHER');

CREATE TABLE dish (
    id int primary key,
    name varchar(255) not null,
    dish_type dish_type not null
);

CREATE TABLE ingredient(
    id int primary key ,
    name varchar(255) not null ,
    price numeric(10,2) not null ,
    category category not null,
    id_dish int,
    constraint dish_fk foreign key (id_dish) references dish(id)
)