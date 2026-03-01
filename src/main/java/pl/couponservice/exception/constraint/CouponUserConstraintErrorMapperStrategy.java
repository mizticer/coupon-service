package pl.couponservice.exception.constraint;

import org.springframework.stereotype.Component;
import pl.couponservice.exception.model.ExceptionDto;

@Component
public class CouponUserConstraintErrorMapperStrategy implements ConstraintErrorMapperStrategy {

    @Override
    public ExceptionDto getExceptionDto(String message) {
        return new ExceptionDto("Coupon has already been used by this user");
    }

    @Override
    public String getType() {
        return "uq_coupon_user";
    }
}