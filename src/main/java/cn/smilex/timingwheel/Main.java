package cn.smilex.timingwheel;

import lombok.extern.slf4j.Slf4j;

/**
 * @author smilex
 */
@SuppressWarnings("unused")
@Slf4j
public class Main {
    static SystemTimer SYSTEM_TIMER = new SystemTimer();

    public static void main(String[] args) {
        SYSTEM_TIMER.addTask(
                new CronTask(
                        () -> {
                            log.info("-每一秒执行一次, {}", System.currentTimeMillis());
                        }, "0/1 * * * * ?"
                ).toTimerTask()
        );

        SYSTEM_TIMER.addTask(
                new CronTask(
                        () -> {
                            log.info("--每两秒执行一次, {}", System.currentTimeMillis());
                        }, "0/2 * * * * ?"
                ).toTimerTask()
        );

        SYSTEM_TIMER.addTask(
                new CronTask(
                        () -> {
                            log.info("----每一分钟执行一次, {}", System.currentTimeMillis());
                        }, "0 0/1 * * * ?"
                ).toTimerTask()
        );

        SYSTEM_TIMER.addTask(
                new CronTask(
                        () -> {
                            log.info("-----每两分钟执行一次, {}", System.currentTimeMillis());
                        }, "0 0/2 * * * ?"
                ).toTimerTask()
        );

        SYSTEM_TIMER.addTask(
                new CronTask(
                        () -> {
                            log.info("--------每天上午10点，下午2点，4点");
                        },
                        "0 0 10,14,16 * * ?"
                ).toTimerTask()
        );
    }
}