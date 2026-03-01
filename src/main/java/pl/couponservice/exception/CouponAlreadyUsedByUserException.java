package pl.couponservice.exception;

public class CouponAlreadyUsedByUserException extends RuntimeException {
    public CouponAlreadyUsedByUserException(String code, String userId) {
        super("Coupon " + code + " has already been used by user: " + userId);
    }
}