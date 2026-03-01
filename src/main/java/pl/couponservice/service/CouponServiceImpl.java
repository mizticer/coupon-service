package pl.couponservice.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import pl.couponservice.model.Coupon;
import pl.couponservice.model.command.CreateCouponCommand;
import pl.couponservice.model.command.RedeemCouponCommand;
import pl.couponservice.model.dto.CouponResponse;
import pl.couponservice.model.dto.RedeemCouponResponse;
import pl.couponservice.model.mapper.CouponMapper;
import pl.couponservice.repository.CouponRepository;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final CouponRedisService couponRedisService;
    private final CouponRedemptionHandler couponRedemptionHandler;
    private final GeoLocationService geoLocationService;
    private final CouponMapper couponMapper;

    @Override
    @Transactional
    public CouponResponse createCoupon(CreateCouponCommand command) {
        Coupon coupon = couponMapper.toEntity(command);
        Coupon saved = couponRepository.save(coupon);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                couponRedisService.initCoupon(
                        saved.getCode(),
                        saved.getMaxUsages(),
                        saved.getId(),
                        saved.getCountryCode()
                );
            }
        });
        return couponMapper.toResponse(saved);
    }

    @Override
    public RedeemCouponResponse redeemCoupon(RedeemCouponCommand command, String ipAddress) {
        String userCountry = geoLocationService.resolveCountryCode(ipAddress);
        return couponRedemptionHandler.handle(command, ipAddress, userCountry);
    }
}