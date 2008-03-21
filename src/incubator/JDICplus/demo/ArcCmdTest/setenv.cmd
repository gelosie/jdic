@echo off
if "%JAVA_HOME%1"=="1" set JAVA_HOME=D:\java1.6_10\build\j2re-image
set JAVA_DBG=-J-verbose -J-Xdebug -J-Xnoagent -J-Djava.compiler=NONE -J-Xrunjdwp:transport=dt_shmem,server=y,suspend=y,address=javadebug
set CAB="%JAVA_HOME%/bin/JAVA.exe" -cp .\..\..\dist\JDICPlus.jar org.jdic.arc.Executor


