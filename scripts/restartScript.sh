#!/bin/bash

address=$1
correctResponse=$2

echo "$(date '+%d/%m/%Y %H:%M:%S'): starting server check for $address with correct response: $correctResponse"

# address='https://scp-reader.com:8443/scp-reader/api/'
# correctResponse='Welcome to ScpReader API!'

scriptDir=/data/scp-reader.com/utils
logDir=$scriptDir/log
logFile=serverRestart.log
mkdir $scriptDir
mkdir $logDir
touch $logDir/$logFile

lockFileName=restartTomcatLockFile.lock

lockFile=$scriptDir/$lockFileName

response=$(curl -s "$address")
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

Restart Tomcat command output: "$restartTomcatOutput"
EOF
	fi
fi