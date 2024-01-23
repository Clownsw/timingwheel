package vip.smilex.timingwheel;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.function.Consumer;

/**
 * 时间轮cron类型定时任务
 *
 * @author yanglujia
 * @date 2024/1/22/18:11
 */
@SuppressWarnings("OptionalGetWithoutIsPresent")
@Slf4j
public class TimingWheelCronTask<T> {
    private static final CronDefinition CRON_DEFINITION = CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING);
    private static final CronParser CRON_PARSER = new CronParser(CRON_DEFINITION);

    private final Consumer<T> task;
    private final T userData;
    private final ExecutionTime executionTime;

    public TimingWheelCronTask(final Consumer<T> task, final T userData, final String cronString) {
        this.task = task;
        this.userData = userData;
        this.executionTime = ExecutionTime.forCron(CRON_PARSER.parse(cronString));
    }

    /**
     * 以当前时间偏移计算下一次执行时间
     *
     * @return 下一次时间
     * @author yanglujia
     * @date 2024/1/22 15:11:10
     */
    public long nextDelayMs() {
        ZonedDateTime zonedDateTime = this.executionTime.nextExecution(ZonedDateTime.now()).get();
        Timestamp timestamp = Timestamp.valueOf(zonedDateTime.toLocalDateTime());
        return timestamp.getTime() - System.currentTimeMillis();
    }

    /**
     * 转换到时间轮任务
     *
     * @return cn.smilex.timingwheel.TimingWheelTask<cn.smilex.timingwheel.CronTask < T>>
     * @author yanglujia
     * @date 2024/1/22 15:10:12
     */
    public TimingWheelTask toTimerTask() {
        return new TimingWheelTask(
                new TimingWheelTaskAction<>(
                        this,
                        this.userData,
                        this.task
                ),
                nextDelayMs()
        );
    }
}
