# Kafka Streams Operations Reference

## Overview

This document provides a comprehensive reference for Kafka Streams operations used in the fraud detection system.

## Core Operations

### 1. Filtering Operations

#### filter()
```java
stream.filter((key, tx) -> tx.amount() > 25000)
       .peek((key, tx) -> log.warn("⚠️ FRAUD ALERT for {}", tx));
```

#### filterNot()
```java
stream.filterNot((key, tx) -> tx.amount() < 10000)
       .peek((key, tx) -> log.warn("⚠️ High-value transaction for {}", tx));
```

### 2. Transformation Operations

#### map()
```java
stream.map((key, tx) ->
        KeyValue.pair(tx.userId(), "user spent amount : " + tx.amount())
).peek((key, value) ->
        log.info("User Transaction Summary: Key: {}, Value: {}", key, value)
);
```

#### mapValues()
```java
stream.mapValues(tx -> "Transaction of ₹" + tx.amount() + " by user " + tx.userId())
       .peek((key, tx) ->
               log.info("Transaction Summary: Key: {}, Value: {}", key, tx)
       );
```

#### flatMap()
```java
stream.flatMap((key, tx) -> {
    List<KeyValue<String, Item>> result = new ArrayList<>();
    for(Item item: tx.items()) {
        result.add(KeyValue.pair(tx.transactionId(), item));
    }
    return result;
}).peek((key, item) ->
        log.info("Item Purchased: Transaction ID: {}, Item: {}", key, item)
);
```

#### flatMapValues()
```java
stream.flatMapValues(Transaction::items)
       .peek((key, item) ->
               log.info("Item Purchased: Transaction ID: {}, Item: {}", key, item)
);
```

### 3. Branching Operations

#### branch()
```java
KStream<String, Transaction>[] branch = stream
        .branch(
                (key, tx) -> tx.type().equalsIgnoreCase("debit"),
                (key, tx) -> tx.type().equalsIgnoreCase("credit")
        );

branch[0].peek((key, tx) ->
        log.info("Debit Transaction: Key: {}, Transaction: {}", key, tx)
).to("debit_transactions", Produced.with(Serdes.String(), new TransactionSerde()));

branch[1].peek((key, tx) ->
        log.info("Credit Transaction: Key: {}, Transaction: {}", key, tx)
).to("credit_transactions", Produced.with(Serdes.String(), new TransactionSerde()));
```

### 4. Aggregation Operations

#### groupBy() + count()
```java
stream.groupBy((key, tx) -> tx.location())
        .count()
        .toStream()
        .peek((loc, count) ->
                log.info("🌍 Location {} has {} transactions", loc, count)
        );
```

#### groupBy() + aggregate()
```java
stream.groupBy((key, tx) -> tx.userId())
        .count(Materialized.as("user-txn-count-store"))
        .toStream()
        .peek((userId, count) ->
                log.info("👥 User {} made {} transactions", userId, count)
        );
```

#### Custom Aggregation
```java
stream.groupBy((key, tx) -> tx.type())
        .aggregate(
                () -> 0.0,
                (type, tx, currentSum) -> currentSum + tx.amount(),
                Materialized.with(Serdes.String(), Serdes.Double())
        ).toStream()
        .peek((type, total) ->
                log.info("CardType: {} | 💰 Running Total Amount: {}", type, total)
        );
```

### 5. Windowed Operations

#### Time Windows
```java
stream.groupBy((key, tx) -> tx.userId())
        .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(10)))
        .count(Materialized.as("user-txn-count-window-store"))
        .toStream()
        .peek((windowedKey, count) -> {
            String user = windowedKey.key();
            log.info("🧾 User={} | Count={} | Window=[{} - {}]",
                    user, count,
                    windowedKey.window().startTime(),
                    windowedKey.window().endTime());
        });
```

#### Hopping Windows
```java
stream.windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(5))
                              .advanceBy(Duration.ofMinutes(1)))
        .count();
```

#### Sliding Windows
```java
stream.windowedBy(SlidingWindows.withTimeDifferenceAndGrace(
                Duration.ofMinutes(5), Duration.ofMinutes(1)))
        .count();
```

## State Store Operations

### Materialized Stores
```java
// Counter store
Materialized.as("user-txn-count-store")

// Aggregation store  
Materialized.with(Serdes.String(), Serdes.Double())

// Windowed store
Materialized.as("user-txn-count-window-store")
```

### Store Types
- **KeyValueStore**: Simple key-value storage
- **WindowStore**: Time-windowed storage
- **SessionStore**: Session-based storage

## Serde Configuration

### Custom Serdes
```java
// Transaction serialization
new TransactionSerde()

// Windowed serialization
WindowedSerdes.timeWindowedSerdeFrom(String.class, 10000L)

// Built-in serialization
Serdes.String(), Serdes.Long(), Serdes.Double()
```

## Output Operations

### to() - Send to Topic
```java
stream.to("output-topic", Produced.with(Serdes.String(), new TransactionSerde()));
```

### through() - Process and Forward
```java
stream.through("intermediate-topic")
       .mapValues(value -> value.toUpperCase());
```

### foreach() - Terminal Operation
```java
stream.foreach((key, value) -> {
    // External system integration
    databaseService.save(key, value);
});
```

## Performance Considerations

### Optimization Tips
1. **Use appropriate serdes** for efficient serialization
2. **Configure state stores** with proper retention
3. **Window sizing** affects memory usage
4. **Partitioning** impacts parallelism

### Memory Management
- **State Store Size**: Monitor and configure limits
- **Window Retention**: Set appropriate cleanup policies
- **Cache Configuration**: Optimize for read/write patterns

## Error Handling

### Exception Handling
```java
stream.mapValues(value -> {
    try {
        return riskyOperation(value);
    } catch (Exception e) {
        log.error("Processing failed", e);
        return defaultValue;
    }
});
```

### Dead Letter Pattern
```java
stream.branch(
        (key, value) -> isValid(value),
        (key, value) -> true  // Catch all for invalid records
)[1].to("dead-letter-topic");
```

## Monitoring

### Stream Metrics
- **Throughput**: Records processed per second
- **Latency**: Processing time per record
- **State Store Size**: Memory usage
- **Error Rates**: Failed operations

### Health Checks
```java
// Stream state monitoring
kafkaStreams.state()
kafkaStreams.metadataForKey(...)
```
