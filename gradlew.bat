@echo off
set APP_NAME=Gradle
set APP_BASE_NAME=%~n0
set APP_HOME=%~dp0

set CLASSPATH=%APP_HOME%gradle\wrapper\gradle-wrapper.jar

if not exist "%CLASSPATH%" (
  echo gradle-wrapper.jar fehlt. Bitte einmal Gradle Wrapper generieren oder Android Studio syncen.
  exit /b 1
)

"%JAVA_HOME%\bin\java" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
