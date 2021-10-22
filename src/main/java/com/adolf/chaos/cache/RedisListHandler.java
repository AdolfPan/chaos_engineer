package com.adolf.chaos.cache;

import java.util.List;

/**
 * <br>
 * <p>
 *      redis链表操作handler
 * </p>
 *
 * <br>
 *
 * @author mason
 * @version 1.0
 * @date 2021/10/22 上午8:30
 */
public interface RedisListHandler<K,V> {

    /**
     * lPush
     * @param key
     * @param value
     * @return
     */
    Long lPush(K key, V value);

    /**
     * lPushX
     * @param key
     * @param value
     * @return
     */
    Long lPushX(K key, V value);

    /**
     * lPushAll
     * @param key
     * @param values
     * @return
     */
    Long lPushAll(K key, V... values);

    /**
     * rPush
     * @param key
     * @param value
     * @return
     */
    Long rPush(K key, V value);

    /**
     * rPushX
     * @param key
     * @param value
     * @return
     */
    Long rPushX(K key, V value);

    /**
     * rPushAll
     * @param key
     * @param values
     * @return
     */
    Long rPushAll(K key, V... values);

    /**
     * lPop
     * @param key
     * @return
     */
    V lPop(K key);

    /**
     * rPop
     * @param key
     * @return
     */
    V rPop(K key);

    /**
     * bLPop
     * @param key
     * @return
     */
    V bLPop(K key);

    /**
     * bRPop
     * @param key
     * @return
     */
    V bRPop(K key);

    /**
     * rPopLPush
     * @param key1
     * @param key2
     * @return
     */
    V rPopLPush(String key1, String key2);

    /**
     * bRPopLPush
     * @param key1
     * @param key2
     * @return
     */
    V bRPopLPush(String key1, String key2);

    /**
     * lRem
     * @param key
     * @param count
     * @param value
     * @return
     */
    Long lRem(K key, long count, Object value);

    /**
     * lLen
     * @param key
     * @return
     */
    Long lLen(K key);

    /**
     * lIndex
     * @param key
     * @param index
     * @return
     */
    V lIndex(K key, int index);

    /**
     * lSet
     * @param key
     * @param index
     * @param value
     */
    void lSet(K key, int index, V value);

    /**
     * lRange
     * @param key
     * @param start
     * @param end
     * @return
     */
    List<V> lRange(K key, int start, int end);

    /**
     * lTrim
     * @param key
     * @param start
     * @param end
     */
    void lTrim(K key, int start, int end);

}
