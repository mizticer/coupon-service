package pl.couponservice.model.command;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCouponCommand(

        @NotBlank(message = "Coupon code is required")
        @Size(min = 3, max = 50, message = "Coupon code must be between 3 and 50 characters")
        String code,

        @Min(value = 1, message = "Max usages must be at least 1")
        int maxUsages,

        @NotBlank(message = "Country code is required")
        @Size(min = 2, max = 2, message = "Country code must be exactly 2 characters (ISO 3166-1 alpha-2)")
        String countryCode
) {
}