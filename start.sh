#!/bin/bash
# Define the log file in the current working directory
LOGFILE="$(pwd)/start.log"
# Ensure the log file is created or cleared if it exists
> $LOGFILE
# Execute the prerequisite script and log any errors

if [ ! -f /home/adyogi/Projects/setupscripts/export_env_vars.sh ]; then
  echo "Error: export_env_vars.sh does not exist."
  exit 1
fi
chmod +x /home/adyogi/Projects/setupscripts/export_env_vars.sh
source /home/adyogi/Projects/setupscripts/export_env_vars.sh
#printenv

# Set JAVA_HOME
export JAVA_HOME=/usr/lib/jvm/java-11-amazon-corretto/
# Run mvn clean install and log any errors
#echo "Running mvn clean install..."
#if ! mvn clean install >> $LOGFILE 2>&1; then
#    echo "Error during mvn clean install. Check the log file at $LOGFILE."
#    exit 1
#fi
echo "Starting the application with nohup..."
nohup /usr/lib/jvm/java-11-amazon-corretto/bin/java -Xmx3g -Dserver.port=9000 -jar target/notification-0.0.1-SNAPSHOT.jar  >> $LOGFILE 2>&1 &
PID=$!
sleep 5
# Check if the process started successfully
if [ -z "$PID" ]; then
    echo "Failed to start the application"
    exit 1
else
    echo "Process running with PID: $PID"
fi
