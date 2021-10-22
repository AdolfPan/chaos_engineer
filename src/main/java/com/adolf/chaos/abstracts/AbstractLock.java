package com.adolf.chaos.abstracts;

import java.io.Closeable;

/**
 * <br>
 * <p>
 *     lock base class
 * </p>
 *
 * <br>
 * @author mason
 * @version 1.0
 * @date 2021/9/30 下午6:01
 */
public abstract class AbstractLock implements Closeable {

    public boolean tryLock() {
        throw new RuntimeException("不支持的操作");
    }

    public boolean tryLock(int waitTime, int leaseTime) {
        throw new RuntimeException("不支持的操作");
    }

}
