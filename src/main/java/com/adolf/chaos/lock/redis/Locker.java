package com.adolf.chaos.lock.redis;

import com.adolf.chaos.abstracts.AbstractLock;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
 * <br>
 * <p>locker bean</p><br>
 *
 * @author panrusen
 * @version 1.0
 * @date 2021/6/2 下午1:54
 */
@Slf4j
public class Locker extends AbstractLock {

    private RLock rLock;

    private Locker(RLock rLock) {
        this.rLock = rLock;
    }

    public Locker(RedissonClient redissonClient, String lockKey) {
        this(redissonClient.getFairLock(lockKey));
    }

    /**
     * 获取锁
     * @return
     */
    @Override
    public boolean tryLock(int waitTime, int leaseTime) {
        String threadName = Thread.currentThread().getName() + ":" + Thread.currentThread().getId();
        String lockKey = this.rLock.getName();
        try {
            boolean result = this.rLock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
            if (result) {
                log.debug("加锁{}成功，当前线程第{}次锁，当前线程：{}", lockKey, rLock.getHoldCount(), threadName);
                return true;
            } else {
                log.warn("加锁{}失败，被其他线程锁住，当前线程：{}", lockKey, threadName);
                return false;
            }
        } catch (InterruptedException e) {
            log.error("加锁{}发生 InterruptedException 异常，保守起见，让其加锁失败，当前线程：{}", lockKey, threadName, e);
            return false;
        }
    }

    @Override
    public void close() {
        String threadName = Thread.currentThread().getName() + ":" + Thread.currentThread().getId();
        String lockKey = this.rLock.getName();
        try {
            this.rLock.unlock();
            log.debug("释放锁{}成功，当前线程：{}", lockKey, threadName);
        } catch (Throwable e) {
            log.error("解锁{}异常,返回false，当前线程：{}", lockKey, threadName, e);
        }
    }
}
