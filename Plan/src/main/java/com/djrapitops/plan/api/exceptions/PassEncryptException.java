package com.djrapitops.plan.api.exceptions;

public class PassEncryptException extends Exception {

    public PassEncryptException() {
    }

    public PassEncryptException(String s) {
        super(s);
    }

    public PassEncryptException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public PassEncryptException(Throwable throwable) {
        super(throwable);
    }

    public PassEncryptException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
