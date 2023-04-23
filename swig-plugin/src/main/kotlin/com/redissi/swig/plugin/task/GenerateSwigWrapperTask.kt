package com.redissi.swig.plugin.task

import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskExecutionException
import java.io.File

public abstract class GenerateSwigWrapperTask : SourceTask() {

    private companion object {
        private const val SWIG_FILE_PROPERTY = "swig.file"

        private const val CPP_ARGUMENT = "-c++"
        private const val OUTPUT_DIR_ARGUMENT = "-outdir"
        private const val CPP_OUT_FILE_ARGUMENT = "-o"
    }

    @get:Input
    public abstract val packageName: Property<String>

    @get:InputFile
    public abstract val interfaceFile: RegularFileProperty

    @get:InputFiles
    public abstract val sourceDirs: ListProperty<File>

    @get:OutputDirectory
    public abstract val outputDir: DirectoryProperty

    @get:OutputFile
    public abstract val wrapFile: RegularFileProperty

    private var subFolders: String = ""

    private val customArgs: MutableList<String> = mutableListOf()

    private fun addArgs(vararg args: String) {
        this.customArgs += args
    }

    @TaskAction
    public open fun generate() {
        val packageName = packageName.get()
        subFolders = packageName.replace('.', '/')

        addArgs("-java")
        addArgs("-package", packageName)

        requireNotNull(interfaceFile.get())

        cleanUp()

        val outputDir = outputDir.get().asFile
        val wrapFile = wrapFile.get().asFile
        val sourceDirs = sourceDirs.get()
        val interfaceFile = interfaceFile.get().asFile

        val outDir = File(outputDir, subFolders)
        outDir.mkdirs()
        wrapFile.parentFile.mkdirs()

        customArgs += listOf(
            CPP_ARGUMENT,
            OUTPUT_DIR_ARGUMENT, outDir.absolutePath,
            CPP_OUT_FILE_ARGUMENT, wrapFile.absolutePath
        )

        for (sourceDir in sourceDirs) {
            customArgs += "-I${sourceDir.absolutePath}"
        }

        val swigCommand = getSwigPath()

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

    private fun getSwigPath(): String {
        val localProperties = gradleLocalProperties(project.rootDir)

        if (localProperties.containsKey(SWIG_FILE_PROPERTY)) {
            return localProperties[SWIG_FILE_PROPERTY] as String
        }

        val properties = project.properties

        if (properties.containsKey(SWIG_FILE_PROPERTY)) {
            return properties[SWIG_FILE_PROPERTY] as String
        }

        return "swig" // from PATH
    }

    private fun cleanUp() {
        val output = outputDir.get().asFile
        output.deleteRecursively()
        val wrapFile = wrapFile.get().asFile
        wrapFile.deleteRecursively()
    }
}
