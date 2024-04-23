package com.drew.accountservice.kafka;

import com.drew.accountservice.entity.Account;
import com.drew.accountservice.entity.Balance;
import com.drew.commonlibrary.dto.AccountIsaDto;
import com.drew.commonlibrary.dto.KafkaBalanceDto;
import com.drew.commonlibrary.dto.RequestAccountBalanceDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;
import com.drew.commonlibrary.types.AccountType;

@Slf4j
@Component
public class KafkaService {
    private final StreamBridge streamBridge;

    public KafkaService(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

//    // OLD - TO ISA SERVICE
//    public void newAccountKafka(String topic, Account account) {
//        var accountIsaDto = new AccountIsaDto(account.getAccountId(), account.getType(), account.getKeycloakUserId());
//        log.info("Sending to Isa Service - New Isa Account: {}", accountIsaDto);
//        streamBridge.send(topic, accountIsaDto);
//    }

//    // OLD - TO ISA SERVICE
//    public void newBalanceKafka(String topic, Balance balance, String keycloakUserId, AccountType accountType) {
//        var kafkaBalanceDto = new KafkaBalanceDto(
//                balance.getAccountId(),
//                keycloakUserId,
//                accountType,
//                balance.getBalance(),
//                balance.getDepositValue(),
//                balance.getWithdrawalValue()
//        );
//        log.info("Alerting isa service about balance update: " + kafkaBalanceDto);
//        streamBridge.send(topic, kafkaBalanceDto);
//    }

    public void requestAccountBalanceKafka(RequestAccountBalanceDto requestAccountBalanceDto) {
        log.info("Requesting account balance update: {}", requestAccountBalanceDto.accountId());
        streamBridge.send("request-account-balance-update-topic", requestAccountBalanceDto);
    }
}
