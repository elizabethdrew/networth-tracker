package com.drew.truelayerservice.util;

import com.drew.truelayerservice.dto.TokenResponse;
import com.drew.truelayerservice.entity.Token;
import com.drew.truelayerservice.repository.TokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;

@Slf4j
@Service
public class TrueLayerUtil {

    private final APIHandler apiHandler;
    private final String clientId;
    private final String clientSecret;
    private final String authBaseUri;
    private final TokenRepository tokenRepository;
    private final RedisService redisService;

    public TrueLayerUtil(APIHandler apiHandler,
                                @Value("${truelayer.client-id}") String clientId,
                                @Value("${truelayer.client-secret}") String clientSecret,
                                @Value("${truelayer.auth-base-uri}") String authBaseUri,
                                TokenRepository tokenRepository, RedisService redisService) {
        this.apiHandler = apiHandler;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.authBaseUri = authBaseUri;
        this.tokenRepository = tokenRepository;
        this.redisService = redisService;
    }

    public String getAccessToken(String keycloakUserId) throws ResponseStatusException {
        String accessToken = redisService.getCachedToken(keycloakUserId);
//        log.info("User Access Token: " + accessToken);
        if (accessToken != null) {
            return accessToken;
        }
        Token refreshTokenEntry = tokenRepository.findByUserId(keycloakUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No refresh token available - please authenticate"));

        return refreshAccessToken(refreshTokenEntry);
    }

    private String refreshAccessToken(Token refreshTokenEntry) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "refresh_token");
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("refresh_token", refreshTokenEntry.getRefreshToken());

        TokenResponse tokenResponse = apiHandler.callAPI(
                authBaseUri + "/connect/token",
                HttpMethod.POST,
                headers,
                map,
                TokenResponse.class
        ).getBody();

        refreshTokenEntry.setRefreshToken(tokenResponse.getRefresh_token());
        refreshTokenEntry.setRefreshedAt(OffsetDateTime.now());
        tokenRepository.save(refreshTokenEntry);

        redisService.cacheToken(String.valueOf(refreshTokenEntry.getUserId()), tokenResponse.getAccess_token(), Long.parseLong(tokenResponse.getExpires_in()));

        return tokenResponse.getAccess_token();
    }
}
