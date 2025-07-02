#!/bin/bash

# Fail fast on any error
set -e

# Build common module first
echo "Building common module..."
cd common
mvn clean install -DskipTests
cd ..

# List of microservices to build
services=("order-service" "inventory-service" "notification-service")

# Build each service
for service in "${services[@]}"; do
  echo "Building $service..."
  cd "$service"
  mvn clean package -DskipTests
  cd ..
done

# Start Docker Compose
echo "Starting services with docker-compose..."
docker-compose up --build