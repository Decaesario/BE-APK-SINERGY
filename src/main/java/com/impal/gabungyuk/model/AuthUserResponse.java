package com.impal.gabungyuk.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthUserResponse {
    private Integer userId;
    private String username;
    private String email;
    private String token;
    private Long expiredAt;
}
