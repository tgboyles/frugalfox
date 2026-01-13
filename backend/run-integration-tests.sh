#!/bin/bash

# Integration Tests Verification Script
# This script runs the REST Assured integration tests

set -e  # Exit on error

echo "====================================================="
echo " Frugal Fox Backend - Integration Tests"
echo "====================================================="
echo ""

# Check if we're in the backend directory
if [ ! -f "pom.xml" ]; then
    echo "Error: This script must be run from the backend directory"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
echo "Detected Java version: $JAVA_VERSION"

if [ "$JAVA_VERSION" -lt 23 ]; then
    echo ""
    echo "Warning: Java 23 is required, but Java $JAVA_VERSION is installed."
    echo ""
    echo "Options:"
    echo "1. Install Java 23: https://adoptium.net/"
    echo "2. Use Docker to run tests:"
    echo "   docker run --rm -v \"\$(pwd)\":/app -w /app maven:3.9-eclipse-temurin-23 mvn verify"
    echo ""
    read -p "Do you want to continue anyway? (tests may fail to compile) [y/N]: " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

echo ""
echo "Running integration tests..."
echo "---------------------------------------------------"
echo ""

# Run the tests
mvn verify

EXIT_CODE=$?

echo ""
echo "---------------------------------------------------"
if [ $EXIT_CODE -eq 0 ]; then
    echo "✅ All integration tests passed!"
else
    echo "❌ Integration tests failed with exit code: $EXIT_CODE"
    echo ""
    echo "Troubleshooting tips:"
    echo "- Check that Java 23 is installed: java -version"
    echo "- Review the test output above for specific failures"
    echo "- See integration tests README: src/test/java/com/tgboyles/frugalfox/integration/README.md"
fi

exit $EXIT_CODE
