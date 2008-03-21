@echo off
call setenv.cmd
call cab.cmd a -r -aLZH -l9 rtLZH9.cab %JAVA_HOME%\lib\rt.jar
call cab.cmd a -r -aLZH -l0 rtLZH0.cab %JAVA_HOME%\lib\rt.jar
call cab.cmd a -hD -r -aLZH -Ext -l9 rtLZH9crc.cab %JAVA_HOME%\lib\rt.jar
call cab.cmd a -hD -r -aLZH -Ext -l0 rtLZH0crc.cab %JAVA_HOME%\lib\rt.jar
call cab.cmd a -hD -r -aMSZIP rtMSZIP.cab %JAVA_HOME%\lib\rt.jar
