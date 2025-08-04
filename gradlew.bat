@echo off
rem ------------------------------------------------------------------------------
rem Minimal Gradle wrapper script for Windows.
rem
rem This script delegates to the Gradle Wrapper JAR located in the
rem `gradle/wrapper` directory.  It assumes `java.exe` is available on your
rem PATH or via the JAVA_HOME environment variable.
rem ------------------------------------------------------------------------------

set DIR=%~dp0

if defined JAVA_HOME (
  set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
) else (
  set "JAVA_CMD=java.exe"
)

"%JAVA_CMD%" -classpath "%DIR%gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*