#!/bin/sh

# Gradle wrapper script
# Adapted for use with gradle/actions/setup-gradle

if [ -n "$JAVA_HOME" ]; then
    JAVA="$JAVA_HOME/bin/java"
else
    JAVA="java"
fi

# Use the Gradle from the setup-gradle action if available
if command -v gradle > /dev/null 2>&1; then
    exec gradle "$@"
fi

# Otherwise look for wrapper jar
DIR="$( cd "$( dirname "$0" )" && pwd )"
if [ -f "$DIR/gradle/wrapper/gradle-wrapper.jar" ]; then
    exec "$JAVA" -jar "$DIR/gradle/wrapper/gradle-wrapper.jar" "$@"
fi

echo "Gradle wrapper not found. Using system gradle..."
exec gradle "$@"
