package com.redissi.swig.plugin

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.*
import java.io.File

public abstract class GenerateSwigWrapperTask : SourceTask() {

    @get:InputFile
    public abstract val interfaceFile: RegularFileProperty

    @get:InputFiles
    public abstract val sourceDirs: ListProperty<File>

    @get:OutputDirectory
    public abstract val outputDir: DirectoryProperty

    @get:OutputDirectory
    public abstract val cppOutputDir: DirectoryProperty

    @get:OutputFile
    public abstract val wrapFile: RegularFileProperty

    @Internal
    protected var subFolders: String = ""

    private val customArgs: MutableList<String> = mutableListOf()

    protected fun addArgs(vararg args: String) {
        this.customArgs += args
    }

    @TaskAction
    public open fun generate() {
        requireNotNull(interfaceFile.get())

        cleanUp()

        val outputDir = outputDir.get().asFile
        val wrapFile = wrapFile.get().asFile
        val cppOutputDir = cppOutputDir.get().asFile
        val sourceDirs = sourceDirs.get()
        val interfaceFile = interfaceFile.get().asFile

        val outDir = File(outputDir, subFolders)
        outDir.mkdirs()
        cppOutputDir.mkdirs()

        customArgs += listOf("-c++", "-outdir", outDir.absolutePath, "-o", wrapFile.absolutePath)

        for (sourceDir in sourceDirs) {
            customArgs += "-I${sourceDir.absolutePath}"
        }

        val command = mutableListOf("swig")

        command.addAll(customArgs)
        command.add(interfaceFile.absolutePath)

        val process = ProcessBuilder(command).start()
        val error = process.errorStream.readAllBytes().joinToString("") {
            it.toInt().toChar().toString()
        }

        if (error.isNotEmpty()) {
            throw TaskExecutionException(this, RuntimeException(error))
        }
    }

    private fun cleanUp() {
        val output = outputDir.get().asFile
        output.deleteRecursively()
        val cppOutput = cppOutputDir.get().asFile
        cppOutput.deleteRecursively()
    }

}