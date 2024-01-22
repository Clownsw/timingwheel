package vip.smilex.timingwheel;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

/**
 * 时间轮任务操作
 *
 * @author yanglujia
 * @date 2024/1/22/15:07
 */
@Slf4j
@Data
public final class TimingWheelTaskAction<T, K> implements Runnable {
    private final T data;
    private final K userData;
    private final Consumer<K> runnable;

    public TimingWheelTaskAction(T data, K userData, Consumer<K> runnable) {
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
