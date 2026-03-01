package pl.couponservice.service;

import pl.couponservice.model.command.CreateCouponCommand;
import pl.couponservice.model.command.RedeemCouponCommand;
import pl.couponservice.model.dto.CouponResponse;
import pl.couponservice.model.dto.RedeemCouponResponse;

public interface CouponService {

    CouponResponse createCoupon(CreateCouponCommand command);

    RedeemCouponResponse redeemCoupon(RedeemCouponCommand command, String ipAddress);
}