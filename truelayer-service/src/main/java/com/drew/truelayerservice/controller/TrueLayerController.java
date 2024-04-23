package com.drew.truelayerservice.controller;

import com.drew.truelayerservice.dto.AccountDto;
import com.drew.truelayerservice.dto.TokenResponse;
import com.drew.truelayerservice.kafka.KafkaService;
import com.drew.truelayerservice.service.TrueLayerService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/v1/bank")
public class TrueLayerController {

    private final TrueLayerService trueLayerService;

    private final KafkaService kafkaService;

    public TrueLayerController(TrueLayerService trueLayerService, KafkaService kafkaService) {
        this.trueLayerService = trueLayerService;
        this.kafkaService = kafkaService;
    }

    @GetMapping("/add")
    public void authenticate(@RequestHeader("X-User-ID") String keycloakUserId, HttpServletResponse response) {
        try {
            String redirectUrl = trueLayerService.authenticateNewBank(keycloakUserId);
            log.info(redirectUrl);
            response.sendRedirect(redirectUrl);
        } catch (Exception e) {
        }
    }

    @GetMapping("/callback")
    public ResponseEntity<TokenResponse> callback(@RequestParam(name = "code") String code, @RequestParam(name = "scope") String scope, @RequestParam(name = "state") String state) {
        log.info("Starting Callback");
        TokenResponse tokenResponse = trueLayerService.exchangeCodeForToken(code, state);
        log.info("Callback token response: " + tokenResponse);
        return ResponseEntity.ok(tokenResponse);
    }

    // Endpoint inplace for testing - not for final application
    @GetMapping("/accounts")
    public ResponseEntity<List<AccountDto>> getAccounts(@RequestHeader("X-User-ID") String keycloakUserId) {
        List<AccountDto> accounts = trueLayerService.fetchAccounts(keycloakUserId);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/update")
    public ResponseEntity<String> updateBankData(@RequestHeader("X-User-ID") String keycloakUserId) {
        trueLayerService.updateAccounts(keycloakUserId);
        return ResponseEntity.ok("Update process initiated.");
    }

}
