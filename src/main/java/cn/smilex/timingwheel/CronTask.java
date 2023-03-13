package cn.smilex.timingwheel;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.time.ZonedDateTime;

/**
 * @author smilex
 */
@SuppressWarnings("OptionalGetWithoutIsPresent")
@Slf4j
public class CronTask {
    private static final CronDefinition CRON_DEFINITION = CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING);
    private static final CronParser CRON_PARSER = new CronParser(CRON_DEFINITION);

    private final SystemTimer systemTimer;
    private final Runnable task;
    private final ExecutionTime executionTime;
    private ZonedDateTime zonedDateTime = null;

    public CronTask(SystemTimer systemTimer, Runnable task, String cronString) {
        this.systemTimer = systemTimer;
        this.task = task;
        this.executionTime = ExecutionTime.forCron(CRON_PARSER.parse(cronString));
    }

    public long nextDelayMs() {
        if (this.zonedDateTime == null) {
            this.zonedDateTime = ZonedDateTime.now();
        }

        this.zonedDateTime = this.executionTime.nextExecution(this.zonedDateTime).get();
        return Timestamp.valueOf(this.zonedDateTime.toLocalDateTime())
                .getTime() - System.currentTimeMillis();
    }

    public TimerTask<Tuple<SystemTimer, CronTask>> toTimerTask() {
        final long delayMs = nextDelayMs();

        return new TimerTask<>(
                new Task<>(
                        TaskType.CRON,
                        this.task,
                        new Tuple<>(this.systemTimer, this),
                        (t, v, c) -> {
                            v.getLeft()
                                    .addTask(
                                            new TimerTask<>(
                                                    new Task<>(
                                                            TaskType.CRON,
                                                            t,
                                                            new Tuple<>(v.getLeft(), v.getRight()),
                                                            c
                                                    ),
                                                    v.getRight()
                                                            .nextDelayMs()
                                            )
                                    );
                        }
                ),
                delayMs
        );
    }
}
