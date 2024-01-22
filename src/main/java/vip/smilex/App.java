package vip.smilex;

import vip.smilex.timingwheel.TimingWheelCronTask;
import vip.smilex.timingwheel.TimingWheelWrapper;
import vip.smilex.timingwheel.TimingWheelTask;
import lombok.extern.slf4j.Slf4j;

/**
 * test for timing wheel
 *
 * @author yanglujia
 * @date 2024/1/22/15:08
 */
@SuppressWarnings("unused")
@Slf4j
public class App {
    static TimingWheelWrapper SYSTEM_TIMER = new TimingWheelWrapper();

    public static void main(String[] args) {
        SYSTEM_TIMER.addTask(new TimingWheelTask<>(
                () -> log.info("过期任务测试"),
                -1000
        ));

        SYSTEM_TIMER.addTask(new TimingWheelTask<>(
                () -> log.info("两秒后执行"),
                2000
        ));

        SYSTEM_TIMER.addTask(
                new TimingWheelCronTask<>(
                        v -> log.info("-每一秒执行一次, {}, v {}", System.currentTimeMillis(), v),
                        1,
                        "0/1 * * * * ?"
                ).toTimerTask()
        );

        SYSTEM_TIMER.addTask(
                new TimingWheelCronTask<>(
                        v -> log.info("--每两秒执行一次, {}, v {}", System.currentTimeMillis(), v),
                        2,
                        "0/2 * * * * ?"
                ).toTimerTask()
        );

        SYSTEM_TIMER.addTask(
                new TimingWheelCronTask<>(
                        v -> log.info("----每一分钟执行一次, {}", System.currentTimeMillis()),
                        null,
                        "0 0/1 * * * ?"
                ).toTimerTask()
        );

        SYSTEM_TIMER.addTask(
                new TimingWheelCronTask<>(
                        v -> log.info("-----每两分钟执行一次, {}", System.currentTimeMillis()),
                        null,
                        "0 0/2 * * * ?"
                ).toTimerTask()
        );

        SYSTEM_TIMER.addTask(
                new TimingWheelCronTask<>(
                        v -> log.info("--------每天上午10点，下午2点，4点"),
                        null,
                        "0 0 10,14,16 * * ?"
                ).toTimerTask()
        );
    }
}