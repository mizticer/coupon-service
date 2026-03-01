package pl.couponservice.model.dto;

import java.time.Instant;

public record RedeemCouponResponse(
        String couponCode,
        String userId,
        Instant redeemedAt,
        int remainingUsages
) {
}