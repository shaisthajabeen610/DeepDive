#!/bin/bash

if [[ -z "$JAVA_HOME" ]]
 then
   echo "JAVA_HOME must point to a valid JRE (You may want to set it permanently in setenv.sh)."
 else
   export JAVA=$JAVA_HOME/bin/java
   export CLASSPATH=../discotek.deepdive-1.5.11-beta.jar:../lib/discotek.deepdive-engine-1.5.11-beta.jar
fi
