package de.hpi.cloud.runner

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import java.io.File
import java.util.concurrent.Executors
import java.util.logging.Logger

val configFile = File("services.conf")
val logger: Logger = Logger.getLogger("Scheduler")

fun main() {
    println("Loading config from: ${configFile.absolutePath}")
    val config = ConfigFactory.parseString(configFile.readText())
    val services = readConfig(config)
    val scheduler = Executors.newScheduledThreadPool(
        if (config.hasPath("threads")) config.getInt("threads")
        else 4
    )
    services.forEach { it.schedule(scheduler) }
}

fun readConfig(config: Config): List<SimpleRuntimeConfiguration> = config
    .getConfigList("services")
    .mapNotNull { SimpleRuntimeConfiguration.parse(it) }
