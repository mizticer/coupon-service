package pl.couponservice.model.dto;

import java.time.Instant;
import java.util.UUID;

public record CouponResponse(
        UUID id,
        String code,
        int maxUsages,
        int currentUsages,
        String countryCode,
        Instant createdAt
) {
}