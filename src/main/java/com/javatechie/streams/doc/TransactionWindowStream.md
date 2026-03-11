# TransactionWindowStream

## Overview

The `TransactionWindowStream` class implements time-windowed stream processing to detect rapid transaction patterns that may indicate fraudulent activity.

## Core Functionality

### Fraud Detection Logic

```java
stream.groupBy((key, tx) -> tx.userId())
        .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(10)))
        .count(Materialized.as("user-txn-count-window-store"))
```

### Key Features

1. **Windowed Counting**
   - Groups transactions by user ID
   - Applies 10-second time windows
   - Counts transactions per window

2. **Fraud Detection**
   - Threshold: >3 transactions in 10 seconds
   - Real-time alert generation
   - Detailed logging with window boundaries

3. **State Management**
   - Named state store: `user-txn-count-window-store`
   - Windowed key-value storage
   - Automatic expiration of old windows

## Data Flow

```
transactions Topic
        ↓
KStream<String, Transaction>
        ↓
groupBy(userId)
        ↓
TimeWindows.ofSizeWithNoGrace(10s)
        ↓
count()
        ↓
Windowed Counts
        ↓
Fraud Alert Logic
        ↓
user-txn-counts Topic
```

## Configuration

### Window Settings
- **Window Size**: 10 seconds
- **Grace Period**: No grace (strict window boundaries)
- **Retention**: Based on Kafka Streams defaults

### State Store
- **Name**: `user-txn-count-window-store`
- **Key Type**: `Windowed<String>`
- **Value Type**: `Long`

### Output Configuration
- **Topic**: `user-txn-counts`
- **Key Serde**: `WindowedSerdes.timeWindowedSerdeFrom(String.class, 10000L)`
- **Value Serde**: `Serdes.Long()`

## Alert Generation

### Fraud Detection Criteria
```java
if (count > 3) {
    log.warn("🚨 FRAUD ALERT: User={} made {} transactions within 10 seconds!", user, count);
}
```

### Log Output Examples
```
INFO - 🧾 User=U1 | Count=2 | Window=[2024-01-01T10:00:00 - 2024-01-01T10:00:10]
WARN - 🚨 FRAUD ALERT: User=U2 made 5 transactions within 10 seconds!
INFO - 🧾 User=U3 | Count=1 | Window=[2024-01-01T10:00:10 - 2024-01-01T10:00:20]
```

## Window Behavior

### Time Windows
- **Fixed Size**: 10-second windows
- **Non-overlapping**: Sequential windows
- **Strict Boundaries**: No grace period

### Window Examples
```
Window 1: 10:00:00 - 10:00:10
Window 2: 10:00:10 - 10:00:20  
Window 3: 10:00:20 - 10:00:30
```

## Performance Characteristics

### Latency
- **Detection Time**: Within window boundaries
- **Alert Generation**: Real-time
- **State Updates**: Immediate

### Memory Usage
- **Window Storage**: Proportional to active users
- **Retention**: Automatic cleanup of expired windows
- **State Store Size**: User count × window retention

### Throughput
- **Processing Rate**: Limited by Kafka Streams
- **Window Operations**: Optimized for counting
- **State Access**: O(1) for updates

## Monitoring

### Key Metrics
- Transaction counts per user
- Fraud alert frequency
- Window processing latency
- State store size

### Operational Considerations
- **State Backup**: Configure for production
- **Window Retention**: Adjust based on requirements
- **Alert Threshold**: Tune for false positives/negatives

## Configuration Example

```yaml
spring:
  kafka:
    streams:
      application-id: fraud-detection-streams
      properties:
        default.windowed.key.serde: org.apache.kafka.common.serialization.Serdes$StringSerde
        default.windowed.value.serde: org.apache.kafka.common.serialization.Serdes$LongSerde
```

## Troubleshooting

### Common Issues
1. **State Store Errors**: Check directory permissions
2. **Window Alignment**: Verify timezone configuration
3. **Serde Mismatches**: Ensure consistent serialization

### Debugging
- Enable DEBUG logging for Kafka Streams
- Monitor state store metrics
- Check consumer lag for input topic
