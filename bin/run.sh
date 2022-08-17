#!/bin/bash

source setenv.sh

if ! [[ -z "$JAVA_HOME" ]]
 then
  COMMAND="$JAVA -Xmx4G -jar ../lib/discotek.deepdive-engine-1.5.11-beta.jar -decompile=true -project-directory=../sample-config -output-directory=/temp/report ../discotek.deepdive-1.5.11-beta.jar

  $COMMAND
fi

