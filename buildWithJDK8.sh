#!/bin/bash
# Script to run build with JDK 8
export JAVA_HOME=$(/usr/libexec/java_home -v 1.8)
echo "Using JAVA_HOME: $JAVA_HOME"
./gradlew clean build --no-daemon 