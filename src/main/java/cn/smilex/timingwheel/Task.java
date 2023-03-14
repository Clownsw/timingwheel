package cn.smilex.timingwheel;

/**
 * @author smilex
 */
public class Task<T> implements Runnable {
    private final TaskType taskType;
    private final Runnable task;
    private final T data;
    private final CronTaskCallBackFunction<T> callBack;

    public Task(TaskType taskType, Runnable task, T data, CronTaskCallBackFunction<T> callBack) {
        this.taskType = taskType;
        this.task = task;
        this.data = data;
        this.callBack = callBack;
    }

    @Override
    public void run() {
        // 计算并添加下一次任务执行
        if (taskType == TaskType.CRON) {
            this.callBack.handle(this, this.data, this.callBack);
        }

        try {
            this.task.run();
        } catch (Exception ignore) {
        }
    }
}
