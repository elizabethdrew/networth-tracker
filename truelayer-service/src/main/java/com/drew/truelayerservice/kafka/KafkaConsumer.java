package com.drew.truelayerservice.kafka;

import com.drew.commonlibrary.dto.RequestAccountBalanceDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.drew.truelayerservice.service.TrueLayerService;

import java.util.function.Consumer;


@Configuration
@Slf4j
public class KafkaConsumer {

    @Bean
    public Consumer<RequestAccountBalanceDto> requestAccountBalanceFromTruelayer(TrueLayerService trueLayerService) {
        return requestAccountBalanceDto -> {
            log.info("Received update request for account balance: " + requestAccountBalanceDto.accountId());
            trueLayerService.updateAccountBalanceFromTruelayer(requestAccountBalanceDto);
        };
    }
}
