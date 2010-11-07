@echo off

set MWSMS_HOME=%~dp0..

set CLASSPATH=%MWSMS_HOME%\lib
set CLASSPATH=%CLASSPATH%;%MWSMS_HOME%\lib\commons-cli-1.1.jar
set CLASSPATH=%CLASSPATH%;%MWSMS_HOME%\lib\commons-codec-1.2.jar
set CLASSPATH=%CLASSPATH%;%MWSMS_HOME%\lib\commons-httpclient-3.1.jar
set CLASSPATH=%CLASSPATH%;%MWSMS_HOME%\lib\commons-logging-1.0.4.jar
set CLASSPATH=%CLASSPATH%;%MWSMS_HOME%\lib\${project.build.finalName}.jar

java -classpath %CLASSPATH% -Djava.util.logging.config.file=%MWSMS_HOME%\lib\logging.properties com._17od.meteorwebsms.client.MeteorSMSClient %*
