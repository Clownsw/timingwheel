package cn.smilex.timingwheel;

/**
 * @author smilex
 */
@SuppressWarnings("unused")
public class Main {
    static SystemTimer SYSTEM_TIMER = new SystemTimer();

    public static void main(String[] args) {
        SYSTEM_TIMER.addTask(
                new CronTask(
                        SYSTEM_TIMER,
                        () -> {
                            System.out.println("我是每两秒执行一次");
                        }, "0/2 * * * * ?"
                ).toTimerTask()
        );

        SYSTEM_TIMER.addTask(
                new CronTask(
                        SYSTEM_TIMER,
                        () -> {
                            System.out.println("我是每两分钟执行一次");
                        }, "0 0/2 * * * ?"
                ).toTimerTask()
        );

        SYSTEM_TIMER.addTask(
                new CronTask(
                        SYSTEM_TIMER,
                        () -> {
                            System.out.println("每天上午10点，下午2点，4点");
                        },
                        "0 0 10,14,16 * * ?"
                ).toTimerTask()
        );
    }
}