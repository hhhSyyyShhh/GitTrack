#!/bin/sh
# Lightweight Gradle wrapper launcher. Android Studio can also sync this project directly.
APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar
if [ ! -f "$CLASSPATH" ]; then
  echo "gradle-wrapper.jar is missing. Open the project in Android Studio, or run: gradle wrapper --gradle-version 8.9"
  exit 1
fi
exec java -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
