#!/usr/bin/env sh

APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`

# Resolve links: $0 may be a link
APP_PATH="$0"
while [ -h "$APP_PATH" ]; do
    ls=`ls -ld "$APP_PATH"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
        APP_PATH="$link"
    else
        APP_PATH=`dirname "$APP_PATH"`"/$link"
    fi
done
APP_HOME=`dirname "$APP_PATH"`

CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$CLASSPATH" ]; then
    echo "gradle-wrapper.jar fehlt. Bitte einmal Gradle Wrapper generieren oder Android Studio syncen." 1>&2
    exit 1
fi

exec java -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
