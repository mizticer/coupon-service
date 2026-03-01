package pl.couponservice.exception;

public class CouponCountryMismatchException extends RuntimeException {
    public CouponCountryMismatchException(String couponCountry, String userCountry) {
        super("Coupon is restricted to country: " + couponCountry + ", but request came from: " + userCountry);
    }
}