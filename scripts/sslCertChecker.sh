#!/bin/bash

for ARGUMENT in "$@"
do
   KEY=$(echo $ARGUMENT | cut -f1 -d=)

   if [[ "$KEY" == "-h" ]]; then
		echo "pass \"path\" to store KTOR SSL files, pass domain to search in letsencrypt certs"
		echo "pass \"domain\" to search in letsencrypt certs"
		echo "pass \"name\" for ssl file to generate"
		echo "pass \"password\" for ssl file to generate"
		echo "I.e. \"./sslCertChecker.sh path='/data' domain='example.com' name='ktor' password='password'\""
		exit 0
   fi

   KEY_LENGTH=${#KEY}
   VALUE="${ARGUMENT:$KEY_LENGTH+1}"

   export "$KEY"="$VALUE"
done

if [ -z ${path+x} ]; then
	echo "path is unset";
else
	echo "path is set to '$path'";
fi

if [ -z ${domain+x} ]; then
	echo "domain is unset";
else
	echo "domain is set to '$domain'";
fi

if [ -z ${name+x} ]; then
	echo "name is unset";
else
	echo "name is set to '$name'";
fi

if [ -z ${password+x} ]; then
	echo "password is unset";
else
	echo "password is set to '$password'";
fi


file=$path/sslChangeTime.log
if [ -e "$file" ]; then
    echo "File exists"
else
    echo "File does not exist"
	touch $file
	#human-readable
	stat -c %z /etc/letsencrypt/live/$domain/ > $file
	#unix time
	stat -c %Z /etc/letsencrypt/live/$domain/ >> $file
fi

lastChangeTime=$(sed -n '2p' $file)
echo $lastChangeTime

certChangedTime="$(stat -c %Z /etc/letsencrypt/live/$domain/)"

if (( $certChangedTime > $lastChangeTime )); then
#for test! Fix me!
# if (( $certChangedTime == $lastChangeTime )); then
    echo "CERT WAS UPDATED, reload Tomcat9!"
	$path/sslCreator.sh
	systemctl restart tomcat9
else
	echo "CERT is OK, do not update."
fi

exit 0