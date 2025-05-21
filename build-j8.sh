#!/bin/bash

# Export JAVA_HOME to use JDK 8
export JAVA_HOME="/Library/Java/JavaVirtualMachines/openlogic-openjdk-8.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"

# Show Java version being used
echo "Using Java:"
java -version

# Clean Gradle daemon
./gradlew --stop

# Execute Gradle with JVM args for Java 8
./gradlew clean --no-daemon -Pkapt.use.jdk.8=true -Dkapt.verbose=true

echo "Clean completed. Now building..."

# Build with Java 8
./gradlew build --no-daemon -Pkapt.use.jdk.8=true -Dkapt.verbose=true

echo "Build complete!" 