package cn.smilex.timingwheel;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

/**
 * @author smilex
 */
@Slf4j
@Data
public class Task<T, K> implements Runnable {
    private final T data;
    private final K userData;
    private final Consumer<K> runnable;

    public Task(T data, K userData, Consumer<K> runnable) {
        this.data = data;
        this.userData = userData;
        this.runnable = runnable;
    }

    @Override
    public void run() {
        try {
            this.runnable.accept(this.userData);
        } catch (Exception ignore) {
        }
    }
}
