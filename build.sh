#!/bin/bash

# Export JAVA_HOME to use JDK 8
export JAVA_HOME="/Library/Java/JavaVirtualMachines/openlogic-openjdk-8.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"

# Show Java version being used
echo "Using Java:"
java -version

# Clean Gradle daemon to ensure fresh start
./gradlew --stop

# Execute Gradle with the clean build commands and JVM arguments
GRADLE_OPTS="--add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED \
--add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED \
--add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED \
--add-opens=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED \
--add-opens=jdk.compiler/com.sun.tools.javac.jvm=ALL-UNNAMED \
--add-opens=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED \
--add-opens=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED \
--add-opens=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED \
--add-opens=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED \
--add-opens=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED \
--add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED" \
./gradlew clean build -Dorg.gradle.jvmargs="-Xmx2048m $GRADLE_OPTS"

echo "Build complete!" 