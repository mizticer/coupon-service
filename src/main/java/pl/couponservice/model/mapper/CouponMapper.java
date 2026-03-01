package pl.couponservice.model.mapper;

import org.springframework.stereotype.Component;
import pl.couponservice.model.Coupon;
import pl.couponservice.model.CouponUsage;
import pl.couponservice.model.command.CreateCouponCommand;
import pl.couponservice.model.dto.CouponResponse;
import pl.couponservice.model.dto.RedeemCouponResponse;

@Component
public class CouponMapper {

    public Coupon toEntity(CreateCouponCommand command) {
        return new Coupon(
                command.code(),
                command.maxUsages(),
                command.countryCode()
        );
    }

    public CouponResponse toResponse(Coupon coupon) {
        return new CouponResponse(
                coupon.getId(),
                coupon.getCode(),
                coupon.getMaxUsages(),
                coupon.getCurrentUsages(),
                coupon.getCountryCode(),
                coupon.getCreatedAt()
        );
    }

    public RedeemCouponResponse toRedeemResponse(Coupon coupon, CouponUsage usage, int remaining) {
        return new RedeemCouponResponse(
                coupon.getCode(),
                usage.getUserId(),
                usage.getUsedAt(),
                remaining
        );
    }
}
