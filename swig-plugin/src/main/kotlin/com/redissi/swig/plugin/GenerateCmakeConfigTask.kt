package com.redissi.swig.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

public abstract class GenerateCmakeConfigTask : DefaultTask() {

    @get:Input
    public abstract val libName: Property<String>

    @get:InputFile
    public abstract val cppFile: RegularFileProperty

    @get:OutputDirectory
    public abstract val outputDir: DirectoryProperty

    @TaskAction
    public fun generate() {
        val libName = libName.get()
        val cppFile = cppFile.get().asFile
        val outputDir = outputDir.get().asFile

        val content = """
            if(NOT TARGET ${libName})
            add_library(${libName} SHARED "${cppFile.absolutePath.replace('\\', '/')}")
            endif()
        """.trimIndent()

        val cmakeFile = File(outputDir, "${libName}Config.cmake")
        cmakeFile.parentFile.mkdirs()
        cmakeFile.writeText(content)
    }
}
