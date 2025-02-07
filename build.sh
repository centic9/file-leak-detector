#!/bin/bash

. setJava17.sh

if [ "${OSTYPE}" == "cygwin" ];then
  MVN=/cygdrive/c/workspaces/devtools/apache-maven-3.8.6/bin/mvn
else
  MVN=/opt/apache/maven/apache-maven-3.9.7/bin/mvn
fi

if [ $# -eq 0 ];then
  "${MVN}" package validate verify
else
  "${MVN}" "$@"
fi
