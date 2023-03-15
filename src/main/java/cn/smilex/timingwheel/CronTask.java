package cn.smilex.timingwheel;

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
 * @author smilex
 */
@SuppressWarnings("OptionalGetWithoutIsPresent")
@Slf4j
public class CronTask<T> {
    private static final CronDefinition CRON_DEFINITION = CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING);
    private static final CronParser CRON_PARSER = new CronParser(CRON_DEFINITION);

    private final Consumer<T> task;
    private final T userData;
    private final ExecutionTime executionTime;

    public CronTask(Consumer<T> task, T userData, String cronString) {
        this.task = task;
        this.userData = userData;
        this.executionTime = ExecutionTime.forCron(CRON_PARSER.parse(cronString));
    }

    public long nextDelayMs() {
        ZonedDateTime zonedDateTime = this.executionTime.nextExecution(ZonedDateTime.now()).get();
        Timestamp timestamp = Timestamp.valueOf(zonedDateTime.toLocalDateTime());
        return timestamp.getTime() - System.currentTimeMillis();
    }

    public TimerTask<CronTask<T>> toTimerTask() {
        return new TimerTask<>(
                new Task<>(
                        this,
                        this.userData,
                        this.task
                ),
                nextDelayMs()
        );
    }
}
