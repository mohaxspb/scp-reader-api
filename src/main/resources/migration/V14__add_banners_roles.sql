INSERT INTO authorities(
    user_id,
    authority
) VALUES(
    (select id from users where full_name like '%Андрей Дуксин%'),
    'BANNER'
) ON CONFLICT DO NOTHING;

INSERT INTO authorities(
    user_id,
    authority
) VALUES(
    (select id from users where username='test@test.ru'),
    'BANNER'
) ON CONFLICT DO NOTHING;

INSERT INTO authorities(
    user_id,
    authority
) VALUES(
    (select id from users where username='mohax.spb@gmail.com'),
    'BANNER'
) ON CONFLICT DO NOTHING;