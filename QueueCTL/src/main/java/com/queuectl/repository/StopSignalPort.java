package com.queuectl.repository;

public interface StopSignalPort {
    boolean isSet();

    void set();

    void clear();
}
