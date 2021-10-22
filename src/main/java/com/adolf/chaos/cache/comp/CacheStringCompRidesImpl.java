package com.adolf.chaos.cache.comp;

import com.adolf.chaos.cache.RedisStringHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <br>
 * <p>
 *     redis string opt
 * </p>
 *
 * <br>
 *
 * @author mason
 * @version 1.0
 * @date 2021/10/22 上午8:19
 */
@Component
@ConditionalOnExpression("${spring.redis.enableHandler:false}")
public class CacheStringCompRidesImpl implements RedisStringHandler<String, Object> {

    @Autowired
    protected RedisTemplate<String,Object> redisTemplate;

    private ValueOperations<String, Object> valueOpt;

    @PostConstruct
    private void init() {
        this.valueOpt = redisTemplate.opsForValue();
    }

    @Override
    public void set(String key, Object value) {
        valueOpt.set(key, value);
    }

    @Override
    public void setEx(String key, Object value, long seconds) {
        valueOpt.set(key, value, seconds, TimeUnit.SECONDS);
    }

    @Override
    public void setEx(String key, Object value, long time, TimeUnit timeUnit) {
        valueOpt.set(key, value, time, timeUnit);
    }

    @Override
    public void psetex(String key, Object value, long milliseconds) {
        valueOpt.set(key, value, milliseconds, TimeUnit.MILLISECONDS);
    }

    @Override
    public Boolean setnx(String key, Object value) {
        return valueOpt.setIfPresent(key, value);
    }

    @Override
    public Boolean setNxEx(String key, Object value, Long timeout, TimeUnit timeUnit) {
        return valueOpt.setIfAbsent(key, value, timeout, timeUnit);
    }

    @Override
    public Object get(String key) {
        return valueOpt.get(key);
    }

    @Override
    public Object getSet(String key, Object value) {
        return valueOpt.getAndSet(key, value);
    }

    @Override
    public Long strLen(String key) {
        return valueOpt.size(key);
    }

    @Override
    public Integer append(String key, String value) {
        return valueOpt.append(key, value);
    }

    @Override
    public void setRange(String key, Object value, int offset) {
        valueOpt.set(key, value, offset);
    }

    @Override
    public String getRange(String key, int start, int end) {
        return valueOpt.get(key, start, end);
    }

    @Override
    public Long incr(String key) {
        return valueOpt.increment(key);
    }

    @Override
    public Long incrBy(String key, Long increment) {
        return valueOpt.increment(key, increment);
    }

    @Override
    public Double incrByFloat(String key, float increment) {
        return valueOpt.increment(key, (double) increment);
    }

    @Override
    public Long dncr(String key) {
        return valueOpt.decrement(key);
    }

    @Override
    public Long dncrBy(String key, Long decrement) {
        return valueOpt.decrement(key, decrement);
    }

    @Override
    public void mSet(Map<String, Object> keyValues) {
        valueOpt.multiSet(keyValues);
    }

    @Override
    public void mSetNx(Map<String, Object> keyValues) {
        valueOpt.multiSetIfAbsent(keyValues);
    }

    @Override
    public List<Object> mGet(List<String> keys) {
        return valueOpt.multiGet(keys);
    }
}
