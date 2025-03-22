package org.craftarix.monitoring.util;

public interface TaskHandle {
    void cancel();

    boolean isCancelled();
}