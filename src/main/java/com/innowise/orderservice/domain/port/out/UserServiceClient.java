package com.innowise.orderservice.domain.port.out;

import com.innowise.orderservice.application.dto.UserInfoResponseDto;

public interface UserServiceClient {

    UserInfoResponseDto geUserByUserId(Long userId);
}
