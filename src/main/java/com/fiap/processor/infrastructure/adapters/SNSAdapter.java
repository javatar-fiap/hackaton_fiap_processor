package com.fiap.processor.infrastructure.adapters;

import com.amazonaws.services.sns.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.processor.config.SNSConfiguration;
import com.fiap.processor.core.application.enums.VideoStatus;
import com.fiap.processor.core.domain.VideoMessageModel;
import com.fiap.processor.infrastructure.exception.PublisherException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class SNSAdapter {

    private final SNSConfiguration snsConfiguration;
    private final Topic productEventsTopic;
    private final ObjectMapper objectMapper;

    public SNSAdapter(@Qualifier("productEventsTopic") Topic productEventsTopic,
                      SNSConfiguration snsConfiguration) {
        this.snsConfiguration = snsConfiguration;
        this.objectMapper = new ObjectMapper();
        this.productEventsTopic = productEventsTopic;
    }

    public void publishMessage(VideoMessageModel videoMessage,
                               VideoStatus status,
                               String zipKeyS3,
                               String videoUrlS3) {
        try {
            Map<String, String> message = new HashMap<>();
            message.put("id", videoMessage.getId());
            message.put("user", videoMessage.getUsername());
            message.put("status", status.toString());
            message.put("email",  videoMessage.getEmail());
            message.put("videoKeyS3", videoMessage.getKeyS3());
            message.put("zipKeyS3", zipKeyS3);
            message.put("videoUrlS3", videoUrlS3);

            var jsonMessage = objectMapper.writeValueAsString(message);

            var publishRequest = new PublishRequest(productEventsTopic.getTopicArn(), jsonMessage)
                    .withMessageGroupId("msg-group-identification")
                    .withMessageDeduplicationId(UUID.randomUUID().toString());

            snsConfiguration.snsClient().publish(publishRequest);

        } catch (Exception e) {
            throw new PublisherException("Erro ao publicar mensagem no SNS", e);
        }
    }
}