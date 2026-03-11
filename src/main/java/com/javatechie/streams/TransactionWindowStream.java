package com.javatechie.streams;

import com.javatechie.events.Transaction;
import com.javatechie.serdes.TransactionSerde;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;

import java.time.Duration;

@Configuration
@EnableKafkaStreams
@Slf4j
public class TransactionWindowStream {

    //source topic (transactions)
    //process (Windowing)10 > 3 -> fraud alert
    //write it back -> txn-fraud-alert

    @Bean
    public KStream<String, Transaction> windowedTransactionStream(StreamsBuilder builder) {

        KStream<String, Transaction> stream =
                builder.stream("transactions", Consumed.with(Serdes.String(), new TransactionSerde()));

        //u1- 5
        //u2- 3
        stream.groupBy((key, tx) -> tx.userId(),
                        Grouped.with(Serdes.String(), new TransactionSerde())
                ).windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(10)))
                .count(Materialized.as("user-txn-count-window-store"))
                .toStream()
                .peek((windowedKey, count) -> {
                    String user = windowedKey.key();
                    log.info("🧾 User={} | Count={} | Window=[{} - {}]",
                            user,
                            count,
                            windowedKey.window().startTime(),
                            windowedKey.window().endTime());

                    if (count > 3) {
                        log.warn("🚨 FRAUD ALERT: User={} made {} transactions within 10 seconds!", user, count);
                    }
                })
                .to("user-txn-counts", Produced.with(
                        WindowedSerdes.timeWindowedSerdeFrom(String.class, 10000L),
                        Serdes.Long()
                ));
        return stream;

    }
}
