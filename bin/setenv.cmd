@echo off
IF "%JAVA_HOME%"=="" GOTO JAVA_HOME_ERROR
SET JAVA="%JAVA_HOME%/bin/java"
SET CLASSPATH=../discotek.deepdive-1.5.11-beta.jar;../lib/discotek.deepdive-engine-1.5.11-beta.jar
GOTO END

:JAVA_HOME_ERROR
ECHO JAVA_HOME must point to a valid JRE (You may want to set it permanently in setenv.cmd).
GOTO END

:END