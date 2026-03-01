package pl.couponservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import pl.couponservice.exception.CouponAlreadyUsedByUserException;
import pl.couponservice.exception.CouponCountryMismatchException;
import pl.couponservice.exception.CouponExhaustedException;
import pl.couponservice.exception.NotFoundException;
import pl.couponservice.model.Coupon;
import pl.couponservice.model.CouponUsage;
import pl.couponservice.model.command.RedeemCouponCommand;
import pl.couponservice.model.dto.RedeemCouponResponse;
import pl.couponservice.model.mapper.CouponMapper;
import pl.couponservice.repository.CouponRepository;
import pl.couponservice.repository.CouponUsageRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CouponRedemptionHandler {

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;
    private final CouponRedisService couponRedisService;
    private final CouponMapper couponMapper;

    @Transactional
    public RedeemCouponResponse handle(RedeemCouponCommand command, String ipAddress, String userCountry) {
        String code = command.code();
        String userId = command.userId();
        String upperCode = code.toUpperCase();

        // 1. Fallback: jesli nie ma w Redis, zaladuj z DB
        if (!couponRedisService.couponExists(upperCode)) {
            Coupon fallback = couponRepository.findByCodeIgnoreCase(code)
                    .orElseThrow(() -> new NotFoundException("Coupon not found: " + code));
            couponRedisService.initCoupon(
                    fallback.getCode(),
                    fallback.getRemainingUsages(),
                    fallback.getId(),
                    fallback.getCountryCode()
            );
        }

        // 2. Walidacja kraju z Redis - zero DB query
        String couponCountry = couponRedisService.getCountryCode(upperCode)
                .orElseThrow(() -> new NotFoundException("Coupon not found: " + code));

        if (!couponCountry.equalsIgnoreCase(userCountry)) {
            throw new CouponCountryMismatchException(couponCountry, userCountry);
        }

        // 3. Atomowy DECR - sprawdz limit
        if (!couponRedisService.tryDecrement(upperCode)) {
            throw new CouponExhaustedException(code);
        }

        // 4. Atomowy SADD - sprawdz duplikat usera
        if (!couponRedisService.tryAddUser(upperCode, userId)) {
            couponRedisService.rollbackDecrement(upperCode);
            throw new CouponAlreadyUsedByUserException(code, userId);
        }

        // 5. Rollback Redis jesli DB commit sie nie powiedzie
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != TransactionSynchronization.STATUS_COMMITTED) {
                    couponRedisService.rollbackDecrement(upperCode);
                    couponRedisService.rollbackUser(upperCode, userId);
                }
            }
        });

        // 6. Pobierz ID kuponu z Redis i zrob atomic UPDATE
        UUID couponId = couponRedisService.getCouponId(upperCode)
                .orElseThrow(() -> new NotFoundException("Coupon not found: " + code));

        int updatedRows = couponRepository.incrementUsage(couponId);
        if (updatedRows == 0) {
            throw new CouponExhaustedException(code);
        }

        // 7. Zapisz usage - potrzebujemy encji Coupon jako FK
        Coupon coupon = couponRepository.getReferenceById(couponId);
        CouponUsage usage = new CouponUsage(coupon, userId, ipAddress);
        couponUsageRepository.save(usage);

        // 8. Swiezy remaining z DB
        int remaining = couponRepository.getRemainingUsages(couponId);
        return couponMapper.toRedeemResponse(coupon, usage, remaining);
    }
}