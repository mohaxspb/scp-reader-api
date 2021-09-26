### DB backup script

```shell script
#!/bin/bash

#create dirs
mkdir -p /data/dbBackups/scpReader/
mkdir -p /data/dbBackups/logs/

chown -R db-backup /data/dbBackups/scpReader
chown -R db-backup /data/dbBackups/logs
chmod -R u=rwx,go=r /data/dbBackups/scpReader
chmod -R u=rwx,go=r /data/dbBackups/logs

touch /data/dbBackups/logs/scpReader_db_backup.log

nowDate=$(date "+%Y-%m-%d-%H-%M-%S")
echo nowDate: $nowDate

gpBackupsDirName=/data/dbBackups/scpReader/
echo gpBackupsDirName: $gpBackupsDirName
backupDirName=$gpBackupsDirName$nowDate
echo backupDirName: $backupDirName

mkdir $backupDirName

pg_dump -U postgres -h localhost \
--file "$backupDirName"/scpReader.sql \
--format=plain scp_reader \
>> /data/dbBackups/logs/scpReader_db_backup.log 2>&1


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
0 0 * * * /data/dbBackups/db_backup.sh $>/data/dbBackups/logs/db_backup.log
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
0 0 * * * /data/dbBackups/dbBackupRotate.sh 60*60*24*7 scpReader_ /data/dbBackups/scpReader >> /data/dbBackups/logs/dbBackupRotate.log 2>&1
```

### backups storing on other server

1. Create user for fetching backups on some other server with `adduser USER_NAME`
2. Generate and upload ssh key (see https://www.digitalocean.com/community/tutorials/how-to-configure-ssh-key-based-authentication-on-a-linux-server)
    1. `ssh-keygen`  
    2. add `KEY_FILE_NAME.pub` content to prod server `home/DB_BACKUP_USER_NAME/.ssh/authorized_keys`
3. Create cron task with `crontab -e` to copy backup files from prod server to another one:

    **receiveBackup.sh**
    ```shell script
    echo Get list of files in other server dir modified not later than 2 days ago
    files=`ssh -i /home/db-backup-fetcher/id_rsa_db-backup-fetcher db-backup@scp-reader.com 'find /data/dbBackups/scpReader/ -type f -mtime -2'`
    
    echo Found files: $files
    
    for file in $files
    do
        echo Start downloading file: $file
        scp -v -r -i /home/db-backup-fetcher/id_rsa_db-backup-fetcher db-backup@scp-reader.com:$file \
        /data/dbBackups/scp-reader.com/files/ \
        >> /data/dbBackups/scp-reader.com/log.log
    done
    
    echo Downloading files complete!
    ``` 
   
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

   ```shell script
   #!/bin/bash
   
   address=$1
   correctResponse=$2
   
   echo "$(date '+%d/%m/%Y %H:%M:%S'): starting server check for $address with correct response: $correctResponse"
   
   # correctResponse='Welcome to ScpReader API!'
   # correctResponse='Welcome to API!'
   # address='https://scp-reader.com:8443/scp-reader/api/'
   # address='https://kuchanov.ru:8443/octa-trade/api/v1/'
   
   lockFile=/data/utils/restartTomcatLockFile.lock
   
   response=$(curl -s $address)
   echo "$(date '+%d/%m/%Y %H:%M:%S'): $response"
   if [[ $response == $correctResponse ]]; then
       echo "$(date '+%d/%m/%Y %H:%M:%S'): response is correct!"
       if [[ -e "$lockFile" ]]; then
           rm -f $lockFile
   # mail -s "SCP Reader server is working again!" admin@scp-reader.com <<EOF
   mail -s "SCP Reader server is working again!" mohax.spb@gmail.com,neva.spb.rx@gmail.com,admin@scp-reader.com <<EOF
   SCP Reader server is working again!
   
   Seems to be we correctly write a script to check if it is down and restart it in that case.
   
   Nice work! =)
   EOF
       fi
   else
       echo "$(date '+%d/%m/%Y %H:%M:%S'): server is down"
       if [[ -e "$lockFile" ]]; then
           echo "$(date '+%d/%m/%Y %H:%M:%S'): lockFile already exists, so we should think, that restarting is in progress and do nothing"
       else
           touch "$lockFile"
           restartTomcatOutput=$(systemctl restart -f tomcat9.service 2>&1)
           echo "$(date '+%d/%m/%Y %H:%M:%S'): restartTomcatOutput: $restartTomcatOutput"
   # mail -s "SCP Reader server is down." admin@scp-reader.com <<EOF
   mail -s "SCP Reader server is down." mohax.spb@gmail.com,neva.spb.rx@gmail.com,admin@scp-reader.com <<EOF
   SCP Reader server is down.
   Trying to restart it.
   
   Restart Tomcat comand output: "$restartTomcatOutput"
   EOF
       fi
   fi
   ```

### cron server restart script

   1. Create dirs `/data/utils/logs`.
   2. Create log file `/data/utils/logs/serverRestart.log`
   3. Copy script above to executable file `/data/utils/restartScript.sh`
   4. Add script below to cron vai `crontab -e` (`Ctrl+x` to save changes in NANO)

   ```shell script
   # use bash to run `*.sh` scripts.
   SHELL=/bin/bash
   
   * * * * * /data/utils/restartScript.sh https://scp-reader.com:8443/scp-reader/api/ 'Welcome to ScpReader API!' >> /data/utils/logs/serverRestart.log 2>&1
   ```