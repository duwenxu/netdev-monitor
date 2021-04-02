package com.xy.netdev.network.retry;

import io.netty.util.concurrent.FastThreadLocal;

public interface RetryPolicy {

    /**
     * 是否可以重连
     * @param retryCount
     * @return
     */
    boolean allowRetry(FastThreadLocal<Integer> retryCount);

    /**
     * 获取重连需要等待的时间
     * @param retryCount
     * @return
     */
    long getSleepTimeMs(FastThreadLocal<Integer> retryCount);

    int getMaxRetries();
}
