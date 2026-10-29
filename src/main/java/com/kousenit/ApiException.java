package com.kousenit;

/** An HTTP response from an AI provider that was not 2xx. The status lets callers decide what to do about it. */
public class ApiException extends RuntimeException {
    private final int status;

    public ApiException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int status() {
        return status;
    }

    public boolean isRateLimited() {
        return status == 429;
    }
}
