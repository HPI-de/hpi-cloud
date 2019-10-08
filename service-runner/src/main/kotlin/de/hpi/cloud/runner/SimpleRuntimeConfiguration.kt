package de.hpi.cloud.runner

import com.typesafe.config.Config
import de.hpi.cloud.runner.utils.LocalDayTime
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
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

    companion object {
        fun parse(config: Config): SimpleRuntimeConfiguration? =
            parseSpecs(config.getString("type"), config.getConfig("specs"))
                ?.at(parseSchedules(config.getConfigList("schedules")))

        private fun parseSpecs(type: String, config: Config): CmdService? {
            return when (type.toLowerCase()) {
                "java" -> SimpleJavaService.parse(config)
                "cmd" -> SimpleCmdService.parse(config)
                else -> {
                    println("Unknown type of service config \"$config\"")
                    null
                }
            }
        }

        private fun parseSchedules(config: List<Config>): Set<LocalDayTime> {
            fun Config.parseLocalDayTime() = LocalDayTime(
                getString("weekday")
                    .let { weekday ->
                        DayOfWeek.values()
                            .first { it.name.equals(weekday, ignoreCase = true) }
                    },
                LocalTime.parse(getString("time"))
            )
            return config.map { it.parseLocalDayTime() }.toSet()
        }
    }
}
