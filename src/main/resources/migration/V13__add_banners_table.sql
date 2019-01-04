CREATE TABLE IF NOT EXISTS banners (
     id                            bigserial not null,
     image_url                     character varying(255),
     logo_url                      character varying(255),
     title                         character varying(255) not null,
     sub_title                     character varying(255) not null,
     cta_button_text               character varying(255) not null,
     redirect_url                  character varying(255) not null,
     enabled                       boolean,
     author_id                     bigint,
     created                       timestamp,
     updated                       timestamp,
     primary key (id)
);