package com.adolf.chaos.cache;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <br>
 * <p>redis字符串操作</p>
 *
 * <br>
 *
 * @author mason
 * @version 1.0
 * @date 2021/10/22 上午8:16
 */
public interface RedisStringHandler<K, V> {

    /**
     * 将指定key的值设置为value，如果key已经有其他值，则覆盖掉旧值
     *
     * @param key
     * @param value
     */
    void set(K key, V value);

    /**
     * 将指定key的值设置为value，并加上过期时间timeout，单位秒
     *
     * @param key
     * @param value
     * @param seconds
     */
    void setEx(K key, V value, long seconds);

    /**
     * 将指定key的值设置为value，并加上过期时间timeout，单位自定义
     * @param key
     * @param value
     * @param time
     * @param timeUnit
     */
    void setEx(K key, V value, long time, TimeUnit timeUnit);

    /**
     * 将指定key的值设置为value，并加上过期时间timeout，单位毫秒
     *
     * @param key
     * @param value
     * @param milliseconds
     */
    void psetex(K key, V value, long milliseconds);

    /**
     * 将指定key的值设置为value，如果指定的key不存在，则将key的值设置为value，如果key已经存在则不做任何操作，故而该命令常被用来实现分布式锁
     *
     * @param key
     * @param value
     * @return
     */
    Boolean setnx(K key, V value);

    /**
     * 将指定key的值设置为value，如果指定的key不存在，则将key的值设置为value，如果key已经存在则不做任何操作，故而该命令常被用来实现分布式锁
     * 时间单位自定义
     * @param key
     * @param value
     * @param timeout
     * @param timeUnit
     * @return
     */
    Boolean setNxEx(K key, V value, Long timeout, TimeUnit timeUnit);

    /**
     * 获取指定key对应的值，如果key不存在返回null，如果key不是字符串类型，该操作就会报错
     *
     * @param key
     * @return
     */
    V get(K key);

    /**
     * 将键 key 的值设为 value ，如果key不存在返回null，如果key不是字符串类型，该操作就会报错
     *
     * @param key
     * @return
     */
    V getSet(K key, V value);

    /**
     * 返回key所对应的字符串的长度，如果key不存在返回0，如果存储的值不是字符串则报错
     *
     * @param key
     * @return
     */
    Long strLen(K key);

    /**
     * 将value追加到key所对应的旧值后面，返回追加后value的长度
     *
     * @param key
     * @param value
     * @return
     */
    Integer append(K key, String value);

    /**
     * 从指定key的指定偏移量开始，覆盖key储存的字符串。如果key不存在，当做空白字符串处理
     * notes：
     * 1，如果offset大于原来value长度，则中间空白部分用空字节填充，在使用get方法获取时，显示的值是不显示空字节的，
     * 但是调用size方法返回的长度是统计空字节长度的
     * 2，因为redis限制字符串的大小是512M以内，所以offset最大值2^29-1(536870911)
     *
     * @param key
     * @param value
     * @param offset
     */
    void setRange(K key, V value, int offset);

    /**
     * \
     * 返回键 key 储存的字符串值的指定部分， 字符串的截取范围由 start 和 end 两个偏移量决定 (包括 start 和 end 在内)。
     * <p>
     * 负数偏移量表示从字符串的末尾开始计数， -1 表示最后一个字符， -2 表示倒数第二个字符， 以此类推。
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    String getRange(K key, int start, int end);

    /**
     * 为指定的key的value加1，如果key不存在，则key对应的值先初始化成0再执行加1操作
     * 如果key对应的值不是数字，则报错
     *
     * @param key
     * @return
     */
    Long incr(K key);

    /**
     * 为指定的key的value加指定的数值increment，increment是整数。如果key不存在，则key对应的值先初始化成0再执行加1操作
     * *  如果key对应的值不是数字，则报错
     *
     * @param key
     * @param increment
     * @return
     */
    Long incrBy(K key, Long increment);

    /**
     * 为指定的key的value加指定的数值increment，increment是浮点数。如果key不存在，则key对应的值先初始化成0再执行加increment操作
     * *  如果key对应的值不是数字，则报错。计算结果最多只保留小数点的后十七位
     *
     * @param key
     * @param increment
     * @return
     */
    Double incrByFloat(K key, float increment);

    /**
     * 为指定的key的value减1，如果key不存在，则key对应的值先初始化成0再执行减1操作
     * 如果key对应的值不是数字，则报错
     *
     * @param key
     * @return
     */
    Long dncr(K key);

    /**
     * 为指定的key的value减去指定的数值decrement ，increment是整数。如果key不存在，则key对应的值先初始化成0再执行减decrement 操作
     * *  如果key对应的值不是数字，则报错
     *
     * @param key
     * @param decrement
     * @return
     */
    Long dncrBy(K key, Long decrement);

    /**
     * 批量给多个键设置值
     *
     * @param keyValues
     */
    void mSet(Map<K, V> keyValues);

    /**
     * 原子操作，要么全部成功，要么全部失败
     * 批量给多个键设置值,当且仅当所有给定键都不存在时， 为所有给定键设置值。
     * 即使只有一个给定键已经存在， MSETNX 命令也会拒绝执行对所有键的设置操作。
     *
     * @param keyValues
     */
    void mSetNx(Map<K, V> keyValues);

    /**
     * 批量查找
     *
     * @param keys
     * @return
     */
    List<V> mGet(List<K> keys);
}
