package pl.couponservice.exception;

public class GeoLocationException extends RuntimeException {
    public GeoLocationException(String ipAddress, String reason) {
        super("Failed to resolve geolocation for IP: " + ipAddress + ", reason: " + reason);
    }
}