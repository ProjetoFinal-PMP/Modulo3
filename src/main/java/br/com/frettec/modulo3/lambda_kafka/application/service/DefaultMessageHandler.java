package br.com.frettec.modulo3.lambda_kafka.application.service;

import br.com.frettec.modulo3.lambda_kafka.domain.Message;
import br.com.frettec.modulo3.lambda_kafka.domain.MessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DefaultMessageHandler implements MessageHandler {

    @Override
    public void handle(Message message) {
        log.info("Processando mensagem de forma lambda-like: {}", message.getContent());
    }
}