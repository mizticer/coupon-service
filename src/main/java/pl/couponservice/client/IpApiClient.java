package pl.couponservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(
        name = "ipApiClient",
        url = "${geolocation.ip-api.base-url}"
)
public interface IpApiClient {

    @GetMapping("/json/{ip}")
    IpApiResponse lookup(
            @PathVariable String ip,
            @RequestParam(value = "fields", defaultValue = "status,countryCode,message") String fields
    );
}