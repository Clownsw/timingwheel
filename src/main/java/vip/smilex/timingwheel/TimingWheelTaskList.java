package vip.smilex.timingwheel;

import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.LinkedList;
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
public final class TimingWheelTaskList implements Delayed {

    /**
     * 过期时间
     */
    private final AtomicLong expiration = new AtomicLong(-1L);

    /**
     * 时间轮任务链表
     */
    private final LinkedList<TimingWheelTask> timingWheelTasks = new LinkedList<>();

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
     * 添加时间轮任务到任务链表中
     *
     * @param timingWheelTask 时间轮任务
     * @author yanglujia
     * @date 2024/1/23 11:08:23
     */
    public void addTask(final TimingWheelTask timingWheelTask) {
        synchronized (this) {
            if (timingWheelTask.timingWheelTaskList == null) {
                timingWheelTask.timingWheelTaskList = this;

                timingWheelTasks.add(timingWheelTask);
            }
        }
    }

    /**
     * 将过期任务弹出
     *
     * @param flush 弹出函数
     * @author yanglujia
     * @date 2024/1/23 11:07:50
     */
    public void flush(Consumer<TimingWheelTask> flush) {
        final Iterator<TimingWheelTask> iterator = this.timingWheelTasks.iterator();

        while (iterator.hasNext()) {
            final TimingWheelTask task = iterator.next();

            synchronized (this) {
                task.timingWheelTaskList = null;

                iterator.remove();
            }

            flush.accept(task);
        }

        expiration.getAndSet(-1L);
    }

    /**
     * 获取剩余延迟时间(小于等于0则表示可以被执行)
     *
     * @param unit 时间单位
     * @return 剩余延迟时间
     * @author yanglujia
     * @date 2024/1/23 11:06:38
     */
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
