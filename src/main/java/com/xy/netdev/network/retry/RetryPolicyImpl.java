package com.xy.netdev.network.retry;


import io.netty.util.concurrent.FastThreadLocal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 *
 * @author cc
 */
@Slf4j
public class RetryPolicyImpl implements RetryPolicy {
    /**
     * 最大可以重连的次数
     */
    private static final int MAX_RETRY_LIMIT = Integer.MAX_VALUE;
    /**
     * 默认重连最长的等待时间
     */
    private static final int DEFAULT_MAX_SLEEP_MS = Integer.MAX_VALUE;

    /**
     * 默认睡眠时间
     */
    private static final int BASE_SLEEP_TIME = 1000;

    private final SecureRandom random = new SecureRandom();

    private final long baseSleepTimeMs;
    private final int maxRetries;
    private final int maxSleepMs;

    public RetryPolicyImpl() {
        this(BASE_SLEEP_TIME, MAX_RETRY_LIMIT, DEFAULT_MAX_SLEEP_MS);
    }


    public RetryPolicyImpl(int baseSleepTimeMs, int maxRetries) {
        this(baseSleepTimeMs, maxRetries, DEFAULT_MAX_SLEEP_MS);
    }

    public RetryPolicyImpl(int baseSleepTimeMs, int maxRetries, int maxSleepMs) {
        this.maxRetries = maxRetries;
        this.baseSleepTimeMs = baseSleepTimeMs;
        this.maxSleepMs = maxSleepMs;
    }


    @Override
    public boolean allowRetry(FastThreadLocal<Integer> retryCount) {
        return retryCount.get() < maxRetries;
    }

    @Override
    public long getSleepTimeMs(FastThreadLocal<Integer> retryCount) {
        if (retryCount.get() < 0) {
            throw new IllegalArgumentException("重试次数必须大于0");
        }
        if(retryCount.get() > MAX_RETRY_LIMIT){
            retryCount.set(MAX_RETRY_LIMIT);
        }
        //long sleepMs = baseSleepTimeMs * Math.max(1, random.nextInt(1 << retryCount.get()));
        long sleepMs = 1000L;
        if(sleepMs > maxSleepMs){
            sleepMs = maxSleepMs;
        }
        retryCount.set(retryCount.get() + 1);
        return sleepMs;
    }

    @Override
    public int getMaxRetries() {
        return maxRetries;
    }
}