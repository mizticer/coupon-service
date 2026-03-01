package pl.couponservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.couponservice.client.IpApiClient;
import pl.couponservice.client.IpApiResponse;
import pl.couponservice.exception.GeoLocationException;

@Service
@RequiredArgsConstructor
public class IpApiGeoLocationService implements GeoLocationService {

    private final IpApiClient ipApiClient;

    @Override
    public String resolveCountryCode(String ipAddress) {
        try {
            IpApiResponse response = ipApiClient.lookup(ipAddress, "status,countryCode,message");

            if (!response.isSuccess()) {
                throw new GeoLocationException(ipAddress, response.message());
            }

            return response.countryCode().toUpperCase();
        } catch (GeoLocationException e) {
            throw e;
        } catch (Exception e) {
            throw new GeoLocationException(ipAddress, e.getMessage());
        }
    }
}
