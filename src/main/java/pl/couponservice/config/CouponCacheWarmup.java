package pl.couponservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import pl.couponservice.repository.CouponRepository;
import pl.couponservice.repository.CouponUsageRepository;
import pl.couponservice.service.CouponRedisService;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponCacheWarmup implements ApplicationRunner {

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;
    private final CouponRedisService couponRedisService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Warming up coupon cache from database...");
        couponRepository.findAll().forEach(coupon -> {
            if (!couponRedisService.couponExists(coupon.getCode())) {
                couponRedisService.initCoupon(
                        coupon.getCode(),
                        coupon.getRemainingUsages(),
                        coupon.getId(),
                        coupon.getCountryCode()
                );
                couponUsageRepository.findAllByCoupon(coupon).forEach(usage ->
                        couponRedisService.tryAddUser(coupon.getCode(), usage.getUserId())
                );
            }
        });
        log.info("Coupon cache warmup complete");
    }
}