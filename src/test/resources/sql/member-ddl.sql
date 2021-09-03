create table member
(
    id                bigint not null,
    age_range         varchar(255),
    email             varchar(255),
    nickname          varchar(255),
    profile_image_url varchar(255),
    primary key (id)
);