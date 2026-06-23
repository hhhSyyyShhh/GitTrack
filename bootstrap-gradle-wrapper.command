#!/bin/zsh
set -euo pipefail
cd "$(dirname "$0")"
mkdir -p gradle/wrapper
URL="https://raw.githubusercontent.com/gradle/gradle/v8.9.0/gradle/wrapper/gradle-wrapper.jar"
echo "Downloading Gradle Wrapper 8.9..."
curl -fL "$URL" -o gradle/wrapper/gradle-wrapper.jar
chmod +x gradlew
echo "Done. You can now run: ./gradlew clean assembleDebug"
