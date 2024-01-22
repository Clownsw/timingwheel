package vip.smilex.timingwheel;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 时间轮的包装
 *
 * @author siran.yao
 * @author yanglujia
 * @date 2024/1/22/14:59
 */
@Slf4j
public final class TimingWheelWrapper {

    /**
     * 底层时间轮
     */
    private final TimingWheel timeWheel;

    /**
     * 一个Timer只有一个delayQueue
     */
    private final DelayQueue<TimingWheelTaskList> delayQueue = new DelayQueue<>();

    /**
     * 过期任务执行线程
     */
    private final ExecutorService workerThreadPool;

    private final ReentrantLock lock = new ReentrantLock();

    /**
     * 构造函数
     */
    public TimingWheelWrapper() {
        this.timeWheel = new TimingWheel(1, 20, System.currentTimeMillis(), delayQueue);

        this.workerThreadPool = Executors.newFixedThreadPool(4);

        // 20ms获取一次过期任务
        final Thread bossThread = new Thread(() -> {
            while (true) {
                try {
                    // 20ms获取一次过期任务
                    this.advanceClock(20);
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        });

        bossThread.start();
    }

    /**
     * 添加任务
     *
     * @param timingWheelTask 任务实例
     * @author yanglujia
     * @date 2024/1/22 18:13:44
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void addTask(final TimingWheelTask<?> timingWheelTask) {
        try {
            this.lock.lock();
            // 添加失败任务直接执行
            if (!timeWheel.addTask(timingWheelTask)) {
                if (timingWheelTask.getTask() instanceof TimingWheelTaskAction) {
                    try {
                        TimingWheelTaskAction<TimingWheelCronTask<?>, ?> tmpTimingWheelTaskAction = (TimingWheelTaskAction<TimingWheelCronTask<?>, ?>) timingWheelTask.getTask();

                        TimingWheelTaskAction<TimingWheelCronTask<?>, ?> nextTimingWheelTaskAction = new TimingWheelTaskAction(tmpTimingWheelTaskAction.getData(), tmpTimingWheelTaskAction.getUserData(), tmpTimingWheelTaskAction.getRunnable());
                        TimingWheelTask<TimingWheelCronTask<?>> nextTimingWheelTask = new TimingWheelTask<>(nextTimingWheelTaskAction, tmpTimingWheelTaskAction.getData().nextDelayMs());
                        addTask(nextTimingWheelTask);
                    } catch (Exception e) {
                        log.error("", e);
                    }
                }

                workerThreadPool.submit(timingWheelTask.getTask());
            }
        } catch (Exception e) {
            log.error("", e);
        } finally {
            this.lock.unlock();
        }
    }

    /**
     * 推进时间轮并弹出过期任务
     *
     * @param timeout 拉取过程超时时间
     * @author yanglujia
     * @date 2024/1/22 18:14:20
     */
    private void advanceClock(@SuppressWarnings("SameParameterValue") final long timeout) {
        try {
            this.lock.lock();

            final TimingWheelTaskList timingWheelTaskList;

            if ((timingWheelTaskList = delayQueue.poll(timeout, TimeUnit.MILLISECONDS)) != null) {
                // 推进时间
                timeWheel.advanceClock(timingWheelTaskList.getExpiration());
                // 执行过期任务（包含降级操作）
                timingWheelTaskList.flush(this::addTask);
            }
        } catch (Exception e) {
            log.error("", e);
        } finally {
            this.lock.unlock();
        }
    }
}