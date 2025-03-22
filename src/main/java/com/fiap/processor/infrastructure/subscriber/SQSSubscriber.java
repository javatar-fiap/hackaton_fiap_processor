package com.fiap.processor.infrastructure.subscriber;

import com.fiap.processor.core.application.usecases.ProcessorVideoUseCase;
import com.fiap.processor.core.domain.VideoMessageModel;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SQSSubscriber {

    private final ProcessorVideoUseCase processorVideoUseCase;

    @Autowired
    public SQSSubscriber(ProcessorVideoUseCase processorVideoUseCase) {
        this.processorVideoUseCase = processorVideoUseCase;
    }

    @SqsListener("video-processed-queue.fifo")
    public void receiveMessage(Message<String> message) {
        String content = message.getPayload();

        try {
            var contentFormatted = new JSONObject(content);
            var messageContent = contentFormatted.getString("Message");
            var messageFormatted = new JSONObject(messageContent);

            var videoMessage = new VideoMessageModel(messageFormatted);
            processorVideoUseCase.execute(videoMessage);
        } catch (Exception e) {
            log.error("Erro ao processar a mensagem da fila", e);
        }
    }
}

