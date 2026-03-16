# Kafka Stream Stateful Fraud Detection System

## Architecture Overview

This project demonstrates a real-time fraud detection system using Apache Kafka Streams with Spring Boot. The system processes financial transactions and identifies potential fraudulent activities through stateful stream processing.

### Technology Stack
- **Spring Boot 3.5.7** - Application framework
- **Apache Kafka Streams** - Stream processing engine
- **Java 21** - Runtime platform
- **Maven** - Build tool
- **Lombok** - Code generation
- **SpringDoc OpenAPI** - API documentation

### System Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│  Transaction    │    │   Kafka Cluster  │    │  Fraud Detection│
│   Controller    │───▶│   (transactions) │───▶│     Streams     │
│                 │    │                  │    │                 │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                                                        │
                                                        ▼
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   REST API     │    │  Output Topics   │    │  Windowed      │
│  /publish      │    │  - txn-fraud-alert│    │  Processing    │
│                 │    │  - user-txn-counts│    │                 │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

## Core Functionalities

### 1. Transaction Processing
- **Ingestion**: REST endpoint `/api/transactions/publish` processes transaction data
- **Serialization**: Custom serdes for Transaction objects
- **Data Model**: Transaction includes ID, user ID, amount, location, type, and items

### 2. Stream Processing Operations
- **Aggregation**: Groups transactions by type and calculates running totals
- **Windowing**: 10-second time windows to detect rapid transaction patterns
- **Filtering**: Identifies suspicious transaction patterns
- **Stateful Operations**: Maintains user transaction counts

### 3. Fraud Detection Logic
- **Frequency Analysis**: Detects users with >3 transactions in 10 seconds
- **Real-time Alerts**: Sends fraud warnings to `txn-fraud-alert` topic
- **Alert Storage**: Maintains in-memory list of recent fraud alerts (max 100)
- **REST API Access**: Provides HTTP endpoint to retrieve fraud alerts

### 4. Data Flow
1. Transactions published to `transactions` topic
2. Stream processors consume and analyze in real-time
3. Fraud alerts sent to `txn-fraud-alert` topic
4. Kafka consumer listens for alerts and stores in memory
5. REST API provides access to stored fraud alerts
6. State stores maintain aggregation data

## Kafka Integration

### Topics
- **Input**: `transactions` - Raw transaction data
- **Output**: `user-txn-counts` - Windowed transaction counts
- **Alerts**: `txn-fraud-alert` - Fraud detection notifications

### Stream Processing
- **Application ID**: `fraud-detection-streams`
- **Bootstrap Servers**: `localhost:9092`
- **State Directory**: Local storage for state stores
- **Serialization**: Custom TransactionSerde for value handling

### Key Features
- **Exactly-once semantics** for reliable processing
- **Stateful operations** with automatic state store management
- **Windowed aggregations** for time-based analysis
- **Fault tolerance** through Kafka's built-in mechanisms

## Build & Deployment

### Prerequisites
- Java 21+
- Docker Desktop installed and running
- Docker Compose available
- Maven 3.6+

### Build Commands
```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Package application
mvn clean package

# Skip tests during build
mvn clean package -DskipTests
```

### Docker Compose Deployment (Recommended)

#### Quick Start
```bash
# Navigate to docker directory
cd docker

# Start all services (Kafka + Zookeeper + Application)
docker-compose up -d

# View logs
docker-compose logs -f fraud-detection-app

# Stop services
docker-compose down
```

#### Services Included
- **Zookeeper**: Port 2181 - Kafka cluster coordination
- **Kafka**: Port 9092 - Message broker with auto-topic creation
- **Fraud Detection App**: Port 8080 - Spring Boot application

#### Access Points
- **REST API**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health
- **OpenAPI Docs**: http://localhost:8080/swagger-ui.html

#### Development Workflow
```bash
# Build and start
docker-compose up --build

# Rebuild application after changes
docker-compose up --build fraud-detection-app

# Clean up everything
docker-compose down -v --remove-orphans
```

### Local Development Setup

#### Manual Kafka Setup
```bash
# Start Kafka (if not running)
bin/kafka-server-start.sh config/server.properties

# Create topics
bin/kafka-topics.sh --create --topic transactions --bootstrap-server localhost:9092
bin/kafka-topics.sh --create --topic user-txn-counts --bootstrap-server localhost:9092

# Run Spring Boot application
mvn spring-boot:run

# Or run JAR directly
java -jar target/fraud-detection-system-0.0.1-SNAPSHOT.jar
```

#### Single Docker Image
```bash
# Build Docker image
mvn spring-boot:build-image

# Run with Docker (requires external Kafka)
docker run -p 8080:8080 fraud-detection-system:0.0.1-SNAPSHOT
```

## API Usage

### Publish Transactions
```bash
curl -X POST http://localhost:8080/api/transactions/publish
```

### Get Fraud Alerts
```bash
curl -X GET http://localhost:8080/api/transactions/fraud-alerts
```

**Response:**
```json
[
  "FRAUD ALERT: User U1 made 4 transactions within 10 seconds",
  "FRAUD ALERT: User U2 made 5 transactions within 10 seconds"
]
```

**Note:** Returns empty array `[]` when no fraud alerts are detected.

### Sample Transaction Data
The system includes sample transaction data in `src/main/resources/transactions.json` with various scenarios:
- Normal transactions (₹5,000 - ₹15,000)
- High-value transactions (₹25,000 - ₹100,000)
- Different locations (India, USA, UK, China)
- Multiple transaction types (debit/credit)

## Monitoring & Logs

### Key Metrics
- Transaction processing rates
- Fraud detection alerts
- User transaction patterns
- Aggregated totals by transaction type

### Log Examples
```
INFO  - CardType: debit | 💰 Running Total Amount: 5200.0
WARN  - 🚨 FRAUD ALERT: User=U1 made 5 transactions within 10 seconds!
INFO  - 🧾 User=U2 | Count=2 | Window=[2024-01-01T10:00:00 - 2024-01-01T10:00:10]
```

## Configuration

### Application Properties
Key configuration in `application.yml`:
- Kafka bootstrap servers
- Serialization settings
- Stream application ID
- State store directory

### Environment Variables
```bash
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
SPRING_KAFKA_STREAMS_APPLICATION_ID=fraud-detection-streams
```

## Development

### Project Structure
```
src/main/java/com/javatechie/
├── FraudDetectionSystemApplication.java  # Main application
├── config/
│   └── KafkaConfig.java                   # Kafka configuration
├── controller/
│   └── TransactionController.java         # REST endpoints
├── events/
│   ├── Transaction.java                   # Data model
│   └── Item.java                         # Item data model
├── streams/
│   ├── FraudDetectionStream.java         # Main stream processor
│   └── TransactionWindowStream.java       # Windowed processing
└── serdes/
    ├── TransactionSerde.java              # Combined serializer
    ├── TransactionSerializer.java         # JSON serializer
    └── TransactionDeserializer.java       # JSON deserializer
```

### Testing
- Unit tests for stream processing
- Integration tests with embedded Kafka
- Transaction serialization tests

### Testing Fraud Detection
```bash
# 1. Start the application
mvn spring-boot:run

# 2. Publish transactions multiple times quickly (within 10 seconds)
curl -X POST http://localhost:8080/api/transactions/publish
curl -X POST http://localhost:8080/api/transactions/publish
curl -X POST http://localhost:8080/api/transactions/publish
curl -X POST http://localhost:8080/api/transactions/publish

# 3. Check for fraud alerts
curl -X GET http://localhost:8080/api/transactions/fraud-alerts
```

### Troubleshooting
- **Empty fraud alerts**: Ensure consumer configuration is properly set in `application.yml`
- **No alerts generated**: Publish at least 4 transactions for the same user within 10 seconds
- **Kafka connection issues**: Verify Kafka is running on `localhost:9092`
- **Topic not found**: Check if `txn-fraud-alert` topic exists: `kafka-topics.bat --list --bootstrap-server localhost:9092`

## Production Considerations

### Scaling
- Horizontal scaling through partitioning
- Multiple instances for high availability
- Load balancing across consumer groups

### Security
- SSL/TLS encryption for Kafka communication
- Authentication and authorization
- Secure credential management

### Monitoring
- Kafka metrics monitoring
- Application health checks
- Performance tracking and alerting