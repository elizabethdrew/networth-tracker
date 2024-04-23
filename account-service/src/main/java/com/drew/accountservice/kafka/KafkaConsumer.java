package com.drew.accountservice.kafka;

import com.drew.accountservice.service.AccountService;
import com.drew.accountservice.service.BalanceService;
import com.drew.commonlibrary.dto.UpdateAccountBalanceDto;
import com.drew.commonlibrary.dto.UpdateAccountDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
@Slf4j
public class KafkaConsumer {

    private final AccountService accountService;
    private final BalanceService balanceService;

    @Autowired
    public KafkaConsumer(AccountService accountService, BalanceService balanceService) {
        this.accountService = accountService;
        this.balanceService = balanceService;
    }

    @Bean
    public Consumer<UpdateAccountDto> updateAccountFromTruelayer() {
        return updateAccountDto -> {
            log.info("Received update for account: " + updateAccountDto);
            accountService.updateAccountFromTruelayer(updateAccountDto);
        };
    }

    @Bean
    public Consumer<UpdateAccountBalanceDto> updateAccountBalanceFromTruelayer() {
        return updateAccountBalanceDto -> {
            log.info("Received update for account: " + updateAccountBalanceDto);
            balanceService.updateAccountBalanceFromTruelayer(updateAccountBalanceDto);
        };
    }
}
