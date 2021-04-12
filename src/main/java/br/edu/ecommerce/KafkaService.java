package br.edu.ecommerce;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.io.Closeable;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Pattern;

class KafkaService<T> implements Closeable {
    private final KafkaConsumer<String, T> consumer;
    private final ConsumerFuncion parse;

    KafkaService(String groupId, String topic, ConsumerFuncion parse, Class<T> type) {
        this(parse, groupId, type);
        consumer.subscribe(Collections.singletonList(topic));

    }

    KafkaService(String groupId, Pattern topic, ConsumerFuncion parse, Class<T> type) {
        this(parse, groupId, type);
        consumer.subscribe(topic);
    }

    private KafkaService(ConsumerFuncion parse, String groupId, Class<T> type) {
        this.parse = parse;
        this.consumer = new KafkaConsumer<>(properties(type, groupId)) ;
    }

    void run(){
        while(true){
            var records = consumer.poll(Duration.ofMillis(100));
            if(!records.isEmpty()){
                System.out.println("Encontrei " + records.count() + " registros");
                for(var record : records){
                    parse.consume(record);
                }
            }
        }
    }

    private Properties properties(Class<T> type, String groupId) {
        var properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, GsonDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, EmailService.class.getSimpleName());
        properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1");
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.CLIENT_ID_CONFIG, UUID.randomUUID().toString());
        properties.setProperty(GsonDeserializer.TYPE_CONFIG, type.getName());

        return properties;
    }

    @Override
    public void close() {
        consumer.close();
    }
}
