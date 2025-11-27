package br.com.frettec.modulo3.lambda_kafka.application.service;

import br.com.frettec.modulo3.lambda_kafka.domain.Message;
import br.com.frettec.modulo3.lambda_kafka.domain.MessageHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final MessageHandler handler;

    @KafkaListener(topics = "lambda-topic", groupId = "lambda-group-juan")
    public void consume(String payload) {
        log.info("[JUAN-LAMBDA] Consumindo mensagem: {}", payload);
        Message message = new Message(payload);
        handler.handle(message);
    }
}
