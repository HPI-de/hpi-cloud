package de.hpi.cloud.runner

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.logging.Logger

interface CmdService {
    val name: String
    val cmd: String
    val args: List<String>
    val workingDir: File
    val logger: Logger get() = Logger.getLogger(name)

    val command get() = listOf(cmd, *args.toTypedArray())

    private fun getLogFile() = workingDir.toPath().resolve("logs/runner-service-$name-${getDateString()}.log").toFile()
    private fun getDateString() = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"))

    fun start() {
        val logFile = getLogFile().apply {
            parentFile.mkdirs()
            createNewFile()
        }

        logger.info("Starting $name as \"${command.joinToString(separator = " ")}\" in working directory \"${workingDir.absolutePath}\"")
        ProcessBuilder(command)
            .redirectErrorStream(true)
            .redirectOutput(logFile)
            .directory(workingDir)
            .start()
            .onExit()
            .thenAccept {
                logger.info("Finished $name with status code ${it.exitValue()}")
            }
    }
}
