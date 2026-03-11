# State Stores and Serialization

## Overview

This document covers state store management and serialization (Serdes) used in the Kafka Streams fraud detection system.

## State Stores

### Types of State Stores

#### 1. KeyValueStore
Used for simple key-value pairs:
```java
Materialized.as("user-txn-count-store")
```

#### 2. WindowStore  
Used for time-windowed data:
```java
Materialized.as("user-txn-count-window-store")
```

### State Store Configuration

#### Basic Configuration
```java
// Counter store
Materialized.<String, Long>as("user-txn-count-store")
    .withKeySerde(Serdes.String())
    .withValueSerde(Serdes.Long())

// Aggregation store
Materialized.<String, Double>as("transaction-total-store")
    .withKeySerde(Serdes.String())
    .withValueSerde(Serdes.Double())
```

#### Advanced Configuration
```java
Materialized.<String, Long>as("user-txn-count-store")
    .withKeySerde(Serdes.String())
    .withValueSerde(Serdes.Long())
    .withRetention(Duration.ofHours(24))
    .withCachingEnabled()
    .withLoggingEnabled()
```

### State Store Operations

#### Querying State Stores
```java
// Get current state store
ReadOnlyKeyValueStore<String, Long> store = 
    kafkaStreams.store("user-txn-count-store", 
        QueryableStoreTypes.keyValueStore());

// Query by key
Long count = store.get("user123");

// Range query
KeyValueIterator<String, Long> iterator = store.range("user100", "user200");
```

#### Window Store Queries
```java
ReadOnlyWindowStore<String, Long> windowStore = 
    kafkaStreams.store("user-txn-count-window-store",
        QueryableStoreTypes.windowStore());

// Query time window
KeyValueIterator<Windowed<String>, Long> iterator = 
    windowStore.fetch("user123", 
        Instant.now().minus(Duration.ofMinutes(10)),
        Instant.now());
```

## Serialization (Serdes)

### Custom Transaction Serde

#### TransactionSerializer
```java
public class TransactionSerializer implements Serializer<Transaction> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(String topic, Transaction data) {
        try {
            return objectMapper.writeValueAsBytes(data);
        } catch (JsonProcessingException e) {
            throw new SerializationException("Error serializing Transaction", e);
        }
    }
}
```

#### TransactionDeserializer
```java
public class TransactionDeserializer implements Deserializer<Transaction> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Transaction deserialize(String topic, byte[] data) {
        try {
            return objectMapper.readValue(data, Transaction.class);
        } catch (IOException e) {
            throw new SerializationException("Error deserializing Transaction", e);
        }
    }
}
```

#### Combined TransactionSerde
```java
public class TransactionSerde implements Serde<Transaction> {
    private final TransactionSerializer serializer = new TransactionSerializer();
    private final TransactionDeserializer deserializer = new TransactionDeserializer();

    @Override
    public Serializer<Transaction> serializer() {
        return serializer;
    }

    @Override
    public Deserializer<Transaction> deserializer() {
        return deserializer;
    }
}
```

### Built-in Serdes

#### Common Serdes
```java
// String
Serdes.String()

// Long  
Serdes.Long()

// Double
Serdes.Double()

// Integer
Serdes.Integer()

// ByteArray
Serdes.ByteArray()
```

#### Windowed Serdes
```java
// Time windowed serde
WindowedSerdes.timeWindowedSerdeFrom(String.class, 10000L)

// Session windowed serde
WindowedSerdes.sessionWindowedSerdeFrom(String.class)
```

### Serde Configuration

#### Application Configuration
```yaml
spring:
  kafka:
    streams:
      properties:
        default.key.serde: org.apache.kafka.common.serialization.Serdes$StringSerde
        default.value.serde: com.javatechie.serdes.TransactionSerde
```

#### Stream-specific Configuration
```java
// Consumed with custom serdes
KStream<String, Transaction> stream = builder.stream(
    "transactions", 
    Consumed.with(Serdes.String(), new TransactionSerde())
);

// Produced with custom serdes
stream.to("output-topic", 
    Produced.with(Serdes.String(), new TransactionSerde())
);
```

## Performance Optimization

### Serde Performance

#### JSON vs Binary
- **JSON**: Human-readable, slower, larger
- **Avro/Protobuf**: Binary, faster, schema evolution
- **Custom**: Optimized for specific use cases

#### Optimization Tips
```java
// Reuse ObjectMapper instances
private static final ObjectMapper MAPPER = new ObjectMapper();

// Use efficient data types
public record Transaction(
    String transactionId,  // String is fine for IDs
    long amount,          // Use long instead of double for money
    String userId         // Keep strings for user IDs
) {}
```

### State Store Performance

#### Memory Management
```java
// Enable caching for frequently accessed data
.withCachingEnabled()

// Configure retention policies
.withRetention(Duration.ofDays(7))

// Enable compaction for key-value stores
.compacted()
```

#### Disk Usage
```java
// Configure state store location
spring:
  kafka:
    streams:
      state-dir: ./kafka-streams-state

// Configure cleanup policies
.withLoggingDisabled()  // Disable changelog logging
.withRetention(Duration.ofHours(6))  // Shorter retention
```

## Troubleshooting

### Common Serde Issues

#### ClassCastException
```java
// Ensure consistent types
Serdes.String()  // Key
new TransactionSerde()  // Value
```

#### Serialization Errors
```java
// Handle null values
@Override
public byte[] serialize(String topic, Transaction data) {
    if (data == null) {
        return null;
    }
    // ... rest of implementation
}
```

### State Store Issues

#### State Store Not Found
```java
// Ensure store name matches
Materialized.as("exact-store-name")  // Must match query

// Wait for store to be ready
kafkaStreams.store("store-name", QueryableStoreTypes.keyValueStore());
```

#### State Directory Permissions
```bash
# Check directory permissions
ls -la /path/to/state/dir

# Create directory if needed
mkdir -p /path/to/state/dir
chmod 755 /path/to/state/dir
```

## Best Practices

### Serde Best Practices
1. **Immutable objects** for thread safety
2. **Null handling** in serializers/deserializers
3. **Efficient data types** (long vs double for money)
4. **Schema evolution** for future changes

### State Store Best Practices
1. **Appropriate retention** periods
2. **Regular cleanup** of old data
3. **Monitoring** of store sizes
4. **Backup strategies** for critical state

### Configuration Best Practices
1. **Consistent serdes** across all operations
2. **Proper partitioning** for scalability
3. **Error handling** in serialization
4. **Testing** with sample data
