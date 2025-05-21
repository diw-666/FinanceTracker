#!/bin/bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home
./gradlew clean build --info -Dkotlin.daemon.jvm.options="--illegal-access=permit"
