#!/bin/bash

set -e

echo "Building Spring Boot PlantUML Server Docker image..."

# Build the Docker image
docker build -t springboot-plantuml-server:latest .

echo "Build completed successfully!"

echo "Starting the container..."
echo "The application will be available at: http://localhost:8080/plantuml"
echo "Admin credentials: admin/admin123"
echo ""
echo "To stop the container, run: docker stop plantuml-server"
echo "To view logs, run: docker logs -f plantuml-server"
echo ""

# Run the container
docker run -d \
  --name plantuml-server \
  -p 8080:8080 \
  -e ADMIN_PASSWORD=admin123 \
  -e JAVA_OPTS="-Xmx512m -Xms256m" \
  --restart unless-stopped \
  springboot-plantuml-server:latest

echo "Container started successfully!"
echo "Container ID: $(docker ps -q --filter name=plantuml-server)" 