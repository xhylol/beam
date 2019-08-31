#!/bin/bash
basepath=$(cd `dirname $0`;pwd)
cd $basepath
./gradlew :runners:spark:job-server:runShadow -PsparkMasterUrl=yarn-client -PjobHost=172.17.117.203 < /dev/null >/dev/null 2>&1 &
