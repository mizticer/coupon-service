package pl.couponservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "coupon")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "code")
    private String code;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "max_usages")
    private int maxUsages;

    @Column(name = "current_usages")
    private int currentUsages;

    @Column(name = "country_code")
    private String countryCode;

    public Coupon(String code, int maxUsages, String countryCode) {
        this.code = code.toUpperCase();
        this.maxUsages = maxUsages;
        this.currentUsages = 0;
        this.countryCode = countryCode.toUpperCase();
        this.createdAt = Instant.now();
    }

    public int getRemainingUsages() {
        return maxUsages - currentUsages;
    }
}
