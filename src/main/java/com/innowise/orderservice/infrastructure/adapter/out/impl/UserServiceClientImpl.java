package com.innowise.orderservice.infrastructure.adapter.out.impl;

import com.innowise.orderservice.application.dto.UserInfoResponseDto;
import com.innowise.orderservice.domain.port.out.UserServiceClient;
import com.innowise.orderservice.infrastructure.adapter.out.AuthServiceClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserServiceClientImpl implements UserServiceClient {

    @Value("${application.userservice.getUserUriUnformatted}")
    private String getUserUriUnformatted;
    private static final String USER_SERVICE = "userService";
    private final RestTemplate restTemplate;
    private final AuthServiceClient authServiceClient;

    private volatile boolean isTokenValid;
    private volatile String serviceAccessToken;

    @Override
    @Cacheable(cacheNames = {"userInfo"}, key = "#userId", unless = "#result == null")
    @CircuitBreaker(name = USER_SERVICE)
    @Retry(name=USER_SERVICE)
    public UserInfoResponseDto getUserByUserId(Long userId) {

        HttpHeaders headers = new HttpHeaders();

        headers.add("Authorization", "Bearer " + serviceAccessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);


        ResponseEntity<UserInfoResponseDto> response = restTemplate.exchange(
                getUserUriUnformatted + userId,
                HttpMethod.GET,
                request,
                UserInfoResponseDto.class);

        log.trace("Get user response status: {}", response.getStatusCode());

        if(response.getStatusCode().equals(HttpStatus.UNAUTHORIZED)){
            isTokenValid = false;
            updateServiceAccessToken();
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED,
                    "Unauthorized - token refresh triggered");
        } else
            isTokenValid = true;

        return response.getBody();
    }

    private synchronized void updateServiceAccessToken(){
        if(isTokenValid)
            return;
        serviceAccessToken = authServiceClient.getServiceAccessToken().accessToken();
        isTokenValid = true;
    }
}
