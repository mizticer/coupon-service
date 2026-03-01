package pl.couponservice.client;

public record IpApiResponse(
        String status,
        String countryCode,
        String message
) {
    public boolean isSuccess() {
        return "success".equals(status);
    }
}