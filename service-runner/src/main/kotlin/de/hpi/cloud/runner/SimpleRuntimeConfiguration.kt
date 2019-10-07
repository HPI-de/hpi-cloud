package de.hpi.cloud.runner

import de.hpi.cloud.runner.utils.LocalDayTime
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

infix fun CmdService.at(times: Set<LocalDayTime>) = SimpleRuntimeConfiguration(
    this,
    times
)

class SimpleRuntimeConfiguration(
    val service: CmdService,
    val dayTimes: Set<LocalDayTime>
) {
    fun schedule(scheduler: ScheduledExecutorService) = dayTimes
        .forEach { schedule(scheduler, it) }

    private fun schedule(scheduler: ScheduledExecutorService, localDayTime: LocalDayTime): ScheduledFuture<*> =
        LocalDateTime.now().let { now ->
            Duration.between(
                now,
                now.with(localDayTime.nextOrSameAdjuster())
            ).seconds.let { secondsUntilNextCall ->
                logger.info("Scheduled ${service.name} - running next in $secondsUntilNextCall seconds every ${LocalDayTime.SecondsOfWeek.SECONDS_IN_WEEK} seconds")
                scheduler.scheduleAtFixedRate(
                    { service.start() },
                    secondsUntilNextCall,
                    LocalDayTime.SecondsOfWeek.SECONDS_IN_WEEK,
                    TimeUnit.SECONDS
                )
            }
        }
}
