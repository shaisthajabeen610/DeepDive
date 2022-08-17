@echo off
call setenv.cmd
%JAVA% -Xmx4G -classpath %CLASSPATH% ca.discotek.deepdive.security.gui.DeepDiveGui