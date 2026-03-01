package pl.couponservice.exception;

public class CouponExhaustedException extends RuntimeException {
    public CouponExhaustedException(String code) {
        super("Coupon has reached maximum usages: " + code);
    }
}