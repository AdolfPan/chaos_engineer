package com.adolf.chaos.cache.comp;

import com.adolf.chaos.cache.RedisHashHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <br>
 * <p>
 *      description：redis哈希表操作
 * </p>
 *
 * <br>
 *
 * @author mason
 * @version 1.0
 * @date 2021/10/22 上午8:44
 */
@Component
@ConditionalOnExpression("${spring.redis.enableHandler:false}")
public class CacheHashCompRidesImpl implements RedisHashHandler<String,Object> {

    private HashOperations<String, String, Object> hashOperations;

    @Autowired
    protected RedisTemplate<String,Object> redisTemplate;

    @PostConstruct
    private void init(){
        this.hashOperations = redisTemplate.opsForHash();
    }

    @Override
    public void hSet(String key, String hashKey, Object value) {
        hashOperations.put(key,hashKey,value);
    }

    @Override
    public Boolean hSetNx(String key, String hashKey, Object value) {
        return hashOperations.putIfAbsent(key,hashKey,value);
    }

    @Override
    public Object hGet(String key, String hashKey) {
        return hashOperations.get(key,hashKey);
    }

    @Override
    public Boolean hExists(String key, String hashKey) {
        return hashOperations.hasKey(key,hashKey);
    }

    @Override
    public Long hDel(String key, Object...hashKeys) {
        return hashOperations.delete(key,hashKeys);
    }

    @Override
    public Long HLen(String key) {
        return hashOperations.size(key);
    }

    @Override
    public Long hStrLen(String key, String hashKey) {
        return hashOperations.lengthOfValue(key,hashKey);
    }

    @Override
    public Long hIncrBy(String key, String hashKey, Long increment) {
        return hashOperations.increment(key,hashKey,increment);
    }

    @Override
    public Double hIncrByFloat(String key, String hashKey, Double increment) {
        return hashOperations.increment(key,hashKey,increment);
    }

    @Override
    public void hMSet(String key, Map<String, Object> hashKeyValues) {
        hashOperations.putAll(key,hashKeyValues);
    }

    @Override
    public List<Object> hMGet(String key, Collection<String> hashKeys) {
        return hashOperations.multiGet(key,hashKeys);
    }

    @Override
    public Set<String> hKeys(String key) {
        return hashOperations.keys(key);
    }

    @Override
    public List<Object> hVals(String key) {
        return hashOperations.values(key);
    }

    @Override
    public Map<String, Object> hGetAll(String key) {
        return hashOperations.entries(key);
    }

}
