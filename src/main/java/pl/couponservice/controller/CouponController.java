package pl.couponservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import pl.couponservice.model.command.CreateCouponCommand;
import pl.couponservice.model.command.RedeemCouponCommand;
import pl.couponservice.model.dto.CouponResponse;
import pl.couponservice.model.dto.RedeemCouponResponse;
import pl.couponservice.service.CouponService;

@RestController
@RequiredArgsConstructor
public class CouponController implements CouponApi {

    private final CouponService couponService;

    @Override
    public CouponResponse createCoupon(CreateCouponCommand command) {
        return couponService.createCoupon(command);
    }

    @Override
    public RedeemCouponResponse redeemCoupon(RedeemCouponCommand command, String ipOverride, HttpServletRequest request) {
        String ipAddress = ipOverride != null ? ipOverride : request.getRemoteAddr();
        return couponService.redeemCoupon(command, ipAddress);
    }
}