#!/bin/bash

source setenv.sh

if ! [[ -z "$JAVA_HOME" ]]
 then
  COMMAND="$JAVA -Xmx4G -classpath $CLASSPATH ca.discotek.deepdive.security.gui.DeepDiveGui"

  echo COMMAND: $COMMAND
  $COMMAND
fi

