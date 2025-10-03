### DB backup script

```shell script
#!/bin/bash

#create dirs
mkdir -p /data/scp-reader.com/dbBackups/files/
mkdir -p /data/scp-reader.com/dbBackups/logs/

chown -R db-backup /data/scp-reader.com/dbBackups
chown -R db-backup /data/scp-reader.com/dbBackups/files
chown -R db-backup /data/scp-reader.com/dbBackups/logs
chmod -R u=rwx,go=r /data/scp-reader.com/dbBackups
chmod -R u=rwx,go=r /data/scp-reader.com/dbBackups/files
chmod -R u=rwx,go=r /data/scp-reader.com/dbBackups/logs

gpBackupsDirName=/data/scp-reader.com/dbBackups/files/
logsDirName=/data/scp-reader.com/dbBackups/logs/


touch $logsDirName/scpReader_db_backup.log

nowDate=$(date "+%Y-%m-%d-%H-%M-%S")
echo nowDate: $nowDate


echo gpBackupsDirName: $gpBackupsDirName
backupDirName=$gpBackupsDirName$nowDate
echo backupDirName: $backupDirName

mkdir $backupDirName

pg_dump -U postgres -h localhost \
--file "$backupDirName"/scpReader.sql \
--format=plain scp_reader \
>> $logsDirName/scpReader_db_backup.log 2>&1


#zip -9 -y -r -q ${backupDirFullName}.zip ${backupDirFullName}
backupArhiveFileName=scpReader_$nowDate.tar.gz
echo backupArhiveFileName: $backupArhiveFileName

cd $gpBackupsDirName

tar -cvzf $backupArhiveFileName $nowDate

rm -rf $backupDirName

chmod -R u=r,go=r $backupArhiveFileName

#test uncompressing
#tar -zxvf ${backupDirName}.tar.gz
```

### cron DB backup script 

```shell script
# use bash to run `*.sh` scripts.
SHELL=/bin/bash

#prod
0 0 * * * /data/scp-reader.com/dbBackups/db_backup.sh $>/data/scp-reader.com/dbBackups/logs/db_backup.log
```

### DB backup rotate script

```shell script
#!/bin/bash

# script iterates through files in dir witch starts with  `fileNamePrefix`
# and removes them if their date is older than `$nowDate-$rotateInterval`

# pass `rotateInterval` in seconds and `fileNamePrefix`, after witch
# comes date in format `Y-m-d-H-M-S`, i.e. `2018-12-02-12-00-01`

echo "$(date '+%Y-%m-%d %H:%M:%S') args: $1, $2, $3"

# cron while running script is in users home dir
pwd

pathToBackUpFiles=$3

# so we need to CD to dir with files
cd "$pathToBackUpFiles"

#don't give a fuck what it does
shopt -s nullglob
#put all files starts with $fileNamePrefix to $array
fileNamePrefix=$2
array=($fileNamePrefix*)
echo "array of files: ${array[@]}"

fileExtension=".tar.gz"

nowDate=$(date +%s)
echo $nowDate
rotateInterval=$1
echo $rotateInterval
# arrayToRemove=()

#loop through $array
for i in "${array[@]}"
do
   echo "$i"
   #removes $fileNamePrefix from $i
   dateString=${i#"$fileNamePrefix"}
   # removes $fileExtension from $i
   dateString=${dateString%"$fileExtension"}
   # echo $dateString

   # magically splits string as `"1-1-1-1"` to array, contains `1,1,1,1`
   arr=(${dateString//-/ })
   # echo ${arr[@]}
   formattedDateString="${arr[0]}/${arr[1]}/${arr[2]} ${arr[3]}:${arr[4]}:${arr[5]}"

   epoch=$(date -d "$formattedDateString" "+%s")
   echo $epoch
   epoch_to_date=$(date -d @$epoch "+%Y-%m-%d %H:%M:%S %z")
   echo $epoch_to_date

   #check files and delete if they are older $rotateInterval
   if(($nowDate-$epoch>$rotateInterval)); then
      echo "remove file: $i"

      echo "seconds: " $(($nowDate-$epoch))
      echo "minutes: "$((($nowDate-$epoch)/60))
      # arrayToRemove+=("$i")
      rm -f "$i"
   fi
done

# for i in "${arrayToRemove[@]}"
# do
#   echo "remove file: $i"
#   rm -f $i
# done
```

### cron DB backup rotate script 

```shell script
# use bash to run `*.sh` scripts.
SHELL=/bin/bash

#backups rotate task
0 0 * * * /data/scp-reader.com/dbBackups/dbBackupRotate.sh 60*60*24*7 scpReader_ /data/scp-reader.com/dbBackups/files >> /data/scp-reader.com/dbBackups/logs/dbBackupRotate.log 2>&1
```

### backups storing on other server

1. Create user for fetching backups on some other server with `adduser USER_NAME`
2. Generate and upload ssh key (see https://www.digitalocean.com/community/tutorials/how-to-configure-ssh-key-based-authentication-on-a-linux-server)
    1. `ssh-keygen`  
    2. add `KEY_FILE_NAME.pub` content to prod server `home/DB_BACKUP_USER_NAME/.ssh/authorized_keys`
3. Create cron task with `crontab -e` to copy backup files from prod server to another one:

    See [receiveBackup.sh](scripts/receiveBackup.sh)
   
   ```shell script
   # use bash to run `*.sh` scripts.
   SHELL=/bin/bash
   
   30 0 * * * /data/dbBackups/scp-reader.com/receiveBackup.sh
    ```

4. And do not forget to rotate this files too!

    ```shell script
   # use bash to run `*.sh` scripts.
   SHELL=/bin/bash
   
   50 0 * * * /data/dbBackups/scp-reader.com/dbBackupRotate.sh 60*60*24*7 scpReader_ /data/dbBackups/scp-reader.com/files/ >> /data/dbBackups/scp-reader.com/log.log
    ```

### Server restart script

see [restartScript.sh](scripts/restartScript.sh)

### cron server restart script

   1. Create dirs `/data/scp-reader.com/utils/log`.
   2. Copy [restartScript.sh](scripts/restartScript.sh) to executable file `/data/scp-reader.com/utils/restartScript.sh`
   3. Add script below to cron vai `crontab -e` (`Ctrl+x` to save changes in NANO)

   ```shell script
   # use bash to run `*.sh` scripts.
   SHELL=/bin/bash
   
   * * * * * /data/utils/restartScript.sh https://scp-reader.com:8443/scp-reader/api/ 'Welcome to ScpReader API!' >> /data/scp-reader.com/utils/log/serverRestart.log 2>&1
   ```