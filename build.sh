#!/bin/bash

if [ "${OSTYPE}" == "cygwin" ];then
  MVN=/cygdrive/c/workspaces/devtools/apache-maven-3.8.6/bin/mvn
else
  MVN=/opt/apache/maven/apache-maven-3.9.0/bin/mvn
fi

if [ $# -eq 0 ];then
  "${MVN}" package validate verify
else
  "${MVN}" "$@"
fi
