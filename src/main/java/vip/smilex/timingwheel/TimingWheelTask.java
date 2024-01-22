package vip.smilex.timingwheel;

import lombok.Data;
import lombok.Getter;

/**
 * 时间轮内部任务
 *
 * @author siran.yao
 * @author yanglujia
 * @date 2020/5/8:上午11:13
 */
@Data
public class TimingWheelTask<T> {
    /**
     * 延迟时间
     */
    @Getter
    private final long delayMs;

    /**
     * 任务
     */
    @Getter
    private final Runnable task;

    /**
     * 时间槽
     */
    protected TimingWheelTaskList timingWheelTaskList;

    /**
     * 下一个节点
     */
    protected TimingWheelTask<T> next;

    /**
     * 上一个节点
     */
    protected TimingWheelTask<T> prev;

    public TimingWheelTask(Runnable task, long delayMs) {
        this.delayMs = System.currentTimeMillis() + delayMs;
        this.task = task;
        this.timingWheelTaskList = null;
        this.next = null;
        this.prev = null;
    }
}
