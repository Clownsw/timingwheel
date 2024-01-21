package cn.smilex.timingwheel;

import java.util.concurrent.DelayQueue;

/**
 * @Author: siran.yao
 * @time: 2020/5/8:上午11:07
 * 时间轮的实现
 */
public class TimingWheel {

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
    private final TimerTaskList[] timerTaskLists;

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
    private final DelayQueue<TimerTaskList> delayQueue;

    public TimingWheel(long tickMs, int wheelSize, long currentTime, DelayQueue<TimerTaskList> delayQueue) {
        this.currentTime = currentTime;
        this.tickMs = tickMs;
        this.wheelSize = wheelSize;
        this.interval = tickMs * wheelSize;
        this.timerTaskLists = new TimerTaskList[wheelSize];
        //currentTime为tickMs的整数倍 这里做取整操作
        this.currentTime = currentTime - (currentTime % tickMs);
        this.delayQueue = delayQueue;
        for (int i = 0; i < wheelSize; i++) {
            timerTaskLists[i] = new TimerTaskList();
        }
    }

    /**
     * 创建或者获取上层时间轮
     */
    private TimingWheel getOverflowWheel() {
        if (overflowWheel == null) {
            synchronized (this) {
                if (overflowWheel == null) {
                    overflowWheel = new TimingWheel(interval, wheelSize, currentTime, delayQueue);
                }
            }
        }
        return overflowWheel;
    }

    /**
     * 添加任务到时间轮
     */
    public boolean addTask(TimerTask timerTask) {
        long expiration = timerTask.getDelayMs();
        // 过期任务直接执行
        if (expiration < currentTime + tickMs) {
            return false;
        } else if (expiration < currentTime + interval) {
            // 当前时间轮可以容纳该任务 加入时间槽
            final int index = (int) ((expiration / tickMs) % wheelSize);

            TimerTaskList timerTaskList = timerTaskLists[index];
            timerTaskList.addTask(timerTask);

            if (timerTaskList.setExpiration(expiration)) {
                //添加到delayQueue中
                delayQueue.offer(timerTaskList);
            }
        } else {
            //放到上一层的时间轮
            TimingWheel timeWheel = getOverflowWheel();
            timeWheel.addTask(timerTask);
        }
        return true;
    }

    /**
     * 推进时间
     */
    public void advanceClock(long timestamp) {
        if (timestamp >= currentTime + tickMs) {
            currentTime = timestamp - (timestamp % tickMs);
            if (overflowWheel != null) {
                //推进上层时间轮时间
                this.getOverflowWheel().advanceClock(timestamp);
            }
        }
    }
}
