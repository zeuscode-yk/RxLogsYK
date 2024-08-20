package com.blackbox.plog.pLogs.workers;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

/**
 * Created by Umair Adil on 13/03/2017.
 */
public class RetryWithDelay implements Function<Observable<? extends Throwable>, Observable<?>> {

    private final int maxRetries;
    private final int retryDelayMillis;
    private String TAG = "RetryWithDelay";
    private int retryCount;

    public RetryWithDelay(final int maxRetries, final int retryDelayMillis) {
        this.maxRetries = maxRetries;
        this.retryDelayMillis = retryDelayMillis;
        this.retryCount = 0;
    }

    @Override
    public Observable<?> apply(final Observable<? extends Throwable> attempts) {
        return attempts
                .flatMap((Function<Throwable, Observable<?>>) throwable -> {

                    if (++retryCount < maxRetries) {
                        return Observable.timer(retryDelayMillis,
                                TimeUnit.MILLISECONDS);
                    }

                    // Max retries hit. Just pass the error along.
                    return Observable.error(throwable);
                });
    }
}
