package cn.smilex.timingwheel;

import lombok.Getter;

/**
 * @author siran.yao
 * @date 2020/5/8:上午11:13
 */
public class TimerTask<T> {
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
    protected TimerTaskList timerTaskList;

    /**
     * 下一个节点
     */
    protected TimerTask<T> next;

    /**
     * 上一个节点
     */
    protected TimerTask<T> prev;

    /**
     * 描述
     */
    public String desc;

    public TimerTask(Runnable task, long delayMs) {
        this.delayMs = System.currentTimeMillis() + delayMs;
        this.task = task;
        this.timerTaskList = null;
        this.next = null;
        this.prev = null;
    }

    @Override
    public String toString() {
        return desc;
    }
}
