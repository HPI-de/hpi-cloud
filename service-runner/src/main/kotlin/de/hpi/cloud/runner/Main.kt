package de.hpi.cloud.runner

import de.hpi.cloud.runner.utils.at
import java.io.File
import java.time.DayOfWeek
import java.time.LocalTime
import java.util.concurrent.Executors
import java.util.logging.Logger

val logger = Logger.getLogger("Scheduler")

fun main() {
    val services = listOf(
        SimpleJavaService("javaTest", File("test.jar")) at setOf(
            DayOfWeek.MONDAY at LocalTime.of(22, 54)
        ),
        SimpleCmdService("echo1", "echo", "monday", "first") at setOf(
            DayOfWeek.MONDAY at LocalTime.of(22, 54, 0),
            DayOfWeek.MONDAY at LocalTime.of(22, 54, 30)
        ),
        SimpleCmdService("echo2", "echo", "monday", "second") at setOf(
            DayOfWeek.MONDAY at LocalTime.of(22, 54, 20)
        )
    )
    val scheduler = Executors.newScheduledThreadPool(2)
    services.forEach { it.schedule(scheduler) }
}
