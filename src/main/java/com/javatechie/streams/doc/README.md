# Kafka Streams Documentation

## Overview

This directory contains the core stream processing logic for the fraud detection system. The streams process real-time transaction data from Kafka topics to identify potential fraudulent activities.

## Architecture

```
Input Topic (transactions)
        ↓
FraudDetectionStream
        ↓
TransactionWindowStream
        ↓
Output Topics (user-txn-counts, fraud-alerts)
```

## Stream Processors

### 1. FraudDetectionStream
- **Purpose**: Main stream processor for transaction analysis
- **Input**: `transactions` topic
- **Operations**: Aggregation by transaction type
- **Output**: Running totals by transaction type

### 2. TransactionWindowStream  
- **Purpose**: Windowed analysis for fraud detection
- **Input**: `transactions` topic
- **Operations**: Time-windowed counting (10-second windows)
- **Output**: `user-txn-counts` topic with fraud alerts

## Key Features

- **Real-time Processing**: Continuous stream processing
- **Stateful Operations**: Maintains aggregation state
- **Windowed Analysis**: Time-based transaction counting
- **Fraud Detection**: Identifies suspicious patterns
- **Fault Tolerance**: Automatic recovery from failures

## Configuration

- **Application ID**: `fraud-detection-streams`
- **Bootstrap Servers**: `localhost:9092`
- **State Store**: Local state directory for aggregations
- **Window Size**: 10 seconds for fraud detection

## Data Flow

1. Transactions published to `transactions` topic
2. Stream processors consume and analyze data
3. Aggregations maintain running totals
4. Windowed operations detect rapid transactions
5. Results published to output topics
6. Fraud alerts logged in real-time
