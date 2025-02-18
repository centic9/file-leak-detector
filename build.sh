#!/bin/sh

if [ "${OSTYPE}" == "msys" ];then
  echo "Run this in a cygwin terminal, not MSYS/Mingw"
  exit 1
fi

. setJava17.sh
echo

if [ "${OSTYPE}" == "cygwin" ];then
  MVN=/cygdrive/c/workspaces/devtools/apache-maven-3.9.6/bin/mvn
else
  MVN=/opt/apache/maven/apache-maven-3.9.7/bin/mvn
fi

if [ $# -eq 0 ];then
  "${MVN}" package validate verify
else
  "${MVN}" "$@"
fi
