#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
mkdir -p build/classes build/test-classes dist
find src/main/java -name '*.java' > build/sources.txt
javac --release 17 -encoding UTF-8 -d build/classes @build/sources.txt
jar --create --file dist/Structura.jar --main-class edu.rungta.dsv.Main -C build/classes .
if [ "${1:-}" = "test" ]; then
  find src/test/java -name '*.java' ! -name 'ModelJUnitTest.java' > build/tests.txt
  javac --release 17 -encoding UTF-8 -cp build/classes -d build/test-classes @build/tests.txt
  java -Djava.awt.headless=true -cp build/classes:build/test-classes edu.rungta.dsv.Verification
fi
echo 'Run with: java -jar dist/Structura.jar'
