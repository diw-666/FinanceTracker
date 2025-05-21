#!/bin/bash

# Stop Gradle daemon
./gradlew --stop

# Set Java module access flags directly - this affects all Java processes
export JAVA_TOOL_OPTIONS="--add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED \
--add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED \
--add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED \
--add-opens=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED \
--add-opens=jdk.compiler/com.sun.tools.javac.jvm=ALL-UNNAMED \
--add-opens=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED \
--add-opens=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED \
--add-opens=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED \
--add-opens=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED \
--add-opens=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED \
--add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED"

# Print information
echo "Using Java with module access flags set"
java -version

# Run clean and build with no daemon
./gradlew clean build --no-daemon

echo "Build completed" 