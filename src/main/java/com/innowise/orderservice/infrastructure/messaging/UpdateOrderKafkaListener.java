package com.innowise.orderservice.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.orderservice.application.dto.UpdateOrderStatusRequestDto;
import com.innowise.orderservice.application.service.OrderApplicationService;
import com.innowise.orderservice.domain.exception.OrderNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * [No Inbox](https://www.youtube.com/watch?v=kr5TtRktdYE)
 * No point in talking about any kind of guarantees here
 * Messages gonna get lost, duplicated, everyone is gonna die, and I'm gonna get some sleep
 */


@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateOrderKafkaListener {

    private final OrderApplicationService orderApplicationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            autoStartup = "${application.kafka.listener.auto-startup:true}",
            topics={"${application.kafka.topics.update-order-status}"
            },
            clientIdPrefix = "${application.kafka.listener.client-id-prefix}",
            groupId = "${application.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    void listener(String data, Acknowledgment ack){

        try {
            UpdateOrderStatusRequestDto requestDto = objectMapper.readValue(data, UpdateOrderStatusRequestDto.class);

            orderApplicationService.updateOrderStatus(requestDto);
            ack.acknowledge();
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize an event received via Kafka. The initial json: {}", data);
            throw new RuntimeException(e);
        } catch (OrderNotFoundException e) {
            log.warn("Received a request to update status of non existent order: {}", data);
            throw new RuntimeException(e);
        }
    }
}
