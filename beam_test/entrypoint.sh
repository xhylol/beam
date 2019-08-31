#!/bin/sh
export CLASSPATH=$(hadoop classpath --glob)
cat /ml/hosts >> /etc/hosts
/opt/apache/beam/boot $*
