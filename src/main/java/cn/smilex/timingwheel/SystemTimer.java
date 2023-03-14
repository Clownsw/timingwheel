package cn.smilex.timingwheel;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author siran.yao
 * @date 2020/5/8:下午1:13
 * 对时间轮的包装
 */
@SuppressWarnings("InfiniteLoopStatement")
@Slf4j
public class SystemTimer {
    /**
     * 底层时间轮
     */
    private final TimingWheel timeWheel;

    /**
     * 一个Timer只有一个delayQueue
     */
    private final DelayQueue<TimerTaskList> delayQueue = new DelayQueue<>();

    /**
     * 过期任务执行线程
     */
    private final ExecutorService workerThreadPool;

    private final ReentrantLock lock = new ReentrantLock();

    /**
     * 构造函数
     */
    public SystemTimer() {
        timeWheel = new TimingWheel(1, 20, System.currentTimeMillis(), delayQueue);
        workerThreadPool = Executors.newFixedThreadPool(4);

        // 轮询delayQueue获取过期任务线程
        ExecutorService bossThreadPool = Executors.newFixedThreadPool(1);

        //20ms获取一次过期任务
        bossThreadPool.submit(() -> {
            while (true) {
                this.advanceClock(20);
            }
        });
    }

    /**
     * 添加任务
     */
    @SuppressWarnings("unchecked")
    public void addTask(TimerTask<?> timerTask) {
        try {
            lock.lock();
            // 添加失败任务直接执行
            if (!timeWheel.addTask(timerTask)) {
                if (timerTask.getTask() instanceof Task) {
                    try {
                        Task<CronTask> tmpTask = (Task<CronTask>) timerTask.getTask();

                        Task<CronTask> nextTask = new Task<>(tmpTask.getData(), tmpTask.getRunnable());
                        TimerTask<CronTask> nextTimerTask = new TimerTask<>(nextTask, tmpTask.getData().nextDelayMs());
                        addTask(nextTimerTask);
                    } catch (Exception e) {
                        log.error("", e);
                    }
                }

                workerThreadPool.submit(timerTask.getTask());
            }
        } catch (Exception e) {
            log.error("", e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取过期任务
     */
    private void advanceClock(long timeout) {
        try {
            lock.lock();
            TimerTaskList timerTaskList = delayQueue.poll(timeout, TimeUnit.MILLISECONDS);
            if (timerTaskList != null) {
                //推进时间
                timeWheel.advanceClock(timerTaskList.getExpiration());
                //执行过期任务（包含降级操作）
                timerTaskList.flush(this::addTask);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}