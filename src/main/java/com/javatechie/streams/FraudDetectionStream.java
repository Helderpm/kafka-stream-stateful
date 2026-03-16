package com.javatechie.streams;


import com.javatechie.events.Transaction;
import com.javatechie.serdes.TransactionSerde;
import com.javatechie.validation.TransactionValidationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@Configuration
@EnableKafkaStreams
@Slf4j
public class FraudDetectionStream {

    @Autowired
    private TransactionValidationService validationService;

//    filter(),
//    filterNot(),
//    map(),
//    mapValues(),
//    flatMap(),
//    flatMapValues(),
//    branch(),
//    groupBy(),
//    aggregate(),
//    count()

    @Bean
    public KStream<String, Transaction> fraudDetectStream(StreamsBuilder builder) {

        KStream<String, Transaction> stream =
                builder.stream("transactions", Consumed.with(Serdes.String(), new TransactionSerde()));

        // Validate transactions and route invalid ones to DLQ using modern approach
        KStream<String, Transaction> validStream = stream.filter((key, tx) -> validationService.isValid(tx));
        KStream<String, Transaction> invalidStream = stream.filter((key, tx) -> !validationService.isValid(tx));

        // Route invalid transactions to DLQ
        invalidStream
                .mapValues(tx -> {
                    log.error("🚨 Invalid transaction detected - routing to DLQ: {}", tx);
                    return tx;
                })
                .to("transactions-dlq", Produced.with(Serdes.String(), new TransactionSerde()));

        // Process valid transactions
        // Add fraud detection logic
        validStream.filter((key, tx) -> tx.amount() > 25000)
                .peek((key, tx) -> log.warn("⚠️ FRAUD ALERT for {}", tx))
                .to("fraud-alerts", Produced.with(Serdes.String(), new TransactionSerde()));

        // Normal transactions processing
        validStream.filterNot((key, tx) -> tx.amount() > 25000)
                .peek((key, tx) -> log.info("✅ Normal transaction processed: {}", tx))
                .to("normal-transactions", Produced.with(Serdes.String(), new TransactionSerde()));

        // Existing aggregation logic for valid transactions only
        validStream.groupBy((key, tx) -> tx.type())
                .aggregate(
                        () -> 0.0,
                        (type, tx, currentSum) -> currentSum + tx.amount(),
                        Materialized.with(Serdes.String(), Serdes.Double())
                ).toStream()
                .peek((type, total) ->
                        log.info("CardType: {} | 💰 Running Total Amount: {}", type, total)
                );

        return stream;
    }

}
