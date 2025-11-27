package br.com.frettec.modulo3.lambda_kafka.domain;

public interface MessageHandler {
    void handle(Message message);
}