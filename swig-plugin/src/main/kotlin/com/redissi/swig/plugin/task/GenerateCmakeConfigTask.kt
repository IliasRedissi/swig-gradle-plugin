package com.redissi.swig.plugin.task

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

internal abstract class GenerateCmakeConfigTask : DefaultTask() {

    @get:Input
    abstract val libName: Property<String>

    @get:InputFile
    abstract val cppFile: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val libName = libName.get()
        val cppFile = cppFile.get().asFile
        val outputDir = outputDir.get().asFile

        val cppPath = cppFile.absolutePath.replace('\\', '/')

        val content = """
            |if(NOT TARGET ${libName})
            |    add_library(${libName} SHARED "$cppPath")
            |endif()
        """.trimMargin()

        val cmakeFile = File(outputDir, "${libName}Config.cmake")
        cmakeFile.parentFile.mkdirs()
        cmakeFile.writeText(content)
    }
}
