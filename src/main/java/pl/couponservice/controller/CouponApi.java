package pl.couponservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import pl.couponservice.exception.model.ExceptionDto;
import pl.couponservice.model.command.CreateCouponCommand;
import pl.couponservice.model.command.RedeemCouponCommand;
import pl.couponservice.model.dto.CouponResponse;
import pl.couponservice.model.dto.RedeemCouponResponse;

@RequestMapping("/api/v1/coupons")
@Tag(name = "Coupons", description = "Coupon management API")
public interface CouponApi {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new coupon")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Coupon created successfully",
                    content = @Content(schema = @Schema(implementation = CouponResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class))),
            @ApiResponse(responseCode = "409", description = "Coupon code already exists",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class)))
    })
    CouponResponse createCoupon(@Valid @RequestBody CreateCouponCommand command);

    @PostMapping("/redemptions")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Redeem a coupon")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Coupon redeemed successfully",
                    content = @Content(schema = @Schema(implementation = RedeemCouponResponse.class))),
            @ApiResponse(responseCode = "404", description = "Coupon not found",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class))),
            @ApiResponse(responseCode = "403", description = "Country not allowed",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class))),
            @ApiResponse(responseCode = "409", description = "Coupon exhausted or already used",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class)))
    })
    RedeemCouponResponse redeemCoupon(
            @Valid @RequestBody RedeemCouponCommand command,
            @Parameter(description = "Override IP for testing", example = "217.119.79.237") @RequestParam(value = "ip", required = false) String ipOverride,
            HttpServletRequest request
    );
}