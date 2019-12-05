alter table article_types
    add column if not exists enum_value TEXT not null default 'NONE';

insert into article_types (enum_value, image_url)
VALUES ('NEUTRAL_OR_NOT_ADDED', '/image/ic_not_add_big.png'),
       ('SAFE', '/image/safe_big.png'),
       ('EUCLID', '/image/euclid_big.png'),
       ('KETER', '/image/keter_big.png'),
       ('THAUMIEL', '/image/thaumiel_big.png'),
       ('NONE', '/image/none_big.png')
ON CONFLICT DO NOTHING;

-- insert all types translations for all langs.
with
    -- types
    NEUTRAL as (select * from article_types where article_types.enum_value = 'NEUTRAL_OR_NOT_ADDED'),
    SAFE as (select * from article_types where article_types.enum_value = 'SAFE'),
    EUCLID as (select * from article_types where article_types.enum_value = 'EUCLID'),
    KETER as (select * from article_types where article_types.enum_value = 'KETER'),
    THAUMIEL as (select * from article_types where article_types.enum_value = 'THAUMIEL'),
    NONE as (select * from article_types where article_types.enum_value = 'NONE'),
    -- langs
    ruLang as (select * from langs where lang_code = 'ru'),
    enLang as (select * from langs where lang_code = 'en'),
    plLang as (select * from langs where lang_code = 'pl'),
    deLang as (select * from langs where lang_code = 'de'),
    frLang as (select * from langs where lang_code = 'fr'),
    itLang as (select * from langs where lang_code = 'it'),
    esLang as (select * from langs where lang_code = 'es'),
    ptLang as (select * from langs where lang_code = 'pt'),
    cnLang as (select * from langs where lang_code = 'cn')
insert
into article_types_to_titles_for_lang (article_type_id, lang_id, title)
values
    -- ru
    ((select id from NEUTRAL), (select id from ruLang), 'Не назначен или нейтрализован'),
    ((select id from SAFE), (select id from ruLang), 'Безопасный'),
    ((select id from EUCLID), (select id from ruLang), 'Евклид'),
    ((select id from KETER), (select id from ruLang), 'Кетер'),
    ((select id from THAUMIEL), (select id from ruLang), 'Таумиэль'),
    ((select id from NONE), (select id from ruLang), ''),
    -- en
    ((select id from NEUTRAL), (select id from enLang), 'Neutralized'),
    ((select id from SAFE), (select id from enLang), 'Safe'),
    ((select id from EUCLID), (select id from enLang), 'Euclid'),
    ((select id from KETER), (select id from enLang), 'Keter'),
    ((select id from THAUMIEL), (select id from enLang), 'Thaumiel'),
    ((select id from NONE), (select id from enLang), ''),
    -- pl
    ((select id from NEUTRAL), (select id from plLang), 'Zneutralizowane'),
    ((select id from SAFE), (select id from plLang), 'Bezpieczne'),
    ((select id from EUCLID), (select id from plLang), 'Euclid'),
    ((select id from KETER), (select id from plLang), 'Keter'),
    ((select id from THAUMIEL), (select id from plLang), 'Thaumiel'),
    ((select id from NONE), (select id from plLang), ''),
    -- de
    ((select id from NEUTRAL), (select id from deLang), 'Neutralisiert'),
    ((select id from SAFE), (select id from deLang), 'Sicher'),
    ((select id from EUCLID), (select id from deLang), 'Euclid'),
    ((select id from KETER), (select id from deLang), 'Keter'),
    ((select id from THAUMIEL), (select id from deLang), 'Thaumiel'),
    ((select id from NONE), (select id from deLang), ''),
    -- fr
    ((select id from NEUTRAL), (select id from frLang), 'Neutralisé'),
    ((select id from SAFE), (select id from frLang), 'Sûr'),
    ((select id from EUCLID), (select id from frLang), 'Euclide'),
    ((select id from KETER), (select id from frLang), 'Keter'),
    ((select id from THAUMIEL), (select id from frLang), 'Thaumiel'),
    ((select id from NONE), (select id from frLang), ''),
    -- it
    ((select id from NEUTRAL), (select id from itLang), 'Nessuna'),
    ((select id from SAFE), (select id from itLang), 'Safe'),
    ((select id from EUCLID), (select id from itLang), 'Euclid'),
    ((select id from KETER), (select id from itLang), 'Keter'),
    ((select id from THAUMIEL), (select id from itLang), 'Thaumiel'),
    ((select id from NONE), (select id from itLang), ''),
    -- es
    ((select id from NEUTRAL), (select id from esLang), 'Neutralizado'),
    ((select id from SAFE), (select id from esLang), 'Seguro'),
    ((select id from EUCLID), (select id from esLang), 'Euclid'),
    ((select id from KETER), (select id from esLang), 'Keter'),
    ((select id from THAUMIEL), (select id from esLang), 'Taumiel'),
    ((select id from NONE), (select id from esLang), ''),
    -- pt
    ((select id from NEUTRAL), (select id from ptLang), 'Neutralizado'),
    ((select id from SAFE), (select id from ptLang), 'Seguro'),
    ((select id from EUCLID), (select id from ptLang), 'Euclídeo'),
    ((select id from KETER), (select id from ptLang), 'Keter'),
    ((select id from THAUMIEL), (select id from ptLang), 'Thaumiel'),
    ((select id from NONE), (select id from ptLang), ''),
    -- cn
    ((select id from NEUTRAL), (select id from cnLang), 'Neutralized'),
    ((select id from SAFE), (select id from cnLang), 'Safe'),
    ((select id from EUCLID), (select id from cnLang), 'Euclid'),
    ((select id from KETER), (select id from cnLang), 'Keter'),
    ((select id from THAUMIEL), (select id from cnLang), 'Thaumiel'),
    ((select id from NONE), (select id from cnLang), '')
on conflict DO NOTHING;