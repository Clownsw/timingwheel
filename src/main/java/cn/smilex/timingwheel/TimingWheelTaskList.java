package cn.smilex.timingwheel;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * 时间轮任务链表 (延迟队列元素)
 *
 * @author siran.yao
 * @author yanglujia
 * @date 2024/1/22/15:12
 */
@Slf4j
public class TimingWheelTaskList implements Delayed {
    /**
     * 过期时间
     */
    private final AtomicLong expiration = new AtomicLong(-1L);

    /**
     * 根节点
     */
    private final TimingWheelTask root = new TimingWheelTask(null, -1L);

    {
        root.prev = root;
        root.next = root;
    }

    /**
     * 设置过期时间
     */
    public boolean setExpiration(long expire) {
        return expiration.getAndSet(expire) != expire;
    }

    /**
     * 获取过期时间
     */
    public long getExpiration() {
        return expiration.get();
    }

    /**
     * 新增任务
     */
    public void addTask(TimingWheelTask timingWheelTask) {
        synchronized (this) {
            if (timingWheelTask.timingWheelTaskList == null) {
                timingWheelTask.timingWheelTaskList = this;
                TimingWheelTask tail = root.prev;
                timingWheelTask.next = root;
                timingWheelTask.prev = tail;
                tail.next = timingWheelTask;
                root.prev = timingWheelTask;
            }
        }
    }

    /**
     * 移除任务
     */
    public void removeTask(TimingWheelTask timingWheelTask) {
        synchronized (this) {
            if (timingWheelTask.timingWheelTaskList.equals(this)) {
                timingWheelTask.next.prev = timingWheelTask.prev;
                timingWheelTask.prev.next = timingWheelTask.next;
                timingWheelTask.timingWheelTaskList = null;
                timingWheelTask.next = null;
                timingWheelTask.prev = null;
            }
        }
    }

    /**
     * 重新分配
     */
    public void flush(Consumer<TimingWheelTask<?>> flush) {
        TimingWheelTask<?> timingWheelTask = root.next;

        while (!timingWheelTask.equals(root)) {
            this.removeTask(timingWheelTask);
            flush.accept(timingWheelTask);
            timingWheelTask = root.next;
        }

        expiration.getAndSet(-1L);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return Math.max(0, unit.convert(expiration.get() - System.currentTimeMillis(), TimeUnit.MILLISECONDS));
    }

    @Override
    public int compareTo(Delayed o) {
        if (o instanceof TimingWheelTaskList) {
            return Long.compare(expiration.get(), ((TimingWheelTaskList) o).expiration.get());
        }
        return 0;
    }
}
