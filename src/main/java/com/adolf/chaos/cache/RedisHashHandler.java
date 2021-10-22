package com.adolf.chaos.cache;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <br>
 * <p>
 *      description：redis 哈希表结构相关操作
 * </p>
 *
 * <br>
 *
 * @author mason
 * @version 1.0
 * @date 2021/10/22 上午8:43
 */
public interface RedisHashHandler<K,V> {

    /**
     * 将指定key对应的hash表中域hashKey对应的值设置为value，如果key不存在则新建hash表，如果value已存在则覆盖掉就得value值
     * @param key
     * @param hashKey
     * @param value
     */
    void hSet(String key, K hashKey, V value);

    /**
     * 将指定key对应的hash表中域hashKey对应的值设置为value，如果key不存在则新建hash表，如果value已存在则不作任何事情
     * @param key
     * @param hashKey
     * @param value
     */
    Boolean hSetNx(String key, K hashKey, V value);

    /**
     * 获取指定key对应的hash表中域为hashKey的值
     * @param key
     * @param hashKey
     * @return
     */
    V hGet(String key, K hashKey);

    /**
     * 判断指定key所对应的hash表中是否存在hashKey这样的域，存在返回true，不存在返回false
     * @param key
     * @param hashKey
     * @return
     */
    Boolean hExists(String key, K hashKey);

    /**
     * 删除指定key所对应的hash表中指定的一个或多个hashKey对应的域
     * @param key
     * @param hashKeys
     * @return
     */
    Long hDel(String key, Object... hashKeys);

    /**
     * 返回指定key所对应hash表中域的数量
     * @param key
     * @return
     */
    Long HLen(String key);

    /**
     * 返回指定key所对应的hash表中指定hashKey对应的value字符串的长度，当key或者hashKey不存在是返回0
     * @param key
     * @param hashKey
     * @return
     */
    Long hStrLen(String key, K hashKey);

    /**
     * 给指定key所对应的hashKey对应的值增加定点数increment，如果对应的值无法转换成数字，则报错
     * @param key
     * @param hashKey
     * @param increment
     * @return
     */
    Long hIncrBy(String key, K hashKey, Long increment);

    /**
     * 给指定key所对应的hashKey对应的值增加浮点数increment，如果对应的值无法转换成数字，则报错
     * @param key
     * @param hashKey
     * @param increment
     * @return
     */
    Double hIncrByFloat(String key, K hashKey, Double increment);

    /**
     * 同时将多个key-value设置到hash表中
     * @param key
     * @param hashKeyValues
     */
    void hMSet(String key, Map<K, V> hashKeyValues);

    /**
     * 批量获取指定key对应的hash表中给定的hashKeys 对应的值
     * @param key
     * @param hashKeys
     * @return
     */
    List<V> hMGet(String key, Collection<K> hashKeys);

    /**
     * 返回指定key对应hash表中所有的hashKey
     * @param key
     * @return
     */
    Set<K> hKeys(String key);

    /**
     * 返回指定key对应hash表中所有的value
     * @param key
     * @return
     */
    List<V> hVals(String key);

    /**
     * 返回指定key所对应hash表中所有的键值对
     * @param key
     */
    Map<K,V> hGetAll(String key);

}
