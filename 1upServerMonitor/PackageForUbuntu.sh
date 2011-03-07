#!/bin/bash
FLEXSDKBIN="/home/john/FlexSDK/bin"
AIRSDKBIN="/home/john/AirSDK/bin"
CERTIFICATE="1upServerMonitor.pfx"
SIGNING_OPTIONS="-storetype pkcs12 -keystore $CERTIFICATE -tsa none"
AIR_FILE="air/1upServerMonitor.air"
APP_XML="application.xml"
FILE_OR_DIR="-C bin ."
PROJPATH="/home/john/Development/1upmodrcon/1upServerMonitor"

# Compile
if [ "$1" == "compile" ]
then
$FLEXSDKBIN/mxmlc -load-config+=obj/linux1upServerMonitorConfig.xml -debug=true -static-link-runtime-shared-libraries=true +configname=air -o $PROJPATH/bin/1upServerMonitor.swf
fi

# Package Air File
if [ "$1" == "air" ]
then
$FLEXSDKBIN/adt -package $SIGNING_OPTIONS $AIR_FILE $APP_XML $FILE_OR_DIR
fi

# Package for Debian
if [ "$1" == "package" ]
then
$AIRSDKBIN/adt -package -target native natives/1upServerMonitor.deb air/1upServerMonitor.air
fi

# Debug
if [ "$1" == "debug" ]
then
$AIRSDKBIN/adl application.xml bin
fi

echo "Finished..."