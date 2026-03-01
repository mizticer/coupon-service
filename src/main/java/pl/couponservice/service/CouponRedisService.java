package pl.couponservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CouponRedisService {
    private static final String REMAINING_KEY = "coupon:%s:remaining";
    private static final String META_KEY = "coupon:%s:meta";
    private static final String USERS_KEY = "coupon:%s:users";

    private static final String FIELD_ID = "id";
    private static final String FIELD_COUNTRY = "country";

    private final StringRedisTemplate redisTemplate;

    public void initCoupon(String code, int remaining, UUID id, String countryCode) {
        String upperCode = code.toUpperCase();
        redisTemplate.opsForHash().putAll(metaKey(upperCode), Map.of(
                FIELD_ID, id.toString(),
                FIELD_COUNTRY, countryCode.toUpperCase()
        ));
        redisTemplate.opsForValue().setIfAbsent(remainingKey(upperCode), String.valueOf(remaining));
    }

    public boolean couponExists(String code) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(remainingKey(code.toUpperCase())));
    }

    public Optional<String> getCountryCode(String code) {
        Object country = redisTemplate.opsForHash().get(metaKey(code.toUpperCase()), FIELD_COUNTRY);
        return Optional.ofNullable(country).map(Object::toString);
    }

    public Optional<UUID> getCouponId(String code) {
        Object id = redisTemplate.opsForHash().get(metaKey(code.toUpperCase()), FIELD_ID);
        return Optional.ofNullable(id).map(o -> UUID.fromString(o.toString()));
    }

    public boolean tryDecrement(String code) {
        Long remaining = redisTemplate.opsForValue().decrement(remainingKey(code.toUpperCase()));
        if (remaining == null || remaining < 0) {
            rollbackDecrement(code);
            return false;
        }
        return true;
    }

    public void rollbackDecrement(String code) {
        redisTemplate.opsForValue().increment(remainingKey(code.toUpperCase()));
    }

    public boolean tryAddUser(String code, String userId) {
        Long added = redisTemplate.opsForSet().add(usersKey(code.toUpperCase()), userId);
        return added != null && added > 0;
    }

    public void rollbackUser(String code, String userId) {
        redisTemplate.opsForSet().remove(usersKey(code.toUpperCase()), userId);
    }

    private String remainingKey(String upperCode) {
        return String.format(REMAINING_KEY, upperCode);
    }

    private String metaKey(String upperCode) {
        return String.format(META_KEY, upperCode);
    }

    private String usersKey(String upperCode) {
        return String.format(USERS_KEY, upperCode);
    }
}
