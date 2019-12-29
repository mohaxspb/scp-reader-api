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
