package com.innowise.orderservice.infrastructure.adapter.out;

import com.innowise.orderservice.infrastructure.security.dto.AuthServiceResponse;
import com.innowise.orderservice.infrastructure.security.dto.ServiceAuthenticationRequestDto;

public interface AuthServiceClient {

    AuthServiceResponse getServiceAccessToken();
}
