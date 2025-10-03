SHELL=/bin/bash

echo Get list of files in other server dir modified not later than 2 days ago
files=`ssh -i /home/db-backup-receiver/id_rsa_db-backup-receiver_scp-reader.com db-backup@scp-reader.com 'find /data/scp-reader.com/dbBackups/files/ -type f -mtime -2'`

echo Found files: $files

for file in $files
do
	echo Start downloading file: $file
	scp -v -r -i /home/db-backup-receiver/id_rsa_db-backup-receiver_scp-reader.com db-backup@scp-reader.com:$file \
	/data/dbBackups/scp-reader.com/files/ \
	>> /data/dbBackups/scp-reader.com/log.log
done

echo Downloading files complete!