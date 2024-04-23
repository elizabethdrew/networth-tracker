package com.drew.truelayerservice.kafka;

import com.drew.commonlibrary.dto.UpdateAccountBalanceDto;
import com.drew.commonlibrary.dto.UpdateAccountDto;
import com.drew.truelayerservice.dto.AccountDto;
import com.drew.truelayerservice.dto.CardDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaService {
    private final StreamBridge streamBridge;

    public KafkaService(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void updateAccountKafka(AccountDto accountDto, String keycloakUserId) {
        var updateAccountDto = new UpdateAccountDto(
                keycloakUserId,
                accountDto.getAccountNumber().getNumber(),
                accountDto.getAccountId(),
                accountDto.getAccountType(),
                accountDto.getDisplayName(),
                accountDto.getCurrency(),
                accountDto.getProvider().getDisplayName(),
                accountDto.getUpdateTimestamp());
        log.info("Sending updated accounts: {}", updateAccountDto);
        streamBridge.send("truelayer-account-update-topic", updateAccountDto);
    }

    public void updateAccountKafka(CardDto cardDto, String keycloakUserId) {
        var updateAccountDto = new UpdateAccountDto(
                keycloakUserId,
                cardDto.getPartialCardNumber(),
                cardDto.getAccountId(),
                "CARD",
                cardDto.getDisplayName(),
                cardDto.getCurrency(),
                cardDto.getProvider().getDisplayName(),
                cardDto.getUpdateTimestamp());
        log.info("Sending updated accounts: {}", updateAccountDto);
        streamBridge.send("truelayer-account-update-topic", updateAccountDto);
    }

    public void updateAccountBalanceKafka(UpdateAccountBalanceDto updateAccountBalanceDto) {
        log.info("Sending updated account balance: {}", updateAccountBalanceDto);
        streamBridge.send("truelayer-account-balance-update-topic", updateAccountBalanceDto);
    }
}

