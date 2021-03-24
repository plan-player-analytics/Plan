package com.djrapitops.plan.utilities;

import java.util.concurrent.atomic.AtomicInteger;

public class SemaphoreAccessCounter {

    private final AtomicInteger accessCounter;
    private final Object lockObject;

    public SemaphoreAccessCounter() {
        accessCounter = new AtomicInteger(0);
        lockObject = new Object();
    }

    public void enter() {
        accessCounter.incrementAndGet();
    }

    public void exit() {
        synchronized (lockObject) {
            int value = accessCounter.decrementAndGet();
            if (value == 0) {
                lockObject.notifyAll();
            }
        }
    }

    public void waitUntilNothingAccessing() {
        while (accessCounter.get() > 0) {
            synchronized (lockObject) {
                try {
                    lockObject.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
