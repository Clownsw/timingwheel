package cn.smilex.timingwheel;

/**
 * @author smilex
 */
@FunctionalInterface
public interface CronTaskCallBackFunction<T> {
    void handle(Task<T> task, T data, CronTaskCallBackFunction<T> callBack);
}
