package pl.couponservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.couponservice.model.Coupon;

import java.util.Optional;
import java.util.UUID;

public interface CouponRepository extends JpaRepository<Coupon, UUID> {

    Optional<Coupon> findByCodeIgnoreCase(String code);

    @Modifying
    @Query("UPDATE Coupon c SET c.currentUsages = c.currentUsages + 1 " +
            "WHERE c.id = :id AND c.currentUsages < c.maxUsages")
    int incrementUsage(@Param("id") UUID id);

    @Query("SELECT c.maxUsages - c.currentUsages FROM Coupon c WHERE c.id = :id")
    int getRemainingUsages(@Param("id") UUID id);
}