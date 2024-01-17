package com.trace.perf.action;

public interface ErrorAction extends Action {
    void onError(String errorStr);

    default void exec(String line) {
        onError(line);
    }
}
