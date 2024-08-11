package com.producer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        kafkaTemplate.send(orderTopic, order);
        log.info("published event : {}", order.toString());
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
