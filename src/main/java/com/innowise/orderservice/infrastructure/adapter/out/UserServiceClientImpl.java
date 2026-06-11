package com.innowise.orderservice.infrastructure.adapter.out;

import com.innowise.orderservice.application.dto.UserInfoResponseDto;
import com.innowise.orderservice.domain.port.out.UserServiceClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class UserServiceClientImpl implements UserServiceClient {

    @Value("${application.userservice.getUserUriUnformatted}")
    private String getUserUriUnformatted;
    private static final String USER_SERVICE = "userService";
    private final RestTemplate restTemplate;

    @Override
    @CircuitBreaker(name = USER_SERVICE)
    @Retry(name=USER_SERVICE)
    public UserInfoResponseDto geUserByUserId(Long userId) {
        return restTemplate.getForObject(getUserUriUnformatted + userId, UserInfoResponseDto.class);
    }
}
