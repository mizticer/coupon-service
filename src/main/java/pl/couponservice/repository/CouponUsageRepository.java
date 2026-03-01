package pl.couponservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.couponservice.model.Coupon;
import pl.couponservice.model.CouponUsage;

import java.util.List;
import java.util.UUID;

public interface CouponUsageRepository extends JpaRepository<CouponUsage, UUID> {

    List<CouponUsage> findAllByCoupon(Coupon coupon);
}

