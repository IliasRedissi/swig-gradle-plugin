package com.redissi.swig.plugin

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.*
import java.io.File
import java.util.*

public abstract class GenerateSwigWrapperTask : SourceTask() {

    internal companion object {
        const val SWIG_FILE_PROPERTY = "swig.file"

        const val CPP_ARGUMENT = "-c++"
        const val OUTPUT_DIR_ARGUMENT = "-outdir"
        const val CPP_OUT_FILE_ARGUMENT = "-o"
    }

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

        customArgs += listOf(
            CPP_ARGUMENT,
            OUTPUT_DIR_ARGUMENT, outDir.absolutePath,
            CPP_OUT_FILE_ARGUMENT, wrapFile.absolutePath
        )

        for (sourceDir in sourceDirs) {
            customArgs += "-I${sourceDir.absolutePath}"
        }

        val properties = Properties()
        properties.load(project.rootProject.file("local.properties").inputStream())

        val swigCommand = if (properties.containsKey(SWIG_FILE_PROPERTY)) {
            properties[SWIG_FILE_PROPERTY] as String
        } else {
            "swig"
        }

        val command = mutableListOf(swigCommand)

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