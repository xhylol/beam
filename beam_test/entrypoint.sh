#!/bin/sh
export CLASSPATH=$(hadoop classpath --glob)
/opt/apache/beam/boot $*
