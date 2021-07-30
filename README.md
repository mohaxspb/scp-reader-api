### Run configurations

To define profile add this to `arguments` in `gradle` run configuration:

>-Dspring.profiles.active=dev

### Deployment 

1. **Windows**
 
    To test on Tomcat on windows use `services.msc` from `cmd` to run/stop Tomcat service

2. **Linux**

    Deploy to `%Tomcat root%/webapps`

### Secret files

To build project you need: 

1. Google Api `clientId` and `clientSecret` in `application{OPTIONAL_PROFILE_HERE}.yml`.
2. Facebook Api `clientId` and `clientSecret`.
3. `V4__import_secret_data.sql` migration file in `/resources/migration/` with insertion of admin user with authorities and oauth clients with `password,refresh_token` and `client_credentials`.  
4. Key for validating purchases for GooglePlay. Add file `googlePlayAndroidDeveloperServiceKey.json` to `/resources/googlePlay/`
5. Firebase admin access keys for all langs. Add files `serviceAccountKey{LANG_CODE_HERE}.json` to `/resources/firebase/keys/`.

### Properties and securing data

To ignore changes in file execute: 

>git update-index --assume-unchanged src/test/resources/application.yml

To unignore it execute:

>git update-index --no-assume-unchanged src/test/resources/application.yml

### Adding categories

1. Add category to article_categories table.
2. Add category_to_lang to article_categories__to__langs table.
3. See migration №23 for example
4. Launch parse category from site: https://domain.zone/scp-reader/api/article/parse/LANG_ENUM/category/ARTICLE_CATEGORY_ID

### Troubleshooting

1. If you have duplicates in articles_langs table - execute smt like this: 

    ```postgresql
    with toDelete as (
        with duplicates as (
            with res as (
                select al.url_relative, count(*)
                from articles_langs al
                group by al.url_relative
                having count(*) > 1
            )
            select al.article_id, res.url_relative, count(*)
            from articles_langs al
                     join res on al.url_relative = res.url_relative
            group by al.article_id, res.url_relative
            having count(*) = 1
            order by url_relative
        )
        select distinct on (al.url_relative) al.id
        from articles_langs al
                 join duplicates on duplicates.url_relative = al.url_relative
    )
    delete
    from articles_langs
    where id in (select toDelete.id from toDelete);
    ```