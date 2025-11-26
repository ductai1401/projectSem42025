package projectSem4.com.service;

import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class PaymentAttemptService {

//    @Autowired
//    private RedisTemplate<String, String> redisTemplate;
//
//    private static final int MAX_ATTEMPTS = 3;      // tối đa 3 lần
//    private static final int TTL_MINUTES = 30;      // trong 30 phút
//
//    public boolean allowPayment(String orderId) {
//        String key = "payment_attempt:" + orderId;
//
//        Long count = redisTemplate.opsForValue().increment(key);
//        if (count == 1) {
//            // Lần đầu thì set TTL cho key
//            redisTemplate.expire(key, TTL_MINUTES, TimeUnit.MINUTES);
//        }
//
//        return count <= MAX_ATTEMPTS;
//    }
}
