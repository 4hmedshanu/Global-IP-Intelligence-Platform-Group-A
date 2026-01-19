package com.ipintelligence.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Subscriptiondto {

    private Integer id;

    // User info (safe fields only)
    private Integer userId;
    private String username;
    private String email;

    // FULL IP ASSET DTO
    private IpAssetDto ipAsset;

    private LocalDateTime createdAt;
}
