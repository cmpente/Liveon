#!/usr/bin/env sh
# ------------------------------------------------------------------------------
# Minimal Gradle wrapper script for POSIX systems (sh, bash, zsh, etc.).
#
# This script delegates to the Gradle Wrapper JAR located in the
# `gradle/wrapper` directory.  It assumes `java` is available on your PATH
# or via the JAVA_HOME environment variable.  For full functionality (such
# as support for unusual shells and environment variables) use the default
# wrapper provided by the Gradle distribution.
# ------------------------------------------------------------------------------

DIR="$(cd "$(dirname "$0")" && pwd)"

if [ -n "$JAVA_HOME" ]; then
  JAVA_CMD="$JAVA_HOME/bin/java"
else
  JAVA_CMD="java"
fi

exec "$JAVA_CMD" -classpath "$DIR/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"