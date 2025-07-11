package com.halverson.swappin_messages;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class SwappinMessagesApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(SwappinMessagesApplication.class, args);
        StartSwappin swappin = ctx.getBean(StartSwappin.class);
        swappin.start();
    }

}
