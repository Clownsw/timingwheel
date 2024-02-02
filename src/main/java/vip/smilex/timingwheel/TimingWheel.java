package vip.smilex.timingwheel;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.DelayQueue;

/**
 * 时间轮的实现
 *
 * @author siran.yao
 * @author yanglujia
 * @date 2024/1/22/15:04
 */
@Slf4j
public final class TimingWheel {

    /**
     * 一个时间槽的范围
     */
    private final long tickMs;

    /**
     * 时间轮大小
     */
    private final int wheelSize;

    /**
     * 时间跨度
     */
    private final long interval;

    /**
     * 时间槽
     */
    private final TimingWheelTaskList[] timingWheelTaskLists;

    /**
     * 当前时间
     */
    private long currentTime;

    /**
     * 上层时间轮
     */
    private volatile TimingWheel overflowWheel;

    /**
     * 一个Timer只有一个delayQueue
     */
    private final DelayQueue<TimingWheelTaskList> delayQueue;

    public TimingWheel(long tickMs, int wheelSize, long currentTime, DelayQueue<TimingWheelTaskList> delayQueue) {
        this.currentTime = currentTime;
        this.tickMs = tickMs;
        this.wheelSize = wheelSize;
        this.interval = tickMs * wheelSize;
        this.timingWheelTaskLists = new TimingWheelTaskList[wheelSize];
        //currentTime为tickMs的整数倍 这里做取整操作
        this.currentTime = currentTime - (currentTime % tickMs);
        this.delayQueue = delayQueue;
        for (int i = 0; i < wheelSize; i++) {
            timingWheelTaskLists[i] = new TimingWheelTaskList();
        }
    }

    /**
     * 创建或者获取上层时间轮
     *
     * @return vip.smilex.timingwheel.TimingWheel 上层时间轮
     * @author yanglujia
     * @date 2024/1/22 18:15:44
     */
    private TimingWheel getOverflowWheel() {
        if (overflowWheel == null) {
            synchronized (this) {
                if (overflowWheel == null) {
                    log.info("{}-{}", overflowWheel, interval);
                    overflowWheel = new TimingWheel(interval, wheelSize, currentTime, delayQueue);
                }
            }
        }
        return overflowWheel;
    }

    /**
     * 添加任务到时间轮
     *
     * @param timingWheelTask 时间轮任务
     * @return 是否添加成功
     * @author yanglujia
     * @date 2024/1/22 18:16:39
     */
    public synchronized boolean addTask(final TimingWheelTask timingWheelTask) {
        long expiration = timingWheelTask.getDelayMs();
        // 过期任务直接执行
        if (expiration < currentTime + tickMs) {
            return false;
        } else if (expiration < currentTime + interval) {
            // 当前时间轮可以容纳该任务 加入时间槽
            final int index = (int) ((expiration / tickMs) % wheelSize);

            TimingWheelTaskList timingWheelTaskList = timingWheelTaskLists[index];
            timingWheelTaskList.addTask(timingWheelTask);

            // 设置过期时间
            timingWheelTaskList.setExpiration(expiration);

            // 添加到延迟队列中中
            delayQueue.offer(timingWheelTaskList);
        } else {
            //放到上一层的时间轮
            TimingWheel timeWheel = getOverflowWheel();
            timeWheel.addTask(timingWheelTask);
        }
        return true;
    }

    /**
     * 推进时间
     *
     * @param timestamp 时间戳
     * @author yanglujia
     * @date 2024/1/22 18:17:04
     */
    public void advanceClock(final long timestamp) {
        if (timestamp >= currentTime + tickMs) {
            currentTime = timestamp - (timestamp % tickMs);
            if (overflowWheel != null) {
                //推进上层时间轮时间
                this.getOverflowWheel().advanceClock(timestamp);
            }
        }
    }
}
