package de.hpi.cloud.runner

import java.io.File

class SimpleCmdService(
    override val name: String,
    override val cmd: String,
    vararg arguments: String,
    override val workingDir: File = File(".")
) : CmdService {
    override val args = arguments.asList()
}

class SimpleJavaService(
    override val name: String,
    val jarFile: File,
    vararg jarArguments: String
) : CmdService {
    override val args = jarArguments.asList()
    override val workingDir: File = jarFile.parentFile
    override val cmd = "java -jar $jarFile"
}
