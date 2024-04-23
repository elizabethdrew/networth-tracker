package com.drew.truelayerservice.service.impl;

import com.drew.commonlibrary.dto.RequestAccountBalanceDto;
import com.drew.commonlibrary.dto.UpdateAccountBalanceDto;
import com.drew.truelayerservice.dto.AccountBalanceDto;
import com.drew.truelayerservice.dto.AccountBalanceResponseDto;
import com.drew.truelayerservice.dto.AccountDto;
import com.drew.truelayerservice.dto.AccountResponseDto;
import com.drew.truelayerservice.dto.CardBalanceDto;
import com.drew.truelayerservice.dto.CardBalanceResponseDto;
import com.drew.truelayerservice.dto.CardDto;
import com.drew.truelayerservice.dto.CardResponseDto;
import com.drew.truelayerservice.dto.ErrorResponse;
import com.drew.truelayerservice.dto.TokenResponse;
import com.drew.truelayerservice.entity.Token;
import com.drew.truelayerservice.exception.AccessDeniedException;
import com.drew.truelayerservice.exception.AccountNotFoundException;
import com.drew.truelayerservice.exception.ApiException;
import com.drew.truelayerservice.exception.ConnectorOverloadException;
import com.drew.truelayerservice.exception.ConnectorTimeoutException;
import com.drew.truelayerservice.exception.EndpointNotSupportedException;
import com.drew.truelayerservice.exception.InternalServerErrorException;
import com.drew.truelayerservice.exception.InvalidTokenException;
import com.drew.truelayerservice.exception.ProviderErrorException;
import com.drew.truelayerservice.exception.ProviderRateLimitExceededException;
import com.drew.truelayerservice.exception.ProviderRequestLimitExceededException;
import com.drew.truelayerservice.exception.ProviderTimeoutException;
import com.drew.truelayerservice.exception.ScaExceededException;
import com.drew.truelayerservice.exception.UnauthorizedException;
import com.drew.truelayerservice.kafka.KafkaService;
import com.drew.truelayerservice.repository.TokenRepository;
import com.drew.truelayerservice.service.TrueLayerService;
import com.drew.truelayerservice.util.APIHandler;
import com.drew.truelayerservice.util.RedisService;
import com.drew.truelayerservice.util.TrueLayerUtil;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TrueLayerServiceImpl implements TrueLayerService {

    private final APIHandler apiHandler;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final String authBaseUri;
    private final String dataBaseUri;
    private final TokenRepository tokenRepository;
    private final RedisService redisService;
    private final TrueLayerUtil trueLayerUtil;

    private final KafkaService kafkaService;

    public TrueLayerServiceImpl(APIHandler apiHandler,
                                @Value("${truelayer.client-id}") String clientId,
                                @Value("${truelayer.client-secret}") String clientSecret,
                                @Value("${truelayer.redirect-uri}") String redirectUri,
                                @Value("${truelayer.auth-base-uri}") String authBaseUri,
                                @Value("${truelayer.data-base-uri}") String dataBaseUri,
                                TokenRepository tokenRepository, RedisService redisService, TrueLayerUtil trueLayerUtil, KafkaService kafkaService) {
        this.apiHandler = apiHandler;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.authBaseUri = authBaseUri;
        this.dataBaseUri = dataBaseUri;
        this.tokenRepository = tokenRepository;
        this.trueLayerUtil = trueLayerUtil;
        this.redisService = redisService;
        this.kafkaService = kafkaService;
    }

    @Override
    public String authenticateNewBank(String keycloakUserId) {
            return UriComponentsBuilder.fromUriString(authBaseUri + "/?response_type=code")
                    .queryParam("client_id", clientId)
                    .queryParam("redirect_uri", redirectUri)
                    .queryParam("scope", "info accounts balance cards transactions direct_debits standing_orders offline_access")
                    .queryParam("state", keycloakUserId)
                    .queryParam("providers", "uk-cs-mock uk-ob-all uk-oauth-all")
                    .toUriString();
    }

    @Override
    public TokenResponse exchangeCodeForToken(String code, String state) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "authorization_code");
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("redirect_uri", redirectUri);
        map.add("code", code);
        map.add("state", state);

        try {
            TokenResponse tokenResponse = apiHandler.callAPI(
                    authBaseUri + "/connect/token",
                    HttpMethod.POST,
                    headers,
                    map,
                    TokenResponse.class
            ).getBody();

            log.info("Token Response: " + tokenResponse);

            // Save token details to the database
            Token token = new Token();
            token.setUserId(state);
            token.setRefreshToken(tokenResponse.getRefresh_token());
            token.setCreatedAt(OffsetDateTime.now());
            tokenRepository.save(token);

            // Cache the access token with Redis
            redisService.cacheToken(state, tokenResponse.getAccess_token(), Long.parseLong(tokenResponse.getExpires_in()));

            return tokenResponse;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            handleApiException(e);
            throw new ApiException("Error handling API exception", e);
        } catch (Exception e) {
            throw new ApiException("An unexpected error occurred while exchanging code for token.", e);
        }
    }

    @Override
    public List<AccountDto> fetchAccounts(String keycloakUserId) {
        String url = dataBaseUri + "/data/v1/accounts";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(trueLayerUtil.getAccessToken(keycloakUserId));

        try {
            ResponseEntity<AccountResponseDto> response = apiHandler.callAPI(
                    url,
                    HttpMethod.GET,
                    headers,
                    null,
                    AccountResponseDto.class
            );

            return Optional.ofNullable(response.getBody())
                    .map(AccountResponseDto::getResults)
                    .orElse(Collections.emptyList());
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            handleApiException(e);
            throw new ApiException("Error handling API exception", e);
        } catch (Exception e) {
            throw new ApiException("An unexpected error occurred while exchanging code for token.", e);
        }
    }

    @Override
    public List<CardDto> fetchCards(String keycloakUserId) {
        String url = dataBaseUri + "/data/v1/cards";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(trueLayerUtil.getAccessToken(keycloakUserId));

        try {
            ResponseEntity<CardResponseDto> response = apiHandler.callAPI(
                    url,
                    HttpMethod.GET,
                    headers,
                    null,
                    CardResponseDto.class
            );

            if (response.getBody() != null) {
                return response.getBody().getResults();
            } else {
                return Collections.emptyList();
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            handleApiException(e);
            throw new ApiException("Error handling API exception", e);
        } catch (Exception e) {
            throw new ApiException("An unexpected error occurred while exchanging code for token.", e);
        }
    }

    @Override
    public void updateAccounts(String keycloakUserId) {

        List<AccountDto> accounts = fetchAccounts(keycloakUserId);
        List<CardDto> cards = fetchCards(keycloakUserId);

        // For each account in accounts
        accounts.forEach(account -> kafkaService.updateAccountKafka(account, keycloakUserId));

        // For each card in cards
        cards.forEach(card -> kafkaService.updateAccountKafka(card, keycloakUserId));

    }

    @Override
    public void updateAccountBalanceFromTruelayer(RequestAccountBalanceDto requestAccountBalanceDto) {

        log.info("Starting request account balance: " + requestAccountBalanceDto);

        if (requestAccountBalanceDto.accountType().equals("CARD")) {
            List<CardBalanceDto> cardBalanceDto = fetchCardBalance(
                    requestAccountBalanceDto.accountId(),
                    requestAccountBalanceDto.keycloakUserId()
            );
            cardBalanceDto.forEach(cardBalance -> {
                UpdateAccountBalanceDto updateAccountBalanceDto = new UpdateAccountBalanceDto(
                        requestAccountBalanceDto.accountId(),
                        requestAccountBalanceDto.keycloakUserId(),
                        cardBalance.getCurrency(),
                        cardBalance.getCurrent(),
                        cardBalance.getUpdateTimestamp()
                );
                kafkaService.updateAccountBalanceKafka(updateAccountBalanceDto);
            });
        } else {
            List<AccountBalanceDto> accountBalanceDto = fetchAccountBalance(
                    requestAccountBalanceDto.accountId(),
                    requestAccountBalanceDto.keycloakUserId()
            );
            accountBalanceDto.forEach(accountBalance -> {
                UpdateAccountBalanceDto updateAccountBalanceDto = new UpdateAccountBalanceDto(
                        requestAccountBalanceDto.accountId(),
                        requestAccountBalanceDto.keycloakUserId(),
                        accountBalance.getCurrency(),
                        accountBalance.getCurrent(),
                        accountBalance.getUpdateTimestamp()
                );
                kafkaService.updateAccountBalanceKafka(updateAccountBalanceDto);
            });
        }

    }

    public List<CardBalanceDto> fetchCardBalance(String accountId, String keycloakUserId) {
        String url = dataBaseUri + "/data/v1/cards/" + accountId + "/balance";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(trueLayerUtil.getAccessToken(keycloakUserId));

        try {
            ResponseEntity<CardBalanceResponseDto> response = apiHandler.callAPI(
                    url,
                    HttpMethod.GET,
                    headers,
                    null,
                    CardBalanceResponseDto.class
            );

            if (response.getBody() != null) {
                return response.getBody().getResults();
            } else {
                return Collections.emptyList();
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            handleApiException(e);
            throw new ApiException("Error handling API exception", e);
        } catch (Exception e) {
            throw new ApiException("An unexpected error occurred while exchanging code for token.", e);
        }
    }

    public List<AccountBalanceDto> fetchAccountBalance(String accountId, String keycloakUserId) {
        String url = dataBaseUri + "/data/v1/accounts/" + accountId + "/balance";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(trueLayerUtil.getAccessToken(keycloakUserId));

        try {
            ResponseEntity<AccountBalanceResponseDto> response = apiHandler.callAPI(
                    url,
                    HttpMethod.GET,
                    headers,
                    null,
                    AccountBalanceResponseDto.class
            );

            if (response.getBody() != null) {
                return response.getBody().getResults();
            } else {
                return Collections.emptyList();
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            handleApiException(e);
            throw new ApiException("Error handling API exception", e);
        } catch (Exception e) {
            throw new ApiException("An unexpected error occurred while exchanging code for token.", e);
        }
    }

    private ErrorResponse parseErrorResponse(String responseBody) {
        return new Gson().fromJson(responseBody, ErrorResponse.class);
    }

    private void handleApiException(RestClientResponseException e) {
        ErrorResponse errorResponse = parseErrorResponse(e.getResponseBodyAsString());
        switch (errorResponse.getError()) {
            case "unauthorized", "unauthorized_client":
                throw new UnauthorizedException(errorResponse.getErrorDescription());
            case "sca_exceeded":
                throw new ScaExceededException(errorResponse.getErrorDescription());
            case "access_denied":
                throw new AccessDeniedException(errorResponse.getErrorDescription());
            case "account_not_found":
                throw new AccountNotFoundException(errorResponse.getErrorDescription());
            case "invalid_request":
                throw new InvalidTokenException(errorResponse.getErrorDescription());
            case "internal_server_error":
                throw new InternalServerErrorException(errorResponse.getErrorDescription());
            case "provider_too_many_requests":
                throw new ProviderRequestLimitExceededException(errorResponse.getErrorDescription());
            case "provider_request_limit_exceeded":
                throw new ProviderRateLimitExceededException(errorResponse.getErrorDescription());
            case "endpoint_not_supported":
                throw new EndpointNotSupportedException(errorResponse.getErrorDescription());
            case "provider_error", "temporarily_unavailable":
                throw new ProviderErrorException(errorResponse.getErrorDescription());
            case "connector_overload":
                throw new ConnectorOverloadException(errorResponse.getErrorDescription());
            case "connector_timeout":
                throw new ConnectorTimeoutException(errorResponse.getErrorDescription());
            case "provider_timeout":
                throw new ProviderTimeoutException(errorResponse.getErrorDescription());
            default:
                throw new ApiException("Error exchanging code for token: " + errorResponse.getErrorDescription());
        }
    }

}
