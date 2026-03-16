# Docker Compose for Kafka Streams Fraud Detection System

This Docker Compose setup provides a complete environment for running the Kafka Streams fraud detection system.

## Services

### 1. Zookeeper
- **Image**: confluentinc/cp-zookeeper:7.4.0
- **Port**: 2181
- **Purpose**: Kafka cluster coordination

### 2. Kafka
- **Image**: confluentinc/cp-kafka:7.4.0
- **Ports**: 9092 (external), 29092 (internal), 9101 (JMX)
- **Purpose**: Message broker and stream processing
- **Features**: Auto-topic creation enabled

### 3. Fraud Detection App
- **Build**: Built from source using Dockerfile
- **Port**: 8080
- **Purpose**: Spring Boot application with Kafka Streams
- **Health Check**: Built-in health monitoring

## Quick Start

### Prerequisites
- Docker Desktop installed and running
- Docker Compose available

### Running the System

1. **Start all services:**
```bash
docker-compose up -d
```

2. **View logs:**
```bash
# View all logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f fraud-detection-app
docker-compose logs -f kafka
```

3. **Stop services:**
```bash
docker-compose down
```

### Development Workflow

1. **Build and start:**
```bash
docker-compose up --build
```

2. **Rebuild application after changes:**
```bash
docker-compose up --build fraud-detection-app
```

3. **Clean up everything:**
```bash
docker-compose down -v --remove-orphans
```

## Accessing Services

### Application Endpoints
- **REST API**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health
- **OpenAPI Docs**: http://localhost:8080/swagger-ui.html
- **Metrics**: http://localhost:8080/actuator/metrics

### Kafka
- **Bootstrap Server**: localhost:9092
- **JMX**: localhost:9101

## Testing the System

### 1. Publish a Transaction
```bash
curl -X POST http://localhost:8080/api/transactions/publish \
  -H "Content-Type: application/json" \
  -d '{
    "transactionId": "txn_001",
    "userId": "user_001",
    "amount": 15000.00,
    "location": "Mumbai",
    "cardType": "debit",
    "items": [
      {"name": "Product A", "price": 8000.00, "quantity": 1},
      {"name": "Product B", "price": 7000.00, "quantity": 1}
    ]
  }'
```

### 2. Monitor Kafka Topics
```bash
# List topics
docker exec -it fraud-detection-kafka kafka-topics --bootstrap-server localhost:9092 --list

# Consume from transactions topic
docker exec -it fraud-detection-kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic transactions --from-beginning

# Consume fraud alerts
docker exec -it fraud-detection-kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic txn-fraud-alert --from-beginning
```

## Configuration

### Environment Variables
You can override configuration using environment variables:

```bash
# Custom Kafka bootstrap servers
SPRING_KAFKA_BOOTSTRAP_SERVERS=my-kafka:9092

# Custom application ID
SPRING_KAFKA_STREAMS_APPLICATION_ID=my-fraud-detector
```

### Profile Configuration
The application uses `application-docker.yml` profile when running in Docker with `SPRING_PROFILES_ACTIVE=docker`.

## Troubleshooting

### Common Issues

1. **Port conflicts**: Ensure ports 8080, 9092, 2181 are available
2. **Memory issues**: Increase Docker memory allocation to at least 4GB
3. **Network issues**: Check Docker network configuration

### Debug Commands

```bash
# Check container status
docker-compose ps

# Check container logs
docker-compose logs fraud-detection-app

# Execute commands in container
docker exec -it fraud-detection-app sh
docker exec -it fraud-detection-kafka bash

# Check Kafka topics
docker exec -it fraud-detection-kafka kafka-topics --bootstrap-server localhost:9092 --list
```

### Performance Monitoring

Monitor application performance using:
- **JMX Metrics**: Connect to localhost:9101
- **Spring Boot Actuator**: http://localhost:8080/actuator/metrics
- **Application Logs**: `docker-compose logs -f fraud-detection-app`

## Production Considerations

For production deployment:
1. Use external Zookeeper and Kafka clusters
2. Configure proper resource limits
3. Set up monitoring and alerting
4. Configure security (SSL/TLS, authentication)
5. Set up proper backup and recovery procedures

## Development Tips

1. **Hot reloading**: Mount source code volume for development
2. **Debugging**: Use `docker-compose exec` to debug inside containers
3. **Testing**: Use the same setup for integration tests
4. **Scaling**: Use `docker-compose up --scale fraud-detection-app=3` for multiple instances
