package com.innowise.orderservice.infrastructure.adapter.out.impl;

import com.innowise.orderservice.infrastructure.adapter.out.AuthServiceClient;
import com.innowise.orderservice.infrastructure.security.dto.AuthServiceResponse;
import com.innowise.orderservice.infrastructure.security.dto.ServiceAuthenticationRequestDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class AuthServiceClientImpl implements AuthServiceClient {

    private final RestTemplate restTemplate;

    private static final String AUTH_SERVICE = "authService";

    @Value("${application.authService.getServiceAccessTokenUri}")
    private String getServiceAccessTokenUri;

    @Value("${application.id}")
    private Long currentServiceId;
    @Value("${application.login}")
    private String currentServiceLogin;
    @Value("${application.secret}")
    private String currentServiceSecret;


    @Override
    @CircuitBreaker(name = AUTH_SERVICE)
    public AuthServiceResponse getServiceAccessToken() {

        return restTemplate.postForObject(
                getServiceAccessTokenUri,
                new ServiceAuthenticationRequestDto(
                        currentServiceId,
                        currentServiceLogin,
                        currentServiceSecret
                ),
                AuthServiceResponse.class);
    }
}
