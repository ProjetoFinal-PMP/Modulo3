package br.com.frettec.modulo3.lambda_kafka.infrastructure.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class KafkaConfig {

    @Bean
        public Map<String, Object> adminConfigs() {
            Map<String, Object> props = new HashMap<>();
            props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
            return props;
        }
}
