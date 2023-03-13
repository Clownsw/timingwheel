package cn.smilex.timingwheel;

@SuppressWarnings("unused")
public class Main {
    static SystemTimer SYSTEM_TIMER = new SystemTimer();
    static int executorCount = 0;
    static int joinCount = 0;

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
    }
}