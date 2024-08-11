package com.producer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Order {
    private String id;
    private String name;
    private int quantity;
}



@Service
@Slf4j
class OrderService {

    @Value("${spring.kafka.topic.order-topic}")
    private String orderTopic;


    private final KafkaTemplate<String, Order> kafkaTemplate;


    OrderService(KafkaTemplate<String, Order> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrder(Order order){
        CompletableFuture<SendResult<String, Order>> completableFuture = kafkaTemplate.send(orderTopic, order);

        // still need to understand more on this topic
        completableFuture
                .thenAccept(result ->
                    log.info("Order event published successfully: {}", order.toString())
                )
                .exceptionally(ex -> {
                    log.error("Failed to publish order event: {}", order.toString(), ex);
                    return null; // Returning null since we don't need a result here
                });
    }

}

@RestController
@RequestMapping("/orders")
class OrderController {

    private final OrderService orderService;

    OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public void publish(@RequestBody Order order){
        orderService.publishOrder(order);
    }

}
