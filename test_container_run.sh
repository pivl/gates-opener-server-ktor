#!/bin/bash

# Test script to run container with direct JAR execution
# This simulates what would happen in Home Assistant

echo "Testing Gates Opener Addon in Docker container..."

# Test 1: Direct JAR execution with parameters
echo "=== Test 1: Direct JAR execution ==="
timeout 5 docker run --rm -p 8081:8080 gates-opener-addon \
    java -jar /usr/bin/gates_opener.jar \
    --api-token "docker-test-api" \
    --initial-token "docker-test-initial" \
    --device-uuid "docker-test-uuid" \
    2>&1 | head -20

echo ""
echo "=== Test 2: Check JAR file exists in container ==="
docker run --rm gates-opener-addon ls -la /usr/bin/gates_opener.jar

echo ""
echo "=== Test 3: Check Java version in container ==="
docker run --rm gates-opener-addon java -version

echo ""
echo "=== Test 4: Test JAR help command ==="
docker run --rm gates-opener-addon java -jar /usr/bin/gates_opener.jar --help 2>&1 | head -10

echo ""
echo "Container tests completed!"
