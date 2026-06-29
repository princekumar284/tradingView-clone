package com.tradingapp.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileResponse {
    private Long id;
    private String username;
    private String email;
}
