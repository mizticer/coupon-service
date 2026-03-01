package pl.couponservice.exception.constraint;

import org.springframework.stereotype.Component;
import pl.couponservice.exception.model.ExceptionDto;

@Component
public class CouponCodeConstraintErrorMapperStrategy implements ConstraintErrorMapperStrategy {

    @Override
    public ExceptionDto getExceptionDto(String message) {
        return new ExceptionDto("Coupon code already exists");
    }

    @Override
    public String getType() {
        return "uq_coupon_code_upper";
    }
}