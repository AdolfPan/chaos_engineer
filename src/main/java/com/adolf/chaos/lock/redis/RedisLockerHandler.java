package com.adolf.chaos.lock.redis;

import com.google.common.base.Joiner;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

/**
 * <br>
 * <p>
 *     基于redisson框架实现redis分布式锁handle
 * </p>
 * <br>
 *
 * @author mason
 * @version 1.0
 * @date 2021/6/2 下午1:56
 */
@Slf4j
public class RedisLockerHandler {

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 获取 redis 锁 key值
     * @param lockKeys
     * @return
     */
    public String buildLockKey(@NonNull String...lockKeys) {
        return Joiner.on("-").join(lockKeys);
    }

    /**
     * 获取 redis 锁，try-with-resources方式
     * @param lockKeys
     * @return
     */
    public Locker tryLock(String...lockKeys) {
        return tryLock(Joiner.on("-").join(lockKeys));
    }

    /**
     * 获取 redis 锁，try-with-resources方式
     *     默认配置：等待12秒，锁后10秒过期
     * @param lockKey
     * @return
     */
    public Locker tryLock(String lockKey) {
        return tryLock(lockKey, 12, 10);
    }

    /**
     * 获取 redis 锁
     * @param lockKey key
     * @param waitTime  等待时间
     * @param leaseTime 锁租约时间
     * @return
     */
    public Locker tryLock(String lockKey, int waitTime, int leaseTime) {
        Locker locker = new Locker(redissonClient, lockKey);
        if(!locker.tryLock(waitTime, leaseTime)){
            log.error("获取分布式锁失败：{}，超时：{}", lockKey, waitTime);
        }
        return locker;
    }

    /**
     * 获取 redis 锁，等待12秒，锁后10秒过期
     * @param lockKey
     * @return
     */
    public void lockTenSecond(String lockKey) {
        boolean lockSucceed = lock(lockKey, 12, 10);
        //等待12秒仍锁失败，则抛异常
        if(!lockSucceed){
            log.error("获取分布式锁超时失败：{}", lockKey);
        }
    }

    /**
     * 释放 redis 锁
     * @param lockKey
     * @return
     */
    public boolean releaseLock(String lockKey) {
        String threadName = Thread.currentThread().getName() + ":" + Thread.currentThread().getId();
        try {
            RLock rLock = redissonClient.getFairLock(lockKey);
            rLock.unlock();
            log.debug("释放锁{}成功，当前线程：{}", lockKey, threadName);
            if(!rLock.isLocked()){
                log.debug("已删除锁{}成功，当前线程：{}", lockKey, threadName);
                return true;
            }else if(rLock.isHeldByCurrentThread()){
                log.debug("未删除锁{}，还需当前线程解锁{}次，当前线程：{}", lockKey, rLock.getHoldCount(), threadName);
                return true;
            }else{
                log.warn("未删除锁{}，还需其他线程解锁{}次，当前线程：{}", lockKey, rLock.getHoldCount(), threadName);
                return false;
            }
        } catch (Throwable e) {
            log.error("解锁{}异常,返回false，当前线程：{}", lockKey, threadName, e);
            return false;
        }
    }

    /**
     * 获取 redis 锁
     * @param lockKey
     * @return
     */
    private boolean lock(String lockKey, int wait_time, int expire_time) {
        String threadName = Thread.currentThread().getName() + ":" + Thread.currentThread().getId();
        try {
            RLock rLock = redissonClient.getFairLock(lockKey);
            boolean result = rLock.tryLock(wait_time, expire_time, TimeUnit.SECONDS);
            if (result) {
                log.debug("加锁{}成功，当前线程第{}次锁，当前线程：{}", lockKey, rLock.getHoldCount(), threadName);
                return true;
            } else {
                log.warn("加锁{}失败，被其他线程锁住，当前线程：{}", lockKey, threadName);
                return false;
            }
        } catch (InterruptedException e) {
            log.error("加锁{}异常，保守起见，让其加锁失败，当前线程：{}", lockKey, threadName, e);
            return false;
        }
    }

}
