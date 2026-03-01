package pl.couponservice.model.command;

import jakarta.validation.constraints.NotBlank;

public record RedeemCouponCommand(

        @NotBlank(message = "Coupon code is required")
        String code,

        @NotBlank(message = "User ID is required")
        String userId
) {
}