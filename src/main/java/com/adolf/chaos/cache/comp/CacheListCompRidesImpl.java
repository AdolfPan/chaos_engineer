package com.adolf.chaos.cache.comp;

import com.adolf.chaos.cache.RedisListHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <br>
 * <p>
 *      description：redis链表操作component
 * </p>
 *
 * <br>
 *
 * @author mason
 * @version 1.0
 * @date 2021/10/22 上午8:35
 */
@Component
@ConditionalOnExpression("${spring.redis.enableHandler:false}")
public class CacheListCompRidesImpl implements RedisListHandler<String, Object> {

    private ListOperations<String, Object> listOperations;

    @Autowired
    protected RedisTemplate<String,Object> redisTemplate;

    @PostConstruct
    private void init(){
        listOperations = redisTemplate.opsForList();
    }

    @Override
    public Long lPush(String key, Object value) {
        return listOperations.leftPush(key,value);
    }

    @Override
    public Long lPushX(String key, Object value) {
        return listOperations.leftPushIfPresent(key,value);
    }

    @Override
    public Long lPushAll(String key, Object... values) {
        return listOperations.leftPushAll(key,values);
    }

    @Override
    public Long rPush(String key, Object value) {
        return listOperations.rightPush(key,value);
    }

    @Override
    public Long rPushX(String key, Object value) {
        return listOperations.rightPushIfPresent(key,value);
    }

    @Override
    public Long rPushAll(String key, Object... values) {
        return listOperations.rightPushAll(key,values);
    }

    @Override
    public Object lPop(String key) {
        return listOperations.leftPop(key);
    }

    @Override
    public Object rPop(String key) {
        return listOperations.rightPop(key);
    }

    @Override
    public Object bLPop(String key) {
        return listOperations.leftPop(key,0, TimeUnit.SECONDS);
    }

    @Override
    public Object bRPop(String key) {
        return listOperations.rightPop(key,0, TimeUnit.SECONDS);
    }

    @Override
    public Object rPopLPush(String key1, String key2) {
        return listOperations.rightPopAndLeftPush(key1,key2);
    }

    @Override
    public Object bRPopLPush(String key1, String key2) {
        return listOperations.rightPopAndLeftPush(key1,key2,0,TimeUnit.SECONDS);
    }

    @Override
    public Long lRem(String key, long count, Object value) {
        return listOperations.remove(key,count,value);
    }

    @Override
    public Long lLen(String key) {
        return listOperations.size(key);
    }

    @Override
    public Object lIndex(String key, int index) {
        return listOperations.index(key,index);
    }

    @Override
    public void lSet(String key, int index, Object value) {
        listOperations.set(key,index,value);
    }

    @Override
    public List<Object> lRange(String key, int start, int end) {
        return listOperations.range(key,start,end);
    }

    @Override
    public void lTrim(String key, int start, int end) {
        listOperations.trim(key,start,end);
    }
}
