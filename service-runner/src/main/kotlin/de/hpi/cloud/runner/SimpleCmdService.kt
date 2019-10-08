package de.hpi.cloud.runner

import com.typesafe.config.Config
import java.io.File

class SimpleCmdService(
    override val name: String,
    override val cmd: String,
    vararg arguments: String,
    override val workingDir: File = File(".")
) : CmdService {
    override val args = arguments.asList()

    companion object {
        fun parse(config: Config) = SimpleCmdService(
            config.getString("name"),
            config.getString("command"),
            *config.getStringList("arguments").toTypedArray()
        )
    }
}

class SimpleJavaService(
    override val name: String,
    val jarFile: File,
    vararg jarArguments: String
) : CmdService {
    override val args = jarArguments.asList()
    override val workingDir: File = jarFile.parentFile ?: error("File does not exist \"$jarFile\"")
    override val cmd = "java -jar $jarFile"

    companion object {
        fun parse(config: Config) = SimpleJavaService(
            config.getString("name"),
            File(config.getString("jar")),
            *config.getStringList("arguments").toTypedArray()
        )
    }
}
