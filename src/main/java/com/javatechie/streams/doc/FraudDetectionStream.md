# FraudDetectionStream

## Overview

The `FraudDetectionStream` class is the main Kafka Streams processor that analyzes financial transactions in real-time to detect potential fraudulent activities.

## Functionality

### Primary Operations

```java
stream.groupBy((key, tx) -> tx.type())
        .aggregate(
                () -> 0.0,
                (type, tx, currentSum) -> currentSum + tx.amount(),
                Materialized.with(Serdes.String(), Serdes.Double())
        )
```

### Key Features

1. **Transaction Aggregation**
   - Groups transactions by type (debit/credit)
   - Calculates running totals for each transaction type
   - Maintains state in Kafka Streams state stores

2. **Real-time Processing**
   - Consumes from `transactions` topic
   - Processes each transaction as it arrives
   - Updates running totals immediately

3. **State Management**
   - Uses Materialized state store
   - Persists aggregation results
   - Enables fault-tolerant processing

## Data Flow

```
transactions Topic
        ↓
KStream<String, Transaction>
        ↓
groupBy(tx.type())
        ↓
aggregate(sum amounts)
        ↓
Running Totals by Type
        ↓
Console Logging
```

## Configuration

- **Input Topic**: `transactions`
- **Key Serializer**: `StringSerde`
- **Value Serializer**: `TransactionSerde`
- **State Store**: In-memory with Serdes.String/Serdes.Double

## Logging

The processor logs running totals:
```
INFO - CardType: debit | 💰 Running Total Amount: 5200.0
INFO - CardType: credit | 💰 Running Total Amount: 15000.0
```

## Commented Operations

The class includes various commented stream operations for reference:

- **Filtering**: High-value transactions
- **Mapping**: Transform transaction data
- **FlatMapping**: Extract individual items
- **Branching**: Split by transaction type
- **Counting**: Location-based statistics

## Usage Example

```java
// Enable the stream processor
@Configuration
@EnableKafkaStreams
public class FraudDetectionStream {
    
    @Bean
    public KStream<String, Transaction> fraudDetectStream(StreamsBuilder builder) {
        // Stream processing logic
    }
}
```

## Performance Considerations

- **State Store Size**: Grows with unique transaction types
- **Memory Usage**: Depends on transaction volume
- **Processing Latency**: Typically sub-second
- **Fault Tolerance**: Automatic recovery from state store
