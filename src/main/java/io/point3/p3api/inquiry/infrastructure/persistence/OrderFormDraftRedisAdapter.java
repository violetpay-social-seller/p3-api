package io.point3.p3api.inquiry.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import io.point3.p3api.inquiry.application.command.CreateOrderFormDraftCommand;
import io.point3.p3api.inquiry.application.draft.OrderFormDraftData;
import io.point3.p3api.inquiry.application.port.OrderFormDraftStorePort;
import io.point3.p3api.inquiry.application.result.OrderFormDraftResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderFormDraftRedisAdapter implements OrderFormDraftStorePort {

    private static final String KEY_PREFIX = "order-form-draft:";
    private static final Duration TTL = Duration.ofMinutes(30);


    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Clock clock;


    @Override
    public OrderFormDraftResult save(CreateOrderFormDraftCommand command) {
        String draftKey = UUID.randomUUID().toString();
        String redisKey = toRedisKey(draftKey);

        OrderFormDraftData draftData = OrderFormDraftData.from(command);
        String payload = write(draftData);

        redisTemplate.opsForValue()
                .set(redisKey, payload,TTL);
        return new OrderFormDraftResult(draftKey,clock.instant().plus(TTL));
    }

    @Override
    public Optional<OrderFormDraftData> findByDraftKey(String draftKey) {
        String payload = redisTemplate.opsForValue().get(toRedisKey(draftKey));

        if (payload == null) {
            return Optional.empty();
        }

        return Optional.of(read(payload));    }

    @Override
    public void delete(String draftKey) {
        redisTemplate.delete(toRedisKey(draftKey));
    }

    private String toRedisKey(String draftKey) {
        return KEY_PREFIX + draftKey;
    }

    private String write(OrderFormDraftData draft) {
        try {
            return objectMapper.writeValueAsString(draft);
        } catch (JsonProcessingException e) {
            throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private OrderFormDraftData read(String payload) {
        try {
            return objectMapper.readValue(payload, OrderFormDraftData.class);
        } catch (JsonProcessingException e) {
            throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
