package cn.smilex.timingwheel;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author smilex
 */
@Slf4j
@Data
public class Task<T> implements Runnable {
    private final T data;
    private final Runnable runnable;

    public Task(T data, Runnable runnable) {
        this.data = data;
        this.runnable = runnable;
    }

    @Override
    public void run() {
        try {
            this.runnable.run();
        } catch (Exception ignore) {

        }
    }
}
