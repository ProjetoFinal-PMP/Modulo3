package br.com.frettec.modulo3.lambda_kafka.api;

import br.com.frettec.modulo3.lambda_kafka.application.dto.MessageDTO;
import br.com.frettec.modulo3.lambda_kafka.application.service.KafkaProducerService;
import br.com.frettec.modulo3.lambda_kafka.domain.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final KafkaProducerService producer;

    @PostMapping
    public ResponseEntity<MessageDTO> send(@RequestBody MessageDTO dto) {
        log.info("[JUAN-LAMBDA] Recebido via HTTP: {}", dto.getContent());

        Message message = new Message(dto.getContent());
        producer.send(message);

        return ResponseEntity.accepted().body(dto);
    }
}
